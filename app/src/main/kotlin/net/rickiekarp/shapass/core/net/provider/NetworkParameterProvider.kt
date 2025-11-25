package net.rickiekarp.shapass.core.net.provider

import net.rickiekarp.shapass.core.net.NetworkAction
import java.util.*

class NetworkParameterProvider private constructor() : NetworkAction.IParameterProvider {
    private val mParameterMap = LinkedHashMap<String, Any>()

    override val parameters: Map<String, Any>
        get() = mParameterMap

    fun put(key: String, parameter: String?): NetworkParameterProvider {
        if (parameter != null) {
            mParameterMap[key] = parameter.toString()
        }
        return this
    }

    fun put(key: String, parameter: Boolean): NetworkParameterProvider {
        mParameterMap[key] = parameter.toString()
        return this
    }

    fun put(key: String, parameter: Int): NetworkParameterProvider {
        mParameterMap[key] = parameter
        return this
    }

    fun put(key: String, parameter: Locale): NetworkParameterProvider {
        mParameterMap[key] = parameter.language + "_" + parameter.country
        return this
    }

    fun putAll(map: Map<String, String>?): NetworkParameterProvider {
        if (map != null) {
            mParameterMap.putAll(map)
        }
        return this
    }

    fun remove(key: String?): NetworkParameterProvider {
        if (key != null) {
            mParameterMap.remove(key)
        }
        return this
    }

    companion object {
        fun create(): NetworkParameterProvider {
            return NetworkParameterProvider()
        }
    }
}
