package net.rickiekarp.shapass.core.view

import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.Button
import javafx.scene.control.CheckBox
import javafx.scene.control.Label
import javafx.scene.layout.*
import javafx.stage.Modality
import javafx.stage.Stage
import net.rickiekarp.shapass.core.debug.DebugHelper
import net.rickiekarp.shapass.core.debug.ExceptionHandler
import net.rickiekarp.shapass.core.debug.LogFileHandler
import net.rickiekarp.shapass.core.net.update.UpdateChecker
import net.rickiekarp.shapass.core.provider.LocalizationProvider
import net.rickiekarp.shapass.core.settings.Configuration
import net.rickiekarp.shapass.core.ui.windowmanager.ImageLoader
import net.rickiekarp.shapass.core.ui.windowmanager.WindowScene
import net.rickiekarp.shapass.core.ui.windowmanager.WindowStage
import java.io.IOException
import java.net.URISyntaxException

/**
 * This class is used for creating different message dialogs.
 * Example: Error Message
 */
class MessageDialog(type: Int, msg: String, width: Int, height: Int) {

    init {
        when (type) {
            0 -> createDialog("error", msg, width, height)
            1 -> createDialog("info", msg, width, height)
        }
    }

    companion object {

        private fun createDialog(title: String, msg: String, width: Int, height: Int) {
            val modalDialog = Stage()
            modalDialog.icons.add(ImageLoader.getAppIconSmall())
            modalDialog.initModality(Modality.APPLICATION_MODAL)
            modalDialog.isResizable = false
            modalDialog.width = width.toDouble()
            modalDialog.height = height.toDouble()
            modalDialog.title = LocalizationProvider.getString(title)

            //Layout
            val contentPane = BorderPane()

            val vbox = VBox()
            vbox.padding = Insets(20.0, 0.0, 0.0, 20.0)

            val controls = HBox()
            controls.padding = Insets(10.0, 0.0, 10.0, 0.0)  //padding top, left, bottom, right
            controls.alignment = Pos.CENTER

            //Components
            val label = Label(msg)
            vbox.children.addAll(label)
            label.isWrapText = true

            val okButton = Button("OK")
            okButton.setOnAction { _ -> modalDialog.close() }
            controls.children.add(okButton)

            // The UI (Client Area) to display
            contentPane.center = vbox
            contentPane.bottom = controls

            val modalDialogScene = WindowScene(WindowStage("message", modalDialog), contentPane, 1)

            modalDialog.scene = modalDialogScene
            modalDialog.show()

            LogFileHandler.logger.info("open.MessageDialog($title)")
        }

        fun confirmDialog(msg: String, width: Int, height: Int): Boolean {
            val modalDialog = Stage()
            modalDialog.icons.add(ImageLoader.getAppIconSmall())
            modalDialog.initModality(Modality.APPLICATION_MODAL)
            modalDialog.isResizable = false
            modalDialog.width = width.toDouble()
            modalDialog.height = height.toDouble()
            modalDialog.title = LocalizationProvider.getString("confirm")

            val bool = BooleanArray(1)

            val borderpane = BorderPane()

            val contentVbox = VBox()
            contentVbox.spacing = 20.0

            val optionHBox = HBox()
            optionHBox.spacing = 20.0
            optionHBox.alignment = Pos.CENTER
            optionHBox.padding = Insets(5.0, 0.0, 15.0, 0.0)

            //components
            val label = Label(LocalizationProvider.getString(msg))
            label.isWrapText = true
            label.padding = Insets(20.0, 10.0, 10.0, 20.0)

            val yesButton = Button(LocalizationProvider.getString("yes"))
            yesButton.setOnAction { _ ->
                bool[0] = true
                modalDialog.close()
            }

            val noButton = Button(LocalizationProvider.getString("no"))
            noButton.setOnAction { _ ->
                bool[0] = false
                modalDialog.close()
            }

            if (DebugHelper.isDebugVersion) {
                contentVbox.style = "-fx-background-color: gray"
                optionHBox.style = "-fx-background-color: #444444;"
            }

            optionHBox.children.addAll(yesButton, noButton)

            // The UI (Client Area) to display
            contentVbox.children.addAll(label, optionHBox)
            VBox.setVgrow(contentVbox, Priority.ALWAYS)

            borderpane.center = contentVbox
            borderpane.bottom = optionHBox

            // The Window as a Scene
            val modalDialogScene = WindowScene(WindowStage("confirm", modalDialog), borderpane, 1)

            modalDialog.scene = modalDialogScene

            LogFileHandler.logger.info("open.confirmDialog")

            modalDialog.showAndWait()

            return bool[0]
        }

        fun restartDialog(msg: String, width: Int, height: Int): Boolean {
            val modalDialog = Stage()
            modalDialog.icons.add(ImageLoader.getAppIconSmall())
            modalDialog.initModality(Modality.APPLICATION_MODAL)
            modalDialog.isResizable = false
            modalDialog.width = width.toDouble()
            modalDialog.height = height.toDouble()
            modalDialog.title = LocalizationProvider.getString("restartApp")

            val bool = BooleanArray(1)

            val borderpane = BorderPane()

            val contentVbox = VBox()
            contentVbox.spacing = 20.0

            val optionHBox = HBox()
            optionHBox.spacing = 20.0
            optionHBox.alignment = Pos.CENTER
            optionHBox.padding = Insets(5.0, 0.0, 15.0, 0.0)

            //components
            val label = Label(LocalizationProvider.getString(msg))
            label.isWrapText = true
            label.padding = Insets(20.0, 10.0, 10.0, 20.0)

            val yesButton = Button(LocalizationProvider.getString("yes"))
            yesButton.setOnAction { _ ->
                try {
                    //save settings
                    try {
                        Configuration.config.save()
                    } catch (e1: Exception) {
                        if (DebugHelper.DEBUG) {
                            e1.printStackTrace()
                        } else {
                            ExceptionHandler(e1)
                        }
                    }

                    //restart
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

            val noButton = Button(LocalizationProvider.getString("restartLater"))
            noButton.setOnAction { _ ->
                bool[0] = false
                modalDialog.close()
            }

            if (DebugHelper.isDebugVersion) {
                contentVbox.style = "-fx-background-color: gray"
                optionHBox.style = "-fx-background-color: #444444;"
            }

            optionHBox.children.addAll(yesButton, noButton)

            // The UI (Client Area) to display
            contentVbox.children.addAll(label, optionHBox)
            VBox.setVgrow(contentVbox, Priority.ALWAYS)

            borderpane.center = contentVbox
            borderpane.bottom = optionHBox

            // The Window as a Scene
            val modalDialogScene = WindowScene(WindowStage("restart", modalDialog), borderpane, 1)

            modalDialog.scene = modalDialogScene

            LogFileHandler.logger.info("open.errorMessageDialog")

            modalDialog.showAndWait()

            return bool[0]
        }

        fun installUpdateDialog(width: Int, height: Int): Stage {
            val modalDialog = Stage()
            modalDialog.icons.add(ImageLoader.getAppIconSmall())
            modalDialog.initModality(Modality.APPLICATION_MODAL)
            modalDialog.isResizable = false
            modalDialog.width = width.toDouble()
            modalDialog.height = height.toDouble()
            modalDialog.title = LocalizationProvider.getString("installUpdate")

            val borderpane = BorderPane()

            val contentVbox = VBox()
            contentVbox.spacing = 20.0

            val options = AnchorPane()
            options.minHeight = 50.0

            val optionHBox = HBox()
            optionHBox.spacing = 10.0

            //components
            val label = Label(LocalizationProvider.getString("update_desc"))
            label.isWrapText = true
            label.padding = Insets(20.0, 10.0, 10.0, 20.0)

            val remember = CheckBox(LocalizationProvider.getString("hideThis"))
            remember.isDisable = true
            remember.setOnAction { _ -> println(remember.isSelected) }

            val yesButton = Button(LocalizationProvider.getString("yes"))
            yesButton.setOnAction { _ ->
                try {
                    UpdateChecker.installUpdate()
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

            val noButton = Button(LocalizationProvider.getString("no"))
            noButton.setOnAction { _ -> modalDialog.close() }

            optionHBox.children.addAll(yesButton, noButton)

            // The UI (Client Area) to display
            contentVbox.children.addAll(label)
            options.children.addAll(remember, optionHBox)

            AnchorPane.setRightAnchor(optionHBox, 5.0)
            AnchorPane.setBottomAnchor(optionHBox, 5.0)
            AnchorPane.setLeftAnchor(remember, 10.0)
            AnchorPane.setBottomAnchor(remember, 10.0)

            borderpane.center = contentVbox
            borderpane.bottom = options

            // The Window as a Scene
            val modalDialogScene = WindowScene(WindowStage("installUpdate", modalDialog), borderpane, 1)
            modalDialog.scene = modalDialogScene
            LogFileHandler.logger.info("open.installUpdateDialog")

            return modalDialog
        }
    }
}
