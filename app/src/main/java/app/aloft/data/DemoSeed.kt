package app.aloft.data

import app.aloft.AppContainer
import app.aloft.domain.Element
import app.aloft.domain.Sports
import app.aloft.domain.Wx
import app.aloft.ru
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

/** The demo build's island: eight weeks of training, a voyage under way, a few guests met. */
object DemoSeed {
    suspend fun seedIfEmpty(c: AppContainer) {
        val dao = c.db.dao()
        if (dao.allWorkouts().isNotEmpty()) return
        val zone = ZoneId.systemDefault()
        val today = LocalDate.now()
        val start = today.minusWeeks(8)
        c.settings.update { it.copy(onboarded = true, season = 0, seasonStart = start.atStartOfDay(zone).toInstant().toEpochMilli()) }
        val ru = c.app.ru
        val own = c.island.addOwn(if (ru) "Скалодром" else "Climbing gym", Element.EARTH)
        fun sp(k: String) = Sports.builtIn.first { it.key == k }
        // A week's plan: day of week (1 = Monday) to sport, minutes, effort, finishing hour.
        val plan = listOf(
            Triple(1, sp("run"), Triple(40, 6, 8)), Triple(2, sp("gym"), Triple(50, 7, 19)), Triple(3, sp("yoga"), Triple(30, 3, 21)),
            Triple(4, sp("run"), Triple(35, 5, 19)), Triple(5, sp("swim"), Triple(45, 5, 18)), Triple(6, own, Triple(90, 7, 14)),
            Triple(7, sp("walk"), Triple(70, 3, 13)),
        )
        var d = start
        var n = 0
        while (!d.isAfter(today)) {
            val dow = d.dayOfWeek.value
            val week = ((d.toEpochDay() - start.toEpochDay()) / 7).toInt()
            for ((day, s, p) in plan) {
                if (day != dow) continue
                // Not every week is perfect: some days skipped, swimming every other week, boxing appears late.
                if ((n++ * 7 + week) % 5 == 4) continue
                if (s.key == "swim" && week % 2 == 1) continue
                val (min, rpe, hour) = p
                val at = d.atTime(LocalTime.of(hour, 0)).atZone(zone).toInstant().toEpochMilli()
                if (at > System.currentTimeMillis()) continue
                val wx = when ((week * 3 + day) % 9) { 0 -> Wx.RAIN; 4 -> Wx.FOG; else -> Wx.CLOUD }
                c.island.log(s, s.name.of(ru), min, rpe, wx, null, at)
            }
            if (dow == 7 && week % 2 == 0) dao.upsert(Calm(d.toEpochDay(), 0))
            if (dow == 3 && week >= 5) {
                val at = d.atTime(LocalTime.of(20, 0)).atZone(zone).toInstant().toEpochMilli()
                if (at < System.currentTimeMillis()) c.island.log(sp("box"), sp("box").name.of(ru), 60, 8, Wx.CLEAR, null, at)
            }
            d = d.plusDays(1)
        }
        for (a in dao.allArrivals()) dao.update(a.copy(read = true))
        for (t in dao.allThanks()) dao.update(t.copy(read = true))
        for (m in dao.allMeetingsList()) dao.upsert(m.copy(read = true))
    }
}
