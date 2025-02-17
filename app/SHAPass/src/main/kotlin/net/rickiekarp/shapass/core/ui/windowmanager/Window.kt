package net.rickiekarp.core.ui.windowmanager

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleDoubleProperty
import javafx.geometry.Insets
import javafx.geometry.NodeOrientation
import javafx.geometry.Pos
import javafx.geometry.Rectangle2D
import javafx.scene.CacheHint
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.TextField
import javafx.scene.control.Tooltip
import javafx.scene.effect.BlurType
import javafx.scene.effect.DropShadow
import javafx.scene.image.ImageView
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination
import javafx.scene.layout.*
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle
import javafx.scene.shape.StrokeType
import javafx.stage.Modality
import javafx.stage.Screen
import javafx.stage.Stage
import javafx.stage.StageStyle
import net.rickiekarp.core.components.button.SidebarButton
import net.rickiekarp.core.debug.DebugHelper
import net.rickiekarp.core.debug.LogFileHandler
import net.rickiekarp.core.provider.LocalizationProvider
import net.rickiekarp.core.settings.AppCommands
import net.rickiekarp.core.settings.Configuration
import net.rickiekarp.core.settings.LoadSave
import net.rickiekarp.core.ui.anim.AnimationHandler
import net.rickiekarp.core.view.AboutScene
import net.rickiekarp.core.view.SettingsScene
import java.util.logging.Level

/**
 * This class, with the WindowController, is the central class for the
 * decoration of Transparent Stages.
 *
 * Bugs (Mac only?): Accelerators crash JVM
 * KeyCombination does not respect keyboard's locale
 * Multi screen: On second screen JFX returns wrong value for MinY (300)
 */
class Window(val windowStage: WindowStage, val clientArea: Region, st: StageStyle, internal val windowType: Int) : StackPane() {
    private var SHADOW_WIDTH = 10
    private var SAVED_SHADOW_WIDTH = 15
    private val RESIZE_PADDING = 7
    private val TITLEBAR_HEIGHT = 35
    private var console: TextField? = null
    internal var sideBarHeightProperty: SimpleDoubleProperty? = null
    private var stageStyle: StageStyle? = null
    private var stageDecoration: AnchorPane? = null
    private var shadowRectangle: Rectangle? = null
    private var dockFeedback: Rectangle? = null
    private var dockFeedbackPopup: Stage? = null
    var controller: WindowController? = null
        private set
    var contentController: WindowContentController? = null
        private set

    private lateinit var resizeRect: Rectangle

    internal lateinit var maximizeProperty: SimpleBooleanProperty
    private var minimizeProperty: SimpleBooleanProperty? = null
    private var closeProperty: SimpleBooleanProperty? = null
    val titleBarButtonBox: HBox
        get() = contentController!!.titlebarButtonBox!!
    val sidebarButtonBox: VBox?
        get() = contentController!!.sidebarButtonBox

    private fun maximizeProperty(): SimpleBooleanProperty {
        return maximizeProperty
    }

    private fun minimizeProperty(): SimpleBooleanProperty? {
        return minimizeProperty
    }

    private fun closeProperty(): SimpleBooleanProperty? {
        return closeProperty
    }

    init {
        create(windowStage, st, windowType)
    }

