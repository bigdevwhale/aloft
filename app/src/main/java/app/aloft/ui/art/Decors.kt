package app.aloft.ui.art

import app.aloft.domain.Decor
import kotlin.math.roundToInt
import kotlin.math.sin

/** The keepsakes from the voyage and the islanders' thanks, each in its own spot on the island. */
object Decors {
    /** Where each one stands, as a fraction of the island's half-width from the centre. */
    private val at = mapOf(
        Decor.FEATHER to -.95, Decor.TELESCOPE to -.62, Decor.OAK to -.05, Decor.LANTERN to .7,
        Decor.CRYSTAL to .28, Decor.KITE to .1, Decor.APPLE to .52, Decor.WELL to -.42,
        Decor.AURORA to .88, Decor.HIVE to .38, Decor.ORRERY to -.75, Decor.NEST to -.2,
        Decor.TENT to -.5, Decor.SIGNPOST to .96, Decor.BIRDHOUSE to -.28, Decor.SWING to .6,
        Decor.BELL to -.88, Decor.WINDVANE to .15,
    )

    fun drawAll(b: Buf, set: Set<Decor>, cx: Int, y: Int, rw: Int, dep: Int, t: Int, time: TimeOfDay) {
        for (d in set) {
            val x = cx + ((at[d] ?: 0.0) * rw).roundToInt()
            draw(b, d, x, y, t, time)
        }
    }

