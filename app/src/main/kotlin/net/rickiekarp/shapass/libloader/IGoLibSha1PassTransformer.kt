package net.rickiekarp.shapass.libloader

import com.sun.jna.Library

interface IGoLibSha1PassTransformer : Library {

    // hashing functions
    fun GetHashMD5(input: String): String
    fun GetHashHex(input: String): String
    fun GetHashBCrypt(input: String, cost: Int): String
    fun GetHashBCryptDefault(input: String): String
    fun GetHashSha3256(input: String): String
    fun GetHashSha3256HMAC(input: String, secret: String): String
    fun GetHashSha3512(input: String): String
    fun GetHashSha3512HMAC(input: String, secret: String): String
    fun GetHashCustom(input: String, length: Int): String
}