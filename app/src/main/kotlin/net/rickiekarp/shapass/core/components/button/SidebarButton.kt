package net.rickiekarp.core.components.button

import javafx.scene.control.Button

class SidebarButton(val identifier: String) : Button() {
    init {
        text = identifier
        styleClass.addAll("sidebar-button")
    }
}