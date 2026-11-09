package io.github.elli0t0.calendar.notify

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * Fires when the reminder alarm goes off. Posts a notification for every
 * reminder whose fireAt has come and was not posted yet, then re-arms the
 * alarm for the next one.
 */
class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val appContext = context.applicationContext
        val now = LocalDateTime.now()
        val prefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val lastPostedMillis = prefs.getLong(KEY_LAST_POSTED, Long.MIN_VALUE)
        val lastPosted = if (lastPostedMillis == Long.MIN_VALUE) {
            null
        } else {
            LocalDateTime.ofInstant(
                Instant.ofEpochMilli(lastPostedMillis), ZoneId.systemDefault()
            )
        }
        // Load from disk: this process may have been started by the alarm
        // alone, with the repositories never initialized.
        val (transactions, installments) = ReminderScheduler.loadFromDisk(appContext)
        val from = lastPosted?.toLocalDate() ?: now.toLocalDate().minusDays(90)
        val due = collectReminders(transactions, installments, from, now.toLocalDate())
            .filter { it.fireAt <= now && (lastPosted == null || it.fireAt > lastPosted) }
            .sortedBy { it.fireAt }
        var maxPosted = lastPosted
        for (reminder in due) {
            ReminderScheduler.postReminder(appContext, reminder)
            if (maxPosted == null || reminder.fireAt > maxPosted) maxPosted = reminder.fireAt
        }
        if (maxPosted != null) {
            prefs.edit().putLong(
                KEY_LAST_POSTED,
                maxPosted.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            ).apply()
        }
        ReminderScheduler.reschedule(appContext, transactions, installments)
    }
}
