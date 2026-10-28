package app.aloft.ui.art

import app.aloft.domain.Element
import app.aloft.domain.Stop
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.roundToInt
import kotlin.math.sin

/** The sky chart of a season's route: a parchment with islands, a dotted course and our island on it. */
class ChartScene(private val route: List<Stop>, private val dist: Int, private val season: Int) : Scene(172, 210) {
    override val key: Any = Triple(route.first().name, dist, season)

    private val pts = listOf(28 to 192, 118 to 172, 46 to 140, 132 to 112, 40 to 84, 128 to 60, 50 to 34, 132 to 18)

    override fun base(b: Buf) {
        b.vgrad(0, 0, w, h, intArrayOf(rgb(0xF4ECD8), rgb(0xEDE2C8), rgb(0xE4D6B6)))
        val ink = rgb(0xB8A888)
        for (i in 0 until 9) {
            val cx = (hash(i, season + 3) * w).toInt()
            val cy = (hash(i, season + 5) * h).toInt()
            for (k in 0..2) arc(b, cx + k * 6 - 6, cy, 4 + k % 2, ink)
        }
        for (x in 0 until w) { b.p(x, 0, rgb(0x8A7A5A)); b.p(x, h - 1, rgb(0x8A7A5A)) }
        for (y in 0 until h) { b.p(0, y, rgb(0x8A7A5A)); b.p(w - 1, y, rgb(0x8A7A5A)) }
        rose(b, 150, 188)
        for (i in 0 until pts.size - 1) {
            val (x0, y0) = pts[i]
            val (x1, y1) = pts[i + 1]
            val len = hypot((x1 - x0).toDouble(), (y1 - y0).toDouble())
            val a = route[i].at
            val bb = route[i + 1].at
            var s = 0.0
            while (s <= len) {
                val u = s / len
                val v = a + (bb - a) * u
                val x = (x0 + (x1 - x0) * u).roundToInt()
                val y = (y0 + (y1 - y0) * u).roundToInt()
                if (v <= dist) { b.p(x, y, rgb(0xE8B030)); b.p(x + 1, y, rgb(0xB07C1C)) } else if ((s / 3).toInt() % 2 == 0) b.p(x, y, rgb(0x8A7A5A))
                s += 1.0
            }
        }
        route.forEachIndexed { i, st ->
            val (x, y) = pts[i]
            val seen = st.at <= dist
            val top = if (seen) STOP_COLS[Math.floorMod(i + season * 3, STOP_COLS.size)] else rgb(0xC8B898)
            val rock = if (seen) rgb(0x8A7A6A) else rgb(0xB8A888)
            for (j in 1..6) b.r(x - 6 + j, y + j, 13 - 2 * j, 1, rock)
            b.el(x, y, 7, 2, top)
            b.el(x - 1, y - 1, 4, 1, mix(top, rgb(0xFFFFFF), .4f))
            if (i == route.lastIndex) for ((dx, dy) in listOf(-3 to -3, 0 to -4, 3 to -3)) { b.el(x + dx, y + dy, 1, 1, rgb(0x4A9A38)); if (seen) b.p(x + dx, y + dy, rgb(0xE84A3A)) }
            b.digits("${i}", x + 9, y - 2, if (seen) rgb(0x3A2A1A) else rgb(0xA89878))
        }
    }

    override fun anim(b: Buf, t: Int) {
        val (x, y) = here()
        val bob = (sin(t / 6.0) * 1.5).roundToInt()
        val yy = y - 8 + bob
        for (j in 1..3) b.r(x - 3 + j, yy + j, 7 - 2 * j, 1, rgb(0x6A5A50))
        b.el(x, yy, 4, 1, rgb(0x5AA83A))
        b.line(x, yy - 1, x, yy - 7, rgb(0x3A2A20))
        b.r(x + 1, yy - 7, 3, 2, rgb(0xE85050))
        if ((t shr 3) % 2 == 0) b.p(x + 4, yy - 6, rgb(0xE85050))
    }

