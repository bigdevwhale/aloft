package app.aloft.ui.isle

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.aloft.R
import app.aloft.container
import app.aloft.data.Arrival
import app.aloft.data.Meeting
import app.aloft.data.Thanks
import app.aloft.domain.Element
import app.aloft.domain.Guest
import app.aloft.domain.Journey
import app.aloft.domain.Mode
import app.aloft.domain.Requests
import app.aloft.domain.Stage
import app.aloft.ui.Home
import app.aloft.ui.Line
import app.aloft.ui.Nav
import app.aloft.ui.Orb
import app.aloft.ui.Overlay
import app.aloft.ui.Talk
import app.aloft.ui.Words
import app.aloft.ui.art.IslandScene
import app.aloft.ui.art.PixelView
import app.aloft.ui.components.Cells
import app.aloft.ui.components.Dialog
import app.aloft.ui.components.Eyebrow
import app.aloft.ui.components.GuestIcon
import app.aloft.ui.components.Kind
import app.aloft.ui.components.Panel
import app.aloft.ui.components.PixelButton
import app.aloft.ui.components.SpiritIcon
import app.aloft.ui.components.pixelBox
import app.aloft.ui.components.textBox
import app.aloft.ui.theme.C
import app.aloft.ui.theme.T
import kotlinx.coroutines.delay

@Composable
fun IsleScreen(h: Home, nav: Nav, orb: Orb?) {
    val ctx = LocalContext.current
    val c = ctx.container
    val scene = remember(h.look, orb) { IslandScene(if (orb != null) h.look.copy(orb = orb.e, orbId = orb.id) else h.look) }
    val lines = remember(h) { Talk.lines(h) }
    var i by remember(h) { mutableIntStateOf(0) }
    var said by remember { mutableStateOf<Line?>(null) }
    LaunchedEffect(orb) {
        if (orb == null) return@LaunchedEffect
        delay(2600)
        said = Talk.caught(orb.e, h.ru, orb.id)
        delay(6000)
        said = null
    }
    val line = said ?: lines.getOrNull(i % lines.size.coerceAtLeast(1))
    var showRequests by remember { mutableStateOf(false) }
    var showRest by remember { mutableStateOf(false) }
    // Everything fits on one screen: the island takes whatever height is left, letterboxed if short.
    Column(Modifier.fillMaxSize().padding(horizontal = 10.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
            Box(Modifier.background(C.ink).padding(3.dp)) {
                PixelView(scene, Modifier.pointerInput(scene) {
                    detectTapGestures { p ->
                        val x = p.x / size.width * IslandScene.W
                        val y = p.y / size.height * IslandScene.H
                        val hit = scene.spots.entries.firstOrNull { (_, s) -> kotlin.math.abs(x - s.first) < 11 && y > s.second - 24 && y < s.second + 3 }
                        if (hit != null) nav.push(Overlay.Spirit(hit.key)) else { said = null; i++ }
                    }
                }, fitHeight = true)
            }
        }
        if (line != null) Column(Modifier.fillMaxWidth().height(86.dp).textBox().clickable { said = null; i++ }.padding(horizontal = 14.dp, vertical = 9.dp)) {
            Text(line.who.uppercase(), style = T.Small.copy(color = line.color), maxLines = 1)
            Text(line.text, style = T.Body, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            for (e in Element.entries) {
                val v = h.isle.of(e)
                Column(
                    Modifier.weight(1f).pixelBox(C.skyDeep, bevel = null, lip = null).clickable { nav.push(Overlay.Spirit(e)) }.padding(vertical = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    SpiritIcon(e, v, scale = 1.5.dp, sleep = h.isle.mode == Mode.CALM)
                    Text(e.word.of(h.ru), style = T.Small.copy(color = C.paper), maxLines = 1)
                    Cells(v, C.element(e), Modifier.padding(top = 2.dp), cell = 4.dp)
                }
            }
        }
        val done = h.requests.count { it.done }
        Row(Modifier.fillMaxWidth().clickable { showRequests = true }.padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            for (a in h.requests) Box(Modifier.padding(end = 4.dp).size(10.dp).background(if (a.done) C.gold else C.sky))
            Text(" " + stringResource(R.string.requests_strip, done, h.requests.size), style = T.Body.copy(color = C.paper2), modifier = Modifier.weight(1f), maxLines = 1)
            Text("›", style = T.Heading.copy(color = C.gold))
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            PixelButton(stringResource(R.string.log_training), { nav.push(Overlay.Log) }, Modifier.weight(1.4f), kind = Kind.RED)
            PixelButton(stringResource(when { h.isle.mode == Mode.ANCHOR -> R.string.anchored; h.calmToday -> R.string.calm_today; else -> R.string.calm_on }), { showRest = true }, Modifier.weight(1f), kind = Kind.GHOST)
        }
    }
    if (showRequests) Dialog(onDismiss = { showRequests = false }) {
        Text(stringResource(R.string.requests_title), style = T.Title)
        for (a in h.requests) {
            val who = a.req.who
            Panel(Modifier.fillMaxWidth(), fill = if (a.done) C.gold else C.paper) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(10.dp).background(who.element?.let { C.element(it) } ?: C.paper2))
                    Text("  " + who.title.of(h.ru), style = T.Small.copy(color = C.ink), modifier = Modifier.weight(1f))
                    Text(if (a.done) stringResource(R.string.req_done) else "${a.have.coerceAtMost(a.req.goal)}/${a.req.goal}", style = T.Small.copy(color = C.ink))
                }
                Text(if (a.done) a.req.thanks.of(h.ru) else a.req.text.of(h.ru), style = T.Body)
            }
        }
        Text(stringResource(R.string.requests_hint), style = T.Small)
        PixelButton(stringResource(R.string.close), { showRequests = false }, Modifier.fillMaxWidth(), kind = Kind.GHOST)
    }
    if (showRest) Dialog(onDismiss = { showRest = false }) {
        val anchored = h.isle.mode == Mode.ANCHOR
        Text(stringResource(R.string.rest_title), style = T.Title)
        Text(stringResource(R.string.rest_hint), style = T.Body)
        PixelButton(stringResource(if (h.calmToday) R.string.calm_off else R.string.calm_set), { c.write { island.calm(h.today, !h.calmToday) }; showRest = false }, Modifier.fillMaxWidth())
        PixelButton(stringResource(if (anchored) R.string.anchor_up else R.string.anchor_down), { c.write { island.anchor(!anchored) }; showRest = false }, Modifier.fillMaxWidth(), kind = Kind.GHOST)
    }
    h.unread.firstOrNull()?.let { u -> Unread(h, u) }
}

