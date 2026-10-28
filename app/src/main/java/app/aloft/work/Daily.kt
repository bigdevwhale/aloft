package app.aloft.work

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import app.aloft.MainActivity
import app.aloft.R
import app.aloft.container
import app.aloft.localized
import app.aloft.ru
import app.aloft.ui.Home
import app.aloft.ui.Talk
import kotlinx.coroutines.flow.first
import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

/** Fresh weather every few hours, and on Sunday evening one quiet note: the council of the elements has met. */
class Daily(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {
    override suspend fun doWork(): Result {
        val c = applicationContext.container
        runCatching { c.refreshWeather() }
        val s = c.settings.now()
        val now = LocalDateTime.now()
        if (!s.onboarded || !s.notify || now.dayOfWeek != DayOfWeek.SUNDAY || now.hour < 18 || now.hour > 22) return Result.success()
        val today = now.toLocalDate().toEpochDay()
        if (s.notifiedDay == today) return Result.success()
        val ctx = applicationContext.localized()
        val home = Home(c.island.state.first(), s, now, ctx.ru)
        post(ctx, ctx.getString(R.string.n_council_title), Talk.council(home).first())
        c.settings.update { it.copy(notifiedDay = today) }
        return Result.success()
    }

    private fun post(ctx: Context, title: String, text: String) {
        if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(ctx, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return
        val nm = ctx.getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(NotificationChannel(CHANNEL, ctx.getString(R.string.channel), NotificationManager.IMPORTANCE_DEFAULT))
        val open = PendingIntent.getActivity(ctx, 0, Intent(ctx, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), PendingIntent.FLAG_IMMUTABLE)
        nm.notify(1, NotificationCompat.Builder(ctx, CHANNEL).setSmallIcon(R.drawable.ic_stat_isle).setContentTitle(title).setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text)).setContentIntent(open).setAutoCancel(true).build())
    }

    companion object {
        private const val CHANNEL = "council"

        fun schedule(ctx: Context) {
            val req = PeriodicWorkRequestBuilder<Daily>(3, TimeUnit.HOURS).setInitialDelay(Duration.ofMinutes(20)).build()
            WorkManager.getInstance(ctx).enqueueUniquePeriodicWork("daily", ExistingPeriodicWorkPolicy.KEEP, req)
        }
    }
}
