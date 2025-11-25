package net.rickiekarp.shapass.core.net.update

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import javafx.application.Platform
import net.rickiekarp.shapass.core.AppContext
import net.rickiekarp.shapass.core.debug.DebugHelper
import net.rickiekarp.shapass.core.debug.ExceptionHandler
import net.rickiekarp.shapass.core.debug.LogFileHandler
import net.rickiekarp.shapass.core.model.dto.ApplicationDTO
import net.rickiekarp.shapass.core.net.NetworkApi
import net.rickiekarp.shapass.core.settings.Configuration
import net.rickiekarp.shapass.core.util.FileUtil
import java.io.File
import java.io.IOException
import java.net.URI
import java.net.URISyntaxException
import java.net.URL
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.*

class UpdateChecker {

    /**
     * Checks the listed server for a new java version
     * @return Returns remote version string
     */
    private val remoteJavaVersion: String
        @Deprecated("Feature was removed because the JreCurrentVersion2.txt was removed")
        get() {

            val version: String
            var javaurl: URL? = null

            LogFileHandler.logger.info("Checking for new java version...")

            try {
                javaurl = URI.create("http://java.com/applet/JreCurrentVersion2.txt").toURL();

                LogFileHandler.logger.info("Connecting to: $javaurl")
                val scanner = Scanner(javaurl.openStream())
                version = scanner.next()
                scanner.close()
                LogFileHandler.logger.info("Success! Current remote java version: $version")

                return version
            } catch (e: IOException) {
                LogFileHandler.logger.warning("Can not connect to: " + javaurl!!)
                if (DebugHelper.DEBUG) {
                    e.printStackTrace()
                } else {
                    Platform.runLater { ExceptionHandler(e) }
                }
                return "no_connection"
            }

        }

    /**
     * Compares local and remote program versions and returns the update status ID
     * @return  Returns update status ID as an integer
     * Status ID's are: 0 (No update), 1 (Update), 2 (No connection), 3 (Error)
     */
    fun checkProgramUpdate(): Int {
        val inputStream = AppContext.context.networkApi.runNetworkAction(NetworkApi.requestVersionInfo(Configuration.updateChannel))
                ?: return 2

        val applicationList: List<ApplicationDTO>
        try {
            applicationList = ObjectMapper().readValue(inputStream, object : TypeReference<List<ApplicationDTO>>() {

            })
        } catch (e: IOException) {
            e.printStackTrace()
            return 3
        }


        var isUpdateEnabled: Boolean // Is the update channel open?
        for (applicationEntry in applicationList) {
            isUpdateEnabled = applicationEntry.isUpdateEnable
            if (isUpdateEnabled) {
                LogFileHandler.logger.info("Checking module [" + applicationEntry.identifier + "] version: " + applicationEntry.version)

                //convert local/remote versions to int
                val remoteVer = applicationEntry.version
                var localVer = -1
                try {
                    localVer = Integer.parseInt(FileUtil.readManifestPropertyFromJar(Configuration.config.jarFile.parent + File.separator + applicationEntry.identifier, "Build-Time"))
                } catch (e: IOException) {
                    LogFileHandler.logger.warning("Error while reading version: " + e.message)
                }

                //compare versions
                if (remoteVer > localVer) {
                    filesToDownload.add(applicationEntry.identifier!!)
                }
            }
        }

        //return update status for files to update
        if (filesToDownload.size > 0) {
            isUpdAvailable = true
            LogFileHandler.logger.info("New updates found: " + filesToDownload.subList(0, filesToDownload.size))
            return 1
        }
        return 0
    }

    companion object {
        var filesToDownload = ArrayList<String>()
        var isUpdAvailable = false

        /**
         * Starts the updater and installs updates.
         * @throws URISyntaxException Exception
         * @throws IOException Exception
         */
        @Throws(URISyntaxException::class, IOException::class)
        fun installUpdate() {
            /* is it a jar file? */
            if (!Configuration.config.jarFile.name.endsWith(".jar")) {
                return
            }

            val javaBin = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java"

            val updater = File(Configuration.config.jarFile.parentFile.toString() + "/data/update/updater.jar")

            //install new updater.jar if it has been downloaded earlier
            if (updater.exists()) {
                val moveFrom = updater.toPath()
                val moveTo = Configuration.config.jarFile.parentFile.toPath()

                //move file
                Files.move(moveFrom, moveTo.resolve(moveFrom.fileName), StandardCopyOption.REPLACE_EXISTING)
            }

            /* Build command: java -jar application.jar */
            val command = ArrayList<String>()
            command.add(javaBin)
            command.add("-jar")
            command.add(Configuration.config.jarFile.parent + File.separator + "updater.jar")
            command.add("update")
            command.add(Configuration.config.jarFile.name)

            if (!File(Configuration.config.jarFile.parentFile.toString() + "/updater.jar").exists()) {
                return
            }

            val builder = ProcessBuilder(command)
            builder.start()
            System.exit(0)
        }
    }
}
