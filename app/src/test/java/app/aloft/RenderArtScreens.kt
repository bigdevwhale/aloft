package app.aloft

import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.core.app.ApplicationProvider
import app.aloft.data.DemoSeed
import app.aloft.domain.Element
import app.aloft.domain.Guest
import app.aloft.ui.Home
import app.aloft.ui.Nav
import app.aloft.ui.art.Art
import app.aloft.ui.isle.IsleScreen
import app.aloft.ui.isle.LogDialog
import app.aloft.ui.journal.JournalScreen
import app.aloft.ui.map.MapScreen
import app.aloft.ui.onboarding.Onboarding
import app.aloft.ui.spirits.GuestDialog
import app.aloft.ui.spirits.SpiritPage
import app.aloft.ui.spirits.SpiritsScreen
import app.aloft.ui.theme.AloftTheme
import app.aloft.ui.theme.C
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File
import java.time.LocalDateTime

/** Every main screen over the demo island, drawn to PNGs (run with `-Prender`), in both languages. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
abstract class ScreensBase {
    @get:Rule val rule = createAndroidComposeRule<ComponentActivity>()
    abstract val lang: String
    private val out by lazy { File(System.getProperty("render.out") ?: "build/art", "screens-$lang").apply { mkdirs() } }
    private val nav = Nav({}, {}, {})

    @Before fun still() { Art.still = true }

    private fun home(hour: Int = 13): Home = runBlocking {
        val app = ApplicationProvider.getApplicationContext<AloftApp>()
        val c = app.container
        DemoSeed.seedIfEmpty(c)
        Home(c.island.state.first(), c.settings.now(), LocalDateTime.now().withHour(hour).withMinute(10), app.ru)
    }

    private fun shot(name: String, content: @Composable () -> Unit) {
        rule.mainClock.autoAdvance = false
        rule.setContent { AloftTheme { Box(Modifier.fillMaxSize().background(C.bg)) { content() } } }
        rule.mainClock.advanceTimeBy(2_000)
        rule.waitForIdle()
        rule.mainClock.advanceTimeBy(500)
        rule.runOnUiThread {
            val view = rule.activity.window.decorView
            val image = android.graphics.Bitmap.createBitmap(view.width, view.height, android.graphics.Bitmap.Config.ARGB_8888)
            view.draw(android.graphics.Canvas(image))
            File(out, "$name.png").outputStream().use { image.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it) }
        }
    }

    @Test fun welcome() = shot("0-welcome") { Onboarding() }
    @Test fun isle() = home().let { h -> shot("1-isle") { IsleScreen(h, nav, null) } }
    @Test fun log() = home().let { h -> shot("2-log") { LogDialog(h, {}, {}) } }
    @Test fun map() = home().let { h -> shot("3-map") { MapScreen(h, nav) } }
    @Test fun journal() = home().let { h -> shot("4-journal") { JournalScreen(h, nav) } }
    @Test fun spirits() = home().let { h -> shot("5-spirits") { SpiritsScreen(h, nav) } }
    @Test fun spirit() = home().let { h -> shot("6-spirit") { SpiritPage(h, Element.EARTH, nav) } }
    @Test fun guest() = home().let { h -> shot("7-guest") { IsleScreen(h, nav, null); GuestDialog(h, Guest.DRIZZLE) {} } }
    @Test fun night() = home(22).let { h -> shot("8-night") { IsleScreen(h, nav, null) } }
}

@Config(sdk = [34], qualifiers = "en-w411dp-h891dp-xxhdpi")
class RenderArtScreensEn : ScreensBase() { override val lang = "en" }

@Config(sdk = [34], qualifiers = "ru-w411dp-h891dp-xxhdpi")
class RenderArtScreensRu : ScreensBase() { override val lang = "ru" }
