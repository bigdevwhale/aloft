package app.aloft

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate
import app.aloft.data.DemoSeed
import app.aloft.data.Island
import app.aloft.data.IslandDb
import app.aloft.data.SettingsStore
import app.aloft.domain.Weather
import app.aloft.work.Daily
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AppContainer(context: Context) {
    val app = context.applicationContext
    val db = IslandDb.create(app)
    val settings = SettingsStore(app)
    val island = Island(db.dao(), settings)
    val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    /** True while the demo build fills its log, so the first screen doesn't jump as it fills. */
    val seeding = MutableStateFlow(BuildConfig.DEMO)

    /** Fire-and-forget so a write survives the screen being closed right after tapping. */
    fun write(block: suspend AppContainer.() -> Unit) {
        scope.launch { block() }
    }

    /** Fetches the weather outside if a town is set and the last fetch is over half an hour old. */
    suspend fun refreshWeather(force: Boolean = false) {
        val s = settings.now()
        val p = s.place ?: return
        if (!force && System.currentTimeMillis() - s.wxAt < 30 * 60 * 1000L) return
        val now = runCatching { withContext(Dispatchers.IO) { Weather.now(p.lat, p.lon) } }.getOrNull() ?: return
        settings.update { it.copy(wx = now.wx.name, wxTemp = now.temp, wxAt = System.currentTimeMillis()) }
    }
}

class AloftApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        if (BuildConfig.DEMO) container.scope.launch { try { DemoSeed.seedIfEmpty(container) } finally { container.seeding.value = false } }
        runCatching { Daily.schedule(this) }
    }
}

val Context.container: AppContainer get() = (applicationContext as AloftApp).container

/** Resources in the in-app language, which AppCompat doesn't localise outside activities before Android 13. */
fun Context.localized(): Context {
    val tags = AppCompatDelegate.getApplicationLocales()
    if (tags.isEmpty) return this
    val config = Configuration(resources.configuration)
    config.setLocale(tags[0])
    return createConfigurationContext(config)
}

val Context.ru: Boolean get() = resources.configuration.locales[0].language == "ru"
