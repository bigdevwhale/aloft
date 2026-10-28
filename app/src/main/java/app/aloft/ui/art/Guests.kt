package app.aloft.ui.art

import app.aloft.domain.Guest
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

/** The guests from the weather, 28×28 like the spirits, feet at (14, 26), four frames each. */
object Guests {
    const val W = 28
    const val H = 28
    const val CX = 14
    const val BY = 26

    private val cache = HashMap<String, Buf>()

    fun draw(g: Guest, f: Int): Buf {
        val key = "$g$f"
        synchronized(cache) { cache[key]?.let { return it } }
        val b = Buf(W, H)
        when (g) {
            Guest.WANDERER -> wanderer(b, f)
            Guest.THUNDERBIRD -> thunderbird(b, f)
            Guest.FROST -> frost(b, f)
            Guest.SUNHARE -> hare(b, f)
            Guest.SNOWOWL -> owl(b, f)
            Guest.MISTLING -> mistling(b, f)
            Guest.DRIZZLE -> drizzle(b, f)
            Guest.ROBIN -> robin(b, f)
            Guest.FIREFLY -> firefly(b, f)
        }
        synchronized(cache) { cache[key] = b }
        return b
    }

    private fun drizzle(b: Buf, f: Int) = with(b) {
        lit(Ramp(rgb(0xFFFFFF), rgb(0xC8D0E0), rgb(0x8A96B0))) { el(CX, 15, 7, 4, K); el(CX - 4, 13, 4, 4, K); el(CX + 3, 11, 4, 4, K) }
        outlineAll(rgb(0x3A4A6A))
        r(CX - 3, 13, 1, 2, rgb(0x22305A)); r(CX + 2, 13, 1, 2, rgb(0x22305A)); p(CX, 16, rgb(0x5A6A8A))
        p(CX - 5, 15, rgb(0xFFB6CC)); p(CX + 4, 15, rgb(0xFFB6CC))
        for (j in 0..3) {
            val x = CX - 5 + j * 3
            val y = 21 + (f + j) % 3 * 2
            p(x, y, rgb(0x4AA8E8)); p(x, y + 1, rgb(0x8AD0FF))
        }
    }

    private fun owl(b: Buf, f: Int) = with(b) {
        val white = Ramp(rgb(0xFFFFFF), rgb(0xE4ECF4), rgb(0xA8B8CC))
        lit(white) { el(CX, 18, 6, 7, K); r(CX - 6, 9, 2, 3, K); r(CX + 4, 9, 2, 3, K); el(CX, 12, 5, 4, K) }
        outlineAll(rgb(0x3A4A6A))
        val blink = f == 3
        for (x in intArrayOf(CX - 3, CX + 2)) {
            if (blink) r(x - 1, 12, 3, 1, rgb(0x3A4A6A)) else { el(x, 12, 1, 1, rgb(0xF0B030)); p(x, 12, rgb(0x1A1A2A)) }
        }
        p(CX - 1, 14, rgb(0xE09030)); p(CX - 1, 15, rgb(0xC07020))
        for ((dx, dy) in listOf(-3 to 18, 1 to 19, -1 to 21, 3 to 22, -4 to 22)) p(CX + dx, dy, rgb(0xA8B8CC))
        r(CX - 3, 25, 2, 1, rgb(0xE09030)); r(CX + 1, 25, 2, 1, rgb(0xE09030))
        for (j in 0..2) {
            val y = (j * 9 + f * 2) % 22 + 2
            p(2 + j * 11, y, rgb(0xFFFFFF))
        }
    }

    private fun robin(b: Buf, f: Int) = with(b) {
        val hop = if (f % 2 == 1) 1 else 0
        lit(Ramp(rgb(0xB08A60), rgb(0x8A6440), rgb(0x5A3E24))) { el(CX + 1, 17 - hop, 6, 5, K); el(CX - 4, 13 - hop, 3, 3, K); r(CX + 6, 14 - hop, 4, 2, K) }
        lit(Ramp(rgb(0xFFB070), rgb(0xF07838), rgb(0xC04A20))) { el(CX - 2, 17 - hop, 3, 3, K) }
        outlineAll(rgb(0x2A1A10))
        p(CX - 5, 12 - hop, rgb(0x000000)); p(CX - 5, 12 - hop, rgb(0xFFFFFF), .4f)
        p(CX - 8, 13 - hop, rgb(0xE8B030)); p(CX - 7, 13 - hop, rgb(0xE8B030))
        p(CX, 23, rgb(0x5A3E24)); p(CX + 2, 23, rgb(0x5A3E24)); p(CX, 24, rgb(0x5A3E24)); p(CX + 2, 24, rgb(0x5A3E24))
        for (k in 0 until 6) {
            val a = k * PI / 3 + f * .25
            p((CX + cos(a) * 11).roundToInt(), (15 + sin(a) * 9).roundToInt(), rgb(0xFFE070), .8f)
        }
    }

    private fun firefly(b: Buf, f: Int) = with(b) {
        glow(CX + 3, 15, 9, rgb(0xFFE060), .5f, 4, screen = false)
        val flap = if (f % 2 == 0) 0 else 2
        el(CX - 2, 9 + flap, 2, 3, rgb(0xC8D8FF), ); el(CX + 1, 9 + flap, 2, 3, rgb(0xC8D8FF))
        lit(Ramp(rgb(0x5A5A7A), rgb(0x3A3A54), rgb(0x22223A))) { el(CX - 4, 13, 2, 2, K); el(CX - 1, 14, 2, 2, K) }
        lit(Ramp(rgb(0xFFFBD0), rgb(0xFFE060), rgb(0xE0A020))) { el(CX + 3, 15, 4, 3, K) }
        line(CX - 5, 11, CX - 7, 8, rgb(0x22223A)); line(CX - 4, 11, CX - 4, 8, rgb(0x22223A))
        p(CX - 5, 13, rgb(0xFFFFFF))
    }

