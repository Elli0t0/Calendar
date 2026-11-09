package io.github.elli0t0.calendar.notify

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import io.github.elli0t0.calendar.BuildConfig
import io.github.elli0t0.calendar.MainActivity
import io.github.elli0t0.calendar.R
import io.github.elli0t0.calendar.data.AppSettings
import io.github.elli0t0.calendar.data.Installment
import io.github.elli0t0.calendar.data.InstallmentStore
import io.github.elli0t0.calendar.data.Transaction
import io.github.elli0t0.calendar.data.TxStore
import io.github.elli0t0.calendar.data.withAppLocale
import io.github.elli0t0.calendar.ui.common.formatAmount
import io.github.elli0t0.calendar.ui.common.formatJalaliDate
import java.io.File
import java.time.LocalDateTime
import java.time.ZoneId

private const val TAG = "ReminderScheduler"
private const val CHANNEL_ID = "reminders"
private const val ALARM_REQUEST_CODE = 0

internal const val PREFS_NAME = "reminders"
internal const val KEY_LAST_POSTED = "last_posted_fire_at"

/** Stable notification id for a reminder; one series keeps one id. */
internal fun notificationId(reminder: Reminder): Int =
    reminder.key.hashCode() and Int.MAX_VALUE

object ReminderScheduler {

    /** Loads both lists straight from disk (the process may be fresh and the
     * repositories empty, e.g. when an alarm or the boot receiver fires). */
    internal fun loadFromDisk(context: Context): Pair<List<Transaction>, List<Installment>> {
        val filesDir = context.filesDir
        return TxStore(File(filesDir, "transactions.json")).load() to
            InstallmentStore(File(filesDir, "installments.json")).load()
    }

    /** Re-arms the single alarm from the files on disk. */
    fun reschedule(context: Context) {
        // Load the stored reminder time (and any other settings) first.
        AppSettings.init(context)
        val (transactions, installments) = loadFromDisk(context)
        reschedule(context, transactions, installments)
    }

    /** Re-arms the single alarm from the given (in-memory) lists. */
    fun reschedule(
        context: Context,
        transactions: List<Transaction>,
        installments: List<Installment>,
    ) {
        val appContext = context.applicationContext
        ensureChannel(appContext)
        val now = LocalDateTime.now()
        val next = nextReminders(transactions, installments, now)
        val alarmManager = appContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pendingIntent = alarmPendingIntent(appContext)
        if (next.isEmpty()) {
            alarmManager.cancel(pendingIntent)
            if (BuildConfig.DEBUG) Log.d(TAG, "no reminders, alarm cancelled")
            return
        }
        val earliest = next.first()
        val millis = earliest.fireAt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        // Inexact on purpose: no exact-alarm permission needed.
        alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, millis, pendingIntent)
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "next reminder at ${earliest.fireAt} (${earliest.title})")
        }
    }

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        // The channel name must be in the app language, not the system one.
        val ctx = context.withAppLocale(AppSettings.readLanguage(context))
        val manager = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (manager.getNotificationChannel(CHANNEL_ID) != null) return
        val channel = NotificationChannel(
            CHANNEL_ID,
            ctx.getString(R.string.reminders_channel_name),
            NotificationManager.IMPORTANCE_DEFAULT
        )
        manager.createNotificationChannel(channel)
    }

    fun postReminder(context: Context, reminder: Reminder) {
        // Notification text (amount, date) must be in the app language,
        // even when the receiver runs on the application context.
        val ctx = context.withAppLocale(AppSettings.readLanguage(context))
        ensureChannel(ctx)
        val intent = Intent(ctx, MainActivity::class.java).apply {
            putExtra(EXTRA_EPOCH_DAY, reminder.dueDate.toEpochDay())
        }
        val pendingIntent = PendingIntent.getActivity(
            ctx,
            notificationId(reminder),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val text = ctx.getString(
            R.string.amount_with_currency,
            formatAmount(reminder.amount)
        ) + " · " + formatJalaliDate(ctx, reminder.dueDate)
        val notification = NotificationCompat.Builder(ctx, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(reminder.title)
            .setContentText(text)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(ctx).notify(notificationId(reminder), notification)
    }

    private fun alarmPendingIntent(context: Context): PendingIntent =
        PendingIntent.getBroadcast(
            context,
            ALARM_REQUEST_CODE,
            Intent(context, ReminderReceiver::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
}
