package app.aloft.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import app.aloft.domain.Element
import app.aloft.domain.Guest
import app.aloft.ui.art.Art
import app.aloft.ui.art.Buf
import app.aloft.ui.art.Guests
import app.aloft.ui.art.Spirits
import app.aloft.ui.art.silhouette
import app.aloft.ui.art.toBitmap
import app.aloft.ui.theme.C
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

/** A frame counter 0..3 that ticks while on screen (still for screenshots). */
@Composable
fun frame(ms: Long = 260): Int {
    var f by remember { mutableIntStateOf(1) }
    if (!Art.still) LaunchedEffect(Unit) { while (true) { delay(ms); f = (f + 1) and 3 } }
    return f
}

/** A pixel picture, [scale] dp per art pixel. */
@Composable
fun Pixels(buf: Buf, modifier: Modifier = Modifier, scale: Dp = 2.dp) {
    val image = remember(buf) { buf.toBitmap().asImageBitmap() }
    Canvas(modifier.size(scale * buf.w, scale * buf.h)) {
        drawImage(image, srcOffset = IntOffset.Zero, srcSize = IntSize(buf.w, buf.h), dstOffset = IntOffset.Zero,
            dstSize = IntSize(size.width.roundToInt(), size.height.roundToInt()), filterQuality = FilterQuality.None)
    }
}

/** A spirit breathing in place. */
@Composable
fun SpiritIcon(e: Element, level: Double, scale: Dp = 2.dp, sleep: Boolean = false, modifier: Modifier = Modifier) {
    val f = frame()
    Pixels(Spirits.draw(e, maxOf(level, .12), f, sleep, if (level < .12) .5f else 0f), modifier, scale)
}

/** A guest, or its pale shadow if never met. */
@Composable
fun GuestIcon(g: Guest, met: Boolean, scale: Dp = 2.dp, modifier: Modifier = Modifier) {
    val f = frame()
    val b = remember(g, met, f) { if (met) Guests.draw(g, f) else Buf(Guests.W, Guests.H).also { it.blit(Guests.draw(g, 0)); it.silhouette(app.aloft.ui.art.rgb(0xC8BCA0)) } }
    Pixels(b, modifier, scale)
}

private fun Buf.blit(src: Buf) { for (i in d.indices) if (src.d[i] ushr 24 != 0) d[i] = src.d[i] or 0xFF000000.toInt() }

/** Ten pixel cells filled by [v] in [color]. */
@Composable
fun Cells(v: Double, color: Color, modifier: Modifier = Modifier, n: Int = 10, cell: Dp = 5.dp) {
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(1.dp)) {
        val on = (v * n).roundToInt()
        for (i in 0 until n) Box(Modifier.width(cell).height(7.dp).background(if (i < on) color else C.sky))
    }
}

