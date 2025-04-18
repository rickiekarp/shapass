package net.rickiekarp.core.settings

import javafx.collections.FXCollections
import javafx.collections.ObservableList
import net.rickiekarp.core.debug.DebugHelper
import net.rickiekarp.core.debug.ExceptionHandler
import net.rickiekarp.core.debug.LogFileHandler
import net.rickiekarp.core.model.ConsoleCommands
import net.rickiekarp.core.provider.LocalizationProvider
import net.rickiekarp.core.view.CommandsScene
import net.rickiekarp.core.view.MessageDialog
import java.io.IOException
import java.lang.reflect.InvocationTargetException
import java.net.URISyntaxException

class AppCommands {

    /**
     * Console commands are added to a list.
     */
    @Throws(NoSuchMethodException::class)
    fun fillCommandsList() {
        commandsList.add(ConsoleCommands("/help", "", LocalizationProvider.getString("commandsList_desc")) {
            help()
        })
        commandsList.add(ConsoleCommands("/exceptionTest", "", LocalizationProvider.getString("throwsException")) {
            exceptionTest()
        })
        commandsList.add(ConsoleCommands("/errorTest", "", LocalizationProvider.getString("showTest_desc")) {
            errorTest()
        })
        commandsList.add(ConsoleCommands("/restart", "", LocalizationProvider.getString("restart_desc")) {
            restart()
        })
    }

    private fun help() {
        val commands = CommandsScene()
        if (commands.commandsWindow!!.win.windowStage.stage.isShowing) {
            commands.commandsWindow!!.win.windowStage.stage.requestFocus()
        } else {
            commands.commandsWindow!!.win.windowStage.stage.show()
        }
    }

    private fun exceptionTest() {
        ExceptionHandler.throwTestException()
    }

    private fun errorTest() {
        MessageDialog(0, "TEST", 450, 220)
    }

    private fun restart() {
        try {
            DebugHelper.restartApplication()
        } catch (e1: URISyntaxException) {
            if (DebugHelper.DEBUG) {
                e1.printStackTrace()
            } else {
                ExceptionHandler(e1)
            }
        } catch (e1: IOException) {
            if (DebugHelper.DEBUG) {
                e1.printStackTrace()
            } else {
                ExceptionHandler(e1)
            }
        }
    }

    companion object {
        var commandsList: ObservableList<ConsoleCommands> = FXCollections.observableArrayList()

        /**
         * Checks and executed the entered command.
         * @param command Entered command
         */
        fun execCommand(command: String) {
            if (commandsList.isEmpty()) {
                println("No commands found!")
                return
            }

            //trim spaces of command string
            val finalCommand = command.replace("\\s+".toRegex(), " ")

            for (i in commandsList.indices) {
                if (finalCommand.startsWith(commandsList[i].commandName)) {
                    if (finalCommand == commandsList[i].commandName || finalCommand == commandsList[i].commandName + " ") {
                        try {
                            commandsList[i].method()
                        } catch (e: InvocationTargetException) {
                            e.printStackTrace()
                        } catch (e: IllegalAccessException) {
                            e.printStackTrace()
                        } catch (e: NoSuchMethodException) {
                            LogFileHandler.logger.warning("Method not found!")
                        }
                    }
                }
            }
        }
    }
}
