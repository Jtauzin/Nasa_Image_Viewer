package com.bronzeswordstudios.nasaimageviewer

import android.util.Log
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.net.URL
import java.nio.charset.Charset
import javax.net.ssl.HttpsURLConnection

class Query {

    companion object {
        private fun makeHTTPSRequest(url: URL): String? {

            // set up query variables
            var jsonResponse: String? = null
            var urlConnection: HttpsURLConnection? = null
            var inputStream: InputStream? = null

            // attempt connection
            try {
                urlConnection = url.openConnection() as HttpsURLConnection
                urlConnection.readTimeout = 10000
                urlConnection.connectTimeout = 15000
                urlConnection.requestMethod = "GET"
                urlConnection.connect()

                // if the connection works we get a 200 response
                // then read the stream and convert it to our response
                if (urlConnection.responseCode == 200) {
                    inputStream = urlConnection.inputStream
                    jsonResponse = readFromStream(inputStream)
                } else {
                    Log.e("Error: ", "makeHTTPSRequest: " + urlConnection.responseCode + " code.")
                }

                // check if failed to connect
            } catch (e: Exception) {
                Log.e("Error: ", "makeHTTPSRequest: $e")
            }

            // close connection when we are finished
            finally {
                urlConnection?.disconnect()
                inputStream?.close()
            }
            return jsonResponse
        }

        // here we translate the response stream into a string
        @Throws(IOException::class)
        private fun readFromStream(input: InputStream?): String {
            val stringBuilder = StringBuilder()
            val inputStreamReader = InputStreamReader(input, Charset.forName("UTF-8"))
            val bufferedReader = BufferedReader(inputStreamReader)
            var line = bufferedReader.readLine()
            while (line != null) {
                stringBuilder.append(line)
                line = bufferedReader.readLine()
            }
            return stringBuilder.toString()
        }

        private fun extractFromJson(inputJson: String?): ArrayList<ImageObj> {
            val imageObjs: ArrayList<ImageObj> = ArrayList()
            try {
                val jsonResponse = JSONObject(inputJson)
                val nasaJsonObj: JSONObject = jsonResponse.getJSONObject("collection")
                val nasaJsonArray: JSONArray = nasaJsonObj.getJSONArray("items")
                var i = 0
                while (i < nasaJsonArray.length()) {
                    val currentObj: JSONObject = nasaJsonArray.getJSONObject(i)
                    val dataObj: JSONObject = currentObj.getJSONArray("data").getJSONObject(0)
                    val linkObj: JSONObject = currentObj.getJSONArray("links").getJSONObject(0)

                    // if we wanted to go further down the JSON stream to get the images,
                    // we would use this value to generate another stream via HTTPS request.
                    val imageAddress: String? = currentObj.getString("href")

                    val title: String? = dataObj.getString("title")
                    val center: String? = dataObj.getString("center")
                    val date: String? = dataObj.getString("date_created")
                    var url: String? = linkObj.getString("href")

                    // only adjust the url if we have one to adjust
                    val backupURL: String? = url
                    if (url != null) {
                        url = adjustURL(url)
                    }
                    imageObjs.add(ImageObj(url, title, center, date, backupURL))
                    i++
                }
            } catch (e: JSONException) {
                Log.e("JSON Error", "extractFromJson: $e")
            }
            return imageObjs
        }

        fun collectData(urlString: String): ArrayList<ImageObj> {
            // this is the method we will call from our loader to return the JSON results
            val url = URL(urlString)
            val jsonResponse: String? = makeHTTPSRequest(url)
            return extractFromJson(jsonResponse)
        }

        private fun adjustURL(inputString: String): String {
            /* this was a design choice given the simplicity of this app. Another option
             * was to generate the higher quality image (down the rabbit hole) by performing an
             * additional HTTP request from this JSON stream's data that led to another JSON
             * stream where one could find the other URL. After reviewing the API's format, this
             * seemed to work just as good, although not all images end up being JPGs, or having
             * the larger format, we have the original low res value to fall back on. That takes
             * place in the image adapter. */
            val urlSplit: List<String> = inputString.split("thumb")
            return urlSplit[0] + "large.jpg"
        }

    }


}