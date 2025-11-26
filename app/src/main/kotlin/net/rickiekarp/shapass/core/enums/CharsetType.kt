package net.rickiekarp.shapass.core.enums

enum class CharsetType(val bit: Int) {
    CYRILLIC(1 shl 0),
    GREEK(1 shl 1),
    LATIN(1 shl 2)
}