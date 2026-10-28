package app.aloft.ui.art

import app.aloft.domain.Element
import app.aloft.domain.Stage
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * The five spirits, drawn in code on a 28×28 canvas, feet at (14, 26). Each has four animation
 * frames, a sleeping pose, and grows with its stage: an ember is small, a great spirit gets a
 * crystal, pearls, antlers, orbiting motes or embers.
 */
object Spirits {
    const val W = 28
    const val H = 28
    const val CX = 14
    const val BY = 26

    private val AIR = Ramp(rgb(0xFFFFFF), rgb(0xD4ECFF), rgb(0x90BCE8))
    private val STONE = Ramp(rgb(0xCBBBA6), rgb(0x9C8B78), rgb(0x66574C))
    private val WATER = Ramp(rgb(0xBFF0FF), rgb(0x58B4EE), rgb(0x2C70BC))
    private val LEAF = Ramp(rgb(0xB4E66C), rgb(0x68B23E), rgb(0x3A7630))
    private val BARK = Ramp(rgb(0xC89A60), rgb(0x8A5A30), rgb(0x5A381C))
    private val SKIN = Ramp(rgb(0xFFF2D8), rgb(0xF2D4A6), rgb(0xC89A70))
    private val FIRE = Ramp(rgb(0xFF9A5A), rgb(0xE2482A), rgb(0x9A2418))
    private val DIM = rgb(0x40486A)

    private val cache = object : LinkedHashMap<String, Buf>(64, .75f, true) {
        override fun removeEldestEntry(e: MutableMap.MutableEntry<String, Buf>?) = size > 240
    }

    fun zoom(stage: Stage) = when (stage) {
        Stage.SPARK, Stage.EMBER -> .8
        Stage.SPIRIT -> 1.0
        Stage.GREAT -> 1.1
    }

    /** A spirit at [level] (an ember at least), animation frame [f] 0..3; [small] is the size it stands on the island. */
    fun draw(e: Element, level: Double, f: Int, sleep: Boolean = false, dim: Float = 0f, small: Boolean = false): Buf {
        val st = Stage.of(level).let { if (it == Stage.SPARK) Stage.EMBER else it }
        val z = zoom(st) * if (small) .68 else 1.0
        val key = "$e$z${f and 3}$sleep$dim"
        synchronized(cache) { cache[key]?.let { return it } }
        val b = Buf(W, H)
        val s = { n: Double -> (n * z).roundToInt() }
        val g = st == Stage.GREAT
        when (e) {
            Element.AIR -> air(b, s, f and 3, sleep, g)
            Element.EARTH -> earth(b, s, f and 3, sleep, g)
            Element.WATER -> water(b, s, f and 3, sleep, g)
            Element.WOOD -> wood(b, s, f and 3, sleep, g)
            Element.FIRE -> fire(b, s, f and 3, sleep, g)
        }
        b.tint(DIM, dim)
        synchronized(cache) { cache[key] = b }
        return b
    }

    private fun Buf.eyes(x1: Int, x2: Int, y: Int, sl: Boolean, c: Int, glint: Int?, w: Int = 2, h: Int = 2) {
        for (x in intArrayOf(x1, x2)) {
            if (sl) { r(x, y + h - 1, w, 1, c); continue }
            r(x, y, w, h, c)
            if (glint != null) p(x, y, glint)
        }
    }

    private fun Buf.flower(x: Int, y: Int, petal: Int = rgb(0xF6A0C0), mid: Int = rgb(0xFFE060)) {
        for ((dx, dy) in listOf(1 to 0, -1 to 0, 0 to 1, 0 to -1)) p(x + dx, y + dy, petal)
        p(x, y, mid)
    }

    private fun Buf.flame(x: Int, y: Int, s: Int, f: Int, sl: Boolean) {
        val k = if (sl) .6 else 1.0
        val h = max(2, ((s + f % 2) * k).roundToInt())
        val wob = intArrayOf(0, 1, 0, -1)[f]
        for (i in 0 until h) {
            val u = i.toDouble() / h
            val w = max(0, ((1 - u) * s * .6 * k).roundToInt())
            val cx = x + (wob * u).roundToInt()
            r(cx - w, y - i, 2 * w + 1, 1, if (u < .35) rgb(0xF08A20) else if (u < .7) rgb(0xFFC040) else rgb(0xFFE890))
        }
        p(x + wob, y - h, rgb(0xFFF8D0))
    }

