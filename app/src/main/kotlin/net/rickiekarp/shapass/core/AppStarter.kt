package net.rickiekarp.core

import javafx.application.Application
import javafx.application.Platform
import javafx.event.EventHandler
import javafx.scene.input.KeyEvent
import javafx.stage.Stage
import net.rickiekarp.core.debug.DebugHelper
import net.rickiekarp.core.debug.ExceptionHandler
import net.rickiekarp.core.provider.LocalizationProvider
import net.rickiekarp.core.settings.Configuration
import net.rickiekarp.core.settings.LoadSave
import net.rickiekarp.core.ui.tray.ToolTrayIcon
import net.rickiekarp.core.ui.windowmanager.ImageLoader
import net.rickiekarp.core.view.MainScene
import net.rickiekarp.core.view.MessageDialog
import net.rickiekarp.core.view.layout.AppLayout

open class AppStarter : Application() {
    private var mainClazz: Class<*>? = null
    private var configClazz: Class<*>? = null
    protected var isConfigLoaded: Boolean = false
    private var winType: Byte = 0
    private var minWidth: Int = 0
    private var minHeight: Int = 0
    private var width: Int = 0
    private var height: Int = 0
    private var resizable: Boolean = false
    private var onKeyPressedHandler: EventHandler<KeyEvent>? = null

    override fun start(stage: Stage) {
        AppContext.create(mainClazz!!.getPackage().name)

        //load config file
        Configuration.config = Configuration("config.xml", mainClazz!!)
        isConfigLoaded = Configuration.config.load()
        if (isConfigLoaded) {
            //load additional application related configuration
            if (configClazz != null) {
                Configuration.config.loadProperties(configClazz!!)
            }

            //log properties of current program state0
            DebugHelper.logProperties()
        } else {
            //if the config file can not be created, set settings anyway
            Configuration.language = LoadSave.language
            LocalizationProvider.setCurrentLocale()
        }

        //load language properties file
        LocalizationProvider.loadLangFile(mainClazz!!.classLoader.getResourceAsStream("language_packs/language_" + Configuration.CURRENT_LOCALE + ".properties"))

        //set the default exception handler
        if (!DebugHelper.DEBUG) {
            Thread.setDefaultUncaughtExceptionHandler { _, e -> Platform.runLater { ExceptionHandler(e) } }
            Thread.currentThread().uncaughtExceptionHandler = Thread.UncaughtExceptionHandler { _, throwable -> ExceptionHandler(
                throwable
            )}
        }

        //application related configuration
        stage.title = AppContext.context.applicationName
        stage.icons.add(ImageLoader.getAppIcon())
        stage.isResizable = resizable
        stage.minWidth = minWidth.toDouble()
        stage.minHeight = minHeight.toDouble()
        stage.width = width.toDouble()
        stage.height = height.toDouble()

        MainScene(stage, winType)

        //set up the Client Area to display
        if (node != null) {
            MainScene.mainScene.borderPane.center = node!!.layout
            node!!.postInit()
        }

        //post launch settings
        if (Configuration.showTrayIcon) {
            ToolTrayIcon()
        }

        if (onKeyPressedHandler != null) {
            MainScene.mainScene.windowScene!!.onKeyPressed = onKeyPressedHandler
        }

        //disable settings view if no config file is present
        if (!isConfigLoaded) {
            MessageDialog(0, LocalizationProvider.getString("config_not_found"), 500, 250)
            MainScene.mainScene.windowScene!!.win.sidebarButtonBox!!.children[0].isDisable = true
        }
    }

    protected fun setMainClazz(clazz: Class<*>) {
        mainClazz = clazz
    }

    protected fun setConfigClazz(clazz: Class<*>) {
        configClazz = clazz
    }

    protected fun setLayout(node: AppLayout?) {
        AppStarter.node = node
    }

    protected fun setWinType(type: Byte) {
        winType = type
    }

    protected fun setMinWidth(width: Int) {
        minWidth = width
    }

    protected fun setMinHeight(height: Int) {
        minHeight = height
    }

    protected fun setWidth(defWidth: Int) {
        width = defWidth
    }

    protected fun setHeight(defHeight: Int) {
        height = defHeight
    }

    protected fun makeResizable() {
        resizable = true
    }

    protected fun setOnKeyPressedHandler(handler: EventHandler<KeyEvent>) {
        onKeyPressedHandler = handler
    }

    companion object {
        private var node: AppLayout? = null
    }
}
