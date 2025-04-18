package net.rickiekarp.core.account

import net.rickiekarp.core.AppContext
import net.rickiekarp.core.debug.LogFileHandler
import net.rickiekarp.core.net.NetResponse
import net.rickiekarp.core.net.NetworkApi
import net.rickiekarp.core.settings.Configuration
import net.rickiekarp.core.util.FileUtil
import net.rickiekarp.core.util.crypt.Base64Coder
import net.rickiekarp.core.util.parser.JsonParser
import org.json.JSONException
import org.json.JSONObject

import java.io.File
import java.io.IOException
import java.util.logging.Level

class AccountManager {
    var account: Account? = null
    var isRememberPass: Boolean = false
        private set
    var isAutoLogin: Boolean = false
        private set

    private val PROFILE_KEY = "profile.json"

    fun loadAccountFromFile(): Account? {
        var activeProfile = File(Configuration.config.profileDirFile.toString() + "/active")
        if (activeProfile.exists()) {
            try {
                val profileString = FileUtil.readFirstLineFromFile(activeProfile)
                activeProfile = File(Configuration.config.profileDirFile.toString() + "/" + profileString + "/" + PROFILE_KEY)
                val profileJson = JsonParser.readJsonFromFile(activeProfile)
                var password: String
                try {
                    password = Base64Coder.decodeString(profileJson.getString("signinkey"))
                    isRememberPass = true
                } catch (e: JSONException) {
                    password = ""
                }

                isAutoLogin = profileJson.getBoolean("autoLogin")
                return Account(profileString, password)
            } catch (e: IOException) {
                e.printStackTrace()
            }

        } else {
            LogFileHandler.logger.warning("File: " + activeProfile.path + " not found!")
        }
        return null
    }

    fun createActiveProfile(rememberPass: Boolean, autoLogin: Boolean) {
        var profileDir = File(Configuration.config.profileDirFile.path + "/" + account!!.user)
        if (profileDir.mkdirs()) {
            LogFileHandler.logger.log(Level.INFO, "created " + profileDir.path)
        }

        val useraccount = JSONObject()
        if (rememberPass) {
            useraccount.put("signinkey", Base64Coder.encodeString(account!!.password!!))
        }
        useraccount.put("autoLogin", autoLogin)
        JsonParser.writeJsonObjectToFile(useraccount, profileDir, PROFILE_KEY)

        profileDir = File(Configuration.config.profileDirFile.toString() + "/active")
        val activeProfileData = account!!.user!!.toByteArray()
        FileUtil.writeFile(activeProfileData, profileDir.path)
    }

    fun updateAccessToken(): Boolean {
        val validationAction = NetworkApi.requestAccessToken(account!!)
        val inputStream = AppContext.context.networkApi.runNetworkAction(validationAction)
        if (inputStream != null) {
            val token = NetResponse.getResponseJson(inputStream)
            if (token != null) {
                account!!.accessToken = token.getString("token")
                return true
            }
        }
        return false
    }
}
