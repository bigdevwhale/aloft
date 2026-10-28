package app.aloft.ui.isle

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.aloft.R
import app.aloft.container
import app.aloft.domain.Element
import app.aloft.domain.Sport
import app.aloft.domain.Wx
import app.aloft.ui.Home
import app.aloft.ui.Words
import app.aloft.ui.components.Chip
import app.aloft.ui.components.Eyebrow
import app.aloft.ui.components.Field
import app.aloft.ui.components.Kind
import app.aloft.ui.components.Page
import app.aloft.ui.components.Panel
import app.aloft.ui.components.PixelButton
import app.aloft.ui.theme.C
import app.aloft.ui.theme.T
import kotlinx.coroutines.launch
import java.time.LocalTime

/** When a training ended, roughly: the hour matters to the dawn and midnight guests. */
private enum class When { NOW, HOUR, MORNING, EVENING, YESTERDAY }

/** Logging a training: what, how long, how hard, the weather, when. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LogDialog(h: Home, onClose: () -> Unit, onLogged: (Element) -> Unit) {
    val ctx = LocalContext.current
    val c = ctx.container
    val scope = rememberCoroutineScope()
    var sport by remember { mutableStateOf<Sport?>(null) }
    var minutes by remember { mutableIntStateOf(45) }
    var rpe by remember { mutableIntStateOf(6) }
    var wx by remember { mutableStateOf(h.wx) }
    var whenAt by remember { mutableStateOf(When.NOW) }
    var adding by remember { mutableStateOf(false) }
    var ownName by remember { mutableStateOf("") }
    var ownEl by remember { mutableStateOf<Element?>(null) }
    var err by remember { mutableStateOf<String?>(null) }

    Page(stringResource(R.string.log_title), onBack = onClose) {
        Eyebrow(stringResource(R.string.log_what))
        for (e in Element.entries) {
            Row(verticalAlignment = Alignment.Top) {
                Box(Modifier.padding(top = 12.dp, end = 8.dp).size(10.dp).background(C.element(e)))
                FlowRow(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    for (s in h.sports.filter { it.element == e }) Chip(s.name.of(h.ru) + if (s.own) " ✦" else "", sport?.key == s.key, { sport = s })
                }
            }
        }
        if (!adding) Chip(stringResource(R.string.own_add), false, { adding = true })
        else Panel(Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.own_title), style = T.Heading)
            Field(ownName, { ownName = it; err = null }, stringResource(R.string.own_hint))
            Text(stringResource(R.string.own_whose), style = T.Small, modifier = Modifier.padding(top = 6.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                for (e in Element.entries) Chip(e.spirit.of(h.ru), ownEl == e, { ownEl = e; err = null })
            }
            Text(err ?: ownEl?.let { ctx.getString(ELEMENT_HINTS.getValue(it)) } ?: stringResource(R.string.own_pick), style = T.Small.copy(color = if (err != null) C.redDeep else C.muted))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PixelButton(stringResource(R.string.add), {
                    val name = ownName.trim().replace(Regex("\\s+"), " ").replaceFirstChar { it.uppercase() }
                    val e = ownEl
                    err = when {
                        name.isEmpty() -> ctx.getString(R.string.own_err_name)
                        h.sports.any { it.name.of(h.ru).equals(name, true) } -> ctx.getString(R.string.own_err_dup)
                        e == null -> ctx.getString(R.string.own_pick)
                        else -> null
                    }
                    if (err == null && e != null) scope.launch {
                        sport = c.island.addOwn(name, e)
                        adding = false; ownName = ""; ownEl = null
                    }
                })
                PixelButton(stringResource(R.string.cancel), { adding = false; err = null }, kind = Kind.GHOST)
            }
        }

        Eyebrow(stringResource(R.string.log_long), Modifier.padding(top = 6.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            for (m in listOf(15, 30, 45, 60, 90, 120)) Chip(Words.minutes(ctx, m), minutes == m, { minutes = m })
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            PixelButton("−5", { minutes = (minutes - 5).coerceAtLeast(5) }, kind = Kind.GHOST)
            Text(Words.minutes(ctx, minutes), style = T.Heading)
            PixelButton("+5", { minutes = (minutes + 5).coerceAtMost(600) }, kind = Kind.GHOST)
        }

        Eyebrow(stringResource(R.string.log_hard), Modifier.padding(top = 6.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            for (n in 1..10) Chip("$n", rpe == n, { rpe = n })
        }
        Text(Words.rpe(ctx, rpe), style = T.Small)

        Eyebrow(stringResource(R.string.log_wx), Modifier.padding(top = 6.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            for (w in Wx.entries) Chip(Words.wx(ctx, w), wx == w, { wx = if (wx == w) null else w })
        }
        if (h.wx != null && h.temp != null) Text(stringResource(R.string.log_wx_now, Words.wx(ctx, h.wx), h.temp.toInt()), style = T.Small)

        Eyebrow(stringResource(R.string.log_when), Modifier.padding(top = 6.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            val names = listOf(R.string.when_now, R.string.when_hour, R.string.when_morning, R.string.when_evening, R.string.when_yesterday)
            When.entries.forEachIndexed { i, w -> Chip(stringResource(names[i]), whenAt == w, { whenAt = w }) }
        }

        val s = sport
        Text(if (s != null) stringResource(R.string.log_flies, s.element.spirit.of(h.ru)) else stringResource(R.string.log_pick), style = T.Body.copy(color = C.muted), modifier = Modifier.padding(top = 8.dp))
        PixelButton(stringResource(R.string.log_go), {
            val sp = sport ?: return@PixelButton
            val at = when (whenAt) {
                When.NOW -> h.nowMs
                When.HOUR -> h.nowMs - 3_600_000L
                When.MORNING -> h.today.atTime(LocalTime.of(8, 30)).atZone(h.zone).toInstant().toEpochMilli().coerceAtMost(h.nowMs)
                When.EVENING -> h.today.atTime(LocalTime.of(20, 0)).atZone(h.zone).toInstant().toEpochMilli().coerceAtMost(h.nowMs)
                When.YESTERDAY -> h.today.minusDays(1).atTime(LocalTime.of(19, 0)).atZone(h.zone).toInstant().toEpochMilli()
            }
            val temp = if (whenAt == When.NOW || whenAt == When.HOUR) h.temp else null
            c.write { island.log(sp, sp.name.of(h.ru), minutes, rpe, wx, temp, at) }
            onLogged(sp.element)
        }, Modifier.fillMaxWidth().padding(bottom = 16.dp), kind = Kind.RED, enabled = s != null)
    }
}

val ELEMENT_HINTS = mapOf(
    Element.AIR to R.string.hint_air,
    Element.EARTH to R.string.hint_earth,
    Element.WATER to R.string.hint_water,
    Element.WOOD to R.string.hint_wood,
    Element.FIRE to R.string.hint_fire,
)
