package net.rickiekarp.shapass.core.net.update

import net.rickiekarp.shapass.core.debug.LogFileHandler
import net.rickiekarp.shapass.core.settings.Configuration
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.RandomAccessFile
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URI
import java.net.URL
import java.util.*

// This class downloads a file from a URL.
class FileDownloader : Observable, Runnable {

    // Get download list
    var downloadList = ArrayList<String>()

    private var url: URL? = null // download URL
    // Get this download's size.
    var size: Int = 0
        private set // size of download in bytes
    private var downloaded: Int = 0 // number of bytes downloaded
    // Get this download's status.
    var status: Int = 0
        private set // current status of download

    // Get this download's progress.
    val progress: Float
        get() = downloaded.toFloat() / size

    // Constructor for Download.
    constructor(url: URL) {
        this.url = url
        this.downloadList.add(getFileName(url))
        begin()
    }

    // Constructor for Download.
    constructor(url: URL, downloadList: ArrayList<String>) {
        this.url = url
        this.downloadList = downloadList
        begin()
    }

    // Pause this download.
    private fun begin() {
        size = -1
        downloaded = 0
        status = PAUSED

        if (!Configuration.config.updatesDirFile.exists()) {
            Configuration.config.updatesDirFile.mkdirs()
        }

        status = DOWNLOADING
        stateChanged()

        //start the download
        download()
    }

    // Mark this download as having an error.
    private fun error() {
        status = ERROR
        stateChanged()
        LogFileHandler.logger.warning("An error occured when trying to update!")
    }

    // Start or resume downloading.
    private fun download() {
        val thread = Thread(this)
        thread.start()
    }

    // Download file.
    override fun run() {
        var file: RandomAccessFile? = null
        var stream: InputStream? = null
        val downloadURL: URL
        try {
            downloadURL = URI.create(getHostString(url!!) + "/" + downloadList[0]).toURL();

        } catch (e: MalformedURLException) {
            e.printStackTrace()
            return
        }

        LogFileHandler.logger.info("Connecting to: $downloadURL")
        try {
            // Open connection to URL.
            val connection = downloadURL.openConnection() as HttpURLConnection

            // Specify what portion of file to download.
            connection.setRequestProperty("Range", "bytes=$downloaded-")

            // Connect to server.
            connection.connect()

            // Make sure response code is in the 200 range.
            if (connection.responseCode / 100 != 2) {
                LogFileHandler.logger.info("error")
                error()
            }

            LogFileHandler.logger.info("Success! Starting download...")

            // Check for valid content length.
            val contentLength = connection.contentLength
            LogFileHandler.logger.info("File size: $contentLength")
            if (contentLength < 1) {
                error()
            }

            /* Set the size for this download if it
     hasn't been already set. */
            if (size == -1) {
                size = contentLength
                stateChanged()
            }

            // Open file and seek to the end of it.
            file = RandomAccessFile(Configuration.config.updatesDirFile.toString() + File.separator + getFileName(downloadURL), "rw")
            LogFileHandler.logger.info("Filepointer: " + file.filePointer)
            file.seek(downloaded.toLong())

            stream = connection.inputStream

            while (status == DOWNLOADING) {
                /* Size buffer according to how much of the file is left to download. */
                val buffer: ByteArray = if (size - downloaded > MAX_BUFFER_SIZE) {
                    ByteArray(MAX_BUFFER_SIZE)
                } else {
                    ByteArray(size - downloaded)
                }

                // Read from server into buffer.
                val read = stream!!.read(buffer)
                if (read == -1) {
                    break
                }

                // Write buffer to file.
                file.write(buffer, 0, read)
                downloaded += read
                stateChanged()
            }

            /* Change status to complete if this point was
     reached because downloading has finished. */
            if (status == DOWNLOADING) {
                LogFileHandler.logger.info("Download complete!")

                //remove just downloaded file from the list and check if there are more files to load
                downloadList.removeAt(0)
                if (downloadList.size >= 1) {
                    begin()
                } else {
                    status = COMPLETE
                    stateChanged()
                    LogFileHandler.logger.info("All done!")
                }
            }
        } catch (e1: IOException) {
            // there was some connection problem, or the file did not exist on the server,
            // or your URL was not in the right format.
            LogFileHandler.logger.warning("Can not connect to $downloadURL")
        } catch (e1: Exception) {
            error()
        } finally {
            // Close file.
            if (file != null) {
                try {
                    file.close()
                } catch (e: Exception) {
                    println("file close error")
                }

            }

            // Close connection to server.
            if (stream != null) {
                try {
                    stream.close()
                } catch (e: Exception) {
                    println("stream close error")
                }

            }
        }
    }

    // Notify observers that this download's status has changed.
    private fun stateChanged() {
        setChanged()
        notifyObservers()
    }

    // Get file name portion of URL.
    private fun getFileName(url: URL): String {
        return url.file.substring(url.file.lastIndexOf('/') + 1)
    }

    // Get host url portion of URL.
    private fun getHostString(url: URL): String {
        return url.toString().substring(0, url.toString().lastIndexOf("/"))
    }

    companion object {

        // Max size of download buffer.
        private val MAX_BUFFER_SIZE = 1024

        // These are the status codes.
        val PAUSED = 0
        val DOWNLOADING = 1
        val COMPLETE = 2
        val ERROR = 3
    }
}