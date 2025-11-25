package net.rickiekarp.shapass.core.view

import javafx.geometry.HPos
import javafx.geometry.Insets
import javafx.geometry.Orientation
import javafx.geometry.Pos
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.Separator
import javafx.scene.image.ImageView
import javafx.scene.layout.BorderPane
import javafx.scene.layout.GridPane
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.stage.Stage
import net.rickiekarp.shapass.core.AppContext
import net.rickiekarp.shapass.core.debug.DebugHelper
import net.rickiekarp.shapass.core.debug.ExceptionHandler
import net.rickiekarp.shapass.core.provider.LocalizationProvider
import net.rickiekarp.shapass.core.ui.windowmanager.ImageLoader
import net.rickiekarp.shapass.core.ui.windowmanager.WindowScene
import net.rickiekarp.shapass.core.ui.windowmanager.WindowStage
import net.rickiekarp.shapass.core.util.CommonUtil
import java.io.IOException
import java.net.URISyntaxException


/**
 * About Stage GUI.
 */
class AboutScene {
    private var grid: GridPane? = null
    private var grid2: GridPane? = null
    private var controls: HBox? = null

    private val content: BorderPane
        get() {

            val borderpane = BorderPane()
            borderpane.style = "-fx-background-color: #1d1d1d;"

            val hBox = HBox()
            hBox.alignment = Pos.CENTER_LEFT

            grid = GridPane()
            grid2 = GridPane()
            controls = HBox()

            val separator2 = Separator()
            separator2.orientation = Orientation.VERTICAL
            separator2.maxHeight = 160.0
            separator2.padding = Insets(0.0, 0.0, 0.0, 0.0)
            if (DebugHelper.isDebugVersion) {
                separator2.style = "-fx-background-color: red;"
            }

            grid!!.vgap = 8.0
            grid!!.padding = Insets(20.0, 15.0, 0.0, 20.0)
            grid!!.minWidth = 180.0

            grid2!!.vgap = 20.0
            grid2!!.padding = Insets(20.0, 15.0, 0.0, 20.0)

            HBox.setHgrow(grid2, Priority.ALWAYS)
            hBox.children.add(0, grid)
            hBox.children.add(1, separator2)
            hBox.children.add(2, grid2)

            val title = Label(AppContext.context.applicationName)
            title.style = "-fx-font-size: 16pt;"
            GridPane.setHalignment(title, HPos.CENTER)
            GridPane.setConstraints(title, 0, 0)
            grid!!.children.add(title)

            val logo = ImageView(ImageLoader.getAppIcon())
            logo.fitHeightProperty().setValue(60)
            logo.fitWidthProperty().setValue(60)
            GridPane.setHalignment(logo, HPos.CENTER)
            GridPane.setConstraints(logo, 0, 1)
            grid!!.children.add(logo)

            val version = Label(AppContext.context.versionNumber)
            version.style = "-fx-font-size: 11pt;"
            GridPane.setHalignment(version, HPos.CENTER)
            GridPane.setConstraints(version, 0, 2)
            grid!!.children.add(version)

            val description = Label(LocalizationProvider.getString("desc"))
            description.setMaxSize(350.0, 200.0)
            description.isWrapText = true
            GridPane.setConstraints(description, 0, 0)
            grid2!!.children.add(description)

            val copyright = Label(LocalizationProvider.getString("copyright"))
            copyright.style = "-fx-font-size: 10pt;"
            GridPane.setConstraints(copyright, 0, 1)
            grid2!!.children.add(copyright)

            val urlBtn = Button(LocalizationProvider.getString("website"))
            controls!!.children.add(urlBtn)

            controls!!.padding = Insets(10.0, 7.0, 10.0, 7.0)
            controls!!.spacing = 10.0
            controls!!.alignment = Pos.CENTER_RIGHT
            borderpane.center = hBox
            borderpane.bottom = controls

            urlBtn.setOnAction { _ ->
                try {
                    CommonUtil.openWebpage(website)
                } catch (e1: IOException) {
                    if (DebugHelper.DEBUG) {
                        e1.printStackTrace()
                    } else {
                        ExceptionHandler(e1)
                    }
                } catch (e1: URISyntaxException) {
                    if (DebugHelper.DEBUG) {
                        e1.printStackTrace()
                    } else {
                        ExceptionHandler(e1)
                    }
                }
            }

            return borderpane
        }

    init {
        create()
    }

    private fun create() {
        val infoStage = Stage()
        infoStage.title = LocalizationProvider.getString("about") + " " + AppContext.context.applicationName
        infoStage.icons.add(ImageLoader.getAppIconSmall())
        infoStage.isResizable = false
        infoStage.width = 500.0
        infoStage.height = 320.0

        val contentVbox = BorderPane()
        contentVbox.center = content

        val aboutWindow = WindowScene(WindowStage("about", infoStage), contentVbox, 1)

        infoStage.scene = aboutWindow
        infoStage.show()

        debugAbout()

        MainScene.stageStack.push(WindowStage("about", infoStage))
    }

    private fun debugAbout() {
        if (DebugHelper.isDebugVersion) {
            grid!!.isGridLinesVisible = true
            grid!!.style = "-fx-background-color: #333333;"
            grid2!!.isGridLinesVisible = true
            grid2!!.style = "-fx-background-color: #444444;"
            controls!!.style = "-fx-background-color: #336699;"
        } else {
            grid!!.isGridLinesVisible = false
            grid!!.style = null
            grid2!!.isGridLinesVisible = false
            grid2!!.style = null
            controls!!.style = null
        }
    }

    companion object {
        private val website = "https://rickiekarp.net"
    }
}