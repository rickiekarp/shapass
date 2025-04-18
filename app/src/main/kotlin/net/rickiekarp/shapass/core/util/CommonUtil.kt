package net.rickiekarp.core.util

import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

object CommonUtil {

    /**
     * Returns the date in a given format
     * Example: dd.MM.yy
     */
    fun getDate(format: String): String {
        val dateFormat = SimpleDateFormat(format)
        val dateToday = Calendar.getInstance().time //Get date using calendar object.
        return dateFormat.format(dateToday) //returns date string
    }

    /**
     * Returns the time in a given format
     * Example: HH-mm-ss
     */
    fun getTime(format: String): String {
        val timeFormat = SimpleDateFormat(format)
        val currentTime = Calendar.getInstance().time //Get time using calendar object.
        return timeFormat.format(currentTime) //returns time string
    }

    /**
     * Returns a pseudo-random number between min and max, inclusive.
     * The difference between min and max can be at most
     * `Integer.MAX_VALUE - 1`.
     *
     * @param min Minimum value
     * @param max Maximum value.  Must be greater than min.
     * @return Integer between min and max, inclusive.
     * @see java.util.Random.nextInt
     */
    fun randInt(min: Int, max: Int): Int {
        val rand = Random()
        return rand.nextInt(max - min + 1) + min
    }

    /**
     * Uses the ProcessBuilder to open a website using the x-www-browser command
     */
    fun openWebpage(url: String?) {
        try {
            ProcessBuilder("x-www-browser", url).start()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}
