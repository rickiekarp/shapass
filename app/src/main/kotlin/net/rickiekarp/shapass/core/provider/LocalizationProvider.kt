package net.rickiekarp.shapass.core.provider

import net.rickiekarp.shapass.core.debug.LogFileHandler
import net.rickiekarp.shapass.core.settings.Configuration
import java.io.IOException
import java.io.InputStream
import java.util.*

object LocalizationProvider {
    private var prop: Properties? = null

    val currentLocale: Int
        get() {
            val var0 = Configuration.CURRENT_LOCALE.toString()
            when (var0.hashCode()) {
                3201 -> if (var0 == "de") {
                    return 1
                }
                3241 -> if (var0 == "en") {
                    return 0
                }
            }
            return 0
        }

    val locale: Int
        get() {
            val var10000 = System.getProperty("user.language")
            if (var10000 != null) {
                when (var10000.hashCode()) {
                    3201 -> if (var10000 == "de") {
                        return 1
                    }
                    3241 -> if (var10000 == "en") {
                        return 0
                    }
                }
            }
            return 0
        }

    @JvmStatic
    fun loadLangFile(utf8in: InputStream?) {
        if (utf8in != null) {
            println("Locale: " + Configuration.CURRENT_LOCALE)
            try {
                prop!!.load(utf8in)
            } catch (var2: IOException) {
                var2.printStackTrace()
            }

            utf8in.close()
        } else {
            LogFileHandler.logger.warning("Could not load language file")
        }
    }

    @JvmStatic
    fun getString(textID: String): String {
        val value = prop!!.getProperty(textID)
        if (value == null || value.isBlank()) {
            LogFileHandler.logger.warning("Error when loading text ID: $textID")
            return textID
        }
        return value
    }

    @JvmStatic
    fun setCurrentLocale() {
        when (Configuration.language) {
            0 -> Configuration.CURRENT_LOCALE = Locale.ENGLISH
            1 -> Configuration.CURRENT_LOCALE = Locale.GERMAN
            else -> Configuration.CURRENT_LOCALE = Locale.ENGLISH
        }
    }

    init {
        prop = Properties()
    }
}