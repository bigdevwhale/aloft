package app.aloft.ui.map

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.aloft.R
import app.aloft.domain.Journey
import app.aloft.ui.Home
import app.aloft.ui.Nav
import app.aloft.ui.Overlay
import app.aloft.ui.Talk
import app.aloft.ui.Words
import app.aloft.ui.art.ChartScene
import app.aloft.ui.art.PixelView
import app.aloft.ui.components.Bar
import app.aloft.ui.components.Dialog
import app.aloft.ui.components.Eyebrow
import app.aloft.ui.components.Panel
import app.aloft.ui.components.PixelButton
import app.aloft.ui.theme.C
import app.aloft.ui.theme.T

/** The season's route on a sky chart, and every stop on it. */
@Composable
fun MapScreen(h: Home, nav: Nav) {
    val ctx = LocalContext.current
    val scene = remember(h.season, h.dist) { ChartScene(h.route, h.dist, h.season) }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 10.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Box(Modifier.fillMaxWidth().background(C.ink).padding(3.dp)) { PixelView(scene, Modifier.fillMaxWidth()) }
        Panel(Modifier.fillMaxWidth()) {
            Eyebrow(stringResource(R.string.season_n, h.season + 1))
            val last = h.route.last()
            Text(stringResource(R.string.route_to, last.to.of(h.ru)), style = T.Heading)
            Bar((h.dist.toFloat() / last.at).coerceIn(0f, 1f), C.gold, Modifier.padding(vertical = 6.dp))
            Text(stringResource(R.string.route_done, Talk.versts(h.dist, h.ru), Talk.versts(last.at, h.ru)), style = T.Small)
            h.next?.let { Text(stringResource(R.string.route_next, it.to.of(h.ru), Talk.versts(it.at - h.dist, h.ru)), style = T.Body) }
            Text(stringResource(R.string.route_hint), style = T.Small, modifier = Modifier.padding(top = 4.dp))
        }
        h.route.forEachIndexed { i, st ->
            val reached = st.at <= h.dist
            val arrival = h.g.arrivals.firstOrNull { it.season == h.season && it.stop == i }
            Panel(Modifier.fillMaxWidth(), fill = if (reached) C.paper else C.paper2, onClick = if (reached) ({ nav.push(Overlay.Letter(h.season, i)) }) else null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("$i", style = T.Heading.copy(color = if (reached) C.goldDeep else C.muted), modifier = Modifier.width(26.dp))
                    Column(Modifier.weight(1f)) {
                        Text(st.name.of(h.ru), style = T.Body.copy(color = if (reached) C.ink else C.muted))
                        Text(
                            when {
                                arrival != null -> stringResource(R.string.stop_reached, Words.day(ctx, app.aloft.data.Island.day(arrival.at), h.today))
                                reached -> stringResource(R.string.stop_start)
                                else -> Talk.versts(st.at, h.ru)
                            },
                            style = T.Small,
                        )
                    }
                    if (reached) Text("›", style = T.Heading.copy(color = C.goldDeep))
                    else Box(Modifier.size(10.dp).background(C.paper))
                }
            }
        }
        if (h.season > 0) Text(stringResource(R.string.seasons_past, h.season), style = T.Small.copy(color = C.faint), modifier = Modifier.padding(bottom = 12.dp))
    }
}

/** A stop's letter, read again. */
@Composable
fun LetterDialog(h: Home, season: Int, stop: Int, onClose: () -> Unit) {
    val st = Journey.route(season).getOrNull(stop) ?: return onClose()
    Dialog(onDismiss = onClose) {
        Text(st.name.of(h.ru), style = T.Title)
        Text(st.letter.of(h.ru), style = T.Body)
        if (st.gift.en.isNotEmpty()) Text(stringResource(R.string.gift, st.gift.of(h.ru)), style = T.Body.copy(color = C.goldDeep))
        PixelButton(stringResource(R.string.close), onClose, Modifier.fillMaxWidth())
    }
}
