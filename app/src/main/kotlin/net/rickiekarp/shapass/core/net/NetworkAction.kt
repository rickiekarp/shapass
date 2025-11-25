package net.rickiekarp.shapass.core.net

import net.rickiekarp.shapass.core.debug.LogFileHandler
import net.rickiekarp.shapass.core.settings.LoadSave

class NetworkAction internal constructor(builder: Builder) {
    internal val method: String? = builder.mMethod
    internal val parameterMap: Map<String, Any> = if (builder.mParameters != null) {
        builder.mParameters!!.parameters
    } else HashMap(0)
    internal val hostUrl: String? = builder.mHostURL
    internal val actionUrl: String = builder.mDomain + "/" + builder.mAction

    interface IParameterProvider {
        val parameters: Map<String, Any>
    }

    /**
     * General NetworkAction Builder class
     */
    class Builder {
        var mHostURL: String? = null
        var mDomain: String? = null
        var mAction: String? = null
        var mParameters: IParameterProvider? = null
        var mMethod: String? = null

        fun setHost(host: String): Builder {
            mHostURL = host
            return this
        }

        fun setDomain(domain: String): Builder {
            mDomain = domain
            return this
        }

        fun setAction(action: String): Builder {
            mAction = action
            return this
        }

        fun setParameters(parameterProvider: IParameterProvider): Builder {
            mParameters = parameterProvider
            return this
        }

        fun setMethod(method: String): Builder {
            mMethod = method
            return this
        }

        fun build(): NetworkAction {
            if (mHostURL == null) {
                LogFileHandler.logger.warning("No host was found, trying default: " + LoadSave.host)
                mHostURL = LoadSave.host
            }
            return NetworkAction(this)
        }

        companion object {

            fun create(): Builder {
                return Builder()
            }
        }
    }

    companion object {
        internal const val LOGINSERVER = "LoginServer"
        const val DATASERVER = "HomeServer"
    }
}
