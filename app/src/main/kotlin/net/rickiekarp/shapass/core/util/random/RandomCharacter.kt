package net.rickiekarp.core.util.random

import net.rickiekarp.core.enums.AlphabetType
import net.rickiekarp.core.util.crypt.Md5Coder
import okhttp3.internal.toImmutableList
import java.util.*

object RandomCharacter {

    fun getMd5Seed(inputData : String) : Long {
        var md5 = Md5Coder.calcMd5(inputData);
        md5 = replaceHexLetterWithDigit(md5)
        return md5.substring(0,16).toLong()
    }

    private fun replaceHexLetterWithDigit(md5: String): String {
        var modifiedMd5 = md5
        modifiedMd5 = modifiedMd5.replace("a", "2")
        modifiedMd5 = modifiedMd5.replace("b", "5")
        modifiedMd5 = modifiedMd5.replace("c", "3")
        modifiedMd5 = modifiedMd5.replace("d", "7")
        modifiedMd5 = modifiedMd5.replace("e", "1")
        modifiedMd5 = modifiedMd5.replace("f", "4")
        return modifiedMd5
    }

    private fun getCharacterList(characterSetConfig: MutableMap<AlphabetType, Boolean>): MutableList<Char> {
        var characterSetString = ""

        if (characterSetConfig[AlphabetType.CYRILLIC] != null && characterSetConfig[AlphabetType.CYRILLIC]!!) {
            characterSetString = AlphabetType.CYRILLIC.getCharacters()
        }
        if (characterSetConfig[AlphabetType.LATIN] != null && characterSetConfig[AlphabetType.LATIN]!!) {
            characterSetString += AlphabetType.LATIN.getCharacters()
        }
        if (characterSetConfig[AlphabetType.GREEK] != null && characterSetConfig[AlphabetType.GREEK]!!) {
            characterSetString += AlphabetType.GREEK.getCharacters()
        }

        if (characterSetString == "") {
            characterSetString = AlphabetType.CYRILLIC.getCharacters() + AlphabetType.LATIN.getCharacters() + AlphabetType.GREEK.getCharacters()
        }

        return characterSetString.toMutableList()
    }

    fun getCharacterListShuffled(seed : Long, characterSetConfig: MutableMap<AlphabetType, Boolean>) : List<Char> {
        val characterList = getCharacterList(characterSetConfig)
        characterList.shuffle(Random(seed));
        return characterList.toImmutableList()
    }

    fun getCharacterAtIndex(index : Int, characterList : List<Char>) : Char {
        return characterList[index % characterList.size]
    }

    fun getCharacterAtIndex(index : Int, characterSetConfig: MutableMap<AlphabetType, Boolean>, seed : Long = Long.MIN_VALUE) : Char {
        val characterList = getCharacterListShuffled(seed, characterSetConfig)
        return characterList[index % characterList.size]
    }

    fun getIndexFromChar(character : Char, characterList : List<Char>) : Int {
        return characterList.indexOf(character)
    }

    fun getRandomCharacter(characterSetConfig: MutableMap<AlphabetType, Boolean>) : Char {
        val characterList = getCharacterList(characterSetConfig)
        return characterList[(characterList.indices).random()]
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