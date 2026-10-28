package app.aloft.ui.art

import app.aloft.domain.Building
import app.aloft.domain.Decor
import app.aloft.domain.Element
import app.aloft.domain.Guest
import app.aloft.domain.Issue
import app.aloft.domain.Mode
import app.aloft.domain.Wx
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

enum class TimeOfDay { MORNING, DAY, EVENING, NIGHT;

    companion object {
        fun of(hour: Int) = when (hour) {
            in 5..8 -> MORNING
            in 9..16 -> DAY
            in 17..20 -> EVENING
            else -> NIGHT
        }
    }
}

/** Everything the island picture needs. */
data class IsleLook(
    val level: Map<Element, Double>,
    val mode: Mode,
    val issues: Set<Issue>,
    val buildings: Set<Building>,
    val height: Double,
    val time: TimeOfDay,
    val wx: Wx? = null,
    val guest: Guest? = null,
    val decor: Set<Decor> = emptySet(),
    /** One pennant for each request done this week, in its islander's colour. */
    val bunting: List<Int> = emptyList(),
    /** How close the next stop is, 0 far … 1 here: it shows on the horizon. */
    val horizon: Double = 0.0,
    /** A light just released from a training, flying to its spirit. */
    val orb: Element? = null,
    val orbId: Int = 0,
)

/** The island in the sky, 172×190 art pixels. */
class IslandScene(val look: IsleLook) : Scene(W, H) {
    override val key: Any = look

    private var orbT0 = -1

    /** Where each spirit stood in the last frame, for taps. */
    val spots = HashMap<Element, Pair<Int, Int>>()

    override fun base(b: Buf) {
        b.vgrad(0, 0, W, H, SKY.getValue(look.time))
        val t = look.time
        when (t) {
            TimeOfDay.NIGHT -> {
                for (dy in -6..6) for (dx in -6..6) if (dx * dx + dy * dy <= 40 && (dx - 3) * (dx - 3) + (dy + 2) * (dy + 2) > 26) b.p(134 + dx, 24 + dy, if (dx + dy < -3) rgb(0xFFFBE8) else rgb(0xF4ECD0))
            }
            else -> {
                val (sx, sy, col) = when (t) {
                    TimeOfDay.MORNING -> Triple(30, 46, rgb(0xFFF0B0))
                    TimeOfDay.DAY -> Triple(140, 22, rgb(0xFFF8D0))
                    else -> Triple(150, 60, rgb(0xFFD080))
                }
                b.el(sx, sy, 9, 9, mix(col, SKY.getValue(t)[3], .55f))
                b.el(sx, sy, 6, 6, col)
                b.el(sx - 1, sy - 1, 3, 3, rgb(0xFFFFFF))
            }
        }
    }

