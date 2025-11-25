package net.rickiekarp.shapass.core.util.math

import kotlin.math.ln

object MathUtil {

    // Function to calculate the log base 2 of an integer
    fun log2(n: Int, defaultValue : Int = Int.MIN_VALUE): Int {
        val result = (ln(n.toDouble()) / ln(2.0)).toInt()
        if (result == Int.MIN_VALUE && defaultValue != Int.MIN_VALUE)
        {
            return defaultValue
        }
        return result
    }

}