    fun create(stag: WindowStage, st: StageStyle, winType: Int) {
        stageStyle = st

        maximizeProperty = SimpleBooleanProperty(false)
        maximizeProperty.addListener { _, _, _ -> controller!!.maximizeOrRestore() }
        minimizeProperty = SimpleBooleanProperty(false)
        minimizeProperty!!.addListener { _, _, _ -> controller!!.minimize() }
        closeProperty = SimpleBooleanProperty(false)
        closeProperty!!.addListener { _, _, _ -> controller!!.close() }

        sideBarHeightProperty = SimpleDoubleProperty(stag.stage.height)

        // The controller
        controller = WindowController(this)
        contentController = WindowContentController()

        //set default values if there is no config.xml and the values have not been set
        if (colorTheme == null) {
            colorTheme = "black"
        }
        if (Configuration.decorationColor == null) {
            Configuration.decorationColor = LoadSave.decorationColor
        }
        if (Configuration.shadowColorFocused == null) {
            Configuration.shadowColorFocused = LoadSave.shadowColorFocused
        }
        if (Configuration.shadowColorNotFocused == null) {
            Configuration.shadowColorNotFocused = LoadSave.shadowColorNotFocused
        }

        // Focus drop shadows: radius, spread, offsets
        dsFocused = DropShadow(BlurType.THREE_PASS_BOX, Configuration.shadowColorFocused, SHADOW_WIDTH.toDouble(), 0.2, 0.0, 0.0)
        dsNotFocused = DropShadow(BlurType.THREE_PASS_BOX, Configuration.shadowColorNotFocused, SHADOW_WIDTH.toDouble(), 0.0, 0.0, 0.0)

        //set highlight/focus color
        clientArea.style = "-fx-accent: $colorTheme; -fx-focus-color: $colorTheme;"

        style = "-fx-background-color:transparent"

        // UI part of the decoration
        val vbox = VBox()
        stageDecoration = getStageDecoration(stag.stage, stag.stage.title, windowType)
        controller!!.setAsStageDraggable(stag.stage, stageDecoration!!)

        val contentStack = StackPane(clientArea)
        VBox.setVgrow(contentStack, Priority.ALWAYS)

        when (winType) {
            0 -> {
                val blurPane = StackPane()
                blurPane.style = "-fx-background-color: #1d1d1d;"
                blurPane.isVisible = false
                blurPane.setOnMouseClicked { _ -> toggleSideBar() }

                //create fadeIn/fadeOut transitions
                AnimationHandler.stackFadeIn = AnimationHandler.fade(blurPane, 200, 0.0, 0.8)
                AnimationHandler.stackFadeOut = AnimationHandler.fade(blurPane, 200, 0.8, 0.0)
                AnimationHandler.stackFadeOut!!.setOnFinished { _ -> blurPane.isVisible = false }

                //add slide handler for sidebar
                AnimationHandler.addSlideHandlers(stag.stage.width)

                //get Menu content
                val menuView = getMenu(stag.stage.height)

                contentStack.children.addAll(blurPane, menuView)
            }
        }

        vbox.style = "-fx-background-color: #2b2a2a;"
        vbox.children.addAll(stageDecoration, contentStack)

        resizeRect = Rectangle()
        resizeRect.fill = null
        resizeRect.strokeWidth = RESIZE_PADDING.toDouble()
        resizeRect.strokeType = StrokeType.INSIDE
        resizeRect.stroke = Configuration.decorationColor
        controller!!.setStageResizableWith(stag.stage, resizeRect, RESIZE_PADDING, SHADOW_WIDTH)

        buildDockFeedbackStage()

        shadowRectangle = Rectangle()
        shadowRectangle!!.isMouseTransparent = true // Do not intercept mouse events on stage's drop shadow

        // Add all layers
        super.getChildren().addAll(shadowRectangle, vbox, resizeRect)

        /*
         * Focused stage
         */
        stag.stage.focusedProperty().addListener { _, _, t1 -> setShadowFocused(t1!!) }

        val primaryScreenBounds = Screen.getPrimary().visualBounds

        clientArea.setMinSize(stag.stage.minWidth - (SHADOW_WIDTH * 2).toDouble() - (RESIZE_PADDING * 2).toDouble(), stag.stage.minHeight - TITLEBAR_HEIGHT.toDouble() - (SHADOW_WIDTH * 2).toDouble() - (RESIZE_PADDING * 2).toDouble())
        clientArea.setPrefSize(stag.stage.width, stag.stage.height - (SHADOW_WIDTH * 2).toDouble() - TITLEBAR_HEIGHT.toDouble() - (RESIZE_PADDING * 2).toDouble())
        clientArea.setMaxSize(primaryScreenBounds.width - RESIZE_PADDING * 2, primaryScreenBounds.height - (SHADOW_WIDTH * 2).toDouble() - (RESIZE_PADDING * 2).toDouble())
    }