    /** One keepsake standing at (x, foot). */
    fun draw(b: Buf, d: Decor, x: Int, foot: Int, t: Int, time: TimeOfDay = TimeOfDay.DAY) = with(b) {
        val wood = rgb(0x6A4228)
        val night = time == TimeOfDay.NIGHT || time == TimeOfDay.EVENING
        when (d) {
            Decor.FEATHER -> {
                line(x, foot, x, foot - 14, wood)
                val f = (t shr 3) % 2
                r(x + 1, foot - 14, 4, 1, rgb(0xFFFFFF)); r(x + 1, foot - 13, 5 + f, 1, rgb(0xE8ECF4)); r(x + 2, foot - 12, 3, 1, rgb(0xC8D0E0))
            }
            Decor.TELESCOPE -> {
                line(x - 2, foot, x, foot - 4, wood); line(x + 2, foot, x, foot - 4, wood)
                line(x - 3, foot - 4, x + 4, foot - 8, rgb(0xC89A40)); line(x - 3, foot - 5, x + 4, foot - 9, rgb(0xE8C060))
                p(x + 4, foot - 8, rgb(0x8AD0FF))
            }
            Decor.OAK -> {
                r(x, foot - 4, 1, 4, wood)
                el(x, foot - 6, 2, 2, rgb(0x3A7A30)); p(x - 1, foot - 7, rgb(0x6AB048))
            }
            Decor.LANTERN -> {
                line(x, foot, x, foot - 10, rgb(0x3A2A20)); line(x, foot - 10, x + 3, foot - 10, rgb(0x3A2A20))
                r(x + 2, foot - 9, 3, 4, rgb(0x3A2A20)); r(x + 3, foot - 8, 1, 2, rgb(0xFFD860))
                if (night) glow(x + 3, foot - 7, 7, rgb(0xFFD860), .45f, 4)
            }
            Decor.CRYSTAL -> {
                p(x, foot - 7, rgb(0xFFFFFF)); r(x - 1, foot - 6, 3, 2, rgb(0xC8F0FF)); r(x - 1, foot - 4, 3, 2, rgb(0x8AC0F0)); r(x - 2, foot - 2, 5, 2, rgb(0x5A8AD8))
                if ((t shr 2) % 6 == 0) p(x + 1, foot - 6, rgb(0xFFFFFF))
            }
            Decor.KITE -> {
                val kx = x + 8 + (sin(t / 9.0) * 2).roundToInt()
                val ky = foot - 34 + (sin(t / 13.0) * 2).roundToInt()
                line(x, foot, kx, ky + 3, rgb(0xF4ECD8), .7f)
                r(kx - 1, ky, 3, 1, rgb(0xE85050)); r(kx - 2, ky + 1, 5, 1, rgb(0xF0C048)); r(kx - 1, ky + 2, 3, 1, rgb(0xE85050)); p(kx, ky + 3, rgb(0xE85050))
                p(kx, ky + 4, rgb(0x5090E0)); p(kx - 1, ky + 5, rgb(0xF0C048))
            }
            Decor.APPLE -> {
                r(x, foot - 5, 2, 5, wood)
                el(x + 1, foot - 8, 4, 3, rgb(0x4A9A38)); el(x, foot - 9, 3, 2, rgb(0x7AC850))
                for ((dx, dy) in listOf(-2 to -8, 2 to -7, 0 to -10, 3 to -9)) p(x + dx, foot + dy, rgb(0xE84A3A))
            }
            Decor.WELL -> {
                r(x - 3, foot - 3, 7, 3, rgb(0xA89684)); r(x - 2, foot - 3, 5, 1, rgb(0xF4F8FF))
                line(x - 3, foot - 3, x - 3, foot - 8, wood); line(x + 3, foot - 3, x + 3, foot - 8, wood); r(x - 4, foot - 9, 9, 1, rgb(0xB84A3A))
            }
            Decor.AURORA -> {
                r(x - 2, foot - 6, 4, 6, rgb(0xC8E8F0)); r(x - 2, foot - 7, 4, 1, rgb(0x8A6A40))
                r(x - 1, foot - 5, 2, 4, if ((t shr 3) % 2 == 0) rgb(0x7AF0A0) else rgb(0x5AC0E0))
                if (night) glow(x, foot - 3, 6, rgb(0x7AF0A0), .35f, 3)
            }
            Decor.HIVE -> {
                for (i in 0..3) r(x - 3 + (if (i == 0 || i == 3) 1 else 0), foot - 1 - i * 2, 7 - (if (i == 0 || i == 3) 2 else 0), 2, if (i % 2 == 0) rgb(0xE8B840) else rgb(0xC8902A))
                p(x, foot - 2, rgb(0x3A2A10))
                val bx = x + 4 + (t shr 1) % 3
                p(bx, foot - 6 - (t shr 2) % 2, rgb(0x2A2010))
            }
            Decor.ORRERY -> {
                r(x - 1, foot - 2, 3, 2, rgb(0x8A6A40)); line(x, foot - 2, x, foot - 6, rgb(0xC89A40))
                p(x, foot - 7, rgb(0xFFD860))
                val a = t / 10.0
                p(x + (kotlin.math.cos(a) * 4).roundToInt(), foot - 7 + (sin(a) * 2).roundToInt(), rgb(0x8AD0FF))
                p(x + (kotlin.math.cos(-a * .6) * 3).roundToInt(), foot - 7 + (sin(-a * .6) * 1).roundToInt(), rgb(0xE85050))
            }
            Decor.NEST -> {
                line(x, foot, x, foot - 9, wood)
                el(x, foot - 10, 3, 1, rgb(0x8A6A40)); p(x - 1, foot - 11, rgb(0x2A2A3A)); p(x + 1, foot - 11, rgb(0x2A2A3A))
            }
            Decor.TENT -> {
                for (i in 0 until 7) r(x - i, foot - 7 + i, 2 * i + 1, 1, if (i % 2 == 0) rgb(0xE85050) else rgb(0xF4ECD8))
                r(x, foot - 3, 1, 3, rgb(0x3A2A20)); p(x, foot - 8, rgb(0xF0C048))
            }
            Decor.SIGNPOST -> {
                line(x, foot, x, foot - 11, wood)
                r(x - 4, foot - 10, 5, 2, rgb(0xC89A60)); p(x - 5, foot - 9, rgb(0xC89A60))
                r(x + 1, foot - 7, 5, 2, rgb(0xC89A60)); p(x + 6, foot - 6, rgb(0xC89A60))
            }
            Decor.BIRDHOUSE -> {
                line(x, foot, x, foot - 8, wood)
                r(x - 2, foot - 12, 5, 4, rgb(0xC89A60)); r(x - 3, foot - 13, 7, 1, rgb(0xB84A3A)); p(x, foot - 11, rgb(0x2A1A10))
            }
            Decor.SWING -> {
                line(x - 3, foot - 12, x + 4, foot - 12, wood)
                val s = (sin(t / 7.0) * 2).roundToInt()
                line(x - 2, foot - 12, x - 2 + s, foot - 4, rgb(0xD8CCB4)); line(x + 3, foot - 12, x + 3 + s, foot - 4, rgb(0xD8CCB4))
                r(x - 2 + s, foot - 4, 6, 1, wood)
            }
            Decor.BELL -> {
                line(x - 3, foot, x - 3, foot - 10, wood); line(x + 3, foot, x + 3, foot - 10, wood); r(x - 4, foot - 11, 9, 1, wood)
                val s = if ((t shr 3) % 4 == 0) 1 else 0
                r(x - 1 + s, foot - 10, 3, 1, rgb(0xE8B840)); r(x - 2 + s, foot - 9, 5, 2, rgb(0xE8B840)); r(x - 2 + s, foot - 7, 5, 1, rgb(0xB07C1C))
            }
            Decor.WINDVANE -> {
                line(x, foot, x, foot - 12, rgb(0x3A2A20))
                val flip = (t shr 4) % 2 == 0
                val dx = if (flip) 1 else -1
                r(x + (if (flip) 0 else -4), foot - 12, 5, 1, rgb(0xE84A2A)); p(x + dx * 5, foot - 13, rgb(0xF0C048))
            }
        }
    }
}
