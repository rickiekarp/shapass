package net.rickiekarp.core.view.layout

import javafx.scene.Node

interface AppLayout {
    val layout: Node
    fun postInit()
}
