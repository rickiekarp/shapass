package net.rickiekarp.core.ui.windowmanager

import javafx.application.Platform
import javafx.geometry.BoundingBox
import javafx.geometry.Bounds
import javafx.scene.Cursor
import javafx.scene.Node
import javafx.scene.input.MouseEvent
import javafx.stage.Screen
import javafx.stage.Stage
import javafx.stage.WindowEvent
import net.rickiekarp.core.debug.LogFileHandler
import net.rickiekarp.core.view.MainScene

class WindowController internal constructor(private val window: Window) {
    private val DOCK_NONE = 0x0
    private val DOCK_LEFT = 0x1
    private val DOCK_RIGHT = 0x2
    private val DOCK_TOP = 0x4
    private var lastDocked = DOCK_NONE
    private var initX = -1.0
    private var initY = -1.0
    private var newX: Double = 0.toDouble()
    private var newY: Double = 0.toDouble()
    private var RESIZE_PADDING: Int = 0
    private var SHADOW_WIDTH: Int = 0
    private var savedBounds: BoundingBox? = null
    private var maximized = false

    fun maximizeOrRestore() {
        val stage = window.windowStage.stage

        if (maximized) {
            restoreSavedBounds(stage)
            window.setShadow(true)
            savedBounds = null
            maximized = false
        } else {
            val screensForRectangle = Screen.getScreensForRectangle(stage.x, stage.y, stage.width, stage.height)
            val screen = screensForRectangle[0]
            val visualBounds = screen.visualBounds

            savedBounds = BoundingBox(stage.x, stage.y, stage.width, stage.height)

            window.setShadow(false)

            stage.x = visualBounds.minX
            stage.y = visualBounds.minY
            stage.width = visualBounds.width
            stage.height = visualBounds.height

            window.sideBarHeightProperty!!.set(visualBounds.height)
            maximized = true
        }
    }

    private fun restoreSavedBounds(stage: Stage) {
        stage.x = savedBounds!!.minX
        stage.y = savedBounds!!.minY
        stage.width = savedBounds!!.width
        stage.height = savedBounds!!.height
        savedBounds = null
    }

    fun close() {
        val stage = window.windowStage
        LogFileHandler.logger.info("close." + stage.identifier)
        Platform.runLater { stage.stage.fireEvent(WindowEvent(stage.stage, WindowEvent.WINDOW_CLOSE_REQUEST)) }
        MainScene.stageStack.pop(stage.identifier!!)
    }

    fun minimize() {
        if (!Platform.isFxApplicationThread())
        // Ensure on correct thread else hangs X under Unbuntu
        {
            Platform.runLater { this._minimize() }
        } else {
            _minimize()
        }
    }

    private fun _minimize() {
        val stage = window.windowStage
        stage.stage.isIconified = true
    }

