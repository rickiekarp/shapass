package net.rickiekarp.shapass.libloader

import com.sun.jna.Native

object GoLibTransformer {
    private const val VELES_LIB_PATH = "libs/natives/veles.so"

    val VelesLib: IGoVelesLibTransformer = Native.load(
        VELES_LIB_PATH, IGoVelesLibTransformer::class.java
    )
}