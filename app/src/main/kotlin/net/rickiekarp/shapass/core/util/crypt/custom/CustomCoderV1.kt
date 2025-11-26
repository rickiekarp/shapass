package net.rickiekarp.shapass.core.util.crypt.custom

import net.rickiekarp.shapass.core.extensions.addCharAtIndex
import net.rickiekarp.shapass.core.extensions.removeCharAtIndex
import net.rickiekarp.shapass.core.model.CharsetFlags
import net.rickiekarp.shapass.core.model.CustomCoderConfig
import net.rickiekarp.shapass.core.util.math.MathUtil
import net.rickiekarp.shapass.core.util.random.RandomCharacter
import net.rickiekarp.shapass.libloader.GoLibTransformer
import kotlin.text.iterator

object CustomCoderV1 {

    fun encode(input: String, config: CustomCoderConfig) : String {
        var outputText = ""
        val computedSeed = RandomCharacter.getMd5SeedAsLong(config.baseSeed)
        val shuffledCharacters = RandomCharacter.getCharacterListShuffled(computedSeed, config.characterSetBitFlag)

        var inputText = input
        inputText = inputText.trim().replace("[^a-zA-Z0-9]".toRegex(), "")

        var index = 0
        for (character in inputText) {
            val seedCharacterAsInt = RandomCharacter.getCharacterFromSeed(index, computedSeed)
            val outChar = RandomCharacter.getCharacterAtIndex(RandomCharacter.letterToAlphabetPos(character) + seedCharacterAsInt, shuffledCharacters)
            outputText += outChar.toString()
            index++
        }

        val numberOfCharsToAdd = MathUtil.log2(config.baseSeed.length, 0)
        val md5 = GoLibTransformer.VelesLib.CalculateHashMD5(config.baseSeed)
            .replace("[^1-9]".toRegex(), "").substring(0, numberOfCharsToAdd)

        for (md5Digit in md5.toSortedSet().sorted()) {
            val randomCharacter = getRandomCharacter(config.characterSetBitFlag)
            outputText = outputText.addCharAtIndex(randomCharacter, md5Digit.digitToInt())
        }

        return outputText
    }

    private fun getRandomCharacter(characterSetBitFlag: CharsetFlags) : Char {
        val characterList = GoLibTransformer.VelesLib.GetCharsetForBitFlag(characterSetBitFlag.asInt())
        return characterList[(characterList.indices).random()]
    }

    fun decode(input: String, config: CustomCoderConfig) : String {
        var outputText = ""

        val seed = RandomCharacter.getMd5SeedAsLong(config.baseSeed)
        val shuffledCharacters = RandomCharacter.getCharacterListShuffled(seed, config.characterSetBitFlag)

        val numberOfCharsToAdd = MathUtil.log2(config.baseSeed.length, 0)
        val md5 = GoLibTransformer.VelesLib.CalculateHashMD5(config.baseSeed)
            .replace("[^1-9]".toRegex(), "").substring(0, numberOfCharsToAdd)

        var inputText = input
        for (md5Digit in md5.toSortedSet().sortedDescending()) {
            inputText = inputText.removeCharAtIndex(md5Digit.digitToInt())
        }

        var index = 0
        for (character in inputText) {
            val seedCharacterAsInt = RandomCharacter.getCharacterFromSeed(index, seed)
            val characterIndex = RandomCharacter.getIndexFromChar(character, shuffledCharacters) - seedCharacterAsInt
            val decodedChar = RandomCharacter.alphabetPosToLetter(characterIndex)
            outputText += decodedChar.toString()
            index++
        }

        return outputText
    }
}