    /**
     * Sets the color of the window decoration & resize Rectangle
     */
    fun setDecorationColor() {
        resizeRect.stroke = Configuration.decorationColor
        stageDecoration!!.style = "-fx-background-color: " + ThemeSelector.getColorHexString(Configuration.decorationColor!!)
    }

    /**
     * Install default accelerators
     * @param scene The scene
     */
    fun installAccelerators(scene: Scene, winType: Int) {
        // Accelerators
        if (winType == 0) {
            scene.accelerators[KeyCodeCombination(KeyCode.ESCAPE)] = Runnable {
                if (isMenuOpen) {
                    toggleSideBar()
                }
            }
        }
    }

    private fun switchMinimize() {
        minimizeProperty()!!.set(!minimizeProperty()!!.get())
    }

    private fun switchMaximize() {
        maximizeProperty().set(!maximizeProperty().get())
    }

    private fun switchClose() {
        closeProperty()!!.set(!closeProperty()!!.get())
    }

    /**
     * Returns the title bar in an AnchorPane.
     * There are 2 different title bar types: (0/1)
     * 0: Creates a title bar with a menu button
     * 1: Creates a title bar with an application logo
     */
    private fun getStageDecoration(stage: Stage, title: String, type: Int): AnchorPane {
        val menuAnchor = AnchorPane()
        val titlebarStyle = this.javaClass.classLoader.getResource("components/titlebar/TitleBarStyle.css")
        menuAnchor.stylesheets.add(titlebarStyle!!.toString())
        menuAnchor.style = "-fx-background-color: " + ThemeSelector.getColorHexString(Configuration.decorationColor!!)
        menuAnchor.maxHeight = TITLEBAR_HEIGHT.toDouble()

        //create left/right boxes for titlebar components
        val leftBox = HBox(20.0)
        AnchorPane.setTopAnchor(leftBox, 0.0)
        AnchorPane.setLeftAnchor(leftBox, 5.0)
        menuAnchor.children.add(leftBox)

        contentController!!.setTitleBarRightButtonBox(HBox())
        val titlebar = contentController!!.titlebarButtonBox
        titlebar!!.alignment = Pos.BASELINE_CENTER
        titlebar.nodeOrientation = NodeOrientation.RIGHT_TO_LEFT
        AnchorPane.setTopAnchor(titlebar, 0.0)
        AnchorPane.setRightAnchor(titlebar, 0.0)
        menuAnchor.children.add(titlebar)

        //defines the type of the menu bar
        when (type) {
            0 -> {
                val imageView = ImageView(ImageLoader.getMenu(this.javaClass.classLoader))
                imageView.viewport = Rectangle2D(AnimationHandler.OFFSET_X.toDouble(), AnimationHandler.OFFSET_Y.toDouble(), AnimationHandler.WIDTH.toDouble(), TITLEBAR_HEIGHT.toDouble())
                imageView.isPickOnBounds = true
                imageView.setOnMouseEntered { _ -> imageView.image = ImageLoader.getMenuHover(this.javaClass.classLoader) }
                imageView.setOnMouseExited { _ -> imageView.image = ImageLoader.getMenu(this.javaClass.classLoader) }
                imageView.setOnMouseClicked { _ -> toggleSideBar() }

                //creates menu button animation
                AnimationHandler.createMenuBtnAnim(imageView, TITLEBAR_HEIGHT)

                leftBox.children.add(imageView)
            }
            1 -> {
                val logo = ImageView(stage.icons[0])
                logo.fitHeightProperty().setValue(20)
                logo.fitWidthProperty().setValue(20)
                HBox.setMargin(logo, Insets(5.0, 0.0, 0.0, 4.0))
                leftBox.children.add(logo)
            }
        }

        val appTitle = Label(title)
        appTitle.padding = Insets(5.0, 0.0, 0.0, 0.0)
        stage.titleProperty().addListener { _, _, _ -> appTitle.text = stage.title }
        leftBox.children.add(appTitle)

        //define minimize/maximize/close button
        val btnClose = Button()
        btnClose.tooltip = Tooltip(LocalizationProvider.getString("close"))
        btnClose.styleClass.add("decoration-button-close")
        btnClose.setOnAction { _ -> switchClose() }

        val btnMinimize = Button()
        btnMinimize.tooltip = Tooltip(LocalizationProvider.getString("minimize"))
        btnMinimize.styleClass.add("decoration-button-minimize")
        btnMinimize.setOnAction { _ -> switchMinimize() }

        titlebar.children.addAll(btnClose, btnMinimize)

        //hide minimize/close button if stage is modal
        if (stage.modality == Modality.APPLICATION_MODAL) {
            btnClose.isDisable = true
            btnMinimize.isDisable = true
        }

        if (stage.isResizable) {
            val btnMaximize = Button()
            btnMaximize.tooltip = Tooltip(LocalizationProvider.getString("maximize"))
            btnMaximize.styleClass.add("decoration-button-maximize")

            btnMaximize.setOnAction { _ -> switchMaximize() }

            // Maximize on double click
            menuAnchor.setOnMouseClicked { mouseEvent ->
                if (this.stageStyle != StageStyle.UTILITY && !stage.isFullScreen && mouseEvent.clickCount > 1) {
                    switchMaximize()
                }
            }

            maximizeProperty().addListener { _, _, _ ->
                val tooltip = btnMaximize.tooltip

                if (maximizeProperty().get()) {
                    btnMaximize.styleClass.remove("decoration-button-maximize")
                    btnMaximize.styleClass.add("decoration-button-restore")

                    if (tooltip.text == LocalizationProvider.getString("maximize")) {
                        tooltip.text = LocalizationProvider.getString("restore")
                    }

                } else {
                    btnMaximize.styleClass.remove("decoration-button-restore")
                    btnMaximize.styleClass.add("decoration-button-maximize")

                    if (tooltip.text == LocalizationProvider.getString("restore")) {
                        tooltip.text = LocalizationProvider.getString("maximize")
                    }
                }
            }
            titlebar.children.add(1, btnMaximize)
        }

        //DEBUG COLORS AND DEV BUTTON
        if (DebugHelper.DEBUG) {
            leftBox.style = "-fx-background-color: gray"
            titlebar.style = "-fx-background-color: red"
        }
        return menuAnchor
    }