    /**
     * Stage resize management
     * @param stage Stage to resize
     * @param node Node to make draggable
     * @param PADDING Resize padding
     * @param SHADOW Shadow width
     */
    fun setStageResizableWith(stage: Stage, node: Node, PADDING: Int, SHADOW: Int) {
        RESIZE_PADDING = PADDING
        SHADOW_WIDTH = SHADOW

        node.setOnMousePressed { mouseEvent ->
            if (mouseEvent.isPrimaryButtonDown) {
                initX = mouseEvent.screenX
                initY = mouseEvent.screenY
                mouseEvent.consume()
            }
        }
        node.setOnMouseDragged { mouseEvent ->
            if (!mouseEvent.isPrimaryButtonDown || initX == -1.0 && initY == -1.0) {
                return@setOnMouseDragged
            }

            //Long press generates drag event!
            if (mouseEvent.isStillSincePress) {
                return@setOnMouseDragged
            }
            if (maximized) {
                // Remove maximized state
                window.maximizeProperty.set(false)
                return@setOnMouseDragged
            } // Docked then moved, so restore state
            else if (savedBounds != null) {
                window.setShadow(true)
            }


            newX = mouseEvent.screenX
            newY = mouseEvent.screenY
            val deltax = newX - initX
            val deltay = newY - initY

            val cursor = node.cursor
            if (Cursor.E_RESIZE == cursor) {
                setStageWidth(stage, stage.width + deltax)
                mouseEvent.consume()
            } else if (Cursor.NE_RESIZE == cursor) {
                if (setStageHeight(stage, stage.height - deltay)) {
                    setStageY(stage, stage.y + deltay)
                }
                setStageWidth(stage, stage.width + deltax)
                mouseEvent.consume()
            } else if (Cursor.SE_RESIZE == cursor) {
                setStageWidth(stage, stage.width + deltax)
                setStageHeight(stage, stage.height + deltay)
                mouseEvent.consume()
            } else if (Cursor.S_RESIZE == cursor) {
                setStageHeight(stage, stage.height + deltay)
                mouseEvent.consume()
            } else if (Cursor.W_RESIZE == cursor) {
                if (setStageWidth(stage, stage.width - deltax)) {
                    stage.x = stage.x + deltax
                }
                mouseEvent.consume()
            } else if (Cursor.SW_RESIZE == cursor) {
                if (setStageWidth(stage, stage.width - deltax)) {
                    stage.x = stage.x + deltax
                }
                setStageHeight(stage, stage.height + deltay)
                mouseEvent.consume()
            } else if (Cursor.NW_RESIZE == cursor) {
                if (setStageWidth(stage, stage.width - deltax)) {
                    stage.x = stage.x + deltax
                }
                if (setStageHeight(stage, stage.height - deltay)) {
                    setStageY(stage, stage.y + deltay)
                }
                mouseEvent.consume()
            } else if (Cursor.N_RESIZE == cursor) {
                if (setStageHeight(stage, stage.height - deltay)) {
                    setStageY(stage, stage.y + deltay)
                }
                mouseEvent.consume()
            }
        }

        node.setOnMouseMoved { mouseEvent ->
            if (maximized) {
                setCursor(node, Cursor.DEFAULT)
                return@setOnMouseMoved  // maximized mode does not support resize
            }
            if (!stage.isResizable) {
                return@setOnMouseMoved
            }
            val x = mouseEvent.x
            val y = mouseEvent.y
            val boundsInParent = node.boundsInParent
            if (isRightEdge(x, boundsInParent)) {
                if (y < RESIZE_PADDING + SHADOW_WIDTH) {
                    setCursor(node, Cursor.NE_RESIZE)
                } else if (y > boundsInParent.height - (RESIZE_PADDING + SHADOW_WIDTH).toDouble()) {
                    setCursor(node, Cursor.SE_RESIZE)
                } else {
                    setCursor(node, Cursor.E_RESIZE)
                }

            } else if (isLeftEdge(x)) {
                if (y < RESIZE_PADDING + SHADOW_WIDTH) {
                    setCursor(node, Cursor.NW_RESIZE)
                } else if (y > boundsInParent.height - (RESIZE_PADDING + SHADOW_WIDTH).toDouble()) {
                    setCursor(node, Cursor.SW_RESIZE)
                } else {
                    setCursor(node, Cursor.W_RESIZE)
                }
            } else if (isTopEdge(y)) {
                setCursor(node, Cursor.N_RESIZE)
            } else if (isBottomEdge(y, boundsInParent)) {
                setCursor(node, Cursor.S_RESIZE)
            } else {
                setCursor(node, Cursor.DEFAULT)
            }
        }
    }

    /**
     * Under Windows, the window Stage could be been dragged below the Task
     * bar and then no way to grab it again... On Mac, do not drag above the
     * menu bar
     *
     * @param y The Y-Location
     */
    private fun setStageY(stage: Stage, y: Double) {
        try {
            val screensForRectangle = Screen.getScreensForRectangle(stage.x, stage.y, stage.width, stage.height)
            if (screensForRectangle.size > 0) {
                val screen = screensForRectangle[0]
                val visualBounds = screen.visualBounds
                if (y < visualBounds.height - 30 && y + SHADOW_WIDTH >= visualBounds.minY) {
                    stage.y = y
                }
            }
        } catch (e: Exception) {
            //ignore
        }

    }

    private fun setStageWidth(stage: Stage, width: Double): Boolean {
        if (width >= stage.minWidth) {
            stage.width = width
            initX = newX
            return true
        }
        return false
    }

    private fun setStageHeight(stage: Stage, height: Double): Boolean {
        if (height >= stage.minHeight) {
            stage.height = height
            if (window.windowType == 0) {
                window.sideBarHeightProperty!!.set(height)
                window.calcSidebarButtonSize(stage.height)
            }
            initY = newY
            return true
        }
        return false
    }

