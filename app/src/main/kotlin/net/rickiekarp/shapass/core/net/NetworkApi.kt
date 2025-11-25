package net.rickiekarp.shapass.core.net

import net.rickiekarp.shapass.core.AppContext
import net.rickiekarp.shapass.core.net.provider.NetworkParameterProvider

import java.io.InputStream

open class NetworkApi {
    private val mConnectionHandler: ConnectionHandler = ConnectionHandler()

    fun runNetworkAction(networkAction: NetworkAction): InputStream? {
        return mConnectionHandler.requestInputStream(networkAction)
    }

    companion object {
        protected var INFO_DOMAIN = "info"
        private val UPDATE_ACTION = "update"

        fun requestVersionInfo(updateChannel: Int): NetworkAction {
            val provider = NetworkParameterProvider.create()
            provider.put("identifier", AppContext.context.contextIdentifier)
            provider.put("channel", updateChannel)
            return NetworkAction.Builder.create().setHost(NetworkAction.DATASERVER).setDomain(INFO_DOMAIN).setAction(UPDATE_ACTION).setParameters(provider).setMethod("GET").build()
        }
    }
}
