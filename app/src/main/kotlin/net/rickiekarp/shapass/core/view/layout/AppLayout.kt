package net.rickiekarp.shapass.core.view.layout

import javafx.scene.Node

interface AppLayout {
    val layout: Node
    fun postInit()
}
