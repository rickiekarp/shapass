package net.rickiekarp.shapass.core.components

import javafx.scene.control.Label
import javafx.scene.control.ListCell
import javafx.scene.control.ListView
import javafx.scene.layout.VBox
import javafx.scene.paint.Paint
import javafx.scene.shape.SVGPath
import net.rickiekarp.shapass.core.debug.DebugHelper
import net.rickiekarp.shapass.core.model.SettingEntry
import net.rickiekarp.shapass.core.provider.LocalizationProvider

class FoldableListCell(private var list: ListView<SettingEntry>?) : ListCell<SettingEntry>() {

    private var isInitialized = false

    override fun updateItem(item: SettingEntry?, empty: Boolean) {
        super.updateItem(item, empty)

        if (isInitialized)
            return

        if (!empty) {
            val vbox = VBox()
            graphic = vbox

            val labelHeader = Label(LocalizationProvider.getString(item!!.title!!))
            labelHeader.graphicTextGap = 10.0
            labelHeader.id = "tableview-columnheader-default-bg"
            labelHeader.prefWidth = list!!.width - 40
            labelHeader.prefHeight = 30.0
            if (DebugHelper.DEBUG) {
                labelHeader.style = "-fx-background-color: gray;"
            }

            vbox.children.add(labelHeader)

            labelHeader.graphic = createArrowPath(!item.isHidden)

            labelHeader.setOnMouseEntered {
                labelHeader.style = "-fx-background-color: derive(-fx-base, 5%);"
                item.content.style = "-fx-background-color: derive(-fx-base, 5%);"
            }
            labelHeader.setOnMouseExited {
                labelHeader.style = null
                item.content.style = null
            }
            labelHeader.setOnMouseClicked {
                item.isHidden = !item.isHidden
                if (item.isHidden) {
                    labelHeader.graphic = createArrowPath(false)
                    for (i in 1 until vbox.children.size) {
                        vbox.children.removeAt(i)
                    }
                } else {
                    labelHeader.graphic = createArrowPath(true)
                    vbox.children.add(getItem().content)
                }
            }

            if (!item.isHidden) {
                vbox.children.add(item.content)
            }

            isInitialized = true
        } else {
            text = null
            graphic = null
        }
    }

    private fun createArrowPath(up: Boolean): SVGPath {
        val svg = SVGPath()
        val width = 30 / 4
        if (up) {
            svg.content = "M" + width + " 0 L" + width * 2 + " " + width + " L0 " + width + " Z"
            svg.stroke = Paint.valueOf("white")
            svg.fill = Paint.valueOf("white")
        } else {
            svg.content = "M0 0 L" + width * 2 + " 0 L" + width + " " + width + " Z"
            svg.stroke = Paint.valueOf("white")
            svg.fill = Paint.valueOf("white")
        }
        return svg
    }
}
