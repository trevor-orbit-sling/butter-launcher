package dev.plain.launcher

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import kotlin.math.roundToInt

data class WeatherQuery(val lat: String, val lon: String, val unit: TempUnit)

data class WeatherNow(
    val query: WeatherQuery,
    val temp: Int,
    val high: Int,
    val low: Int,
    val condition: String,
    val glyph: WeatherGlyph,
    val fetchedAt: Long = System.currentTimeMillis(),
) {
    fun isFreshFor(other: WeatherQuery) = query == other && System.currentTimeMillis() - fetchedAt < 20 * 60_000
}

data class Place(val name: String, val detail: String, val lat: String, val lon: String)

// Open-Meteo forecast and geocoding are used only after choosing a location.
object Weather {
    suspend fun fetch(q: WeatherQuery): WeatherNow? = withContext(Dispatchers.IO) {
        val unit = if (q.unit == TempUnit.Fahrenheit) "&temperature_unit=fahrenheit" else ""
        val json = getJson(
            "https://api.open-meteo.com/v1/forecast?latitude=${q.lat}&longitude=${q.lon}" +
                "&current=temperature_2m,weather_code,is_day&daily=temperature_2m_max,temperature_2m_min&timezone=auto&forecast_days=1$unit",
        ) ?: return@withContext null
        val now = json.getJSONObject("current")
        val today = json.getJSONObject("daily")
        val (condition, glyph) = describe(now.getInt("weather_code"), now.optInt("is_day", 1) == 1)
        WeatherNow(
            query = q,
            temp = now.getDouble("temperature_2m").roundToInt(),
            high = today.getJSONArray("temperature_2m_max").getDouble(0).roundToInt(),
            low = today.getJSONArray("temperature_2m_min").getDouble(0).roundToInt(),
            condition = condition,
            glyph = glyph,
        )
    }

    suspend fun search(name: String): List<Place> = withContext(Dispatchers.IO) {
        val json = getJson("https://geocoding-api.open-meteo.com/v1/search?count=8&language=en&name=" + URLEncoder.encode(name.trim(), "UTF-8"))
        val results = json?.optJSONArray("results") ?: return@withContext emptyList()
        (0 until results.length()).map { i ->
            val r = results.getJSONObject(i)
            Place(r.getString("name"), join(r.optString("admin1"), r.optString("country")), r.getDouble("latitude").toString(), r.getDouble("longitude").toString())
        }
    }

    // WMO weather codes.
    private fun describe(code: Int, day: Boolean) = when (code) {
        0, 1 -> (if (code == 0) "Clear" else "Mostly clear") to (if (day) WeatherGlyph.Sun else WeatherGlyph.Moon)
        2 -> "Partly cloudy" to (if (day) WeatherGlyph.PartlyDay else WeatherGlyph.PartlyNight)
        3 -> "Overcast" to WeatherGlyph.Cloud
        45, 48 -> "Fog" to WeatherGlyph.Fog
        in 51..57 -> "Drizzle" to WeatherGlyph.Rain
        in 61..67 -> "Rain" to WeatherGlyph.Rain
        in 71..77 -> "Snow" to WeatherGlyph.Snow
        in 80..82 -> "Showers" to WeatherGlyph.Rain
        85, 86 -> "Snow showers" to WeatherGlyph.Snow
        in 95..99 -> "Thunderstorm" to WeatherGlyph.Storm
        else -> "—" to WeatherGlyph.Cloud
    }

    private fun join(vararg parts: String) = parts.filter { it.isNotBlank() }.joinToString(", ")

    private fun getJson(url: String): JSONObject? {
        val conn = URL(url).openConnection() as HttpURLConnection
        conn.connectTimeout = 5000
        conn.readTimeout = 10000
        return try {
            if (conn.responseCode == 200) JSONObject(conn.inputStream.bufferedReader().readText()) else null
        } catch (e: Exception) {
            null
        } finally {
            conn.disconnect()
        }
    }
}
