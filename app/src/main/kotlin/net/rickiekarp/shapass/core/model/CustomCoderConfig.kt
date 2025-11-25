package net.rickiekarp.shapass.core.model

import net.rickiekarp.shapass.core.enums.AlphabetType
import net.rickiekarp.shapass.core.enums.CustomCoderType
import net.rickiekarp.shapass.math.PerlinNoise2D

class CustomCoderConfig(
    var coderType: CustomCoderType,
    var baseSeed: String,
    var characterSetConfig: MutableMap<AlphabetType, Boolean>,
    var preserveWhiteSpaces: Boolean = false,
    var noiseGenerator: PerlinNoise2D? = null
)