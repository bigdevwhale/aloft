package app.aloft

import app.aloft.data.Anchor
import app.aloft.data.Workout
import app.aloft.domain.Element
import app.aloft.domain.Guest
import app.aloft.domain.Issue
import app.aloft.domain.Journey
import app.aloft.domain.Load
import app.aloft.domain.Mode
import app.aloft.domain.Requests
import app.aloft.domain.Stage
import app.aloft.domain.Wx
import app.aloft.ui.Talk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneOffset

class LoadTest {
    private val day = Load.DAY
    private val now = 100 * day

    private fun w(daysAgo: Double, e: Element, min: Int = 45, rpe: Int = 6) =
        Workout(at = now - (daysAgo * day).toLong(), sport = "x", name = "x", element = e.name, minutes = min, rpe = rpe)

    /** Three trainings a week of [e] for four weeks. */
    private fun steady(e: Element) = (0 until 12).map { w(it * 7.0 / 3 + .5, e) }

    @Test fun steadyTrainingMakesASpirit() {
        val lv = Load.levels(steady(Element.AIR), emptyList(), now)
        assertEquals(Stage.SPIRIT, Stage.of(lv.getValue(Element.AIR)))
        assertEquals(0.0, lv.getValue(Element.EARTH), 1e-9)
    }

    @Test fun oldTrainingFades() {
        val fresh = Load.levels(listOf(w(1.0, Element.FIRE)), emptyList(), now).getValue(Element.FIRE)
        val old = Load.levels(listOf(w(30.0, Element.FIRE)), emptyList(), now).getValue(Element.FIRE)
        assertTrue(old < fresh / 4)
    }

    @Test fun anchoredDaysDontFade() {
        val ws = listOf(w(30.0, Element.WOOD))
        val anchor = Anchor(from = now - 29 * day, to = now - day)
        val withAnchor = Load.levels(ws, listOf(anchor), now).getValue(Element.WOOD)
        val fresh = Load.levels(listOf(w(2.0, Element.WOOD)), emptyList(), now).getValue(Element.WOOD)
        assertEquals(fresh, withAnchor, 1e-6)
    }

    @Test fun bigSessionBringsAStorm() {
        val isle = Load.isle(steady(Element.AIR) + w(.2, Element.FIRE, 90, 9), emptyList(), false, now)
        assertEquals(Mode.STORM, isle.mode)
        assertEquals(Mode.CALM, Load.isle(steady(Element.AIR) + w(.2, Element.FIRE, 90, 9), emptyList(), true, now).mode)
    }

    @Test fun onlyRunningMakesGusts() {
        val isle = Load.isle(steady(Element.AIR), emptyList(), false, now)
        assertTrue(Issue.GUSTS in isle.issues)
        assertTrue(Issue.LOW in isle.issues)
    }

    @Test fun requestsAreStableAndCounted() {
        val monday = LocalDate.of(2026, 10, 26)
        val lv = Element.entries.associateWith { .3 }.toMutableMap().apply { put(Element.WATER, .05) }
        val reqs = Requests.of(monday, lv)
        assertEquals(reqs.map { it.id }, Requests.of(monday, lv).map { it.id })
        assertEquals(Element.WATER, reqs[0].who.element)
        val start = monday.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        val swim = Workout(at = start + day / 2, sport = "swim", name = "", element = "WATER", minutes = 60, rpe = 5)
        val got = Requests.progress(reqs, listOf(swim), 0, ZoneOffset.UTC)
        assertTrue(got[0].done)
    }

    @Test fun guestsByWeather() {
        assertEquals(Guest.WANDERER, Guest.pick(true, 5, Wx.RAIN, null, 12))
        assertEquals(Guest.DRIZZLE, Guest.pick(false, 5, Wx.RAIN, 10.0, 12))
        assertEquals(Guest.FROST, Guest.pick(false, 5, Wx.SNOW, -15.0, 12))
        assertEquals(Guest.ROBIN, Guest.pick(false, 5, Wx.CLEAR, 5.0, 6))
        assertEquals(null, Guest.pick(false, 5, Wx.CLOUD, 12.0, 14))
    }

    @Test fun voyage() {
        assertEquals(0 to Journey.route(0)[1], Journey.position(0, 100))
        assertEquals(7, Journey.position(0, 5000).first)
        assertEquals("21 верста", Talk.versts(21, true))
        assertEquals("12 вёрст", Talk.versts(12, true))
        assertEquals("103 версты", Talk.versts(103, true))
    }
}
