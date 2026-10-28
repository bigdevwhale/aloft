package app.aloft.ui.journal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.aloft.R
import app.aloft.container
import app.aloft.data.Island
import app.aloft.domain.Element
import app.aloft.domain.Guest
import app.aloft.ui.Home
import app.aloft.ui.Nav
import app.aloft.ui.Overlay
import app.aloft.ui.Talk
import app.aloft.ui.Words
import app.aloft.ui.art.CampfireScene
import app.aloft.ui.art.PixelView
import app.aloft.ui.components.Chip
import app.aloft.ui.components.Dialog
import app.aloft.ui.components.Eyebrow
import app.aloft.ui.components.GuestIcon
import app.aloft.ui.components.Kind
import app.aloft.ui.components.Panel
import app.aloft.ui.components.PixelButton
import app.aloft.ui.theme.C
import app.aloft.ui.theme.T

/** The council by the fire, the week day by day, and every training. */
@Composable
fun JournalScreen(h: Home, nav: Nav) {
    val ctx = LocalContext.current
    val fire = remember(h.isle.level) { CampfireScene(h.isle.level) }
    var shown by remember { mutableIntStateOf(30) }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 10.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Panel(Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.council), style = T.Title, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            Text(stringResource(R.string.council_sub), style = T.Small, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp))
            Box(Modifier.fillMaxWidth().background(C.ink).padding(2.dp)) { PixelView(fire, Modifier.fillMaxWidth()) }
            for (p in remember(h) { Talk.council(h) }) Text(p, style = T.Body, modifier = Modifier.padding(top = 6.dp))
        }
        Panel(Modifier.fillMaxWidth()) {
            Eyebrow(stringResource(R.string.this_week))
            Row(Modifier.fillMaxWidth().padding(top = 6.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                for (k in 0..6) {
                    val d = h.monday.plusDays(k.toLong())
                    val ws = h.g.workouts.filter { Island.day(it.at, h.zone) == d }
                    val calm = h.g.calms.any { it.day == d.toEpochDay() }
                    Column(
                        Modifier.weight(1f).heightIn(min = 54.dp).background(if (calm) C.calm else if (d == h.today) C.paper else C.paper2).padding(3.dp),
                        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        Text(Words.weekday(ctx, d), style = T.Small.copy(color = if (d == h.today) C.ink else C.muted))
                        for (w in ws.take(4)) Box(Modifier.width(16.dp).height(6.dp).background(C.element(Element.of(w.element))))
                        if (calm) Text("~", style = T.Small)
                    }
                }
            }
        }
        Eyebrow(stringResource(R.string.all_trainings), Modifier.padding(top = 4.dp), color = C.gold)
        if (h.g.workouts.isEmpty()) Text(stringResource(R.string.no_trainings), style = T.Body.copy(color = C.paper2))
        var lastDay: java.time.LocalDate? = null
        for (w in h.g.workouts.take(shown)) {
            val d = Island.day(w.at, h.zone)
            if (d != lastDay) { Text(Words.day(ctx, d, h.today), style = T.Small.copy(color = C.faint), modifier = Modifier.padding(top = 4.dp)); lastDay = d }
            Panel(Modifier.fillMaxWidth(), onClick = { nav.push(Overlay.Workout(w.id)) }, padding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(12.dp).background(C.element(Element.of(w.element))))
                    Text("  " + h.sport(w).name.of(h.ru).ifEmpty { w.name }, style = T.Body, modifier = Modifier.weight(1f), maxLines = 1)
                    Guest.of(w.guest)?.let { GuestIcon(it, true, scale = 1.dp) }
                    Text("${Words.minutes(ctx, w.minutes)} · ${w.rpe}/10", style = T.Small, modifier = Modifier.padding(start = 6.dp))
                }
            }
        }
        if (h.g.workouts.size > shown) PixelButton(stringResource(R.string.more), { shown += 30 }, Modifier.fillMaxWidth(), kind = Kind.GHOST)
        Box(Modifier.height(12.dp))
    }
}

/** One training: change how long or how hard, or take it out of the log. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WorkoutDialog(h: Home, id: Long, onClose: () -> Unit) {
    val ctx = LocalContext.current
    val c = ctx.container
    val w = h.g.workouts.firstOrNull { it.id == id } ?: return onClose()
    var minutes by remember { mutableIntStateOf(w.minutes) }
    var rpe by remember { mutableIntStateOf(w.rpe) }
    var sure by remember { mutableStateOf(false) }
    Dialog(onDismiss = onClose) {
        Text(h.sport(w).name.of(h.ru), style = T.Title)
        Text("${Words.day(ctx, Island.day(w.at, h.zone), h.today)}, ${Words.time(w.at)} · ${h.sport(w).element.spirit.of(h.ru)}", style = T.Small)
        Guest.of(w.guest)?.let { Text(stringResource(R.string.guest_came, it.title.of(h.ru)), style = T.Small.copy(color = C.goldDeep)) }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            PixelButton("−5", { minutes = (minutes - 5).coerceAtLeast(5) }, kind = Kind.GHOST)
            Text(Words.minutes(ctx, minutes), style = T.Heading)
            PixelButton("+5", { minutes = (minutes + 5).coerceAtMost(600) }, kind = Kind.GHOST)
        }
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            for (n in 1..10) Chip("$n", rpe == n, { rpe = n })
        }
        Text(Words.rpe(ctx, rpe), style = T.Small)
        PixelButton(stringResource(R.string.save), {
            c.write { island.update(w.copy(minutes = minutes, rpe = rpe)); island.settle() }
            onClose()
        }, Modifier.fillMaxWidth(), enabled = minutes != w.minutes || rpe != w.rpe)
        PixelButton(stringResource(if (sure) R.string.delete_sure else R.string.delete), {
            if (!sure) sure = true else { c.write { island.delete(w) }; onClose() }
        }, Modifier.fillMaxWidth(), kind = Kind.GHOST)
    }
}