    override fun anim(b: Buf, t: Int) {
        val lk = look
        val storm = lk.mode == Mode.STORM
        val cl = CLOUD.getValue(lk.time)
        val skyMid = SKY.getValue(lk.time)[2]
        if (lk.time == TimeOfDay.NIGHT) stars(b, t)
        if (lk.horizon > 0) horizonIsle(b, lk.horizon)
        if (lk.height > .5 && !storm) airship(b, t)
        clouds(b, 148, t, .12, IntArray(3) { mix(cl[it], skyMid, .35f) }, 3, false)

        val gust = Issue.GUSTS in lk.issues
        val cx = 86 + if (gust) (sin(t / 7.0) * 2).roundToInt() else 0
        val y = (138 - lk.height * 84).roundToInt() + (sin(t / 20.0) * 1.5).roundToInt()
        if ((Building.RAINBOW in lk.buildings || lk.mode == Mode.CALM) && !storm) rainbow(b, cx, y + 14)

        val e = { k: Element -> lk.level[k] ?: 0.0 }
        val rw = (34 + e(Element.EARTH) * 20).roundToInt()
        val dep = (28 + e(Element.EARTH) * 32).roundToInt()
        rock(b, cx, y, rw, dep, t)
        if (lk.bunting.isNotEmpty()) bunting(b, cx, y, rw, lk.bunting, t)
        if (lk.mode == Mode.ANCHOR) {
            var yy = y + dep
            while (yy < H) {
                if (((yy - y) / 3) % 2 == 1) { b.r(cx - 1, yy, 3, 3, rgb(0x8A8A9A)); b.p(cx, yy + 1, rgb(0x40343A)) } else b.r(cx, yy, 1, 3, rgb(0x6A6A7A))
                yy += 3
            }
        }

        val lean = if (gust) 1 + sin(t / 6.0).roundToInt() else 0
        val dry = if (Issue.DROUGHT in lk.issues) .85f else if (Issue.CRACK in lk.issues) .4f else 0f
        val bs = lk.buildings
        if (Building.TOWER in bs) tower(b, cx + (rw * .04).roundToInt(), y - 1, t)
        if (Building.MILL in bs) mill(b, cx - (rw * .55).roundToInt() - 3, y - 1, t, if (gust) 2.4 else 1.0)
        if (Building.GROVE in bs) tree(b, cx + (rw * .74).roundToInt(), y, 4, dry, lean, true)
        val slots = doubleArrayOf(-.35, .62, -.85, .35, -.12, .86, -.66).take((e(Element.WOOD) * 7).roundToInt())
        for ((i, s) in slots.withIndex()) {
            if (Building.GROVE in bs && s > .55) continue
            if (Building.MILL in bs && s < -.45 && s > -.7) continue
            tree(b, cx + (s * rw).roundToInt(), y - 1 + i % 2, 1 + (i * 7) % 2, dry, lean, false)
        }
        if (Building.FORGE in bs) forge(b, cx + (rw * .5).roundToInt(), y + 1, t)
        if (Building.SPRING in bs) spring(b, cx - (rw * .32).roundToInt(), y + 2, t)
        if (Building.GARDEN in bs) garden(b, cx + (rw * .18).roundToInt(), y + 2)
        Decors.drawAll(b, lk.decor, cx, y, rw, dep, t, lk.time)
        if (lk.height > .25 && bs.isNotEmpty() && !storm) people(b, cx, y, rw, min(4, bs.size), if (lk.mode == Mode.CALM) 0 else t)

        val water = e(Element.WATER)
        if (water > .3 && Issue.DROUGHT !in lk.issues) waterfall(b, cx + rw - 3, y, 1 + (water * 2).roundToInt(), t)
        if (water > .7 && Issue.DROUGHT !in lk.issues) waterfall(b, cx - rw + 2, y, 1, t)
        if (Issue.SLIDE in lk.issues) for (i in 0..4) {
            val fy = ((t * 1.6 + i * 23) % 70).toInt()
            val side = if (i % 2 == 1) 1 else -1
            b.p(cx + side * (rw * (.62 + i * .06)).roundToInt(), y + 6 + fy, ROCK[2 + i % 2])
        }

        for (k in Element.entries) {
            val (sx, sy) = spiritAt(k, cx, y, rw, t)
            spots[k] = sx to sy
            val v = e(k)
            if (v < .12) { Spirits.spark(b, k, sx, sy, t); continue }
            val sleep = lk.mode == Mode.CALM
            if (v >= .75 && !sleep) Spirits.aura(b, k, sx, sy, t)
            b.blit(Spirits.draw(k, v, (t shr 2) and 3, sleep, if (lk.mode == Mode.ANCHOR) .45f else 0f, small = true), sx - Spirits.CX, sy - Spirits.BY - 1)
            if (sleep) {
                val z = (t / 3 + k.ordinal * 5) % 18
                val zx = sx + 5 + z / 6
                val zy = sy - 16 - z / 2
                val a = 1f - z / 18f
                b.r(zx, zy, 3, 1, rgb(0xFFFFFF), a); b.p(zx + 1, zy + 1, rgb(0xFFFFFF), a); b.r(zx, zy + 2, 3, 1, rgb(0xFFFFFF), a)
            }
        }
        lk.guest?.let { g ->
            val gx = cx - (rw * .3).roundToInt()
            val gy = if (g == Guest.WANDERER) y + 2 else y - 22 + (sin(t / 9.0) * 2).roundToInt()
            b.blitA(Guests.draw(g, (t shr 2) and 3), gx - Guests.CX, gy - Guests.BY - 1)
        }

        clouds(b, 162, t, .3, cl, 7, true)

        if (lk.height < .32) {
            val dens = (.32 - lk.height) / .32 * .75
            val fog = mix(cl[0], cl[1], .5f)
            for (yy in max(0, y - 26) until H) for (xx in 0 until W) if (bay(xx + (t shr 3), yy) / 16.0 < dens * ((yy - y + 26) / 40.0).coerceIn(0.0, 1.0)) b.p(xx, yy, fog)
        }
        if (gust && !storm) for (i in 0..5) {
            val wy = 20 + (hash(i, 51) * 110).toInt()
            val wx = ((t * 3 + (hash(i, 52) * 200).toInt()) % (W + 40)) - 20
            b.r(wx, wy, 8 + (i % 3) * 3, 1, rgb(0xFFFFFF), .8f)
        }
        weather(b, t, cx, y, storm)
        orb(b, t)
    }