    private fun mistling(b: Buf, f: Int) = with(b) {
        val sway = intArrayOf(0, 1, 0, -1)[f]
        lit(Ramp(rgb(0xFFFFFF), rgb(0xD8DCE8), rgb(0xA0A8C0))) {
            el(CX + sway, 10, 5, 5, K)
            for (y in 10..22) {
                val w = (5 + (y - 10) * .2).roundToInt()
                r(CX - w + sway * (22 - y) / 12, y, 2 * w + 1, 1, K)
            }
            for (x in CX - 7..CX + 7) if (Math.floorMod(x + f, 3) == 0) p(x, 23, K)
        }
        outlineAll(rgb(0x6A7090))
        fade(.7f)
        r(CX - 2 + sway, 9, 1, 2, rgb(0x40486A)); r(CX + 2 + sway, 9, 1, 2, rgb(0x40486A))
    }

    private fun wanderer(b: Buf, f: Int) = with(b) {
        lit(Ramp(rgb(0x8A6AB0), rgb(0x5A4A8A), rgb(0x3A2E5A))) {
            for (y in 10..25) {
                val w = (2 + (y - 8) * .38).roundToInt()
                r(CX - w, y, 2 * w + 1, 1, K)
            }
            el(CX, 9, 4, 4, K)
        }
        outlineAll(rgb(0x1A1428))
        el(CX, 10, 2, 2, rgb(0x1A1428))
        if (f != 2) { p(CX - 1, 10, rgb(0xFFE060)); p(CX + 1, 10, rgb(0xFFE060)) }
        line(CX + 7, 5, CX + 7, 25, rgb(0x8A5A30))
        r(CX - 8, 15, 3, 4, rgb(0xFFE060)); r(CX - 8, 14, 3, 1, rgb(0x3A2A20)); r(CX - 8, 19, 3, 1, rgb(0x3A2A20))
        line(CX - 7, 13, CX - 4, 14, rgb(0x3A2A20))
        if (f % 2 == 0) glow(CX - 7, 17, 5, rgb(0xFFE060), .35f, 3)
    }

    private fun thunderbird(b: Buf, f: Int) = with(b) {
        val up = f % 2 == 0
        lit(Ramp(rgb(0x6A8AE0), rgb(0x3A54B0), rgb(0x22306A))) {
            el(CX + 2, if (up) 8 else 13, 7, 2, K)
            el(CX, 15, 5, 4, K)
            el(CX - 5, 11, 3, 3, K)
        }
        outlineAll(rgb(0x10183A))
        p(CX - 6, 10, rgb(0xFFE060))
        p(CX - 9, 11, rgb(0xF0C040)); p(CX - 9, 12, rgb(0xF0C040))
        val bolt = rgb(0xFFF8A0)
        line(CX + 5, 16, CX + 8, 18, bolt); line(CX + 8, 18, CX + 6, 20, bolt); line(CX + 6, 20, CX + 10, 24, bolt)
    }

    private fun frost(b: Buf, f: Int) = with(b) {
        lit(Ramp(rgb(0xD8F0FF), rgb(0x8AC0E8), rgb(0x4A80B8))) { for (y in 14..25) { val w = (1 + (y - 14) * .45).roundToInt(); r(CX - w, y, 2 * w + 1, 1, K) } }
        lit(Ramp(rgb(0xFFF2D8), rgb(0xF2D4A6), rgb(0xC89A70))) { el(CX, 11, 3, 3, K) }
        lit(Ramp(rgb(0xFFFFFF), rgb(0xE8F0F8), rgb(0xB0C0D0))) { for (y in 12..19) { val w = ((19 - y) * .5 + 1).roundToInt(); r(CX - w, y, 2 * w + 1, 1, K) } }
        lit(Ramp(rgb(0xD8F0FF), rgb(0x8AC0E8), rgb(0x4A80B8))) { el(CX, 7, 4, 2, K); p(CX, 4, K) }
        outlineAll(rgb(0x1E3A68))
        p(CX - 1, 10, rgb(0x22305A)); p(CX + 1, 10, rgb(0x22305A))
        p(CX - 2, 11, rgb(0xFF9AB0)); p(CX + 2, 11, rgb(0xFF9AB0))
        line(CX + 6, 8, CX + 6, 25, rgb(0xA8D8F8))
        for ((dx, dy) in listOf(0 to -1, 1 to 0, -1 to 0, 0 to 1)) p(CX + 6 + dx, 8 + dy, rgb(0xFFFFFF))
        if (f % 2 == 0) p(CX + 6, 8, rgb(0xFFFFFF)) else p(CX + 6, 8, rgb(0xA8D8F8))
    }

    private fun hare(b: Buf, f: Int) = with(b) {
        val jump = intArrayOf(0, 2, 3, 1)[f]
        lit(Ramp(rgb(0xFFFBD0), rgb(0xFFE070), rgb(0xE0A830))) {
            el(CX + 1, 19 - jump, 6, 4, K)
            el(CX - 5, 15 - jump, 3, 3, K)
            r(CX - 7, 7 - jump, 2, 7, K); r(CX - 4, 6 - jump, 2, 8, K)
            el(CX + 7, 18 - jump, 2, 2, K)
        }
        outlineAll(rgb(0xB06A10))
        p(CX - 6, 14 - jump, rgb(0x3A2A10)); p(CX - 8, 16 - jump, rgb(0xFF9AB0))
        glow(CX, 16, 13, rgb(0xFFF0A0), .25f, 3)
    }
}
