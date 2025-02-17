package net.rickiekarp.core.debug

import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.TextArea
import javafx.scene.layout.*
import javafx.scene.paint.Color
import javafx.stage.Modality
import javafx.stage.Stage
import net.rickiekarp.core.provider.LocalizationProvider
import net.rickiekarp.core.ui.windowmanager.ThemeSelector
import net.rickiekarp.core.util.ClipboardUtil
import java.util.logging.Level

/**
 * This class is the default uncaught exception handler.
 * It contains the GUI if an exception is thrown
 */
class ExceptionHandler(throwable: Throwable) {

    init {
        LogFileHandler.logger.log(Level.SEVERE, getExceptionString(throwable))
        createExceptionGUI(throwable)
    }

    /**
     * Creates exception gui
     */
    private fun createExceptionGUI(e: Throwable) {
        val modalDialog = Stage()
        modalDialog.title = LocalizationProvider.getString("error")
        modalDialog.initModality(Modality.APPLICATION_MODAL)
        modalDialog.isResizable = true
        modalDialog.setOnCloseRequest { modalDialog.close() }

        val borderpane = BorderPane()
        val grid = GridPane()
        val controls = HBox()

        val exception = Scene(borderpane, 800.0, 440.0, Color.TRANSPARENT)
        exception.stylesheets.add(ThemeSelector.DARK_THEME_CSS)

        modalDialog.scene = exception
        modalDialog.show()

        //add components
        val exTF = TextArea()
        exTF.isEditable = false

        val status = Label()
        status.isVisible = false
        controls.children.add(status)

        val copy = Button(LocalizationProvider.getString("copy"))
        controls.children.add(copy)

        //set layout
        grid.padding = Insets(5.0)
        val column1 = ColumnConstraints()
        column1.percentWidth = 100.0
        val row1 = RowConstraints()
        row1.percentHeight = 100.0
        grid.columnConstraints.addAll(column1)
        grid.rowConstraints.add(row1)

        controls.padding = Insets(15.0, 12.0, 15.0, 12.0)  //padding top, left, bottom, right
        controls.spacing = 10.0
        controls.alignment = Pos.CENTER_RIGHT

        grid.children.add(0, exTF)

        borderpane.center = grid
        borderpane.bottom = controls

        //action listener
        copy.setOnAction {
            status.isVisible = true
            ClipboardUtil.setStringToClipboard(exTF.text)
            status.style = "-fx-text-fill: #55c4fe;"
            status.text = LocalizationProvider.getString("exception_copied")
        }

        exTF.text = getExceptionString(e)
    }

    companion object {

        /**
         * Returns exception string
         */
        fun getExceptionString(t: Throwable): String {

            val sb = StringBuilder()

            sb.append(t.toString()).append(System.getProperty("line.separator"))

            val trace = t.stackTrace
            for (aTrace in trace) {
                sb.append("       at ").append(aTrace.toString()).append(System.getProperty("line.separator"))
            }
            return sb.toString()
        }


        /**
         * Throws an exception (for testing)
         */
        fun throwTestException() {
            try {
                throw IndexOutOfBoundsException("TEST")
            } catch (e1: IndexOutOfBoundsException) {
                if (DebugHelper.DEBUG) {
                    e1.printStackTrace()
                    ExceptionHandler(e1)
                } else {
                    ExceptionHandler(e1)
                }
            }

        }
    }
}
