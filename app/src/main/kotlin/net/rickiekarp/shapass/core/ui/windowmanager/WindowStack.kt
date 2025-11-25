package net.rickiekarp.shapass.core.ui.windowmanager

import javafx.scene.Node
import javafx.scene.control.Button
import net.rickiekarp.shapass.core.view.MainScene
import java.util.*

class WindowStack : Stack<Node>() {
    private lateinit var backButton: Button

    override fun push(item: Node): Node {
        addElement(item)
        MainScene.mainScene.borderPane.center = item
        if (this.size == 2) {
            initializeBackButton()
            MainScene.mainScene.windowScene!!.win.titleBarButtonBox.children.add(backButton)
        }
        return item
    }

    private fun initializeBackButton() {
        backButton = Button("Back")
        backButton.setOnAction { _ ->
            this.pop()
            MainScene.mainScene.borderPane.center = super.peek()
            if (this.size == 1) {
                MainScene.mainScene.windowScene!!.win.titleBarButtonBox.children.remove(backButton)
            }
        }
    }
}
