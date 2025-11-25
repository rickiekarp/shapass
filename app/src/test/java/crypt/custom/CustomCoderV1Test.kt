package crypt.custom

import net.rickiekarp.core.enums.AlphabetType
import net.rickiekarp.core.enums.CustomCoderType
import net.rickiekarp.core.model.CustomCoderConfig
import net.rickiekarp.shapass.core.util.crypt.custom.CustomCoderV1
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class CustomCoderV1Test {

    @Test
    fun testEncode() {
        val expected = "ШΞoЛЛ"
        val coderConfig = CustomCoderConfig(
            CustomCoderType.V1,
            "",
            mutableMapOf(
                AlphabetType.CYRILLIC to true,
                AlphabetType.LATIN to true,
                AlphabetType.GREEK to true,
            ),
            false
        )

        val actual = CustomCoderV1.encode("input", coderConfig)
        assertEquals(expected, actual)
    }

    @Test
    fun testDecode() {
        val expected = "INPUT"
        val coderConfig = CustomCoderConfig(
            CustomCoderType.V1,
            "",
            mutableMapOf(
                AlphabetType.CYRILLIC to true,
                AlphabetType.LATIN to true,
                AlphabetType.GREEK to true,
            ),
            false
        )

        val actual = CustomCoderV1.decode("ШΞoЛЛ", coderConfig)
        assertEquals(expected, actual)
    }
}
