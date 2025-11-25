package net.rickiekarp.shapass.core.ui.windowmanager

import javafx.scene.Scene
import javafx.scene.layout.Region
import javafx.scene.paint.Color
import javafx.stage.StageStyle
import net.rickiekarp.shapass.core.debug.DebugHelper
import net.rickiekarp.shapass.core.settings.Configuration

class WindowScene
/**
 * WindowScene constructor
 * @param stage The main stage
 * @param stageStyle could be StageStyle.UTILITY or StageStyle.TRANSPARENT
 * @param root your UI to be displayed in the Stage
 * @param winType The type of the window decoration (0: with Sidebar / 1: without Sidebar)
 */
private constructor(stage: WindowStage, stageStyle: StageStyle, root: Region, winType: Int) : Scene(root) {

    lateinit var win: Window
        private set

    /**
     * Basic constructor with built-in behavior
     * @param stage The main stage
     * @param root your UI to be displayed in the Stage
     */
    constructor(stage: WindowStage, root: Region, winType: Int) : this(stage, StageStyle.TRANSPARENT, root, winType) {}

    init {

        //add custom button style for settings/about scene button
        val buttonStyle = this.javaClass.classLoader.getResource("components/button/ButtonStyle.css")
        root.stylesheets.add(buttonStyle!!.toString())

        // behaviour when using system borders instead of custom implementation
        if (Configuration.useSystemBorders) {
            root.prefHeight = stage.stage.height
            root.prefWidth = stage.stage.width
        } else {
            win = Window(stage, root, stageStyle, winType)
            super.setRoot(win)

            // Transparent scene and stage
            stage.stage.initStyle(stageStyle)

            if (DebugHelper.DEBUG) {
                super.setFill(Color.SLATEGRAY)
            } else {
                super.setFill(Color.TRANSPARENT)
            }

            // Default Accelerators
            win.installAccelerators(this, winType)
        }

        ThemeSelector.setTheme(this, this.javaClass.classLoader)
    }
}
