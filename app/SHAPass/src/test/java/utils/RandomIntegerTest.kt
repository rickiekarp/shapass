package utils

import net.rickiekarp.core.util.CommonUtil
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class RandomIntegerTest {

    @Test
    fun testNumberGeneration() {
        var actual: Int
        for (i in 1..10) {
            actual = CommonUtil.randInt(1, i)
            assertTrue(actual >= 1 && actual <= i)
        }
    }
}
