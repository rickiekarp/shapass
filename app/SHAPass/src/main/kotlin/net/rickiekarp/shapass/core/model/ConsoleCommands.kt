package net.rickiekarp.core.model

import javafx.beans.property.SimpleStringProperty

/**
 * This is the model class for all ConsoleCommands
 */
class ConsoleCommands(aCommandName: String, aCommandHelper: String, aCommandDesc: String, val method: () -> Unit) {
    private val name: SimpleStringProperty = SimpleStringProperty(aCommandName)
    private val helper: SimpleStringProperty = SimpleStringProperty(aCommandHelper)
    private val desc: SimpleStringProperty = SimpleStringProperty(aCommandDesc)

    val commandName: String
        get() = name.get()
    val commandHelper: String
        get() = helper.get()
    val commandDesc: String
        get() = desc.get()

}
