package net.rickiekarp.core.ui.tray

import javafx.application.Platform
import net.rickiekarp.core.AppContext
import net.rickiekarp.core.view.MainScene
import java.io.IOException
import javax.imageio.ImageIO
import kotlin.system.exitProcess

class ToolTrayIcon {
    private var trayIcon: java.awt.TrayIcon? = null
    var systemTray: java.awt.SystemTray? = null
        private set

    init {
        setUpTray()
        addAppToTray()
    }

    private fun setUpTray() {
        try {
            // ensure awt toolkit is initialized.
            java.awt.Toolkit.getDefaultToolkit()

            // app requires system tray support, just exit if there is no support.
            if (!java.awt.SystemTray.isSupported()) {
                println("No system tray support, application exiting.")
                Platform.exit()
            }

            // set up a system tray icon.
            systemTray = java.awt.SystemTray.getSystemTray()

            val imgName = "ui/icons/app_icon_small.png"
            val `is` = this.javaClass.classLoader.getResourceAsStream(imgName)

            val image = ImageIO.read(`is`!!)
            trayIcon = java.awt.TrayIcon(image)

            trayIcon!!.toolTip = "Giveaway Bot"

            // if the user double-clicks on the tray icon, show the main app stage.
            trayIcon!!.addActionListener { _ -> Platform.runLater { this.showStage() } }

            // if the user selects the default menu item (which includes the app name),
            // show the main app stage.
            val openItem = java.awt.MenuItem(AppContext.context.applicationName)
            openItem.addActionListener { _ -> Platform.runLater { this.showStage() } }

            // the convention for tray icons seems to be to set the default icon for opening
            // the application stage in a bold font.
            val defaultFont = java.awt.Font.decode(null)
            val boldFont = defaultFont.deriveFont(java.awt.Font.BOLD)
            openItem.font = boldFont

            // to really exit the application, the user must go to the system tray icon
            // and select the exit option, this will shut down JavaFX and remove the
            // tray icon (removing the tray icon will also shut down AWT).
            val exitItem = java.awt.MenuItem("Exit")
            exitItem.addActionListener { _ -> exitProcess(0) }

            // setup the popup menu for the application.
            val popup = java.awt.PopupMenu()
            popup.add(openItem)
            popup.addSeparator()
            popup.add(exitItem)
            trayIcon!!.popupMenu = popup

        } catch (e: IOException) {
            println("Unable to init system tray")
            e.printStackTrace()
        }

    }

    /**
     * Sets up a system tray icon for the application.
     */
    fun addAppToTray() {
        // add the application tray icon to the system tray.
        try {
            systemTray!!.add(trayIcon!!)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // instructs the javafx system not to exit implicitly when the last application window is shut.
        Platform.setImplicitExit(false)
    }

    fun displayTrayMessage() {
        trayIcon!!.displayMessage(
                "Information!",
                "Bot starts in 15 seconds...",
                java.awt.TrayIcon.MessageType.INFO
        )
    }

    fun removeTrayIcon() {
        systemTray!!.remove(trayIcon)
        Platform.setImplicitExit(true)
    }

    /**
     * Shows the application stage and ensures that it is brought ot the front of all stages.
     */
    private fun showStage() {
        MainScene.mainScene.windowScene!!.win.windowStage.stage.show()
        MainScene.mainScene.windowScene!!.win.windowStage.stage.toFront()
    }

    companion object {
        var icon: ToolTrayIcon = ToolTrayIcon()
    }
}