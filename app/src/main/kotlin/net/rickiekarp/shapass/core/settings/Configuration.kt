package net.rickiekarp.core.settings

import javafx.geometry.Side
import javafx.scene.paint.Color
import net.rickiekarp.core.AppContext
import net.rickiekarp.core.debug.LogFileHandler
import net.rickiekarp.core.provider.LocalizationProvider
import net.rickiekarp.core.ui.windowmanager.ThemeSelector
import net.rickiekarp.core.ui.windowmanager.Window
import net.rickiekarp.core.util.CommonUtil
import net.rickiekarp.core.util.FileUtil
import net.rickiekarp.core.view.SettingsScene
import java.io.File
import java.io.IOException
import java.lang.reflect.Field
import java.net.URISyntaxException
import java.util.*
import java.util.jar.JarFile

/**
 * Contains general application information (Name, Version etc.)
 */
class Configuration
/**
 * Initialize configuration.
 * @param configFileName the filename
 */
(internal val configFileName: String, clazz: Class<*>) {
    var settingsXmlFactory: SettingsXmlFactory? = null
        private set

    /** Jar location  */
    lateinit var jarFile: File
        private set
    val configDirFile: File
        get() = File(jarFile.parentFile.toString() + File.separator + "data")
    val profileDirFile: File
        get() = File(jarFile.parentFile.toString() + File.separator + "data" + File.separator + "profiles")
    val pluginDirFile: File
        get() = File(configDirFile.toString() + File.separator + "plugins")
    val logsDirFile: File
        get() = File(jarFile.parentFile.toString() + File.separator + "logs" + File.separator + CommonUtil.getTime("yyyy-MM-dd"))
    val updatesDirFile: File
        get() = File(configDirFile.toString() + File.separator + "update")

    init {

        try {
            jarFile = File(clazz.protectionDomain.codeSource.location.toURI().path)
        } catch (e: URISyntaxException) {
            e.printStackTrace()
        }

        //set internal version number
        try {
            val manifest = JarFile(jarFile.path).manifest
            AppContext.context.internalVersion = FileUtil.readManifestProperty(manifest, "Build-Time")
        } catch (e: IOException) {
            AppContext.context.internalVersion = CommonUtil.getDate("yyMMddHHmm")
        }

    }

    /**
     * Try to load the configuration file.
     */
    fun load(): Boolean {
        //instantiate SettingsXmlFactory
        settingsXmlFactory = SettingsXmlFactory()

        //check if config file exists
        if (!File(config.configDirFile.toString() + File.separator + configFileName).exists()) {
            var isDirectoryCreated = config.configDirFile.exists()
            if (!isDirectoryCreated) {
                isDirectoryCreated = config.configDirFile.mkdir()
            }
            if (isDirectoryCreated) {
                settingsXmlFactory!!.createConfigXML()
            } else {
                return false
            }
        }
        loadProperties(this.javaClass)

        //set current Locale
        LocalizationProvider.setCurrentLocale()
        //sets up the logger
        LogFileHandler.setupLogger()
        //starts logging
        LogFileHandler.startLogging()

        //post config set-ups
        when (colorScheme) {
            0 -> Window.colorTheme = "darkgray"
            1 -> Window.colorTheme = "gray"
            2 -> Window.colorTheme = "black"
            3 -> Window.colorTheme = "red"
            4 -> Window.colorTheme = "orange"
            5 -> Window.colorTheme = "yellow"
            6 -> Window.colorTheme = "blue"
            7 -> Window.colorTheme = "magenta"
            8 -> Window.colorTheme = "purple"
            9 -> Window.colorTheme = "green"
            else -> Window.colorTheme = "darkgray"
        }
        return true
    }

    /**
     * Assign properties to fields.
     */
    fun loadProperties(clazz: Class<*>) {
        try {
            for (f in clazz.declaredFields) {
                if (f.isAnnotationPresent(LoadSave::class.java)) {
                    f.isAccessible = true //work around issue where fields are private (AppConfiguration)
                    val n = f.name
                    if (f.type == java.lang.Boolean.TYPE) {
                        val s = settingsXmlFactory!!.getElementValue(n, clazz)
                        if (s != null) {
                            f.set(this, java.lang.Boolean.valueOf(s))
                        }
                    } else if (f.type == Integer.TYPE || f.type == Int::class.java) {
                        val s = settingsXmlFactory!!.getElementValue(n, clazz)
                        if (s != null) {
                            f.set(this, Integer.valueOf(s))
                        }
                    } else if (f.type == String::class.java) {
                        val s = settingsXmlFactory!!.getElementValue(n, clazz)
                        f.set(this, s)
                    } else if (f.type == Byte::class.javaPrimitiveType) {
                        val s = settingsXmlFactory!!.getElementValue(n, clazz)
                        f.set(this, java.lang.Byte.valueOf(s!!))
                    } else if (f.type == Color::class.java) {
                        val s = settingsXmlFactory!!.getElementValue(n, clazz)
                        f.set(this, Color.valueOf(s!!))
                    } else if (f.type == Side::class.java) {
                        val s = settingsXmlFactory!!.getElementValue(n, clazz)
                        f.set(this, Side.valueOf(s!!))
                    } else if (f.type.isEnum) {
                        f.set(this, f.type)
                    } else {
                        LogFileHandler.logger.warning("Field type '" + f.type + "' not found!")
                    }
                }
            }
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        }
    }

    /**
     * Try to save the configuration file.
     */
    fun save() {
        saveProperties(this.javaClass)
    }

    /**
     * Try to save the configuration file.
     */
    fun saveProperties(clazz: Class<*>) {
        try {
            for (f in clazz.declaredFields) {
                if (f.isAnnotationPresent(LoadSave::class.java)) {
                    val n = f.name
                    val o = f.get(this)
                    if (f.type == Color::class.java) {
                        settingsXmlFactory!!.setElementValue(n, ThemeSelector.getColorHexString(o as Color))
                    } else {
                        settingsXmlFactory!!.setElementValue(n, o.toString())
                    }
                }
            }
        } catch (ex: IllegalAccessException) {
            ex.printStackTrace()
        }

    }

    /**
     * Resets all settings to the default value.
     */
    fun setDefaults() {
        val fields0 = LoadSave::class.java.declaredFields
        val list = LinkedList<Field>()

        try {
            for (i in 0 until LoadSave::class.java.declaredFields.size) {
                for (f in this.javaClass.declaredFields) {
                    if (f.name == fields0[i].name) {
                        //System.out.println(f.getName() + " cur: " + f.get(LoadSave.class) + " - def: " + fields0[i].get(LoadSave.class));

                        //add setting that has changed to a list
                        if (f.get(LoadSave::class.java) != fields0[i].get(LoadSave::class.java)) {
                            list.add(f)
                        }

                        //Integer
                        if (f.type == Integer.TYPE) {
                            f.set(this, fields0[i].get(LoadSave::class.java))
                            break
                        } else if (f.type == java.lang.Boolean.TYPE) {
                            f.set(this, fields0[i].get(LoadSave::class.java))
                            break
                        } else if (f.type == Color::class.java) {
                            f.set(this, fields0[i].get(LoadSave::class.java))
                            break
                        } else if (f.type == Side::class.java) {
                            f.set(this, fields0[i].get(LoadSave::class.java))
                            break
                        }
                    }
                }
            }
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        }

        //update Settings GUI
        if (SettingsScene.settingsScene != null) {
            SettingsScene.settingsScene!!.updateGUI(list)
        }
    }

    companion object {
        lateinit var config: Configuration

        /** settings  */
        @LoadSave
        var host: String? = null
        @LoadSave
        var updateChannel: Int = 0
        @LoadSave
        var language: Int = 0
        @LoadSave
        var themeState: Int = 0
        @LoadSave
        var colorScheme: Int = 0
        @LoadSave
        var animations: Boolean = false
        @LoadSave
        var useSystemBorders: Boolean = false
        @LoadSave
        var logState: Boolean = false
        @LoadSave
        var showTrayIcon: Boolean = false

        /** advanced settings  */
        @LoadSave
        var debugState: Boolean = false
        @LoadSave
        var decorationColor: Color? = null
        @LoadSave
        var shadowColorFocused: Color? = null
        @LoadSave
        var shadowColorNotFocused: Color? = null
        @LoadSave
        var tabPosition: Side? = null

        /** locale  */
        var CURRENT_LOCALE: Locale? = null
    }
}