    /**
     * Allow this node to drag the Stage
     */
    fun setAsStageDraggable(stage: Stage, node: Node) {
        node.setOnMousePressed { mouseEvent ->
            if (mouseEvent.isPrimaryButtonDown) {
                initX = mouseEvent.screenX
                initY = mouseEvent.screenY
                mouseEvent.consume()
            } else {
                initX = -1.0
                initY = -1.0
            }
        }
        node.setOnMouseDragged { mouseEvent ->
            if (!mouseEvent.isPrimaryButtonDown || initX == -1.0) {
                return@setOnMouseDragged
            }

            /*
             * Long press generates drag event!
             */
            if (mouseEvent.isStillSincePress) {
                return@setOnMouseDragged
            }
            if (maximized) {
                // Remove Maximized state
                window.maximizeProperty.set(false)
                // Center
                stage.x = mouseEvent.screenX - stage.width / 2
                stage.y = mouseEvent.screenY - SHADOW_WIDTH
            } // Docked then moved, so restore state
            else if (savedBounds != null) {
                restoreSavedBounds(stage)
                window.setShadow(true)
                // Center
                stage.x = mouseEvent.screenX - stage.width / 2
                stage.y = mouseEvent.screenY - SHADOW_WIDTH
            }
            val newX1 = mouseEvent.screenX
            val newY1 = mouseEvent.screenY
            val deltax = newX1 - initX
            val deltay = newY1 - initY
            initX = newX1
            initY = newY1
            setCursor(node, Cursor.HAND)
            stage.x = stage.x + deltax
            setStageY(stage, stage.y + deltay)

            testDock(stage, mouseEvent)
            mouseEvent.consume()
        }

        node.setOnMouseReleased { t ->
            if (stage.isResizable) {
                window.setDockFeedbackInvisible()
                setCursor(node, Cursor.DEFAULT)
                initX = -1.0
                initY = -1.0
                dockActions(stage, t)
            }
        }

        node.setOnMouseExited { _ -> setCursor(node, Cursor.DEFAULT) }
    }

    /**
     * (Humble) Simulation of Windows behavior on screen's edges Feedbacks
     */
    private fun testDock(stage: Stage, mouseEvent: MouseEvent) {
        if (!stage.isResizable) {
            return
        }

        val dockSide = getDockSide(mouseEvent)
        // Dock Left
        when (dockSide) {
            DOCK_LEFT -> {
                if (lastDocked == DOCK_LEFT) {
                    return
                }
                val screensForRectangle = Screen.getScreensForRectangle(stage.x, stage.y, stage.width, stage.height)
                val screen = screensForRectangle[0]
                val visualBounds = screen.visualBounds
                // Dock Left
                val x = visualBounds.minX
                val y = visualBounds.minY
                val width = visualBounds.width / 2
                val height = visualBounds.height

                window.setDockFeedbackVisible(x, y, width, height)
                lastDocked = DOCK_LEFT
            } // Dock Right
            DOCK_RIGHT -> {
                if (lastDocked == DOCK_RIGHT) {
                    return
                }
                val screensForRectangle = Screen.getScreensForRectangle(stage.x, stage.y, stage.width, stage.height)
                val screen = screensForRectangle[0]
                val visualBounds = screen.visualBounds
                // Dock Right (visualBounds = (javafx.geometry.Rectangle2D) Rectangle2D [minX = 1440.0, minY=300.0, maxX=3360.0, maxY=1500.0, width=1920.0, height=1200.0])
                val x = visualBounds.minX + visualBounds.width / 2
                val y = visualBounds.minY
                val width = visualBounds.width / 2
                val height = visualBounds.height

                window.setDockFeedbackVisible(x, y, width, height)
                lastDocked = DOCK_RIGHT
            } // Dock top
            DOCK_TOP -> {
                if (lastDocked == DOCK_TOP) {
                    return
                }
                val screensForRectangle = Screen.getScreensForRectangle(stage.x, stage.y, stage.width, stage.height)
                val screen = screensForRectangle[0]
                val visualBounds = screen.visualBounds
                // Dock Left
                val x = visualBounds.minX
                val y = visualBounds.minY
                val width = visualBounds.width
                val height = visualBounds.height
                window.setDockFeedbackVisible(x, y, width, height)
                lastDocked = DOCK_TOP
            }
            else -> {
                window.setDockFeedbackInvisible()
                lastDocked = DOCK_NONE
            }
        }
    }

