package uk.airbyte.skrrt

import android.content.Context
import org.json.JSONException
import org.json.JSONObject


class Rapper(
    var objectId: String? = null,
    var name: String? = null,
    var birthplace: String? = null,
    var imageUrl: String? = null,
    var detailUrl: String? = null,
    var status: String? = null,
    var rating: Long? = 0
) {

    companion object {

        fun create(): Rapper = Rapper()

        fun getRappersFromFile(filename: String, context: Context): ArrayList<Rapper> {
            val rapperList = ArrayList<Rapper>()

            try {
                // Load data
                val jsonString = loadJsonFromAsset("rappers.json", context)
                val json = JSONObject(jsonString)
                val rappers = json.getJSONArray("rappers")

                // Get Rapper objects from data
                (0 until rappers.length()).mapTo(rapperList) {
                    Rapper(
                        rappers.getJSONObject(it).getString("name"),
                        rappers.getJSONObject(it).getString("birthplace"),
                        rappers.getJSONObject(it).getString("image"),
                        rappers.getJSONObject(it).getString("url"),
                        rappers.getJSONObject(it).getString("status")
                    )
                }
            } catch (e: JSONException) {
                e.printStackTrace()
            }

            return rapperList
        }

        private fun loadJsonFromAsset(filename: String, context: Context): String? {
            var json: String? = null

            try {
                val inputStream = context.assets.open(filename)
                val size = inputStream.available()
                val buffer = ByteArray(size)
                inputStream.read(buffer)
                inputStream.close()
                json = String(buffer, Charsets.UTF_8)
            } catch (ex: java.io.IOException) {
                ex.printStackTrace()
                return null
            }

            return json
        }
    }
}