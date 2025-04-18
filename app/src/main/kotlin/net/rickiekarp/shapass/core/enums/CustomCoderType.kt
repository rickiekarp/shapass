package net.rickiekarp.core.enums

import net.rickiekarp.core.math.noise.NoiseConfig
import net.rickiekarp.core.math.noise.NoiseInput

enum class CustomCoderType(private val seed: Long, private val noiseConfig: NoiseConfig) {
    V1(
        0,
        NoiseConfig(NoiseInput(640, 640, 6, 0.01), 0xFF, 0)
    ),
    V2(
        4358922007433029608,
        NoiseConfig(NoiseInput(640, 640, 8, 0.01), 0xFF, 0x100)
    );

    fun getDefaultSeed() = seed
    fun getDefaultNoiseConfig() = noiseConfig
}