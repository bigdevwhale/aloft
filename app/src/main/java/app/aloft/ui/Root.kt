package app.aloft.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.aloft.R
import app.aloft.container
import app.aloft.data.IslandState
import app.aloft.data.Settings
import app.aloft.domain.Element
import app.aloft.domain.Guest
import app.aloft.ru
import app.aloft.ui.isle.IsleScreen
import app.aloft.ui.isle.LogDialog
import app.aloft.ui.journal.JournalScreen
import app.aloft.ui.journal.WorkoutDialog
import app.aloft.ui.map.LetterDialog
import app.aloft.ui.map.MapScreen
import app.aloft.ui.onboarding.Onboarding
import app.aloft.ui.settings.SettingsPage
import app.aloft.ui.spirits.GuestDialog
import app.aloft.ui.spirits.SpiritPage
import app.aloft.ui.spirits.SpiritsScreen
import app.aloft.ui.theme.C
import app.aloft.ui.theme.T
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDateTime

enum class Tab { ISLE, MAP, LOG, SPIRITS }

sealed interface Overlay {
    data class Spirit(val e: Element) : Overlay
    data class GuestCard(val g: Guest) : Overlay
    data class Letter(val season: Int, val stop: Int) : Overlay
    data class Workout(val id: Long) : Overlay
    data object Log : Overlay
    data object Settings : Overlay
}

/** What the screens can ask the root to do. */
class Nav(val push: (Overlay) -> Unit, val pop: () -> Unit, val tab: (Tab) -> Unit)

/** A light released by a training, flying to its spirit: [id] restarts the flight. */
data class Orb(val e: Element, val id: Int)

@Composable
fun AloftRoot(settings: Settings) {
    val ctx = LocalContext.current
    val c = ctx.container
    val state: IslandState? by c.island.state.collectAsStateWithLifecycle(initialValue = null)
    var now by remember { mutableStateOf(LocalDateTime.now()) }
    LaunchedEffect(Unit) { while (true) { delay(30_000); now = LocalDateTime.now() } }
    LifecycleResumeEffect(Unit) {
        now = LocalDateTime.now()
        val job = c.scope.launch { runCatching { c.refreshWeather() } }
        onPauseOrDispose { job.cancel() }
    }
    val seeding by c.seeding.collectAsStateWithLifecycle()
    if (seeding) { Box(Modifier.fillMaxSize().background(C.bg)); return }
    if (!settings.onboarded) { Onboarding(); return }
    val g = state ?: run { Box(Modifier.fillMaxSize().background(C.bg)); return }
    val home = remember(g, settings, now.hour, now.minute / 10, ctx.ru) { Home(g, settings, now, ctx.ru) }

    var tab by remember { mutableStateOf(Tab.ISLE) }
    val overlays = remember { mutableStateListOf<Overlay>() }
    val nav = remember { Nav(push = { overlays.add(it) }, pop = { if (overlays.isNotEmpty()) overlays.removeAt(overlays.lastIndex) }, tab = { tab = it }) }
    var orb by remember { mutableStateOf<Orb?>(null) }
    var orbs by remember { mutableIntStateOf(0) }
    BackHandler(enabled = overlays.isEmpty() && tab != Tab.ISLE) { tab = Tab.ISLE }

    Box(Modifier.fillMaxSize().background(C.bg)) {
        Column(Modifier.fillMaxSize()) {
            Header(home, onSettings = { nav.push(Overlay.Settings) })
            Box(Modifier.weight(1f).fillMaxWidth()) {
                when (tab) {
                    Tab.ISLE -> IsleScreen(home, nav, orb)
                    Tab.MAP -> MapScreen(home, nav)
                    Tab.LOG -> JournalScreen(home, nav)
                    Tab.SPIRITS -> SpiritsScreen(home, nav)
                }
            }
            TabBar(tab) { tab = it }
        }
        for (o in overlays) {
            key(o) {
                when (o) {
                    is Overlay.Spirit -> SpiritPage(home, o.e, nav)
                    is Overlay.GuestCard -> GuestDialog(home, o.g, onClose = nav.pop)
                    is Overlay.Letter -> LetterDialog(home, o.season, o.stop, onClose = nav.pop)
                    is Overlay.Workout -> WorkoutDialog(home, o.id, onClose = nav.pop)
                    Overlay.Log -> LogDialog(home, onClose = nav.pop, onLogged = { e -> nav.pop(); tab = Tab.ISLE; orb = Orb(e, ++orbs) })
                    Overlay.Settings -> SettingsPage(home, nav)
                }
            }
        }
    }
}

/** The title on the night sky, with how high the island floats. */
@Composable
private fun Header(h: Home, onSettings: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().background(C.skyDeep).statusBarsPadding().drawBehind {
            drawRect(C.sky, Offset(0f, size.height - 3.dp.toPx()), Size(size.width, 3.dp.toPx()))
        }.padding(start = 14.dp, end = 4.dp, top = 8.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(stringResource(R.string.app_title), style = T.Display.copy(color = C.gold, shadow = Shadow(C.ink, Offset(4f, 4f), 0f)))
        Spacer(Modifier.weight(1f))
        Text(stringResource(R.string.height_m, Talk.metres(h.isle.metres)), style = T.Small.copy(color = C.paper2), modifier = Modifier.background(C.ink).padding(horizontal = 6.dp, vertical = 3.dp))
        Box(Modifier.size(44.dp).clickable(onClick = onSettings), contentAlignment = Alignment.Center) { Text("☰", style = T.Heading.copy(color = C.paper2)) }
    }
}

@Composable
private fun TabBar(tab: Tab, onTab: (Tab) -> Unit) {
    val items = listOf(Tab.ISLE to R.string.tab_isle, Tab.MAP to R.string.tab_map, Tab.LOG to R.string.tab_log, Tab.SPIRITS to R.string.tab_spirits)
    Row(Modifier.fillMaxWidth().background(C.skyDeep).drawBehind { drawRect(C.ink, Offset.Zero, Size(size.width, 2.dp.toPx())) }.navigationBarsPadding().padding(horizontal = 4.dp, vertical = 8.dp)) {
        for ((t, label) in items) {
            val sel = t == tab
            Box(Modifier.weight(1f).clickable { onTab(t) }.padding(vertical = 6.dp), contentAlignment = Alignment.Center) {
                Text((if (sel) "▶" else "") + stringResource(label), style = T.Heading.copy(color = if (sel) C.gold else C.faint), maxLines = 1)
            }
        }
    }
}
