package net.rickiekarp.core.util.crypt.custom

import net.rickiekarp.core.enums.CustomCoderType
import net.rickiekarp.core.extensions.addCharAtIndex
import net.rickiekarp.core.extensions.removeCharAtIndex
import net.rickiekarp.core.model.CustomCoderConfig
import net.rickiekarp.core.util.crypt.Md5Coder
import net.rickiekarp.core.util.math.MathUtil
import net.rickiekarp.core.util.random.RandomCharacter
import kotlin.math.absoluteValue

object CustomCoderV2 {

    fun encode(input: String, config: CustomCoderConfig) : String {
        var outputText = ""

        val computedSeed = if (config.baseSeed.isEmpty()) {
            config.baseSeed = CustomCoderType.V2.getDefaultSeed().toString()
            RandomCharacter.getMd5Seed(CustomCoderType.V2.getDefaultSeed().toString())
        } else {
            RandomCharacter.getMd5Seed(config.baseSeed)
        }

        val shuffledCharacters = RandomCharacter.getCharacterListShuffled(computedSeed, config.characterSetConfig)

        var inputText = input
        inputText = inputText.replace("[^a-zA-Z0-9 ]".toRegex(), "")

        var index = 0
        for (character in inputText) {
            if (config.preserveWhiteSpaces && character.isWhitespace())
            {
                outputText += " "
                index++
                continue
            }
            val seedCharacterAsInt = RandomCharacter.getCharacterFromSeed(index, computedSeed)
            val outChar = RandomCharacter.getCharacterAtIndex(RandomCharacter.letterToAlphabetPos(character) + seedCharacterAsInt, shuffledCharacters)
            outputText += outChar.toString()
            index++
        }

        val md5 = Md5Coder.calcMd5(config.baseSeed).replace("[^1-9]".toRegex(), "")
        val md5Digits = md5.toSortedSet().sorted()
        for (md5Digit in md5Digits) {
            val randomCharacter = RandomCharacter.getCharacterAtIndex(md5Digit.digitToInt(), config.characterSetConfig)
            outputText = outputText.addCharAtIndex(randomCharacter, md5Digit.digitToInt())
        }

        val noise = config.noiseGenerator!!.getNoise(12.443, 6.347, 6, 9.432)
        val noiseShifted = (noise.absoluteValue * 100000).toInt().toString().toSortedSet().sorted()
        for (noiseDigit in noiseShifted) {
            val randomCharacter = RandomCharacter.getCharacterAtIndex(noiseDigit.digitToInt(), config.characterSetConfig)
            outputText = outputText.addCharAtIndex(randomCharacter, noiseDigit.digitToInt())
        }

        return outputText
    }

    fun decode(input: String, config: CustomCoderConfig) : String {
        var outputText = ""

        val computedSeed = if (config.baseSeed.isEmpty()) {
            config.baseSeed = CustomCoderType.V2.getDefaultSeed().toString()
            RandomCharacter.getMd5Seed(CustomCoderType.V2.getDefaultSeed().toString())
        } else {
            RandomCharacter.getMd5Seed(config.baseSeed)
        }

        var inputText = input

        val noise = config.noiseGenerator!!.getNoise(12.443, 6.347, 6, 9.432)
        val noiseShifted = (noise.absoluteValue * 100000).toInt().toString().toSortedSet().sortedDescending()
        for (noiseDigit in noiseShifted) {
            inputText = inputText.removeCharAtIndex(noiseDigit.digitToInt())
        }

        val md5 = Md5Coder.calcMd5(config.baseSeed).replace("[^1-9]".toRegex(), "")
        val md5Digits = md5.toSortedSet().sortedDescending()
        for (md5Digit in md5Digits) {
            inputText = inputText.removeCharAtIndex(md5Digit.digitToInt())
        }

        val shuffledCharacters = RandomCharacter.getCharacterListShuffled(computedSeed, config.characterSetConfig)
        var index = 0
        for (character in inputText) {
            if (config.preserveWhiteSpaces && character.isWhitespace())
            {
                outputText += " "
                index++
                continue
            }

            val seedCharacterAsInt = RandomCharacter.getCharacterFromSeed(index, computedSeed)
            val characterIndex = RandomCharacter.getIndexFromChar(character, shuffledCharacters) - seedCharacterAsInt
            val decodedChar = RandomCharacter.alphabetPosToLetter(characterIndex)

            if (shuffledCharacters.contains(decodedChar)) {
                outputText += decodedChar.toString()
            }

            index++
        }

        return outputText
    }
}