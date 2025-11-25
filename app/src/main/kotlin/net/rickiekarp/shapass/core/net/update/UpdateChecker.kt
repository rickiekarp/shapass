package net.rickiekarp.shapass.core.net.update

import net.rickiekarp.shapass.core.AppContext
import net.rickiekarp.shapass.core.debug.LogFileHandler
import net.rickiekarp.shapass.core.model.dto.ApplicationDTO
import net.rickiekarp.shapass.core.net.NetworkApi
import net.rickiekarp.shapass.core.settings.Configuration
import net.rickiekarp.shapass.core.util.FileUtil
import tools.jackson.core.type.TypeReference
import tools.jackson.databind.ObjectMapper
import java.io.File
import java.io.IOException
import java.net.URISyntaxException
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.*

class UpdateChecker {

    /**
     * Compares local and remote program versions and returns the update status ID
     * @return  Returns update status ID as an integer
     * Status IDs are: 0 (No update), 1 (Update), 2 (No connection), 3 (Error)
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
