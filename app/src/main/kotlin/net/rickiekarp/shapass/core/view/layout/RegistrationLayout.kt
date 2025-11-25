package net.rickiekarp.shapass.core.view.layout

import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.PasswordField
import javafx.scene.control.TextField
import javafx.scene.layout.GridPane
import javafx.scene.layout.VBox
import net.rickiekarp.shapass.core.AppContext
import net.rickiekarp.shapass.core.account.Account
import net.rickiekarp.shapass.core.net.NetworkApi
import net.rickiekarp.shapass.core.view.MessageDialog

/**
 * Main Login Mask layout class.
 */
internal class RegistrationLayout {
    private val main: VBox = VBox()
    private val grid: GridPane
    private val loginLabel: Label
    private val username: TextField
    private val password: PasswordField

    val maskNode: Node
        get() = main

    init {
        main.spacing = 20.0
        main.alignment = Pos.CENTER
        grid = GridPane()
        grid.alignment = Pos.CENTER
        grid.hgap = 10.0
        grid.vgap = 10.0
        loginLabel = Label("Register")
        main.children.add(loginLabel)

        val usernameLabel = Label("User")
        grid.add(usernameLabel, 0, 1)

        username = TextField()
        grid.add(username, 1, 1)

        val passwordLabel = Label("Password")
        grid.add(passwordLabel, 0, 2)

        password = PasswordField()
        grid.add(password, 1, 2)

        val registerButton = Button("Submit")
        registerButton.setOnAction { _ ->
            if (username.text.isNotEmpty() && password.text.isNotEmpty()) {
                val account = Account(username.text, password.text)
                AppContext.context.networkApi.runNetworkAction(NetworkApi.requestCreateAccount(account))
            } else {
                MessageDialog(0, "Enter account details!", 400, 200)
            }
        }

        main.children.add(grid)
        main.children.add(registerButton)
    }
}
