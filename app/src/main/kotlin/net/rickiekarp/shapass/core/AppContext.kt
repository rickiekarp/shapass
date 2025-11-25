package net.rickiekarp.shapass.core

import net.rickiekarp.shapass.core.net.NetworkApi
import net.rickiekarp.shapass.core.provider.LocalizationProvider
import net.rickiekarp.shapass.core.settings.Configuration
import net.rickiekarp.shapass.core.util.FileUtil
import java.io.IOException
import java.util.jar.JarFile
import java.util.jar.Manifest

class AppContext private constructor(val contextIdentifier: String, val networkApi: NetworkApi) {
    var internalVersion: String? = null

    val applicationName: String
        get() = LocalizationProvider.getString(contextIdentifier)

    val versionNumber: String?
        get() {
            val manifest: Manifest
            return try {
                manifest = JarFile(Configuration.config.jarFile.path).manifest
                FileUtil.readManifestProperty(manifest, "Version")
            } catch (e: IOException) {
                internalVersion
            }

        }

    companion object {
        lateinit var context: AppContext
            private set

        fun create(identifier: String) {
            context = AppContext(identifier, NetworkApi())
        }

        fun create(identifier: String, network: NetworkApi) {
            context = AppContext(identifier, network)
        }
    }
}
