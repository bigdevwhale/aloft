package app.aloft.ui.spirits

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.aloft.R
import app.aloft.container
import app.aloft.domain.Element
import app.aloft.domain.Guest
import app.aloft.domain.Mode
import app.aloft.domain.Stage
import app.aloft.domain.Tx
import app.aloft.ui.Home
import app.aloft.ui.Nav
import app.aloft.ui.Overlay
import app.aloft.ui.Words
import app.aloft.ui.components.Cells
import app.aloft.ui.components.Chip
import app.aloft.ui.components.Dialog
import app.aloft.ui.components.Eyebrow
import app.aloft.ui.components.GuestIcon
import app.aloft.ui.components.Page
import app.aloft.ui.components.Panel
import app.aloft.ui.components.PixelButton
import app.aloft.ui.components.SpiritIcon
import app.aloft.ui.theme.C
import app.aloft.ui.theme.T

val CHARACTER = mapOf(
    Element.AIR to Tx("Never tires and always calls you out for a run. Holds the island up in the sky.", "Носится без устали и вечно зовёт на пробежку. Держит остров в небе."),
    Element.EARTH to Tx("Quiet. Every weight you lift is a stone in the island's foundations.", "Молчалив. Каждый поднятый тобой вес — камень в основании острова."),
    Element.WATER to Tx("Giggly. Without her the springs dry up and the grass turns yellow.", "Смешливая. Без неё пересыхают родники и желтеет трава."),
    Element.WOOD to Tx("Unhurried. Makes sure nothing on the island goes stiff — except the trees.", "Неторопливая. Следит, чтобы на острове ничего не одеревенело — кроме деревьев."),
    Element.FIRE to Tx("Hot-tempered. Loves the heat, but is the first to notice when it's time to cool down.", "Вспыльчивая. Любит жару, но первой замечает, когда пора остыть."),
)

/** The five spirits and the guests from the weather. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SpiritsScreen(h: Home, nav: Nav) {
    val ctx = LocalContext.current
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 10.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        for (e in Element.entries) {
            val v = h.isle.of(e)
            Panel(Modifier.fillMaxWidth(), onClick = { nav.push(Overlay.Spirit(e)) }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SpiritIcon(e, v, scale = 3.dp, sleep = h.isle.mode == Mode.CALM)
                    Column(Modifier.weight(1f).padding(start = 10.dp)) {
                        Text(e.spirit.of(h.ru), style = T.Title.copy(color = C.elementInk(e)))
                        Text("${Words.stage(ctx, Stage.of(v))} · ${e.word.of(h.ru).lowercase()}", style = T.Small)
                        Cells(v, C.element(e), Modifier.padding(top = 4.dp))
                    }
                }
                Text(CHARACTER.getValue(e).of(h.ru), style = T.Small.copy(color = C.ink), modifier = Modifier.padding(top = 6.dp))
            }
        }
        Eyebrow(stringResource(R.string.guests_title, h.met.size, Guest.entries.size), Modifier.padding(top = 6.dp), color = C.gold)
        Text(stringResource(R.string.guests_hint), style = T.Small.copy(color = C.faint))
        FlowRow(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp), maxItemsInEachRow = 3) {
            for (g in Guest.entries) {
                val met = g in h.met
                Panel(Modifier.weight(1f), fill = if (met) C.paper else C.paper2, onClick = { nav.push(Overlay.GuestCard(g)) }) {
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { GuestIcon(g, met, scale = 2.dp) }
                    Text(if (met) g.title.of(h.ru) else "?", style = T.Small.copy(color = C.ink), textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth(), maxLines = 2)
                }
            }
        }
        Box(Modifier.height(12.dp))
    }
}

/** One spirit up close: what it is, what feeds it, how it has done lately. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SpiritPage(h: Home, e: Element, nav: Nav) {
    val ctx = LocalContext.current
    val c = ctx.container
    val v = h.isle.of(e)
    val month = h.g.workouts.filter { Element.of(it.element) == e && h.nowMs - it.at < 28L * 86_400_000 }
    Page(e.spirit.of(h.ru), onBack = nav.pop) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            SpiritIcon(e, v, scale = 5.dp, sleep = h.isle.mode == Mode.CALM)
            Column(Modifier.padding(start = 12.dp)) {
                Text(e.spirit.of(h.ru), style = T.Display.copy(color = C.elementInk(e)))
                Text("${Words.stage(ctx, Stage.of(v))} · ${e.word.of(h.ru).lowercase()}", style = T.Small)
                Cells(v, C.element(e), Modifier.padding(top = 6.dp))
            }
        }
        Text(CHARACTER.getValue(e).of(h.ru), style = T.Body)
        Text(ctx.getString(app.aloft.ui.isle.ELEMENT_HINTS.getValue(e)), style = T.Small)
        Eyebrow(stringResource(if (e.female) R.string.feeds_her else R.string.feeds_him))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            for (s in h.sports.filter { it.element == e }) Chip(s.name.of(h.ru) + if (s.own) " ✕" else "", false, {
                if (s.own) h.g.own.firstOrNull { it.key == s.key }?.let { o -> c.write { island.hideOwn(o) } }
            })
        }
        if (h.sports.any { it.element == e && it.own }) Text(stringResource(R.string.own_remove_hint), style = T.Small)
        Eyebrow(stringResource(R.string.four_weeks))
        Text(if (month.isEmpty()) stringResource(R.string.four_weeks_none) else stringResource(R.string.four_weeks_n, month.size, Words.hours(month.sumOf { it.minutes })), style = T.Body)
        Text(stringResource(R.string.stage_hint), style = T.Small)
    }
}

@Composable
fun GuestDialog(h: Home, g: Guest, onClose: () -> Unit) {
    val met = g in h.met
    val visits = h.g.meetings.firstOrNull { it.guest == g.name }?.count ?: 0
    Dialog(onDismiss = onClose) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            GuestIcon(g, met, scale = 4.dp)
            Column(Modifier.padding(start = 10.dp)) {
                Text(if (met) g.title.of(h.ru) else "?", style = T.Title)
                if (met) Text(stringResource(R.string.visits_n, visits), style = T.Small)
            }
        }
        if (met) Text(g.about.of(h.ru), style = T.Body)
        Text(stringResource(R.string.guest_when, g.whenTx.of(h.ru)), style = T.Small)
        PixelButton(stringResource(R.string.close), onClose, Modifier.fillMaxWidth())
    }
}