    /** Our island's position on the chart, between the stops it lies between. */
    private fun here(): Pair<Int, Int> {
        for (i in 0 until route.size - 1) {
            val a = route[i].at
            val bb = route[i + 1].at
            if (dist in a until bb) {
                val u = (dist - a).toDouble() / (bb - a)
                val (x0, y0) = pts[i]
                val (x1, y1) = pts[i + 1]
                return (x0 + (x1 - x0) * u).roundToInt() to (y0 + (y1 - y0) * u).roundToInt()
            }
        }
        return pts[if (dist <= 0) 0 else pts.lastIndex]
    }

    private fun arc(b: Buf, cx: Int, cy: Int, r: Int, c: Int) {
        var a = PI
        while (a <= 2 * PI) { b.p((cx + cos(a) * r).roundToInt(), (cy + sin(a) * r * .7).roundToInt(), c); a += .15 }
    }

    private fun rose(b: Buf, cx: Int, cy: Int) {
        val c = rgb(0x8A6A40)
        b.line(cx, cy - 10, cx, cy + 10, c); b.line(cx - 10, cy, cx + 10, cy, c)
        for (i in 1..3) { b.line(cx - i, cy - 10 + i * 2, cx + i, cy - 10 + i * 2, rgb(0xC84A3A)) }
        b.p(cx, cy, rgb(0xF0C048))
    }

    companion object {
        private val STOP_COLS = intArrayOf(rgb(0x7AC850), rgb(0xF4ECD8), rgb(0xC8A070), rgb(0x4AA8E8), rgb(0xF07030), rgb(0xA8D8FF), rgb(0xE070C0), rgb(0x9AD85A))
    }
}

/** Sunday night: the spirits round the fire. */
class CampfireScene(private val level: Map<Element, Double>) : Scene(160, 64) {
    override val key: Any = level

    override fun base(b: Buf) {
        b.vgrad(0, 0, w, h, intArrayOf(rgb(0x080C26), rgb(0x1C2858), rgb(0x34447A)))
        for (i in 0 until 20) b.p((hash(i, 61) * 160).toInt(), (hash(i, 62) * 30).toInt(), rgb(0xE8ECFF))
        b.r(0, 50, w, 14, rgb(0x2A3A28))
        for (i in 0 until w step 3) b.p(i, 50 + (hash(i, 63) * 2).toInt(), rgb(0x3A5A30))
    }

    override fun anim(b: Buf, t: Int) {
        val fx = 80
        val fy = 56
        b.glow(fx, fy - 4, 26, rgb(0xF0A040), .3f, 5)
        b.line(fx - 6, fy + 1, fx + 6, fy - 1, rgb(0x6A4228)); b.line(fx - 6, fy - 1, fx + 6, fy + 1, rgb(0x5A3A20))
        val fl = (t shr 1) % 3
        b.el(fx, fy - 3, 4, 3, rgb(0xE84A2A)); b.el(fx, fy - 5 - fl, 2, 3, rgb(0xF08A20)); b.p(fx, fy - 8 - fl, rgb(0xFFE060)); b.p(fx - 1, fy - 4, rgb(0xFFE060))
        for (i in 0..2) {
            val sy = (t + i * 7) % 20
            b.p(fx - 2 + i * 2 + (sin((t + i * 5) / 4.0) * 1.5).roundToInt(), fy - 10 - sy, rgb(0xFFB040), 1f - sy / 20f)
        }
        val seats = listOf(Element.AIR to (80 to 30), Element.EARTH to (36 to 58), Element.WATER to (124 to 58), Element.WOOD to (56 to 52), Element.FIRE to (104 to 52))
        for ((e, p) in seats) {
            val v = level[e] ?: 0.0
            val y = p.second + if (e == Element.AIR) sin(t / 5.0).roundToInt() else 0
            b.blit(Spirits.draw(e, maxOf(v, .12), (t shr 2) and 3, dim = if (v < .12) .5f else 0f), p.first - Spirits.CX, y - Spirits.BY - 1)
        }
    }
}
