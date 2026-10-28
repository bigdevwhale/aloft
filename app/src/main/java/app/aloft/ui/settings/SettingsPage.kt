package app.aloft.ui.settings

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import app.aloft.R
import app.aloft.container
import app.aloft.domain.Place
import app.aloft.domain.Weather
import app.aloft.ui.Home
import app.aloft.ui.Nav
import app.aloft.ui.Words
import app.aloft.ui.components.Chip
import app.aloft.ui.components.CommunityLinks
import app.aloft.ui.components.Eyebrow
import app.aloft.ui.components.Field
import app.aloft.ui.components.Kind
import app.aloft.ui.components.Page
import app.aloft.ui.components.Panel
import app.aloft.ui.components.PixelButton
import app.aloft.ui.theme.C
import app.aloft.ui.theme.T
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** A search for the town whose weather the island sees. */
@Composable
fun PlacePicker(ru: Boolean, onPicked: (Place) -> Unit) {
    val scope = rememberCoroutineScope()
    val focus = LocalFocusManager.current
    var q by remember { mutableStateOf("") }
    var results by remember { mutableStateOf<List<Place>>(emptyList()) }
    var failed by remember { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Field(q, { q = it }, stringResource(R.string.place_search), Modifier.weight(1f))
            Spacer(Modifier.width(8.dp))
            PixelButton(stringResource(R.string.find), {
                if (q.isNotBlank()) scope.launch {
                    val r = runCatching { withContext(Dispatchers.IO) { Weather.search(q, if (ru) "ru" else "en") } }
                    failed = r.isFailure
                    results = r.getOrDefault(emptyList())
                }
            })
        }
        if (failed) Text(stringResource(R.string.place_fail), style = T.Small.copy(color = C.redDeep))
        for (p in results) Panel(Modifier.fillMaxWidth(), onClick = { results = emptyList(); q = p.name; focus.clearFocus(); onPicked(p) }, fill = C.paper2) {
            Text(p.name, style = T.Body)
            Text(p.region, style = T.Small)
        }
    }
}

@Composable
fun SettingsPage(home: Home, nav: Nav) {
    val ctx = LocalContext.current
    val c = ctx.container
    val set = home.settings
    Page(stringResource(R.string.settings), nav.pop) {
        Eyebrow(stringResource(R.string.weather))
        Text(set.place?.let { stringResource(R.string.place_now, it.name) } ?: stringResource(R.string.place_none), style = T.Body)
        home.wx?.let { w -> Text(stringResource(R.string.log_wx_now, Words.wx(ctx, w), (home.temp ?: 0.0).toInt()), style = T.Small) }
        Text(stringResource(R.string.place_hint), style = T.Small)
        PlacePicker(home.ru) { picked -> c.write { settings.update { it.copy(place = picked, wxAt = 0) }; refreshWeather(force = true) } }
        if (set.place != null) PixelButton(stringResource(R.string.place_off), { c.write { settings.update { it.copy(place = null, wx = "") } } }, Modifier.fillMaxWidth(), kind = Kind.GHOST)

        Eyebrow(stringResource(R.string.notify_title))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.notify_text), style = T.Body, modifier = Modifier.weight(1f))
            PixelButton(stringResource(if (set.notify) R.string.on else R.string.off), { c.write { settings.update { it.copy(notify = !it.notify) } } }, kind = if (set.notify) Kind.GREEN else Kind.GHOST)
        }

        Eyebrow(stringResource(R.string.language))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            val cur = AppCompatDelegate.getApplicationLocales().toLanguageTags().substringBefore('-').substringBefore(',')
            for ((tag, label) in listOf("" to stringResource(R.string.lang_system), "ru" to "Русский", "en" to "English")) {
                Chip(label, cur == tag, {
                    AppCompatDelegate.setApplicationLocales(if (tag.isEmpty()) LocaleListCompat.getEmptyLocaleList() else LocaleListCompat.forLanguageTags(tag))
                })
            }
        }

        Eyebrow(stringResource(R.string.how_title))
        Text(stringResource(R.string.how_text), style = T.Body)
        Text(stringResource(R.string.how_text2), style = T.Small)

        Eyebrow(stringResource(R.string.community))
        CommunityLinks()

        Eyebrow(stringResource(R.string.about))
        Text(stringResource(R.string.about_text), style = T.Body)
        Text(stringResource(R.string.credits), style = T.Small)
    }
}
