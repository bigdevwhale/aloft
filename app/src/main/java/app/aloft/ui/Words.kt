package app.aloft.ui

import android.content.Context
import app.aloft.R
import app.aloft.domain.Stage
import app.aloft.domain.Wx
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle

object Words {
    fun stage(c: Context, s: Stage): String = c.getString(
        when (s) {
            Stage.SPARK -> R.string.st_spark
            Stage.EMBER -> R.string.st_ember
            Stage.SPIRIT -> R.string.st_spirit
            Stage.GREAT -> R.string.st_great
        },
    )

    fun wx(c: Context, w: Wx): String = c.getString(
        when (w) {
            Wx.CLEAR -> R.string.wx_clear
            Wx.CLOUD -> R.string.wx_cloud
            Wx.RAIN -> R.string.wx_rain
            Wx.SNOW -> R.string.wx_snow
            Wx.FOG -> R.string.wx_fog
            Wx.STORM -> R.string.wx_storm
        },
    )

    /** How hard a training felt, in words, for a value 1–10. */
    fun rpe(c: Context, n: Int): String = c.getString(
        when {
            n <= 3 -> R.string.rpe_easy
            n <= 6 -> R.string.rpe_mid
            n <= 8 -> R.string.rpe_hard
            else -> R.string.rpe_max
        },
    )

    fun minutes(c: Context, m: Int): String = when {
        m < 60 -> c.getString(R.string.min_n, m)
        m % 60 == 0 -> c.getString(R.string.hours_only, m / 60)
        else -> c.getString(R.string.hours_n, m / 60, m % 60)
    }

    fun hours(m: Int): String = String.format(java.util.Locale.US, "%.1f", m / 60.0).replace('.', ',')

    fun day(c: Context, d: LocalDate, today: LocalDate): String {
        val l = c.resources.configuration.locales[0]
        return when (d) {
            today -> c.getString(R.string.today)
            today.minusDays(1) -> c.getString(R.string.yesterday)
            else -> d.format(DateTimeFormatter.ofPattern(if (d.year == today.year) "d MMMM" else "d MMMM yyyy", l))
        }
    }

    fun time(ms: Long): String = Instant.ofEpochMilli(ms).atZone(ZoneId.systemDefault()).toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm"))

    fun weekday(c: Context, d: LocalDate): String = d.dayOfWeek.getDisplayName(TextStyle.SHORT, c.resources.configuration.locales[0]).lowercase()
}
