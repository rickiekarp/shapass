package net.rickiekarp.shapass.core.util

import java.util.*

object OSValidator {

    val os: OperatingSystem?
        get() {
            val os = System.getProperty("os.name").lowercase(Locale.getDefault())
            return if (isWindows(os)) {
                OperatingSystem.WINDOWS
            } else if (isMac(os)) {
                OperatingSystem.MAC
            } else if (isUnix(os)) {
                OperatingSystem.UNIX
            } else if (isSolaris(os)) {
                OperatingSystem.SOLARIS
            } else {
                println("Could not detect operating system!")
                null
            }
        }

    enum class OperatingSystem {
        WINDOWS,
        MAC,
        UNIX,
        SOLARIS
    }

    @JvmStatic
    fun main(args: Array<String>) {
        val os = os
        if (os != null) {
            when (os) {
                OperatingSystem.WINDOWS -> println("This is Windows")
                OperatingSystem.MAC -> println("This is Mac")
                OperatingSystem.UNIX -> println("This is Unix or Linux")
                OperatingSystem.SOLARIS -> println("This is Solaris")
            }
        }
    }

    private fun isWindows(os: String): Boolean {
        return os.contains("win")
    }

    private fun isMac(os: String): Boolean {
        return os.contains("mac")
    }

    private fun isUnix(os: String): Boolean {
        return os.contains("nix") || os.contains("nux") || os.indexOf("aix") > 0
    }

    private fun isSolaris(os: String): Boolean {
        return os.contains("sunos")
    }
}