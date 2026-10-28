package app.aloft.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import app.aloft.R
import app.aloft.domain.Element

/** Night sky around the island, cream paper for the words, gold for what matters. */
@Immutable
object C {
    val bg = Color(0xFF1A2450)
    val sky = Color(0xFF2A3A78)
    val skyDeep = Color(0xFF10163A)
    val skyLight = Color(0xFF4A5CA0)
    val paper = Color(0xFFF4ECD8)
    val paper2 = Color(0xFFD8CCB0)
    /** The background of full pages over the tabs. */
    val page = Color(0xFFEBE0C6)
    val ink = Color(0xFF141A36)
    val muted = Color(0xFF7A7060)
    val faint = Color(0xFF8A90B8)
    val frame = Color(0xFF2A3A78)
    val red = Color(0xFFE8607A)
    val redDeep = Color(0xFFA03050)
    val redLight = Color(0xFFFFA0B0)
    val green = Color(0xFFF0C048)
    val greenDeep = Color(0xFFB07C1C)
    val gold = Color(0xFFF0C048)
    val goldDeep = Color(0xFFB07C1C)
    val goldLight = Color(0xFFFFF0A0)
    val calm = Color(0xFFD8E4F0)

    fun element(e: Element) = Color(e.color)

    /** An element's colour darkened enough to read on paper. */
    fun elementInk(e: Element) = Color(e.color).let { Color(it.red * .55f + .08f * .45f, it.green * .55f + .1f * .45f, it.blue * .55f + .21f * .45f) }
}

/** m6x11 by Daniel Linssen with Cyrillic by Natto Games (free with attribution): one family, sizes set the hierarchy. */
val Pixel = FontFamily(Font(R.font.m6x11, FontWeight.Normal))

object T {
    val Display = TextStyle(fontFamily = Pixel, fontSize = 32.sp, lineHeight = 31.sp, color = C.ink)
    val Title = TextStyle(fontFamily = Pixel, fontSize = 27.sp, lineHeight = 27.sp, color = C.ink)
    val Heading = TextStyle(fontFamily = Pixel, fontSize = 23.sp, lineHeight = 24.sp, color = C.ink)
    val Body = TextStyle(fontFamily = Pixel, fontSize = 20.sp, lineHeight = 21.sp, color = C.ink)
    val Say = TextStyle(fontFamily = Pixel, fontSize = 22.sp, lineHeight = 23.sp, color = C.ink)
    val Small = TextStyle(fontFamily = Pixel, fontSize = 16.sp, lineHeight = 17.sp, color = C.muted)
    val Eyebrow = TextStyle(fontFamily = Pixel, fontSize = 16.sp, lineHeight = 17.sp, letterSpacing = 0.04.em, color = C.muted)
    val Button = TextStyle(fontFamily = Pixel, fontSize = 22.sp, lineHeight = 22.sp)
    val Number = TextStyle(fontFamily = Pixel, fontSize = 28.sp, lineHeight = 26.sp, color = C.ink)
}

@Composable
fun AloftTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = C.gold, onPrimary = C.ink, background = C.bg, surface = C.paper, onSurface = C.ink,
            onBackground = C.paper, surfaceContainer = C.paper, surfaceContainerHigh = C.paper, surfaceContainerHighest = C.paper2,
            outline = C.ink, error = C.red,
        ),
        typography = MaterialTheme.typography.copy(bodyLarge = T.Body, bodyMedium = T.Body, bodySmall = T.Small, labelLarge = T.Button, titleLarge = T.Title),
        content = content,
    )
}
