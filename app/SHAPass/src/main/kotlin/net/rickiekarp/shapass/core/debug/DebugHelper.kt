package net.rickiekarp.core.debug

import net.rickiekarp.core.AppContext
import net.rickiekarp.core.settings.Configuration
import java.io.File
import java.io.IOException
import java.net.URISyntaxException

/**
 * This class contains helper functions used for debugging purposes.
 */
object DebugHelper {
    const val DEBUG = false
    private var startTime: Long = 0

    val isDebugVersion: Boolean
        get() {
            if (DEBUG) {
                return true
            } else {
                if (Configuration.debugState) {
                    return true
                }
            }
            return false
        }

    fun logProperties() {
        LogFileHandler.logger.info("JAVA_VERSION=" + System.getProperty("java.version"))
        LogFileHandler.logger.config("PROGRAM_VERSION=" + AppContext.context.internalVersion)
        LogFileHandler.logger.config("DEBUGVERSION=" + DebugHelper.isDebugVersion)
        LogFileHandler.logger.config("UPDATE_CHANNEL=" + Configuration.updateChannel)
        LogFileHandler.logger.config("LOGS=" + Configuration.logState)
        LogFileHandler.logger.config("PROGRAM_LANGUAGE={" + Configuration.language + "," + Configuration.CURRENT_LOCALE.toString() + "}")
        LogFileHandler.logger.config("SYSTEM_TRAY=" + Configuration.showTrayIcon)
        LogFileHandler.logger.config("HOST=" + Configuration.host)
    }

    fun profile(state: String, name: String) {
        when (state) {
            "start" -> if (startTime == 0L) {
                startTime = System.nanoTime()
                LogFileHandler.logger.info("Start monitoring $name")
            } else {
                LogFileHandler.logger.warning("Profiler already started!")
            }
            "stop" -> if (startTime != 0L) {
                val endTime = System.nanoTime()
                val elapsedTimeInMillis = (endTime - startTime).toDouble() / 1000000
                LogFileHandler.logger.info("Stop monitoring " + name + "! Total execution time: " + elapsedTimeInMillis + "ms")
                startTime = 0
            } else {
                LogFileHandler.logger.warning("Profiler not started!")
            }
        }
    }

    /**
     * Restarts the application. Only works when running the jar file.
     * @throws URISyntaxException URISyntaxException
     * @throws IOException IOException
     */
    @Throws(URISyntaxException::class, IOException::class)
    fun restartApplication() {

        val javaBin = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java"
        val currentJar = File(DebugHelper::class.java.protectionDomain.codeSource.location.toURI())

        /* is it a jar file? */
        if (!currentJar.name.endsWith(".jar")) {
            return
        }

        /* Build command: java -jar application.jar */
        val command = ArrayList<String>()
        command.add(javaBin)
        command.add("-jar")
        command.add(currentJar.path)

        val builder = ProcessBuilder(command)
        builder.start()
        System.exit(0)
    }
}
