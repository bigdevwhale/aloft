package app.aloft.ui.onboarding

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.aloft.R
import app.aloft.container
import app.aloft.domain.Element
import app.aloft.domain.Mode
import app.aloft.ru
import app.aloft.ui.art.IslandScene
import app.aloft.ui.art.IsleLook
import app.aloft.ui.art.PixelView
import app.aloft.ui.art.TimeOfDay
import app.aloft.ui.components.Kind
import app.aloft.ui.components.Panel
import app.aloft.ui.components.PixelButton
import app.aloft.ui.components.SpiritIcon
import app.aloft.ui.isle.ELEMENT_HINTS
import app.aloft.ui.settings.PlacePicker
import app.aloft.ui.theme.C
import app.aloft.ui.theme.T

/** Three pages: what this is, the five spirits, the town for the weather. */
@Composable
fun Onboarding() {
    val ctx = LocalContext.current
    val c = ctx.container
    var page by rememberSaveable { mutableIntStateOf(0) }
    var town by rememberSaveable { mutableStateOf<String?>(null) }
    val notify = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
    val scene = remember {
        IslandScene(IsleLook(Element.entries.associateWith { .3 }, Mode.DAY, emptySet(), emptySet(), .45, TimeOfDay.DAY))
    }
    Column(Modifier.fillMaxSize().background(C.bg).imePadding()) {
        Box(Modifier.fillMaxWidth().background(C.skyDeep).statusBarsPadding().padding(16.dp)) {
            Text(stringResource(R.string.app_title), style = T.Display.copy(color = C.gold))
        }
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            when (page) {
                0 -> {
                    Box(Modifier.fillMaxWidth().background(C.ink).padding(3.dp)) { PixelView(scene, Modifier.fillMaxWidth()) }
                    Panel(Modifier.fillMaxWidth()) {
                        Text(stringResource(R.string.ob_1), style = T.Say)
                        Text(stringResource(R.string.ob_1b), style = T.Body, modifier = Modifier.padding(top = 8.dp))
                    }
                }
                1 -> {
                    Panel(Modifier.fillMaxWidth()) { Text(stringResource(R.string.ob_2), style = T.Say) }
                    for (e in Element.entries) Panel(Modifier.fillMaxWidth()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            SpiritIcon(e, .5, scale = 2.dp)
                            Column(Modifier.padding(start = 10.dp)) {
                                Text(e.spirit.of(ctx.ru), style = T.Heading.copy(color = C.elementInk(e)))
                                Text(ctx.getString(ELEMENT_HINTS.getValue(e)), style = T.Small)
                            }
                        }
                    }
                    Text(stringResource(R.string.ob_2b), style = T.Body.copy(color = C.paper2))
                }
                else -> {
                    Panel(Modifier.fillMaxWidth()) {
                        Text(stringResource(R.string.ob_3), style = T.Say)
                        Text(stringResource(R.string.ob_3b), style = T.Small, modifier = Modifier.padding(top = 6.dp))
                        if (town != null) Text(stringResource(R.string.place_now, town!!), style = T.Body.copy(color = C.goldDeep), modifier = Modifier.padding(top = 6.dp))
                    }
                    PlacePicker(ctx.ru) { p -> town = p.name; c.write { settings.update { it.copy(place = p, wxAt = 0) }; refreshWeather(force = true) } }
                }
            }
        }
        Row(Modifier.fillMaxWidth().navigationBarsPadding().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
            if (page > 0) PixelButton(stringResource(R.string.back), { page-- }, kind = Kind.GHOST)
            Box(Modifier.weight(1f))
            PixelButton(stringResource(when { page == 2 && town == null -> R.string.skip_start; page == 2 -> R.string.ob_start; else -> R.string.next }), {
                if (page < 2) page++ else {
                    if (Build.VERSION.SDK_INT >= 33) runCatching { notify.launch(Manifest.permission.POST_NOTIFICATIONS) }
                    c.write { settings.update { it.copy(onboarded = true, seasonStart = System.currentTimeMillis()) } }
                }
            }, kind = if (page == 2) Kind.RED else Kind.GREEN)
        }
    }
}