    private fun spiritAt(k: Element, cx: Int, y: Int, rw: Int, t: Int): Pair<Int, Int> = when (k) {
        Element.AIR -> cx + (sin(t / 35.0) * rw * .9).roundToInt() to y - 24 + (sin(t / 12.0) * 3).roundToInt()
        Element.EARTH -> cx - (rw * .78).roundToInt() to y + 3
        Element.WATER -> cx + (rw * .82).roundToInt() - 2 to y + 1 + sin(t / 10.0).roundToInt()
        Element.WOOD -> cx - (rw * .18).roundToInt() to y - 1
        Element.FIRE -> cx + (rw * .42).roundToInt() to y + 3
    }

    private fun stars(b: Buf, t: Int) {
        for (i in 0 until 46) {
            val sx = (hash(i, 1) * W).toInt()
            val sy = (hash(i, 2) * 120).toInt()
            val tw = (t + (hash(i, 3) * 50).toInt()) % 50
            b.p(sx, sy, if (tw < 4) rgb(0x8090C0) else rgb(0xE8ECFF))
            if (i % 9 == 0 && tw > 40) { b.p(sx - 1, sy, rgb(0x8090C0)); b.p(sx + 1, sy, rgb(0x8090C0)) }
        }
    }

    private fun clouds(b: Buf, baseY: Int, t: Int, speed: Double, pal: IntArray, seed: Int, fill: Boolean) {
        val list = (0 until 11).map { i ->
            val r = 10 + (hash(i, seed * 13) * 9).toInt()
            val cx = ((i * 22 + t * speed + seed * 31) % (W + 50)) - 25
            Triple(cx, baseY + (hash(i, seed * 17) * 6).toInt(), r)
        }
        for ((cx, cy, r) in list) b.el(cx + 2, cy + 2.0, r, (r * .55).roundToInt(), pal[2])
        for ((cx, cy, r) in list) b.el(cx, cy.toDouble(), r, (r * .55).roundToInt(), pal[1])
        for ((cx, cy, r) in list) b.el(cx - 2, cy - 2.0, (r * .6).roundToInt(), (r * .3).roundToInt(), pal[0])
        if (fill) b.r(0, baseY + 6, W, H - baseY, pal[1])
    }

    private fun rainbow(b: Buf, cx: Int, cy: Int) {
        val cols = intArrayOf(rgb(0xE85050), rgb(0xF0A040), rgb(0xF0E060), rgb(0x70C860), rgb(0x5090E0), rgb(0x8060C0))
        cols.forEachIndexed { i, c ->
            val r = 74 - i * 2
            var a = PI
            while (a <= 2 * PI) {
                val x = (cx + cos(a) * r).roundToInt()
                val y = (cy + sin(a) * r * .8).roundToInt()
                if (bay(x, y) < 12) { b.p(x, y, c, .6f); b.p(x, y + 1, c, .6f) }
                a += .008
            }
        }
    }

    private fun airship(b: Buf, t: Int) {
        val ax = ((t * .25) % (W + 60)).toInt() - 30
        val ay = 26 + (sin(t / 25.0) * 2).roundToInt()
        val night = look.time == TimeOfDay.NIGHT
        val env = if (night) rgb(0x7A6A70) else rgb(0xD8A060)
        val envD = if (night) rgb(0x4A4050) else rgb(0xA87038)
        b.el(ax, ay, 12, 5, envD); b.el(ax - 1, ay - 1, 11, 4, env)
        for (i in -8..8 step 4) b.r(ax + i, ay - 4, 1, 9, envD)
        b.line(ax - 5, ay + 5, ax - 3, ay + 8, rgb(0x3A2A28)); b.line(ax + 5, ay + 5, ax + 3, ay + 8, rgb(0x3A2A28))
        b.r(ax - 4, ay + 8, 9, 3, rgb(0x6A3A22))
        val win = if (night) rgb(0xFFD860) else rgb(0xC8E0FF)
        b.p(ax - 2, ay + 9, win); b.p(ax + 1, ay + 9, win)
        b.p(ax - 13, ay + (t shr 2) % 2, envD)
    }

