package app.aloft.domain

import app.aloft.data.Anchor
import app.aloft.data.Workout
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.min

/** What the sky over the island is doing. */
enum class Mode { DAY, STORM, CALM, ANCHOR }

/** What an imbalance between the elements does to the island. */
enum class Issue { DROUGHT, SLIDE, GUSTS, HEAVY, CRACK, LOW }

/** What grows on the island when two elements are both strong. */
enum class Building(val a: Element, val b: Element?) {
    MILL(Element.AIR, Element.EARTH),
    FORGE(Element.FIRE, Element.EARTH),
    SPRING(Element.WATER, Element.FIRE),
    GARDEN(Element.WATER, Element.WOOD),
    GROVE(Element.WOOD, Element.EARTH),
    RAINBOW(Element.AIR, Element.WATER),
    TOWER(Element.AIR, null),
}

/** A spirit's stage by its element's level. */
enum class Stage { SPARK, EMBER, SPIRIT, GREAT;

    companion object {
        fun of(v: Double) = when {
            v < .12 -> SPARK
            v < .4 -> EMBER
            v < .75 -> SPIRIT
            else -> GREAT
        }
    }
}

/** The island at one moment. */
class Isle(
    val level: Map<Element, Double>,
    val mode: Mode,
    val issues: Set<Issue>,
    val buildings: Set<Building>,
    /** 0 deep in the clouds … 1 as high as it goes. */
    val height: Double,
    /** Training load of the last seven days and the weekly average of the last four weeks. */
    val acute: Double,
    val chronic: Double,
) {
    fun of(e: Element) = level[e] ?: 0.0
    val mean get() = level.values.average()
    val metres get() = (height * 3000 / 10).toInt() * 10
}

object Load {
    const val DAY = 86_400_000L

    /** How fast a training fades from an element: e-folding time in days. */
    const val TAU = 14.0

    /** The chronic load at which an element is half way up. */
    const val HALF = 800.0

    /** Session load: minutes × how hard it felt (1–10), the sRPE way. */
    fun of(w: Workout): Double = w.minutes.toDouble() * w.rpe

    /** Days between two moments, not counting the days spent at anchor. */
    fun days(from: Long, to: Long, anchors: List<Anchor>): Double {
        var ms = (to - from).toDouble()
        for (a in anchors) {
            val s = max(a.from, from)
            val e = min(a.to ?: to, to)
            if (e > s) ms -= (e - s)
        }
        return max(0.0, ms / DAY)
    }

    fun levels(ws: List<Workout>, anchors: List<Anchor>, now: Long): Map<Element, Double> {
        val sum = Element.entries.associateWith { 0.0 }.toMutableMap()
        for (w in ws) {
            if (w.at > now) continue
            val age = days(w.at, now, anchors)
            if (age > 70) continue
            val e = Element.of(w.element)
            sum[e] = sum.getValue(e) + of(w) * exp(-age / TAU)
        }
        return sum.mapValues { (_, c) -> c / (c + HALF) }
    }

    /** Load inside the last [d] days (anchored days don't count). */
    private fun within(ws: List<Workout>, anchors: List<Anchor>, now: Long, d: Double) =
        ws.filter { it.at <= now && days(it.at, now, anchors) <= d }.sumOf { of(it) }

    fun isle(ws: List<Workout>, anchors: List<Anchor>, calmToday: Boolean, now: Long): Isle {
        val lv = levels(ws, anchors, now)
        val acute = within(ws, anchors, now, 7.0)
        val chronic = within(ws, anchors, now, 28.0) / 4
        val anchored = anchors.any { it.to == null || it.to > now }
        val bigSession = ws.any { now - it.at in 0..30 * 3_600_000L && of(it) >= 720 }
        val spike = acute >= 1000 && acute > 1.5 * max(chronic, 450.0)
        val mode = when {
            anchored -> Mode.ANCHOR
            calmToday -> Mode.CALM
            bigSession || spike -> Mode.STORM
            else -> Mode.DAY
        }
        val e = { k: Element -> lv.getValue(k) }
        val mean = lv.values.average()
        val issues = mutableSetOf<Issue>()
        if (e(Element.FIRE) - e(Element.WATER) > .3) issues += Issue.DROUGHT
        if (e(Element.WATER) - e(Element.EARTH) > .3) issues += Issue.SLIDE
        if (e(Element.AIR) - e(Element.EARTH) > .35) issues += Issue.GUSTS
        if (e(Element.EARTH) - e(Element.AIR) > .35) issues += Issue.HEAVY
        if (e(Element.WOOD) < .2 && mean > .2) issues += Issue.CRACK
        val h = (mean * 1.2 - (if (Issue.HEAVY in issues) .14 else 0.0) - (if (Issue.CRACK in issues) .05 else 0.0)).coerceIn(0.0, 1.0)
        if (h < .3) issues += Issue.LOW
        val b = mutableSetOf<Building>()
        for (k in Building.entries) {
            val o = k.b ?: continue
            val need = if (k == Building.RAINBOW) .6 else .45
            if (e(k.a) >= need && e(o) >= need) b += k
        }
        if (lv.values.count { it >= .5 } >= 4) b += Building.TOWER
        return Isle(lv, mode, issues, b, h, acute, chronic)
    }
}
