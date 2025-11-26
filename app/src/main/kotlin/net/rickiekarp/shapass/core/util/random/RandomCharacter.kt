package net.rickiekarp.shapass.core.util.random

import net.rickiekarp.shapass.core.model.CharsetFlags
import net.rickiekarp.shapass.libloader.GoLibTransformer
import java.util.*

object RandomCharacter {

    fun getMd5SeedAsLong(inputData : String) : Long {
        var md5 = GoLibTransformer.VelesLib.CalculateHashMD5(inputData)

        // take the first 16 characters to provide a proper seed length
        md5 = md5.take(16)

        // replace hexadecimal letters with digits
        md5 = md5.replace("a", "2")
        md5 = md5.replace("b", "5")
        md5 = md5.replace("c", "3")
        md5 = md5.replace("d", "7")
        md5 = md5.replace("e", "1")
        md5 = md5.replace("f", "4")

        return md5.toLong()
    }

    fun getCharacterListShuffled(seed : Long, characterSetBitFlag: CharsetFlags) : List<Char> {
        val characterList = GoLibTransformer.VelesLib.GetCharsetForBitFlag(characterSetBitFlag.asInt()).toMutableList()
        characterList.shuffle(Random(seed));
        return characterList.toList()
    }

    fun getCharacterAtIndex(index : Int, characterList : List<Char>) : Char {
        return characterList[index % characterList.size]
    }

    fun getCharacterAtIndex(index : Int, characterSetBitFlag: CharsetFlags) : Char {
        val characterList = getCharacterListShuffled(Long.MIN_VALUE, characterSetBitFlag)
        return characterList[index % characterList.size]
    }

    fun getIndexFromChar(character : Char, characterList : List<Char>) : Int {
        return characterList.indexOf(character)
    }

    fun letterToAlphabetPos(letter: Char, characterShift : Int = 64): Int {
        return (letter.uppercaseChar() - characterShift).code
    }

    fun getCharacterFromSeed(index : Int, seed : Long = Long.MIN_VALUE) : Int {
        val seedString = seed.toString()
        return seedString[index % seedString.length].digitToInt()
    }

    fun alphabetPosToLetter(pos: Int, characterShift : Int = 64): Char {
        return (pos + characterShift).toChar()
    }
}