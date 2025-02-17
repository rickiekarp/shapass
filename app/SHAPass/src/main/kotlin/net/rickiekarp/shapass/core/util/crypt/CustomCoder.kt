package net.rickiekarp.core.util.crypt

import net.rickiekarp.core.enums.CustomCoderType
import net.rickiekarp.core.model.CustomCoderConfig
import net.rickiekarp.core.util.crypt.custom.CustomCoderV1
import net.rickiekarp.core.util.crypt.custom.CustomCoderV2

object CustomCoder {

    fun encode(input: String, config: CustomCoderConfig) : String {
        return when (config.coderType) {
            CustomCoderType.V1 -> { CustomCoderV1.encode(input, config) }
            CustomCoderType.V2 -> { CustomCoderV2.encode(input, config) }
        }
    }

    fun decode(input: String, config: CustomCoderConfig) : String {
        return when (config.coderType) {
            CustomCoderType.V1 -> { CustomCoderV1.decode(input, config) }
            CustomCoderType.V2 -> { CustomCoderV2.decode(input, config) }
        }
    }
}