    /** The next stop of the voyage, small and hazy on the horizon, growing as it nears. */
    private fun horizonIsle(b: Buf, near: Double) {
        val r = (3 + near * 9).roundToInt()
        val x = 26
        val y = 118
        val haze = mix(SKY.getValue(look.time)[2], rgb(0x6A7090), .35f)
        b.el(x, y, r, max(1, r / 4), mix(haze, rgb(0x7AC850), .3f))
        for (i in -r..r) {
            val d = ((1 - abs(i) / (r + .5)) * r * .9).roundToInt()
            for (j in 1..d) b.p(x + i, y + j, haze)
        }
    }

    private fun rock(b: Buf, cx: Int, y: Int, rw: Int, dep: Int, t: Int) {
        val iss = look.issues
        for (xc in cx - rw..cx + rw) {
            val dx = (xc - cx).toDouble() / rw
            val a = abs(dx)
            val n = (hash(xc, 7) * 9).toInt()
            var d = dep * (1 - a).pow(1.2) + (n - 4) * .9 + if (Math.floorMod(xc, 4) == 0) n * .5 else 0.0
            if (Issue.SLIDE in iss && a > .55) d *= .45 + .15 * (hash(xc, 8) * 4).toInt()
            val dd = max(2, d.roundToInt())
            for (yy in 0 until dd) {
                val col = if (yy < 3) (if (yy == 0) rgb(0x3A2416) else rgb(0x6A4228)) else {
                    var v = yy.toDouble() / dep * 2.4 + max(0.0, dx) * 1.4 - max(0.0, -dx) * .4 + .3
                    if ((yy + (hash(Math.floorDiv(xc, 7), 9) * 4).toInt()) % 9 == 0) v += .9
                    if (Issue.HEAVY in iss) v += .3
                    val i = v.toInt()
                    ROCK[(if ((v - i) * 16 > bay(xc, yy)) i + 1 else i).coerceIn(0, 4)]
                }
                b.p(xc, y + 3 + yy, col)
            }
        }
        if (Issue.CRACK in iss) for (k in doubleArrayOf(-.5, .1, .6)) {
            var x = (cx + k * rw).roundToInt()
            var yy = y + 4
            for (i in 0 until 14) {
                b.p(x, yy, ROCK[4]); yy++
                if (i % 3 == 0) x += (hash((k * 10).toInt(), i) * 3).toInt() - 1
            }
        }
        for (i in 0..2) {
            val bx = cx - 10 + i * 10 + if (i == 1) 2 else 0
            val by = y + dep + 8 + i * 4 + (sin(t / 14.0 + i) * 1.5).roundToInt()
            val r = 3 - if (i == 2) 1 else 0
            b.el(bx, by, r, r - 1, ROCK[2]); b.p(bx - 1, by - 1, ROCK[0]); b.p(bx, by + r - 1, ROCK[4])
        }
        val dry = if (Issue.DROUGHT in iss) .85f else if (Issue.CRACK in iss) .35f else 0f
        val g = IntArray(3) { mix(GRASS[it], DRY[it], dry) }
        b.el(cx, y, rw, 4, g[1])
        b.el(cx - 3, y - 1, rw - 8, 2, g[0])
        for (xc in cx - rw + 2..cx + rw - 2) {
            val a = abs(xc - cx).toDouble() / rw
            val yb = y + (4 * sqrt(1 - a * a)).roundToInt()
            b.p(xc, yb, g[2])
            if (hash(xc, 11) < .25) b.p(xc, yb + 1 + (hash(xc, 12) * 3).toInt(), g[2])
            if (hash(xc, 13) < .09 && dry == 0f) b.p(xc, y - 1 + (hash(xc, 14) * 3).toInt(), FLOWERS[Math.floorMod(xc, 3)])
        }
        if (Issue.CRACK in iss) for (i in 0..3) {
            val x = (cx - rw + 10 + i * rw * .5).roundToInt()
            b.line(x, y - 1, x + 3, y + 2, rgb(0x5A4A30))
        }
    }

