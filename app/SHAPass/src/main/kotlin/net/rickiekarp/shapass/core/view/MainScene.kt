package net.rickiekarp.core.view

import javafx.scene.layout.BorderPane
import javafx.scene.layout.Region
import javafx.stage.Stage
import net.rickiekarp.core.settings.AppCommands
import net.rickiekarp.core.ui.windowmanager.WindowScene
import net.rickiekarp.core.ui.windowmanager.WindowStack
import net.rickiekarp.core.ui.windowmanager.WindowStage
import net.rickiekarp.core.ui.windowmanager.WindowStageStack

class MainScene {
    var windowScene: WindowScene? = null
        private set
    private var layoutRegion: Region? = null
    val borderPane: BorderPane
        get() = layoutRegion as BorderPane

    val sceneViewStack: WindowStack
        get() = stageStack.sceneViewStack

    constructor(stage: Stage) {
        createScene(stage, 0.toByte())
    }

    constructor(stage: Stage, winType: Byte) {
        createScene(stage, winType)
    }

    private fun createScene(stage: Stage, winType: Byte) {
        val mainStage = WindowStage("main", stage)
        mainScene = this
        layoutRegion = BorderPane()
        windowScene = WindowScene(mainStage, layoutRegion!!, winType.toInt())
        stage.scene = windowScene
        stage.show()

        when (winType) {
            0.toByte() -> {
                windowScene!!.win.calcSidebarButtonSize(stage.height)

                //add available commands to a list
                try {
                    AppCommands().fillCommandsList()
                } catch (e: NoSuchMethodException) {
                    e.printStackTrace()
                }

            }
            1.toByte() -> {
            }
        }

        stageStack = WindowStageStack()
        stageStack.push(mainStage)
    }

    companion object {
        lateinit var stageStack: WindowStageStack
        lateinit var mainScene: MainScene
    }
}
