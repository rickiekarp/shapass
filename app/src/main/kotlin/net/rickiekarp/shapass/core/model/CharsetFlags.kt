package net.rickiekarp.shapass.core.model

import net.rickiekarp.shapass.core.enums.CharsetType

class CharsetFlags(private var flags: Int = 0) {

    // Add a charset
    fun add(charset: CharsetType) {
        flags = flags or charset.bit
    }

    // Remove a charset
    fun remove(charset: CharsetType) {
        flags = flags and charset.bit.inv()
    }

    // Check if a charset is set
    fun has(charset: CharsetType): Boolean {
        return flags and charset.bit != 0
    }

    // Check if all charset in another set are set
    fun hasAll(vararg charsets: CharsetType): Boolean {
        val combined = charsets.fold(0) { acc, p -> acc or p.bit }
        return flags and combined == combined
    }

    // Get current flags as Int
    fun asInt() = flags

    override fun toString(): String {
        return CharsetType.values()
            .filter { has(it) }
            .joinToString(", ")
    }
}