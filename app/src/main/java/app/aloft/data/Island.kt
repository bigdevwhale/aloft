package app.aloft.data

import app.aloft.domain.Element
import app.aloft.domain.Guest
import app.aloft.domain.Journey
import app.aloft.domain.Load
import app.aloft.domain.Requests
import app.aloft.domain.Sport
import app.aloft.domain.Tx
import app.aloft.domain.Wx
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/** Everything on the island at one moment, straight from the database. */
class IslandState(
    val workouts: List<Workout>,
    val calms: List<Calm>,
    val own: List<OwnSport>,
    val anchors: List<Anchor>,
    val arrivals: List<Arrival>,
    val meetings: List<Meeting>,
    val thanks: List<Thanks>,
) {
    val ownSports: List<Sport> = own.map { Sport(it.key, Tx(it.name, it.name), Element.of(it.element), own = true) }
    val anchor: Anchor? = anchors.lastOrNull { it.to == null }
}

private data class Part(val workouts: List<Workout>, val calms: List<Calm>, val own: List<OwnSport>, val anchors: List<Anchor>)

class Island(private val dao: IslandDao, private val settings: SettingsStore) {
    val state: Flow<IslandState> = combine(
        combine(dao.workouts(), dao.calms(), dao.own(), dao.anchors(), ::Part),
        dao.arrivals(), dao.meetings(), dao.thanks(),
    ) { a, arrivals, meetings, thanks -> IslandState(a.workouts, a.calms, a.own, a.anchors, arrivals, meetings, thanks) }

    private val zone get() = ZoneId.systemDefault()

    /**
     * Logs a training and everything it sets off: a guest from the weather, the island reaching
     * stops on its voyage, islanders' requests coming true. The dialogs for these are the unread rows.
     */
    suspend fun log(sport: Sport, name: String, minutes: Int, rpe: Int, wx: Wx?, temp: Double?, at: Long = System.currentTimeMillis()) {
        val before = dao.allWorkouts()
        val first = before.none { it.sport == sport.key }
        val start = Instant.ofEpochMilli(at - minutes * 60_000L).atZone(zone).hour
        val guest = Guest.pick(first, rpe, wx, temp, start)
        dao.insert(Workout(at = at, sport = sport.key, name = name, element = sport.element.name, minutes = minutes, rpe = rpe, wx = wx?.name ?: "", temp = temp, guest = guest?.name))
        if (guest != null) {
            val m = dao.meeting(guest.name)
            dao.upsert(m?.copy(count = m.count + 1) ?: Meeting(guest.name, at))
        }
        settle(at)
    }

    /** Records the stops reached and the requests done, after any change to the log. */
    suspend fun settle(now: Long = System.currentTimeMillis()) {
        val ws = dao.allWorkouts()
        val s = settings.now()
        val arrived = dao.allArrivals()
        val dist = Journey.distance(ws, s.seasonStart)
        val route = Journey.route(s.season)
        for ((i, stop) in route.withIndex()) {
            if (i == 0 || stop.at > dist) continue
            if (arrived.none { it.season == s.season && it.stop == i }) dao.insert(Arrival(season = s.season, stop = i, at = now))
        }
        if (dist >= route.last().at) settings.update { it.copy(season = it.season + 1, seasonStart = now + 1) }

        val today = Instant.ofEpochMilli(now).atZone(zone).toLocalDate()
        val monday = Requests.monday(today)
        val weekStart = monday.atStartOfDay(zone).toInstant().toEpochMilli()
        val anchors = dao.allAnchors()
        val reqs = Requests.of(monday, Load.levels(ws, anchors, weekStart))
        val week = ws.filter { it.at >= weekStart }
        val calmDays = dao.allCalms().count { it.day >= monday.toEpochDay() }
        val got = dao.allThanks().filter { it.week == monday.toEpochDay() }.map { it.req }.toSet()
        for (a in Requests.progress(reqs, week, calmDays, zone)) if (a.done && a.req.id !in got) dao.insert(Thanks(week = monday.toEpochDay(), req = a.req.id, at = now))
    }

    suspend fun update(w: Workout) = dao.update(w)

    suspend fun delete(w: Workout) {
        dao.deleteWorkout(w.id)
        val m = w.guest?.let { dao.meeting(it) }
        if (m != null && dao.allWorkouts().none { it.guest == m.guest }) dao.upsert(m.copy(count = 0)) else if (m != null) dao.upsert(m.copy(count = (m.count - 1).coerceAtLeast(1)))
    }

    /** Today as a rest day, or not. */
    suspend fun calm(day: LocalDate, on: Boolean) {
        if (on) dao.upsert(Calm(day.toEpochDay(), System.currentTimeMillis())) else dao.deleteCalm(day.toEpochDay())
        settle()
    }

    suspend fun anchor(on: Boolean) {
        val now = System.currentTimeMillis()
        val open = dao.allAnchors().lastOrNull { it.to == null }
        if (on && open == null) dao.insert(Anchor(from = now))
        if (!on && open != null) dao.update(open.copy(to = now))
    }

    suspend fun addOwn(name: String, e: Element): Sport {
        val id = dao.insert(OwnSport(name = name, element = e.name))
        return Sport("own_$id", Tx(name, name), e, own = true)
    }

    suspend fun hideOwn(o: OwnSport) = dao.update(o.copy(hidden = true))

    suspend fun read(a: Arrival) = dao.update(a.copy(read = true))
    suspend fun read(m: Meeting) = dao.upsert(m.copy(read = true))
    suspend fun read(t: Thanks) = dao.update(t.copy(read = true))

    companion object {
        fun day(ms: Long, zone: ZoneId = ZoneId.systemDefault()): LocalDate = Instant.ofEpochMilli(ms).atZone(zone).toLocalDate()
    }
}
