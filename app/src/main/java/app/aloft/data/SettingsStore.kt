package app.aloft.data

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import app.aloft.domain.Place
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("settings")

data class Settings(
    val onboarded: Boolean = false,
    /** The town the weather comes from; without one the weather is picked by hand when logging. */
    val place: Place? = null,
    /** The weather outside as last fetched: a Wx name, °C, when. */
    val wx: String = "",
    val wxTemp: Double? = null,
    val wxAt: Long = 0,
    /** The voyage: which season's route the island is on and when that season began. */
    val season: Int = 0,
    val seasonStart: Long = 0,
    /** A quiet Sunday-evening note that the council of the elements has met. */
    val notify: Boolean = true,
    val notifiedDay: Long = -1,
)

class SettingsStore(context: Context) {
    private val store = context.applicationContext.dataStore

    val settings: Flow<Settings> = store.data.map { it.read() }

    private fun Preferences.read(): Settings {
        val lat = this[LAT]
        val lon = this[LON]
        return Settings(
            onboarded = this[ONBOARDED] ?: false,
            place = if (lat != null && lon != null) Place(this[PLACE] ?: "", this[REGION] ?: "", lat, lon) else null,
            wx = this[WX] ?: "",
            wxTemp = this[WX_TEMP],
            wxAt = this[WX_AT] ?: 0,
            season = (this[SEASON] ?: 0L).toInt(),
            seasonStart = this[SEASON_START] ?: 0,
            notify = this[NOTIFY] ?: true,
            notifiedDay = this[NOTIFIED] ?: -1,
        )
    }

    suspend fun now(): Settings = settings.first()

    suspend fun update(change: (Settings) -> Settings) {
        store.edit {
            val s = change(it.read())
            it[ONBOARDED] = s.onboarded
            val p = s.place
            if (p != null) {
                it[PLACE] = p.name; it[REGION] = p.region; it[LAT] = p.lat; it[LON] = p.lon
            } else {
                it.remove(PLACE); it.remove(REGION); it.remove(LAT); it.remove(LON)
            }
            it[WX] = s.wx
            if (s.wxTemp != null) it[WX_TEMP] = s.wxTemp else it.remove(WX_TEMP)
            it[WX_AT] = s.wxAt
            it[SEASON] = s.season.toLong()
            it[SEASON_START] = s.seasonStart
            it[NOTIFY] = s.notify
            it[NOTIFIED] = s.notifiedDay
        }
    }

    private companion object {
        val ONBOARDED = booleanPreferencesKey("onboarded")
        val PLACE = stringPreferencesKey("place")
        val REGION = stringPreferencesKey("region")
        val LAT = doublePreferencesKey("lat")
        val LON = doublePreferencesKey("lon")
        val WX = stringPreferencesKey("wx")
        val WX_TEMP = doublePreferencesKey("wx_temp")
        val WX_AT = longPreferencesKey("wx_at")
        val SEASON = longPreferencesKey("season")
        val SEASON_START = longPreferencesKey("season_start")
        val NOTIFY = booleanPreferencesKey("notify")
        val NOTIFIED = longPreferencesKey("notified")
    }
}
