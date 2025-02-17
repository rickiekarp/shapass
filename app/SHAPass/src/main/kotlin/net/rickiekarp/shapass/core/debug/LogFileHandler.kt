package net.rickiekarp.core.debug

import javafx.collections.FXCollections
import net.rickiekarp.core.provider.LocalizationProvider
import net.rickiekarp.core.settings.Configuration
import net.rickiekarp.core.util.CommonUtil
import net.rickiekarp.core.view.MessageDialog
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.io.PrintStream
import java.util.*
import java.util.logging.*
import java.util.logging.Formatter

/**
 * This class handles the Logfile behaviour (set up logger, create logfile etc.)
 */
class LogFileHandler {

    companion object {

        var logger = Logger.getLogger("AppLog")

        private var formatter: Formatter? = null
        private var fh: FileHandler? = null
        private val logData = FXCollections.observableArrayList<String>()
        private val logLevel = Level.CONFIG

        /**
         * Sets up the logger
         * Called on program start
         */
        fun setupLogger() {

            logger.useParentHandlers = false
            logger.level = logLevel

            // defines the Log File formatting
            formatter = object : Formatter() {
                override fun format(record: LogRecord): String {
                    val sb = StringBuilder()

                    sb.append(Date(record.millis))
                            .append(" - ")
                            .append(record.level)
                            .append(" in ").append(record.sourceMethodName)
                            .append(": ")
                            .append(formatMessage(record))
                            .append(System.getProperty("line.separator"))

                    logData.add(sb.toString())
                    return sb.toString()
                }
            }
        }

        fun startLogging() {
            //starts the fileHandler if logState = true
            if (Configuration.logState) {
                onLogStateChange()
            }

            //shows logging in the console if DEBUGVERSION = true
            val ch = ConsoleHandler()
            ch.level = logLevel
            ch.formatter = formatter

            //add the console handler to the logger
            logger.addHandler(ch)
        }

        /**
         * Starts / Stops the FileHandler
         * Called when: program starts / settings change / log file was created
         */
        fun onLogStateChange() {
            if (fh == null) {
                if (!Configuration.config.logsDirFile.exists()) {
                    Configuration.config.logsDirFile.mkdirs()
                }
                try {
                    fh = FileHandler(Configuration.config.logsDirFile.path + File.separator + getLogFileName())
                } catch (e1: IOException) {
                    if (DebugHelper.DEBUG) {
                        e1.printStackTrace()
                    } else {
                        ExceptionHandler(e1)
                    }
                }

                fh!!.setLevel(logLevel)
                fh!!.setFormatter(formatter)

                //add the file handler to the logger
                logger.addHandler(fh)
                logger.log(Level.INFO, "Logging started")
            } else {
                logger.removeHandler(fh)
                fh!!.close()
                fh = null
            }
        }

        /**
         * Writes all logged data to a file
         * Only called when using the /log create command
         */
        fun createLogFile() {
            //checks if the fileHandler is active
            if (fh != null) {
                //fileHandler is closed if active
                logger.removeHandler(fh)
                fh!!.close()
                fh = null
                logData.clear()

                //open a new fileHandler for a new logfile
                if (Configuration.logState) {
                    onLogStateChange()
                }
            } else
            //executed if fileHandler is not active
            {
                //checks if there is logData that can be written to a file
                if (logData.size > 0) {

                    //checks if 'logs' directory exists
                    if (Configuration.config.logsDirFile.exists()) {
                        writeLog()
                    } else {
                        //create logs directory
                        Configuration.config.logsDirFile.mkdirs()
                        logger.log(Level.INFO, "'logs' directory created in " + Configuration.config.jarFile.parent)
                        writeLog()
                    }
                } else {
                    MessageDialog(0, LocalizationProvider.getString("logFile_created_fail") + " " + LocalizationProvider.getString("no_logData_desc"), 450, 220)
                }
            }
        }

        private fun writeLog() {
            try {
                val ps = PrintStream(File(Configuration.config.logsDirFile, getLogFileName()))
                //logData.forEach(ps::print);
                logData.forEach( { ps.print(it) })
                ps.close()
            } catch (e1: FileNotFoundException) {
                if (DebugHelper.DEBUG) {
                    e1.printStackTrace()
                } else {
                    ExceptionHandler(e1)
                }
            }

            logData.clear()
        }

        /**
         * Deletes all logged data
         */
        fun clearLogData() {
            if (logData.size != 0) {
                logData.clear()
                logger.log(Level.INFO, "Logfile cleared!")
            }
        }

        /**
         * Deletes all logged data
         */
        fun getLogSize() {
            MessageDialog(1, LocalizationProvider.getString("log_size_current") + " " + logData.size, 450, 220)
        }

        /**
         * Returns log file name
         */
        private fun getLogFileName(): String {
            val logTime = CommonUtil.getTime("HH-mm-ss") //time String
            return "log_$logTime.log"
        }

    }


}