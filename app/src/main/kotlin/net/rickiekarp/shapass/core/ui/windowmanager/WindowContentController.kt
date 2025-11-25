package net.rickiekarp.shapass.core.ui.windowmanager

import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import net.rickiekarp.shapass.core.components.button.SidebarButton

class WindowContentController internal constructor() {
    private val sidebarNodes: MutableList<SidebarButton>
    var titlebarButtonBox: HBox? = null
        private set
    var sidebarButtonBox: VBox? = null

    val list: List<SidebarButton>
        get() = sidebarNodes

    init {
        sidebarNodes = ArrayList(3)
    }

    fun addSidebarItem(position: Int, node: SidebarButton) {
        sidebarNodes.add(position, node)
    }

    fun addSidebarItem(node: SidebarButton) {
        addSidebarItem(sidebarNodes.size, node)
    }

    /**
     * Removes a SidebarButton item by its given identifier
     * @param identifier SidebarButton identifier
     */
    fun removeSidebarItemByIdentifier(identifier: String) {
        for (i in sidebarNodes.indices) {
            if (sidebarNodes[i].identifier == identifier) {
                sidebarNodes.removeAt(i)
            }
        }
        sidebarButtonBox!!.children.setAll(sidebarNodes)
    }

    fun setTitleBarRightButtonBox(titleBar: HBox) {
        this.titlebarButtonBox = titleBar
    }
}
