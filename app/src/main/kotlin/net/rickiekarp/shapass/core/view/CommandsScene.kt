package net.rickiekarp.shapass.core.view

import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.layout.*
import javafx.stage.Stage
import net.rickiekarp.shapass.core.debug.DebugHelper
import net.rickiekarp.shapass.core.debug.LogFileHandler
import net.rickiekarp.shapass.core.provider.LocalizationProvider
import net.rickiekarp.shapass.core.settings.AppCommands
import net.rickiekarp.shapass.core.ui.windowmanager.ImageLoader
import net.rickiekarp.shapass.core.ui.windowmanager.WindowScene
import net.rickiekarp.shapass.core.ui.windowmanager.WindowStage
import java.util.logging.Level

class CommandsScene {
    var commandsWindow: WindowScene? = null
        private set

    init {
        commandsScene = this
        createStage()
    }

    private fun createStage() {
        val commandsStage = Stage()
        commandsStage.icons.add(ImageLoader.getAppIconSmall())
        commandsStage.isResizable = true
        commandsStage.width = 640.0
        commandsStage.height = (200 + AppCommands.commandsList.size * 35).toDouble()
        commandsStage.minWidth = 620.0
        commandsStage.minHeight = (180 + AppCommands.commandsList.size * 35).toDouble()
        commandsStage.title = LocalizationProvider.getString("commands")

        val contentVbox = VBox()

        val borderpane = BorderPane()

        val grid = GridPane()
        val controls = HBox()
        controls.padding = Insets(15.0, 12.0, 15.0, 12.0)  //padding top, left, bottom, right
        controls.spacing = 10.0
        controls.alignment = Pos.CENTER_RIGHT

        //set Layout
        val column1 = ColumnConstraints()
        column1.percentWidth = 45.0
        val column2 = ColumnConstraints()
        column2.percentWidth = 45.0
        grid.columnConstraints.addAll(column1, column2)

        if (DebugHelper.isDebugVersion) {
            grid.style = "-fx-background-color: gray;"
            grid.isGridLinesVisible = true
            controls.style = "-fx-background-color: #336699;"
        }

        for (i in AppCommands.commandsList.indices) {

            //build the commandNameLabel string
            val sb = StringBuilder()
            sb.append(AppCommands.commandsList[i].commandName)
            if (!AppCommands.commandsList[i].commandHelper.isEmpty()) {
                sb.append(" ").append(AppCommands.commandsList[i].commandHelper)
            }

            val commandNameLabel = Label(sb.toString())
            GridPane.setConstraints(commandNameLabel, 0, i)
            grid.children.add(commandNameLabel)

            val commandDescLabel = Label(AppCommands.commandsList[i].commandDesc)
            GridPane.setConstraints(commandDescLabel, 1, i)
            grid.children.add(commandDescLabel)
        }

        val okButton = Button(LocalizationProvider.getString("close"))
        controls.children.add(okButton)

        grid.alignment = Pos.BASELINE_CENTER
        grid.hgap = 25.0
        grid.vgap = 15.0
        grid.padding = Insets(15.0, 0.0, 0.0, 0.0)

        borderpane.center = grid
        borderpane.bottom = controls

        okButton.setOnAction { _ -> commandsStage.close() }


        // The UI (Client Area) to display
        contentVbox.children.addAll(borderpane)
        VBox.setVgrow(borderpane, Priority.ALWAYS)

        // The Window as a Scene
        commandsWindow = WindowScene(WindowStage("commands", commandsStage), contentVbox, 1)

        commandsStage.scene = commandsWindow
        commandsStage.show()

        LogFileHandler.logger.log(Level.INFO, "open.CommandsDialog")
    }

    companion object {
        lateinit var commandsScene: CommandsScene
    }
}
