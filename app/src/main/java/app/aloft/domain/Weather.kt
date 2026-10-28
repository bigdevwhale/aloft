package app.aloft.domain

import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.Locale

/** A town the weather comes from. */
data class Place(val name: String, val region: String, val lat: Double, val lon: Double)

/** The weather outside, as much of it as the island cares about. */
enum class Wx { CLEAR, CLOUD, RAIN, SNOW, FOG, STORM;

    companion object {
        fun of(s: String?): Wx? = entries.firstOrNull { it.name == s }

        /** WMO weather codes, as Open-Meteo gives them. */
        fun wmo(code: Int): Wx = when (code) {
            0, 1 -> CLEAR
            2, 3 -> CLOUD
            45, 48 -> FOG
            in 51..67, in 80..82 -> RAIN
            in 71..77, 85, 86 -> SNOW
            in 95..99 -> STORM
            else -> CLOUD
        }

        /** MET Norway symbol codes like "lightrainshowers_day". */
        fun met(symbol: String): Wx = when {
            "thunder" in symbol -> STORM
            "snow" in symbol || "sleet" in symbol -> SNOW
            "rain" in symbol -> RAIN
            "fog" in symbol -> FOG
            "cloudy" in symbol -> CLOUD
            else -> CLEAR
        }
    }
}

data class Now(val wx: Wx, val temp: Double)

object Weather {
    private fun get(url: String): String {
        val c = URL(url).openConnection() as HttpURLConnection
        c.connectTimeout = 6000
        c.readTimeout = 9000
        c.setRequestProperty("User-Agent", "Aloft/1.0 github.com/bigdevwhale/aloft")
        try {
            if (c.responseCode !in 200..299) error("HTTP ${c.responseCode}")
            return c.inputStream.bufferedReader().use { it.readText() }
        } finally {
            c.disconnect()
        }
    }

    /**
     * The weather right now: from Open-Meteo, or from MET Norway when Open-Meteo can't be reached
     * (some networks block it). Nothing but the coordinates is sent.
     */
    fun now(lat: Double, lon: Double): Now = runCatching { openMeteo(lat, lon) }.getOrElse { metNo(lat, lon) }

    private fun openMeteo(lat: Double, lon: Double): Now {
        val j = JSONObject(get(String.format(Locale.US, "https://api.open-meteo.com/v1/forecast?latitude=%.3f&longitude=%.3f&current=temperature_2m,weather_code&timezone=auto", lat, lon)))
        val c = j.getJSONObject("current")
        return Now(Wx.wmo(c.getInt("weather_code")), c.getDouble("temperature_2m"))
    }

    private fun metNo(lat: Double, lon: Double): Now {
        val j = JSONObject(get(String.format(Locale.US, "https://api.met.no/weatherapi/locationforecast/2.0/compact?lat=%.3f&lon=%.3f", lat, lon)))
        val d = j.getJSONObject("properties").getJSONArray("timeseries").getJSONObject(0).getJSONObject("data")
        val temp = d.getJSONObject("instant").getJSONObject("details").optDouble("air_temperature", 10.0)
        val sym = d.optJSONObject("next_1_hours")?.optJSONObject("summary")?.optString("symbol_code") ?: ""
        return Now(Wx.met(sym), temp)
    }

    /** Towns matching a name, for the settings search. */
    fun search(name: String, lang: String): List<Place> {
        val q = URLEncoder.encode(name.trim(), "UTF-8")
        val j = JSONObject(get("https://geocoding-api.open-meteo.com/v1/search?name=$q&count=8&language=$lang&format=json"))
        val r = j.optJSONArray("results") ?: return emptyList()
        return (0 until r.length()).map { i ->
            val o = r.getJSONObject(i)
            Place(o.getString("name"), listOf(o.optString("admin1"), o.optString("country")).filter { it.isNotBlank() }.joinToString(", "),
                o.getDouble("latitude"), o.getDouble("longitude"))
        }
    }
}