    private fun air(b: Buf, s: (Double) -> Int, f: Int, sl: Boolean, g: Boolean) = with(b) {
        val hy = BY - s(17.0) + if (sl) 2 else 0
        lit(AIR) {
            for (i in 0..8) {
                val u = i / 8.0
                val x = CX + sin(u * 4 + f * 1.57) * s(1.6) + u * s(6.0)
                val y = hy + s(5.0) + u * s(11.0) - if (sl) u * s(4.0) else 0.0
                val r = max(1, (s(4.6) * (1 - u * .8)).roundToInt())
                el(x, y, r, r, K)
            }
        }
        lit(AIR) { el(CX, hy, s(5.0), s(5.0), K); el(CX + s(3.0), hy - s(5.0), s(2.0), 1, K); p(CX + s(5.0), hy - s(6.0), K) }
        lit(AIR) {
            val a = if (sl) 0 else f % 2
            el(CX - s(6.0), hy + s(6.0) - a, 1, s(2.0), K)
            el(CX + s(6.0), hy + s(5.0) + a, 1, s(2.0), K)
        }
        outlineAll(rgb(0x4A72B8))
        eyes(CX - s(2.0) - 1, CX + s(2.0), hy, sl, rgb(0x22305A), rgb(0xFFFFFF))
        p(CX - s(3.0) - 1, hy + 2, rgb(0xFFB6CC)); p(CX + s(3.0) + 1, hy + 2, rgb(0xFFB6CC))
        p(CX, hy + s(3.0), rgb(0x5A7AB8))
        if (!sl) for (j in 0..1) {
            val a0 = f * .6 + j * PI
            for (q in 0..4) {
                val a = a0 + q * .28
                p(CX + cos(a) * s(11.0), hy + s(7.0) + sin(a) * s(5.0), rgb(0xFFFFFF), .75f)
            }
        }
        if (g) for (j in 0..2) {
            val a = f * PI / 2 + j * 2.09
            val mx = (CX + cos(a) * s(9.0)).roundToInt()
            val my = (hy - s(1.0) + sin(a) * s(4.0)).roundToInt()
            p(mx, my, rgb(0xFFFFFF)); p(mx + 1, my, rgb(0xA8D8FF)); p(mx, my + 1, rgb(0xA8D8FF))
        }
    }

    private fun earth(b: Buf, s: (Double) -> Int, f: Int, sl: Boolean, g: Boolean) = with(b) {
        val breathe = if (sl) 1 else (f shr 1) % 2
        val drop = if (sl) 2 else 0
        lit(STONE) { r(CX - s(6.0), BY - s(4.0), s(4.0), s(4.0) + 1, K); r(CX + s(2.0), BY - s(4.0), s(4.0), s(4.0) + 1, K) }
        lit(STONE) { el(CX, BY - s(10.0) + breathe, s(8.0), s(6.0), K) }
        lit(STONE) { el(CX, BY - s(18.0) + breathe + drop, s(5.0), s(4.0), K) }
        lit(STONE) { el(CX - s(10.0), BY - s(8.0) + drop, s(3.0), s(5.0), K); el(CX + s(10.0), BY - s(8.0) + drop, s(3.0), s(5.0), K) }
        outlineAll(rgb(0x2A2028))
        val hy = BY - s(18.0) + breathe + drop
        val crack = rgb(0x54463E)
        line(CX - s(4.0), BY - s(14.0), CX - s(2.0), BY - s(10.0), crack); p(CX - s(2.0) + 1, BY - s(10.0) + 1, crack)
        line(CX + s(9.0), BY - s(11.0), CX + s(11.0), BY - s(7.0), crack)
        val glow = if (sl) rgb(0xA87C30) else if (f % 2 == 1) rgb(0xFFE890) else rgb(0xF0B838)
        val rx = CX + s(2.0)
        val ry = BY - s(10.0) + breathe
        for ((dx, dy) in listOf(0 to -2, 1 to -1, 2 to 0, 1 to 1, 0 to 2, -1 to 1, -2 to 0, -1 to -1)) p(rx + dx, ry + dy, glow)
        p(rx, ry, if (sl) crack else rgb(0xFFF8C0))
        eyes(CX - s(2.0) - 1, CX + s(2.0), hy, sl, if (sl) rgb(0x4A3E38) else rgb(0xFFD84A), rgb(0xFFF8C0), 2, 1)
        for (x in CX - s(3.0)..CX + s(2.0)) p(x, hy - s(4.0) + if (x == CX - s(3.0) || x == CX + s(2.0)) 1 else 0, if (x % 2 == 1) rgb(0x6AB048) else rgb(0x4A9038))
        p(CX - s(1.0), hy - s(3.0), rgb(0x3A7A30)); p(CX + 1, hy - s(3.0) + 1, rgb(0x3A7A30))
        for (sx in intArrayOf(CX - s(10.0), CX + s(10.0))) {
            val sy = BY - s(13.0) + drop
            p(sx, sy, rgb(0x6AB048)); p(sx - 1, sy + 1, rgb(0x4A9038)); p(sx + 1, sy, rgb(0x4A9038))
        }
        if (g) {
            val cx = CX + s(2.0)
            val cy = hy - s(5.0)
            p(cx, cy - 2, rgb(0xE8F8FF)); r(cx - 1, cy - 1, 3, 1, rgb(0xA8E0FF)); r(cx - 1, cy, 3, 1, rgb(0x5A8AD8)); p(cx, cy + 1, rgb(0x3A5AA8))
            flower(CX - s(3.0), hy - s(5.0), rgb(0xFFFFFF))
        }
    }