    private fun bunting(b: Buf, cx: Int, y: Int, rw: Int, cols: List<Int>, t: Int) {
        val x0 = cx - (rw * .6).roundToInt()
        val x1 = cx + (rw * .6).roundToInt()
        val n = cols.size * 3
        for (x in x0..x1) {
            val u = (x - x0).toDouble() / (x1 - x0)
            b.p(x, y + 6 + (sin(u * PI) * 4).roundToInt(), rgb(0x5A4030))
        }
        for (i in 0 until n) {
            val u = (i + .5) / n
            val x = (x0 + u * (x1 - x0)).roundToInt()
            val yy = y + 7 + (sin(u * PI) * 4).roundToInt()
            val c = cols[i % cols.size]
            val sway = if (((t shr 3) + i) % 4 == 0) 1 else 0
            b.r(x - 1, yy, 3, 1, c); b.r(x, yy + 1, 2, 1, c); b.p(x + sway, yy + 2, c)
        }
    }

    private fun tree(b: Buf, x: Int, foot: Int, s: Int, dry: Float, lean: Int, big: Boolean) {
        val trunk = if (big) rgb(0x5A3A20) else rgb(0x6A4228)
        val leaf = IntArray(3) { mix(LEAF[it], DRYLEAF[it], dry) }
        b.r(x, foot - (4 + s), if (big) 3 else 2, 4 + s, trunk)
        val cy = foot - (4 + s) - s + 1
        val r = 3 + s
        b.el(x + 1 + lean, cy + 1, r, r - 1, leaf[2])
        b.el(x + lean, cy, r, r - 1, leaf[1])
        b.el(x - 1 + lean, cy - 1, max(1, r - 2), max(1, r - 3), leaf[0])
    }

    private fun mill(b: Buf, x: Int, foot: Int, t: Int, speed: Double) {
        for (i in 0 until 14) {
            val w = 7 - i / 5
            b.r(x - w + 3, foot - i, 2 * w - 2, 1, if (i % 4 == 0) rgb(0xB8A890) else rgb(0xD8CCB4))
        }
        b.r(x + 2, foot - 4, 2, 4, rgb(0x4A3020))
        b.el(x + 3, foot - 16, 4, 2, rgb(0xA04A30))
        val a0 = t * .08 * speed
        for (k in 0..3) {
            val a = a0 + k * PI / 2
            b.line(x + 3.0, foot - 15.0, x + 3 + cos(a) * 9, foot - 15 + sin(a) * 9, rgb(0xF4ECD8))
        }
        b.p(x + 3, foot - 15, rgb(0x3A2A20))
    }

    private fun forge(b: Buf, x: Int, foot: Int, t: Int) {
        b.r(x - 6, foot - 7, 12, 7, rgb(0x8A7060)); b.r(x - 6, foot - 7, 12, 1, rgb(0xA89080))
        for (i in 0..3) b.r(x - 7 + i, foot - 8 - i, 14 - 2 * i, 1, rgb(0xB84A3A))
        b.r(x + 3, foot - 14, 3, 6, rgb(0x6A5A50))
        b.r(x - 2, foot - 4, 3, 4, if ((t shr 1) % 3 != 0) rgb(0xFFB040) else rgb(0xFFE080))
        for (i in 0..3) {
            val sy = (t * .5 + i * 6) % 24
            val sx = x + 4 + (sin((t + i * 9) / 8.0) * 1.5).roundToInt() + (sy / 6).toInt()
            b.el(sx, (foot - 15 - sy).roundToInt(), 1 + if (sy > 10) 1 else 0, 1, mix(rgb(0xC8C0C8), SKY.getValue(look.time)[1], (sy / 24).toFloat()))
        }
    }

    private fun spring(b: Buf, x: Int, foot: Int, t: Int) {
        b.el(x, foot, 7, 2, rgb(0x7A6A5A)); b.el(x, foot, 5, 1, rgb(0x5AC8C0)); b.p(x - 2, foot - 1, rgb(0xC8FFF8))
        for (i in 0..2) {
            val sy = (t * .4 + i * 5) % 14
            b.p(x - 3 + i * 3 + sin((t + i * 7) / 6.0).roundToInt(), (foot - 3 - sy).toInt(), rgb(0xFFFFFF), (.8 - sy / 18).toFloat())
        }
    }

