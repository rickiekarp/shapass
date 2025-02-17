package net.rickiekarp.shapass.libloader

import com.sun.jna.Native

object GoLibTransformer {
    private const val SHA1PASS_LIB_PATH = "libs/natives/libsha1pass.so"

    val Sha1PassLib: IGoLibSha1PassTransformer = Native.load(SHA1PASS_LIB_PATH, IGoLibSha1PassTransformer::class.java)
}