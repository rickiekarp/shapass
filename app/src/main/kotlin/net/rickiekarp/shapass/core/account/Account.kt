package net.rickiekarp.core.account

import net.rickiekarp.core.debug.LogFileHandler

class Account(var user: String?, var password: String?) {
    var accessToken: String? = null
        internal set(accessToken) {
            LogFileHandler.logger.info("Setting new access token!")
            field = accessToken
        }

    override fun toString(): String {
        return "Account{" +
                "user='" + user + '\''.toString() +
                ", password='" + password + '\''.toString() +
                '}'.toString()
    }
}