    private fun garden(b: Buf, x: Int, foot: Int) {
        b.el(x, foot, 7, 2, rgb(0x2A64A8)); b.el(x - 1, foot, 5, 1, rgb(0x4AA8E8))
        for ((dx, dy) in listOf(-3 to 0, 2 to 0, 4 to 1)) { b.p(x + dx, foot + dy, rgb(0x5AA83A)); b.p(x + dx + 1, foot + dy, rgb(0x5AA83A)) }
        b.p(x + 2, foot - 1, rgb(0xF0A0C0))
        for (dx in intArrayOf(-9, -8, 9, 10)) { b.p(x + dx, foot - 1, rgb(0xF0A0C0)); b.p(x + dx, foot, rgb(0x3A7A30)) }
    }

    private fun tower(b: Buf, x: Int, foot: Int, t: Int) {
        for (i in 0 until 24) b.r(x - 4, foot - i, 8, 1, if (i % 5 == 0) rgb(0xA89A8A) else rgb(0xC8BAA8))
        b.r(x + 2, foot - 24, 2, 24, rgb(0x8A7C6C))
        val lit = look.time == TimeOfDay.NIGHT || look.time == TimeOfDay.EVENING
        for (wy in intArrayOf(7, 15)) b.r(x - 1, foot - wy - 2, 2, 3, if (lit) rgb(0xFFD860) else rgb(0x2A3048))
        for (i in 0 until 9) {
            val k = (i / 1.8).toInt()
            b.r(x - 5 + k, foot - 25 - i, 10 - 2 * k, 1, if (i < 2) rgb(0x2A4A90) else rgb(0x3A64B8))
        }
        b.line(x, foot - 34, x, foot - 38, rgb(0x3A2A20))
        b.r(x + 1, foot - 38, 4, 2, rgb(0xE85050)); b.p(x + 5, foot - 37 - (t shr 2) % 2, rgb(0xE85050))
    }

    private fun people(b: Buf, cx: Int, y: Int, rw: Int, n: Int, t: Int) {
        val cols = intArrayOf(rgb(0xC84A3A), rgb(0x3A64B8), rgb(0xE8B840), rgb(0x8A5AA8))
        for (i in 0 until n) {
            val x = cx + (sin(t / (50.0 + i * 17) + i * 2) * rw * .6).roundToInt()
            val yy = y + if (i % 2 == 1) 2 else 0
            val step = (t shr 2) % 2
            b.r(x, yy - 3, 1, 2, cols[i % 4]); b.p(x, yy - 4, rgb(0xF0C8A0)); b.p(x - step, yy - 1, rgb(0x3A2A20))
        }
    }

    private fun waterfall(b: Buf, x: Int, y: Int, w: Int, t: Int) {
        for (yy in y + 2 until H) {
            for (i in 0 until w) b.p(x + i, yy, if (Math.floorMod(yy - t * 2 + i * 2, 4) == 0) rgb(0xFFFFFF) else rgb(0x8AD0FF))
            b.p(x + w, yy, rgb(0x4A90D0))
        }
    }

    /** Real weather outside, and the storm of a sudden jump in training. */
    private fun weather(b: Buf, t: Int, cx: Int, y: Int, storm: Boolean) {
        val wx = look.wx
        if (storm) {
            b.r(0, 0, W, H, rgb(0x14162C), .5f)
            for (i in 0 until 7) {
                val bx = cx - 50 + i * 17 + (sin(t / 30.0 + i) * 3).roundToInt()
                val by = y - 46 + (i % 2) * 5
                b.el(bx + 2, by + 2, 13, 6, rgb(0x20223A)); b.el(bx, by, 12, 5, rgb(0x3A3C58)); b.el(bx - 2, by - 2, 7, 2, rgb(0x55577A))
            }
        }
        if (storm || wx == Wx.RAIN || wx == Wx.STORM) {
            val n = if (storm || wx == Wx.STORM) 50 else 30
            for (i in 0 until n) {
                val ry = Math.floorMod((hash(i, 21) * H).toInt() + t * 5, H)
                val rx = Math.floorMod((hash(i, 22) * W).toInt() - t * 2, W)
                b.p(rx, ry, rgb(0x9AB0E0)); b.p(rx - 1, ry + 1, rgb(0x9AB0E0)); b.p(rx - 1, ry + 2, rgb(0x6A80B0))
            }
        }
        if (wx == Wx.SNOW) for (i in 0 until 40) {
            val sy = Math.floorMod((hash(i, 23) * H).toInt() + t, H)
            val sx = Math.floorMod((hash(i, 24) * W).toInt() + (sin((t + i * 13) / 9.0) * 2).roundToInt(), W)
            b.p(sx, sy, rgb(0xFFFFFF))
        }
        if (wx == Wx.FOG) for (yy in 0 until H) for (xx in 0 until W) if (bay(xx + (t shr 3), yy) < 4) b.p(xx, yy, rgb(0xE8ECF4), .7f)
        if (storm && t % 80 < 4) {
            if (t % 80 < 2) b.r(0, 0, W, H, rgb(0xFFFFFF), .4f)
            var lx = cx + 20
            var ly = y - 40
            while (ly < y - 2) {
                val nx = lx + (hash(ly, t / 80) * 7).toInt() - 3
                b.line(lx, ly, nx, ly + 4, rgb(0xFFF8C0))
                lx = nx; ly += 4
            }
        }
    }