/** Arrivals, new guests and thanks are told one at a time, as they happen. */
@Composable
private fun Unread(h: Home, u: Any) {
    val c = LocalContext.current.container
    when (u) {
        is Arrival -> {
            val st = Journey.route(u.season).getOrNull(u.stop) ?: return
            Dialog(onDismiss = { c.write { island.read(u) } }) {
                Eyebrow(stringResource(R.string.arrived))
                Text(st.name.of(h.ru), style = T.Title)
                Text(st.letter.of(h.ru), style = T.Body)
                if (st.gift.en.isNotEmpty()) Text(stringResource(R.string.gift, st.gift.of(h.ru)), style = T.Body.copy(color = C.goldDeep))
                PixelButton(stringResource(R.string.onward), { c.write { island.read(u) } }, Modifier.fillMaxWidth())
            }
        }
        is Meeting -> {
            val g = Guest.of(u.guest) ?: return
            Dialog(onDismiss = { c.write { island.read(u) } }) {
                Eyebrow(stringResource(R.string.new_guest))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    GuestIcon(g, true, scale = 4.dp)
                    Text(g.title.of(h.ru), style = T.Title, modifier = Modifier.padding(start = 10.dp))
                }
                Text(g.about.of(h.ru), style = T.Body)
                Text(stringResource(R.string.guest_when, g.whenTx.of(h.ru)), style = T.Small)
                PixelButton(stringResource(R.string.welcome), { c.write { island.read(u) } }, Modifier.fillMaxWidth())
            }
        }
        is Thanks -> {
            val r = Requests.byId(u.req) ?: run { c.write { island.read(u) }; return }
            val n = h.g.thanks.count { it.at <= u.at }
            val gift = Journey.thanksDecor.firstOrNull { it.first == n }?.second?.let { Journey.thanksGift[it] }
            Dialog(onDismiss = { c.write { island.read(u) } }) {
                Eyebrow(r.who.title.of(h.ru))
                Text(r.thanks.of(h.ru), style = T.Say)
                if (gift != null) Text(stringResource(R.string.gift, gift.of(h.ru)), style = T.Body.copy(color = C.goldDeep))
                PixelButton(stringResource(R.string.ok), { c.write { island.read(u) } }, Modifier.fillMaxWidth())
            }
        }
    }
}