    /**
     * Returns the SideBar in an AnchorPane
     * @param stageHeight The height of the sidebar
     */
    private fun getMenu(stageHeight: Double): AnchorPane {
        val anchor = AnchorPane()
        contentController!!.sidebarButtonBox = VBox()

        val sidebar = contentController!!.sidebarButtonBox
        sidebar!!.padding = Insets(5.0, 5.0, 10.0, 5.0) //top, right, bottom, left
        sidebar.spacing = 10.0

        val clipShape = Rectangle(0.0, 0.0, AnimationHandler.menuWidth, stageHeight)
        anchor.clip = clipShape
        clipShape.xProperty().bind(AnimationHandler.xPosMenu)
        clipShape.heightProperty().bind(sideBarHeightProperty)

        val btnCfg = SidebarButton(LocalizationProvider.getString("settings"))
        btnCfg.setOnAction { _ ->
            SettingsScene()
            toggleSideBar()
        }

        val btnAbout = SidebarButton(LocalizationProvider.getString("about"))
        btnAbout.setOnAction { _ ->
            AboutScene()
            toggleSideBar()
        }

        contentController!!.addSidebarItem(btnCfg)
        contentController!!.addSidebarItem(btnAbout)
        sidebarButtonBox!!.children.setAll(contentController!!.list)

        anchor.children.addAll(sidebarButtonBox)
        AnchorPane.setTopAnchor(sidebar, 0.0)

        console = TextField()
        console!!.tooltip = Tooltip(LocalizationProvider.getString("console_input_desc"))
        console!!.setPrefSize(AnimationHandler.menuWidth - 10, 30.0)
        console!!.style = "-fx-accent: $colorTheme;-fx-focus-color: $colorTheme;"
        console!!.setOnKeyPressed { ke ->
            if (ke.code == KeyCode.ENTER) {
                if (!console!!.text.isEmpty()) {
                    LogFileHandler.logger.log(Level.INFO, "console_input: " + console!!.text)
                    AppCommands.execCommand(console!!.text)
                }
            }
        }
        AnchorPane.setBottomAnchor(console, 5.0)
        AnchorPane.setLeftAnchor(console, 5.0)
        anchor.children.add(console)

        // set debug colors
        if (DebugHelper.isDebugVersion) {
            anchor.style = "-fx-background-color: gray;"
        } else {
            anchor.style = "-fx-background-color: #1d1d1d;-fx-focus-color: $colorTheme;"
        }
        return anchor
    }

