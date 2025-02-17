package net.rickiekarp.core.ui.windowmanager

import javafx.scene.image.Image

object ImageLoader {
    private var appIcon: Image? = null
    private var appIconSmall: Image? = null
    private var menu: Image? = null
    private var menuHover: Image? = null

    fun getAppIcon(): Image? {
        if (appIcon == null) {
            appIcon = Image("ui/icons/app_icon_big.png")
        }
        return appIcon
    }

    fun getAppIconSmall(): Image? {
        if (appIconSmall == null) {
            appIconSmall = Image("ui/icons/app_icon_small.png")
        }
        return appIconSmall
    }

    internal fun getMenu(loader: ClassLoader): Image? {
        if (menu == null) {
            menu = Image(loader.getResource("components/titlebar/menu.png").toString())
        }
        return menu
    }

    internal fun getMenuHover(loader: ClassLoader): Image? {
        if (menuHover == null) {
            menuHover = Image(loader.getResource("components/titlebar/menu-hover.png").toString())
        }
        return menuHover
    }
}