    private fun orb(b: Buf, t: Int) {
        val k = look.orb ?: return
        if (orbT0 < 0) orbT0 = t
        val f = (t - orbT0) / 22.0
        val (tx, ty) = spots[k] ?: return
        if (f > 1.6) return
        if (f <= 1) {
            val i = k.ordinal
            for (j in 0..5) {
                val ff = max(0.0, f - j * .03)
                val (ox, oy) = quad(W / 2.0, H + 4.0, W / 2.0 + (i - 2) * 34, 30.0, tx.toDouble(), ty - 8.0, ff)
                if (j == 0) { b.r(ox.roundToInt() - 1, oy.roundToInt() - 1, 3, 3, k.color.toInt()); b.p(ox.roundToInt(), oy.roundToInt(), rgb(0xFFFFFF)) }
                else b.p(ox.roundToInt(), oy.roundToInt(), mix(k.color.toInt(), rgb(0xFFFFFF), j / 6f))
            }
        } else {
            val r = ((f - 1) * 20).roundToInt()
            for (j in 0 until 12) {
                val a = j / 12.0 * 2 * PI
                b.p((tx + cos(a) * r).roundToInt(), (ty - 8 + sin(a) * r).roundToInt(), if (r < 6) rgb(0xFFFFFF) else k.color.toInt())
            }
        }
    }

    companion object {
        const val W = 172
        const val H = 190

        val SKY = mapOf(
            TimeOfDay.MORNING to intArrayOf(rgb(0x5A70C8), rgb(0xB890C0), rgb(0xF0A8A0), rgb(0xFFE0B0)),
            TimeOfDay.DAY to intArrayOf(rgb(0x2E5CC0), rgb(0x4A7AD8), rgb(0x78A8F0), rgb(0xC8E0FF)),
            TimeOfDay.EVENING to intArrayOf(rgb(0x2A2068), rgb(0x7A3A80), rgb(0xD85A6A), rgb(0xFFB070)),
            TimeOfDay.NIGHT to intArrayOf(rgb(0x080C26), rgb(0x121A48), rgb(0x1C2858), rgb(0x34447A)),
        )
        val CLOUD = mapOf(
            TimeOfDay.MORNING to intArrayOf(rgb(0xFFF4E8), rgb(0xF0D0D0), rgb(0xC8A8C0)),
            TimeOfDay.DAY to intArrayOf(rgb(0xFFFFFF), rgb(0xDFE8F8), rgb(0xA8B8E0)),
            TimeOfDay.EVENING to intArrayOf(rgb(0xFFD8C0), rgb(0xE8A0A8), rgb(0x9A6890)),
            TimeOfDay.NIGHT to intArrayOf(rgb(0x6A78A0), rgb(0x465484), rgb(0x2C3664)),
        )
        val ROCK = intArrayOf(rgb(0xA89684), rgb(0x84725F), rgb(0x605048), rgb(0x40343A), rgb(0x2A2028))
        private val GRASS = intArrayOf(rgb(0x9AD85A), rgb(0x5AA83A), rgb(0x3A7A30))
        private val DRY = intArrayOf(rgb(0xE0C870), rgb(0xB89A40), rgb(0x806A30))
        private val LEAF = intArrayOf(rgb(0x7AC850), rgb(0x4A9A38), rgb(0x2A6A28))
        private val DRYLEAF = intArrayOf(rgb(0xD8B860), rgb(0xA88838), rgb(0x705A28))
        private val FLOWERS = intArrayOf(rgb(0xFFFFFF), rgb(0xF0A0C0), rgb(0xFFE060))
    }
}