    fun calcSidebarButtonSize(stageHeight: Double) {
        val sidebar = contentController!!.sidebarButtonBox

        // calculate button size
        var buttonSize = (stageHeight - TITLEBAR_HEIGHT.toDouble() - (SHADOW_WIDTH * 2).toDouble() - (RESIZE_PADDING * 2).toDouble() - console!!.height - sidebar!!.spacing * (contentController!!.list.size + 1)) / contentController!!.list.size
        if (buttonSize > 90) {
            buttonSize = 90.0
        } //set maximum button size

        // set prefSize of button
        var button: SidebarButton
        for (i in 0 until contentController!!.list.size) {
            button = contentController!!.list[i]
            button.setPrefSize(AnimationHandler.menuWidth - 10, buttonSize)
        }

        sidebar.children.setAll(contentController!!.list)
    }

    /**
     * Toggles the sidebar
     */
    fun toggleSideBar() {
        isMenuOpen = !isMenuOpen
        if (isMenuOpen) {
            AnimationHandler.slideOut.stop()
            AnimationHandler.stackFadeOut!!.stop()

            AnimationHandler.stackFadeIn!!.node.isVisible = true
            AnimationHandler.stackFadeIn!!.play()

            AnimationHandler.menuBtnAnim.rate = 1.0
            AnimationHandler.menuBtnAnim.play()
            AnimationHandler.slideIn.play()

            console!!.requestFocus()
        } else {
            AnimationHandler.slideIn.stop()
            AnimationHandler.stackFadeIn!!.stop()

            AnimationHandler.stackFadeOut!!.play()

            AnimationHandler.menuBtnAnim.rate = -1.0
            AnimationHandler.menuBtnAnim.playFrom("end")
            AnimationHandler.slideOut.play()
        }
    }


    /**
     * Switch the visibility of the window's drop shadow
     */
    internal fun setShadow(shadow: Boolean) {
        // Already removed?
        if (!shadow && shadowRectangle!!.effect == null) {
            return
        }
        // From fullscreen to maximize case
        if (shadow && maximizeProperty.get()) {
            return
        }
        if (!shadow) {
            shadowRectangle!!.effect = null
            SAVED_SHADOW_WIDTH = SHADOW_WIDTH
            SHADOW_WIDTH = 0
        } else {
            shadowRectangle!!.effect = dsFocused
            SHADOW_WIDTH = SAVED_SHADOW_WIDTH
        }
    }

    /**
     * Set on/off the stage shadow effect
     * @param b Shadow effect bool
     */
    private fun setShadowFocused(b: Boolean) {
        // Do not change anything while maximized (in case of dialog closing for instance)
        if (maximizeProperty().get()) {
            return
        }
        if (b) {
            shadowRectangle!!.effect = dsFocused
        } else {
            shadowRectangle!!.effect = dsNotFocused
        }
    }

