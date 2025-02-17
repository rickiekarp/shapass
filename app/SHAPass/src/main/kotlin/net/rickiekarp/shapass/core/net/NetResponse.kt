package net.rickiekarp.core.net

import okhttp3.Response
import org.json.JSONException
import org.json.JSONObject
import java.io.*

class NetResponse {

    companion object {
        fun getResponseString(inputStream: InputStream?): String {
            val response = StringBuilder()

            val `in`: BufferedReader
            try {
                `in` = BufferedReader(InputStreamReader(inputStream, "UTF-8"))
            } catch (e: UnsupportedEncodingException) {
                e.printStackTrace()
                return response.toString()
            } catch (e: NullPointerException) {
                return response.toString()
            }

            try {
                while (true) {
                    val line = `in`.readLine() ?: break
                    response.append(line)
                }
                `in`.close()
            } catch (e: IOException) {
                e.printStackTrace()
                return response.toString()
            }

            return response.toString()
        }

        @Throws(IOException::class)
        fun getResponseString(r: Response): String {
            val inputStream = r.body!!.byteStream()
            return getResponseString(inputStream)
        }

        private fun getResponseByteArray(inputStream: InputStream?): ByteArray {
            val bos = ByteArrayOutputStream()
            val buffer = ByteArray(4096)
            try {
                val len = inputStream!!.read(buffer)
                while (-1 != (len)) {
                    bos.write(buffer, 0, len)
                }
                inputStream.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }

            return bos.toByteArray()
        }

        fun getResponseJson(inputStream: InputStream?): JSONObject? {
            val responseString = getResponseString(inputStream)
            return try {
                JSONObject(responseString)
            } catch (e: JSONException) {
                null
            }
        }

        fun getResponseResultAsBoolean(inputStream: InputStream?, key: String): Boolean {
            return getResponseJson(inputStream)!!.getBoolean(key)
        }
    }
}