    private fun water(b: Buf, s: (Double) -> Int, f: Int, sl: Boolean, g: Boolean) = with(b) {
        val tip = if (sl) 0 else intArrayOf(0, 1, 2, 1)[f]
        lit(WATER) { el(CX, BY - 1, s(9.0), s(2.0), K) }
        lit(WATER) {
            el(CX, BY - s(8.0), s(6.0), s(7.0), K)
            val n = s(9.0)
            for (i in 0 until n) {
                val u = i.toDouble() / n
                val w = (s(5.0) * (1 - u)).roundToInt()
                r(CX - w + (tip * u * u * 2).roundToInt(), BY - s(14.0) - i, 2 * w + 1, 1, K)
            }
        }
        outlineAll(rgb(0x163A7A))
        for (i in 0..4) p(CX - s(4.0) + if (i > 2) 1 else 0, BY - s(12.0) + i, rgb(0xFFFFFF))
        line(CX + s(1.0), BY - s(17.0), CX + s(3.0), BY - s(13.0), WATER.d)
        eyes(CX - s(3.0) - 1, CX + s(2.0), BY - s(10.0), sl, rgb(0x0A2A58), rgb(0xFFFFFF))
        val my = BY - s(6.0)
        val mouth = rgb(0x1A4A8A)
        if (sl) r(CX - 1, my, 3, 1, mouth) else { p(CX - 1, my, mouth); p(CX + 1, my, mouth); p(CX, my + 1, mouth) }
        p(CX - s(4.0) - 1, BY - s(8.0), rgb(0xFFA8C8)); p(CX + s(4.0) + 1, BY - s(8.0), rgb(0xFFA8C8))
        if (!sl) for (j in 0..2) {
            val by = BY - s(3.0) - Math.floorMod(f * 3 + j * 5, max(1, s(14.0)))
            val bx = CX + s(8.0) + (if (j % 2 == 1) 1 else -1) - j
            p(bx, by, rgb(0xE8FBFF))
            if (j == 1) { p(bx + 1, by, rgb(0x8AD0FF)); p(bx, by - 1, rgb(0x8AD0FF)) }
        }
        if (g) for (j in -1..1) {
            val x = CX + j * s(3.0)
            val y = BY - s(14.0) - if (j == 0) 2 else 0
            p(x, y, rgb(0xFFFFFF)); p(x, y + 1, rgb(0xC0C8E0))
        }
    }

    private fun wood(b: Buf, s: (Double) -> Int, f: Int, sl: Boolean, g: Boolean) = with(b) {
        val a = if (sl) 0 else f % 2
        val droop = if (sl) 2 else 0
        line(CX - s(4.0), BY, CX - s(2.0), BY - s(2.0), BARK.d); line(CX + s(4.0), BY, CX + s(2.0), BY - s(2.0), BARK.d)
        p(CX - s(5.0), BY, BARK.d); p(CX + s(5.0), BY, BARK.d)
        lit(BARK) {
            val top = BY - s(11.0)
            for (y in top until BY) {
                val w = (s(1.5) + (y - top) * .4).roundToInt()
                r(CX - w, y, 2 * w + 1, 1, K)
            }
            for (side in intArrayOf(-1, 1)) for (i in 0 until s(6.0)) r(CX + side * (s(2.0) + i), BY - s(9.0) - (i * .6).roundToInt() + droop - if (side > 0) a else 0, 1, 2, K)
        }
        lit(SKIN) { el(CX, BY - s(14.0), s(3.5), s(3.0), K) }
        lit(LEAF) {
            el(CX, BY - s(18.0), s(6.5), s(4.0), K)
            el(CX - s(5.0), BY - s(14.0), s(2.5), s(3.5), K)
            el(CX + s(5.0), BY - s(14.0) + if (f > 1 && !sl) 1 else 0, s(2.5), s(3.5), K)
            el(CX - s(8.0), BY - s(13.0) + droop, s(1.6), s(1.4), K)
            el(CX + s(8.0), BY - s(13.0) + droop - a, s(1.6), s(1.4), K)
        }
        outlineAll(rgb(0x1E3A18))
        var x = CX - s(3.0)
        while (x <= CX + s(3.0)) { p(x, BY - s(17.0) + 1, LEAF.d); x += 2 }
        eyes(CX - s(2.0), CX + s(1.0) + 1, BY - s(14.0), sl, rgb(0x2A1A0A), null, 1, 2)
        p(CX - s(3.0), BY - s(13.0), rgb(0xF6A8A0)); p(CX + s(3.0), BY - s(13.0), rgb(0xF6A8A0))
        p(CX, BY - s(12.0), rgb(0xA06A50))
        flower(CX - s(4.0), BY - s(20.0))
        if (g || s(10.0) > 8) flower(CX + s(4.0), BY - s(19.0), rgb(0xFFFFFF))
        if (!sl) { p(CX + s(10.0) - f, BY - s(8.0) + f * 2, LEAF.l); p(CX + s(10.0) - f + 1, BY - s(8.0) + f * 2, LEAF.b) }
        if (g) {
            for (side in intArrayOf(-1, 1)) {
                val bx = CX + side * s(3.0)
                line(bx, BY - s(21.0), bx + side * s(2.0), BY - s(25.0), BARK.b)
                line(bx + side * s(1.0), BY - s(23.0), bx + side * s(3.0), BY - s(23.0), BARK.b)
            }
            flower(CX + s(7.0), BY - s(17.0)); flower(CX - s(6.0), BY - s(16.0), rgb(0xFFFFFF))
        }
    }