    /**
     * Set the layout of different layers of the stage
     */
    public override fun layoutChildren() {
        val b = super.getLayoutBounds()
        val w = b.width
        val h = b.height
        val list = super.getChildren()
        for (node in list) {
            if (node === shadowRectangle) {
                shadowRectangle!!.width = w - SHADOW_WIDTH * 2
                shadowRectangle!!.height = h - SHADOW_WIDTH * 2
                shadowRectangle!!.x = SHADOW_WIDTH.toDouble()
                shadowRectangle!!.y = SHADOW_WIDTH.toDouble()
            } else if (node === stageDecoration) {
                stageDecoration!!.resize(w - SHADOW_WIDTH * 2, h - SHADOW_WIDTH * 2)
                stageDecoration!!.layoutX = SHADOW_WIDTH.toDouble()
                stageDecoration!!.layoutY = SHADOW_WIDTH.toDouble()
            } else if (node === resizeRect) {
                resizeRect.width = w - SHADOW_WIDTH * 2
                resizeRect.height = h - SHADOW_WIDTH * 2
                resizeRect.layoutX = SHADOW_WIDTH.toDouble()
                resizeRect.layoutY = SHADOW_WIDTH.toDouble()
            } else {
                node.resize(w - (SHADOW_WIDTH * 2).toDouble() - (RESIZE_PADDING * 2).toDouble(), h - (SHADOW_WIDTH * 2).toDouble() - (RESIZE_PADDING * 2).toDouble())
                node.layoutX = (SHADOW_WIDTH + RESIZE_PADDING).toDouble()
                node.layoutY = (SHADOW_WIDTH + RESIZE_PADDING).toDouble()
            }
        }
    }

    /**
     * Activate dock feedback on screen's bounds
     * @param x X-Coordinate
     * @param y Y-Coordinate
     */
    internal fun setDockFeedbackVisible(x: Double, y: Double, width: Double, height: Double) {
        dockFeedbackPopup!!.x = x
        dockFeedbackPopup!!.y = y

        dockFeedback!!.x = SHADOW_WIDTH.toDouble()
        dockFeedback!!.y = SHADOW_WIDTH.toDouble()
        dockFeedback!!.height = height - SHADOW_WIDTH * 2
        dockFeedback!!.width = width - SHADOW_WIDTH * 2

        dockFeedbackPopup!!.width = width
        dockFeedbackPopup!!.height = height

        dockFeedback!!.opacity = 1.0
        dockFeedbackPopup!!.show()
    }

    internal fun setDockFeedbackInvisible() {
        if (dockFeedbackPopup!!.isShowing) {
            dockFeedbackPopup!!.hide()
        }
    }

    /**
     * Prepare Stage for dock feedback display
     */
    private fun buildDockFeedbackStage() {
        dockFeedbackPopup = Stage(StageStyle.TRANSPARENT)
        //dockFeedbackPopup.getIcons().add(ImageLoaderUtil.app_icon_small);
        dockFeedback = Rectangle(0.0, 0.0, 100.0, 100.0)
        dockFeedback!!.arcHeight = 10.0
        dockFeedback!!.arcWidth = 10.0
        dockFeedback!!.fill = Color.TRANSPARENT
        dockFeedback!!.stroke = Color.valueOf("#1d1d1d")
        dockFeedback!!.strokeWidth = 2.0
        dockFeedback!!.isCache = true
        dockFeedback!!.cacheHint = CacheHint.SPEED
        dockFeedback!!.effect = DropShadow(BlurType.TWO_PASS_BOX, Color.BLACK, 10.0, 0.2, 3.0, 3.0)
        dockFeedback!!.isMouseTransparent = true
        val borderpane = BorderPane()
        borderpane.style = "-fx-background-color:transparent"
        borderpane.center = dockFeedback
        val scene = Scene(borderpane)
        scene.fill = Color.TRANSPARENT
        dockFeedbackPopup!!.scene = scene
        dockFeedbackPopup!!.sizeToScene()
    }

    companion object {
        var colorTheme: String? = null
        lateinit var dsFocused: DropShadow
        lateinit var dsNotFocused: DropShadow
        private var isMenuOpen: Boolean = false
    }
}
