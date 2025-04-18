package net.rickiekarp.core.net

import net.rickiekarp.core.AppContext
import net.rickiekarp.core.account.Account
import net.rickiekarp.core.net.provider.NetworkParameterProvider
import okhttp3.Response

import java.io.InputStream

open class NetworkApi {
    private val mConnectionHandler: ConnectionHandler = ConnectionHandler()

    fun runNetworkAction(networkAction: NetworkAction): InputStream? {
        return mConnectionHandler.requestInputStream(networkAction)
    }

    fun requestResponse(networkAction: NetworkAction): Response? {
        return mConnectionHandler.request(networkAction)
    }

    companion object {
        protected var ACCOUNT_DOMAIN = "account"
        protected var INFO_DOMAIN = "info"
        private val TOKEN_ACTION = "authorize"
        private val REGISTER_ACTION = "create"
        private val LOGIN_ACTION = "login"
        private val UPDATE_ACTION = "update"
        private val CHANGELOG_ACTION = "changelog"

        fun requestAccessToken(account: Account): NetworkAction {
            val provider = NetworkParameterProvider.create()
            provider.put("username", account.user)
            provider.put("password", account.password)
            return NetworkAction.Builder.create().setHost(NetworkAction.LOGINSERVER).setDomain(ACCOUNT_DOMAIN).setAction(TOKEN_ACTION).setParameters(provider).setMethod("POST").build()
        }


        fun requestLoginData(): NetworkAction {
            return NetworkAction.Builder.create().setHost(NetworkAction.LOGINSERVER).setDomain(ACCOUNT_DOMAIN).setAction(LOGIN_ACTION).setMethod("POST").build()
        }

        fun requestCreateAccount(account: Account): NetworkAction {
            val provider = NetworkParameterProvider.create()
            provider.put("username", account.user)
            provider.put("password", account.password)
            return NetworkAction.Builder.create().setHost(NetworkAction.LOGINSERVER).setDomain(ACCOUNT_DOMAIN).setAction(REGISTER_ACTION).setParameters(provider).setMethod("POST").build()
        }

        fun requestVersionInfo(updateChannel: Int): NetworkAction {
            val provider = NetworkParameterProvider.create()
            provider.put("identifier", AppContext.context.contextIdentifier)
            provider.put("channel", updateChannel)
            return NetworkAction.Builder.create().setHost(NetworkAction.DATASERVER).setDomain(INFO_DOMAIN).setAction(UPDATE_ACTION).setParameters(provider).setMethod("GET").build()
        }

        fun requestChangelog(): NetworkAction {
            val provider = NetworkParameterProvider.create()
            provider.put("identifier", AppContext.context.contextIdentifier)
            return NetworkAction.Builder.create().setHost(NetworkAction.DATASERVER).setDomain(INFO_DOMAIN).setAction(CHANGELOG_ACTION).setParameters(provider).setMethod("GET").build()
        }
    }
}
