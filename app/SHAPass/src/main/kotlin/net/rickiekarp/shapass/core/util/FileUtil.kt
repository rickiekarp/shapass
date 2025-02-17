package net.rickiekarp.core.util

import net.rickiekarp.core.debug.DebugHelper
import net.rickiekarp.core.debug.ExceptionHandler
import java.io.*
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.util.jar.Attributes
import java.util.jar.JarFile
import java.util.jar.Manifest

object FileUtil {

    fun readManifestProperty(manifest: Manifest, key: String): String {
        val attributes = manifest.mainAttributes
        return attributes.getValue(key)
    }

    @Throws(IOException::class)
    fun readManifestPropertyFromJar(jarPath: String, key: String): String {
        val jar = JarFile(jarPath)
        val manifest = jar.manifest
        val attributes = manifest.mainAttributes
        jar.close()
        return attributes.getValue(key)
    }


    @Throws(IOException::class)
    fun readManifestPropertiesFromJar(jarPath: String, vararg keys: String): List<String> {
        val values = ArrayList<String>()
        for (key in keys) {
            values.add(getManifestAttributes(jarPath).getValue(key))
        }
        return values
    }

    @Throws(IOException::class)
    private fun getManifestAttributes(jarPath: String): Attributes {
        val jar = JarFile(jarPath)
        val manifest = jar.manifest
        val attributes = manifest.mainAttributes
        jar.close()
        return attributes
    }

    fun getListOfFiles(selectedDirectory: File): Array<File>? {
        //list all files in start directory
        return selectedDirectory.listFiles()
    }

    fun moveFile(moveFrom: Path, moveTo: Path) {
        if (!moveTo.toFile().exists()) {
            moveTo.toFile().mkdirs()
        }
        try {
            Files.move(moveFrom, moveTo.resolve(moveFrom.fileName), StandardCopyOption.REPLACE_EXISTING)
        } catch (e: IOException) {
            if (DebugHelper.DEBUG) {
                e.printStackTrace()
            } else {
                ExceptionHandler(e)
            }
        }
    }

    @Throws(IOException::class)
    fun readFirstLineFromFile(filepath: File): String {
        val brTest = BufferedReader(FileReader(filepath))
        return brTest.readLine()
    }

    fun writeFile(data: ByteArray, filepath: String) {
        val out: FileOutputStream
        try {
            out = FileOutputStream(filepath)
            out.write(data)
            out.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}