    /**
     * Based on mouse position returns dock side
     * @param mouseEvent The mouse event
     * @return DOCK_LEFT,DOCK_RIGHT,DOCK_TOP
     */
    private fun getDockSide(mouseEvent: MouseEvent): Int {
        var minX = java.lang.Double.POSITIVE_INFINITY
        var minY = java.lang.Double.POSITIVE_INFINITY
        var maxX = 0.0
        var maxY = 0.0

        // Get "big" screen bounds
        val screens = Screen.getScreens()
        for (screen in screens) {
            val visualBounds = screen.visualBounds
            minX = Math.min(minX, visualBounds.minX)
            minY = Math.min(minY, visualBounds.minY)
            maxX = Math.max(maxX, visualBounds.maxX)
            maxY = Math.max(maxY, visualBounds.maxY)
        }
        // Dock Left
        if (mouseEvent.screenX == minX) {
            return DOCK_LEFT
        } else if (mouseEvent.screenX >= maxX - 1) { // MaxX returns the width? Not width -1 ?!
            return DOCK_RIGHT
        } else if (mouseEvent.screenY <= minY) {   // Mac menu bar
            return DOCK_TOP
        }
        return 0
    }

    /**
     * (Humble) Simulation of Windows behavior on screen's edges Actions
     */
    private fun dockActions(stage: Stage, mouseEvent: MouseEvent) {
        val screensForRectangle = Screen.getScreensForRectangle(stage.x, stage.y, stage.width, stage.height)
        val screen = screensForRectangle[0]
        val visualBounds = screen.visualBounds
        // Dock Left
        if (mouseEvent.screenX == visualBounds.minX) {
            savedBounds = BoundingBox(stage.x, stage.y, stage.width, stage.height)

            stage.x = visualBounds.minX
            stage.y = visualBounds.minY
            // Respect Stage Max size
            var width = visualBounds.width / 2
            if (stage.maxWidth < width) {
                width = stage.maxWidth
            }

            stage.width = width

            var height = visualBounds.height
            if (stage.maxHeight < height) {
                height = stage.maxHeight
            }

            stage.height = height
            window.setShadow(false)
        } // Dock Right (visualBounds = [minX = 1440.0, minY=300.0, maxX=3360.0, maxY=1500.0, width=1920.0, height=1200.0])
        else if (mouseEvent.screenX >= visualBounds.maxX - 1) { // MaxX returns the width? Not width -1 ?!
            savedBounds = BoundingBox(stage.x, stage.y, stage.width, stage.height)

            stage.x = visualBounds.width / 2 + visualBounds.minX
            stage.y = visualBounds.minY
            // Respect Stage Max size
            var width = visualBounds.width / 2
            if (stage.maxWidth < width) {
                width = stage.maxWidth
            }

            stage.width = width

            var height = visualBounds.height
            if (stage.maxHeight < height) {
                height = stage.maxHeight
            }

            stage.height = height
            window.setShadow(false)
        } else if (mouseEvent.screenY <= visualBounds.minY) { // Mac menu bar
            window.maximizeProperty.set(true)
        }
    }

    private fun isRightEdge(x: Double, boundsInParent: Bounds): Boolean {
        return x < boundsInParent.width && x > boundsInParent.width - RESIZE_PADDING.toDouble() - SHADOW_WIDTH.toDouble()
    }

    private fun isTopEdge(y: Double): Boolean {
        return y >= 0 && y < RESIZE_PADDING + SHADOW_WIDTH
    }

    private fun isBottomEdge(y: Double, boundsInParent: Bounds): Boolean {
        return y < boundsInParent.height && y > boundsInParent.height - RESIZE_PADDING.toDouble() - SHADOW_WIDTH.toDouble()
    }

    private fun isLeftEdge(x: Double): Boolean {
        return x >= 0 && x < RESIZE_PADDING + SHADOW_WIDTH
    }

    private fun setCursor(n: Node, c: Cursor) {
        n.cursor = c
    }
}
