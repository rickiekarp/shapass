package net.rickiekarp.shapass.libloader

import com.sun.jna.Library

interface IGoVelesLibTransformer : Library {

    // hashing functions
    fun CalculateHashMD5(input: String): String
    fun CalculateHashHex(input: String): String
    fun CalculateHashBCrypt(input: String, cost: Int): String
    fun CalculateHashBCryptDefault(input: String): String
    fun CalculateHashSha3256(input: String): String
    fun CalculateHashSha3256HMAC(input: String, secret: String): String
    fun CalculateHashSha3512(input: String): String
    fun CalculateHashSha3512HMAC(input: String, secret: String): String
    fun CalculateHashCustom(input: String, length: Int, charsetFlags: Int, configFlags: Int): String
    fun CalculateHashCustomDefault(input: String, length: Int): String
    fun GetCharsetForBitFlag(bitFlag: Int): String
}