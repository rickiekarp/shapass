package net.rickiekarp.shapass.core.util.parser

import net.rickiekarp.shapass.core.debug.LogFileHandler
import org.apache.commons.io.IOUtils
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.io.FileWriter
import java.io.IOException

object JsonParser {

    fun readJsonFromFile(file: File): JSONObject {
        if (file.exists()) {
            try {
                val `is` = FileInputStream(file)
                val jsonTxt = IOUtils.toString(`is`, "utf-8")
                return JSONObject(jsonTxt)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        } else {
            LogFileHandler.logger.warning("File does not exist at " + file.path)
        }
        return JSONObject()
    }

    fun writeJsonObjectToFile(obj: JSONObject, outputFile: File, fileName: String) {
        if (!outputFile.exists()) {
            if (!outputFile.mkdirs()) {
                println("Could not create directory at " + outputFile.path)
                return
            }
        }

        try {
            FileWriter(outputFile.toString() + File.separator + fileName).use { file -> file.write(obj.toString(4)) }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}
