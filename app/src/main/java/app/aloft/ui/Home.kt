package app.aloft.ui

import androidx.compose.runtime.Immutable
import app.aloft.data.IslandState
import app.aloft.data.Settings
import app.aloft.data.Workout
import app.aloft.domain.Decor
import app.aloft.domain.Element
import app.aloft.domain.Guest
import app.aloft.domain.Journey
import app.aloft.domain.Load
import app.aloft.domain.Requests
import app.aloft.domain.Sport
import app.aloft.domain.Sports
import app.aloft.domain.Wx
import app.aloft.ui.art.IsleLook
import app.aloft.ui.art.TimeOfDay
import java.time.LocalDateTime
import java.time.ZoneId

/** Everything a screen reads, at one moment. */
@Immutable
class Home(val g: IslandState, val settings: Settings, val now: LocalDateTime, val ru: Boolean) {
    val zone: ZoneId = ZoneId.systemDefault()
    val nowMs: Long = now.atZone(zone).toInstant().toEpochMilli()
    val today = now.toLocalDate()
    val calmToday = g.calms.any { it.day == today.toEpochDay() }
    val isle = Load.isle(g.workouts, g.anchors, calmToday, nowMs)

    /** The sports to pick from: the built-in ones and the user's own that aren't hidden. */
    val sports: List<Sport> = Sports.builtIn + g.own.filter { !it.hidden }.map { o -> g.ownSports.first { it.key == o.key } }

    fun sport(w: Workout): Sport = Sports.byKey(w.sport, g.ownSports) ?: Sport(w.sport, app.aloft.domain.Tx(w.name, w.name), Element.of(w.element))

    /** The weather outside, if fetched in the last three hours. */
    val wx: Wx? = Wx.of(settings.wx).takeIf { nowMs - settings.wxAt < 3 * 3_600_000L }
    val temp: Double? = settings.wxTemp.takeIf { wx != null }

    val season = settings.season
    val route = Journey.route(season)
    val dist = Journey.distance(g.workouts, settings.seasonStart)
    val position = Journey.position(season, dist)
    val next = position.second

    val decor: Set<Decor> = buildSet {
        for (a in g.arrivals) Journey.route(a.season).getOrNull(a.stop)?.decor?.let { add(it) }
        for ((n, d) in Journey.thanksDecor) if (g.thanks.size >= n) add(d)
    }

    val monday = Requests.monday(today)
    private val weekStart = monday.atStartOfDay(zone).toInstant().toEpochMilli()
    val week: List<Workout> = g.workouts.filter { it.at >= weekStart }
    val weekCalms = g.calms.count { it.day >= monday.toEpochDay() }
    val requests = Requests.progress(Requests.of(monday, Load.levels(g.workouts, g.anchors, weekStart)), week, weekCalms, zone)

    val guestToday: Guest? = g.workouts.firstOrNull { it.guest != null && app.aloft.data.Island.day(it.at, zone) == today }?.let { Guest.of(it.guest) }

    val look = IsleLook(
        level = isle.level, mode = isle.mode, issues = isle.issues, buildings = isle.buildings, height = isle.height,
        time = TimeOfDay.of(now.hour), wx = wx, guest = guestToday, decor = decor,
        bunting = requests.filter { it.done }.map { (it.req.who.element?.color ?: 0xFFF0C048).toInt() },
        horizon = next?.let { ((120.0 - (it.at - dist)) / 120).coerceIn(0.0, 1.0) } ?: 0.0,
    )

    /** What is waiting to be told, oldest first: arrivals, new guests, islanders' thanks. */
    val unread: List<Any> = buildList {
        addAll(g.arrivals.filter { !it.read })
        addAll(g.meetings.filter { !it.read && it.count > 0 })
        addAll(g.thanks.filter { !it.read })
    }

    val met: Set<Guest> = g.meetings.filter { it.count > 0 }.mapNotNull { Guest.of(it.guest) }.toSet()
}