    private fun fire(b: Buf, s: (Double) -> Int, f: Int, sl: Boolean, g: Boolean) = with(b) {
        val tx = CX + s(11.0)
        val ty = BY - s(14.0)
        lit(FIRE) {
            for (i in 0..8) {
                val u = i / 8.0
                val x = CX + s(5.0) + u * s(6.0) + (sin(u * 3.2) * s(1.5)).roundToInt()
                val y = BY - s(4.0) - u * s(10.0)
                val r = max(1, (s(2.6) * (1 - u * .65)).roundToInt())
                el(x, y, r, r, K)
            }
            el(CX, BY - s(4.0), s(7.0), s(3.0), K)
            el(CX - s(7.0), BY - s(6.0), s(4.0), s(3.0), K)
            r(CX - s(5.0), BY - s(2.0), s(2.0), s(2.0) + 1, K); r(CX + s(3.0), BY - s(2.0), s(2.0), s(2.0) + 1, K)
        }
        outlineAll(rgb(0x4A0E0A))
        for (x in CX - s(6.0)..CX + s(5.0)) p(x, BY - s(2.0), if (x % 3 != 0) rgb(0xFFB050) else rgb(0xF09038))
        for ((dx, dy) in listOf(-2 to 6, 2 to 6, 5 to 8, -5 to 5)) p(CX + s(dx.toDouble()), BY - s(dy.toDouble()), FIRE.d)
        val ex = CX - s(8.0)
        val ey = BY - s(8.0)
        if (sl) r(ex - 1, ey + 1, 2, 1, rgb(0x4A0E0A)) else { r(ex - 1, ey, 2, 2, rgb(0xFFE060)); p(ex - 1, ey + 1, rgb(0x1A0A00)) }
        p(CX - s(10.0), BY - s(7.0), FIRE.d)
        line(CX - s(10.0), BY - s(5.0), CX - s(7.0), BY - s(5.0), rgb(0x6A1410))
        for (x in intArrayOf(CX - s(5.0), CX - s(2.0), CX + s(1.0), CX + s(4.0))) {
            val h = 1 + Math.floorMod(x + f, 2) + if (g) 1 else 0
            for (i in 1..(if (sl) 1 else h)) p(x, BY - s(7.0) - i + 1, if (i == h) rgb(0xFFE060) else rgb(0xF08A20))
        }
        flame(tx, ty - 1, s(3.5) + if (g) 2 else 0, f, sl)
        if (g && !sl) for (j in 0..2) {
            val y = ty - s(6.0) - Math.floorMod(f * 2 + j * 4, 8)
            p(tx + (j - 1) * 2 + f % 2, y, rgb(0xFFB040))
        }
    }

    /** The faint ring of colour round a great spirit. */
    fun aura(b: Buf, e: Element, x: Int, y: Int, t: Int) {
        val c = e.color.toInt()
        for (dy in -15..15) for (dx in -15..15) {
            val r = kotlin.math.hypot(dx.toDouble(), dy * 1.1)
            if (r > 11 && r < 15 && bay(x + dx + (t shr 3), y + dy) < 3) b.p(x + dx, y - 12 + dy, c)
        }
    }

    /** A spirit too weak to take shape: a blinking spark. */
    fun spark(b: Buf, e: Element, x: Int, y: Int, t: Int) {
        val on = Math.floorMod(t + e.ordinal * 7, 20) < 14
        val c = e.color.toInt()
        b.p(x, y - 3, if (on) rgb(0xFFFFFF) else c)
        if (on) for ((dx, dy) in listOf(1 to 0, -1 to 0, 0 to 1, 0 to -1)) b.p(x + dx, y - 3 + dy, c)
    }
}
