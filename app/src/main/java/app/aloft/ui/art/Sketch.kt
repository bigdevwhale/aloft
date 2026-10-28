package app.aloft.ui.art

import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.math.sqrt

/** Light, base and shadow of one material. */
class Ramp(val l: Int, val b: Int, val d: Int)

/** A filled ellipse by whole rows, the way the prototype drew it; rows are centred on a fractional [cx]. */
fun Buf.el(cx: Double, cy: Double, rx: Int, ry: Int, c: Int) {
    for (y in -ry..ry) {
        val w = (rx * sqrt(max(0.0, 1 - (y * y) / ((ry + .5) * (ry + .5))))).roundToInt()
        val x0 = (cx - w).roundToInt()
        val yy = (cy + y).roundToInt()
        for (i in 0..2 * w) p(x0 + i, yy, c)
    }
}

fun Buf.el(cx: Int, cy: Int, rx: Int, ry: Int, c: Int) = el(cx.toDouble(), cy.toDouble(), rx, ry, c)

/**
 * Recolours every opaque pixel by where it sits in its shape: shadow on the lower-right edge,
 * light on the upper-left edge, base inside.
 */
fun Buf.shade(r: Ramp) {
    val m = BooleanArray(w * h) { d[it] ushr 24 != 0 }
    fun f(x: Int, y: Int) = x in 0 until w && y in 0 until h && m[y * w + x]
    for (y in 0 until h) for (x in 0 until w) {
        if (!f(x, y)) continue
        val dk = !f(x + 1, y) || !f(x, y + 1) || !f(x + 1, y + 1)
        val lt = !f(x - 1, y) || !f(x, y - 1) || !f(x - 1, y - 1)
        d[y * w + x] = if (dk) r.d else if (lt) r.l else r.b
    }
}

/** Draws a shape with [draw] as a mask, shades it with [r] and lays it over this buffer. */
inline fun Buf.lit(r: Ramp, draw: Buf.() -> Unit) {
    val t = Buf(w, h)
    t.draw()
    t.shade(r)
    blit(t, 0, 0)
}

/** Mixes every opaque pixel towards [c] by [t]. */
fun Buf.tint(c: Int, t: Float) {
    if (t <= 0f) return
    for (i in d.indices) if (d[i] ushr 24 != 0) d[i] = mix(d[i], c, t)
}

/** Multiplies the alpha of every pixel by [a]. */
fun Buf.fade(a: Float) {
    for (i in d.indices) {
        val c = d[i]
        if (c ushr 24 == 0) continue
        d[i] = (c and 0xFFFFFF) or (((c ushr 24) * a).toInt().coerceIn(1, 255) shl 24)
    }
}

const val K = 0xFF000000.toInt()

/** Like [blit], but honours each pixel's own alpha. */
fun Buf.blitA(src: Buf, x: Int, y: Int, alpha: Float = 1f) {
    for (j in 0 until src.h) for (i in 0 until src.w) {
        val c = src.d[j * src.w + i]
        val a = (c ushr 24) / 255f * alpha
        if (a > 0f) p(x + i, y + j, c, a)
    }
}
