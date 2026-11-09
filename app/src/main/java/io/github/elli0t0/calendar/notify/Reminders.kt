package io.github.elli0t0.calendar.notify

import io.github.elli0t0.calendar.data.Installment
import io.github.elli0t0.calendar.data.Transaction
import io.github.elli0t0.calendar.data.dueDate
import io.github.elli0t0.calendar.data.occurrencesBetween
import io.github.elli0t0.calendar.data.offsetDays
import java.time.LocalDate
import java.time.LocalDateTime

/** Intent extra carrying the due date (epoch day) a notification opens. */
const val EXTRA_EPOCH_DAY = "epochDay"

/**
 * One reminder to fire. [key] identifies the source series (tx-<id> or
 * inst-<id>-<k>); all occurrences of one series share it, so their
 * notifications replace each other instead of stacking.
 */
data class Reminder(
    val title: String,
    val amount: Long,
    val dueDate: LocalDate,
    val fireAt: LocalDateTime,
    val key: String,
)

/** All reminders whose due date falls in [from, to], with fireAt computed
 * (due date minus the offset, at 09:00 local time). No time filtering. */
internal fun collectReminders(
    transactions: List<Transaction>,
    installments: List<Installment>,
    from: LocalDate,
    to: LocalDate,
): List<Reminder> {
    val out = mutableListOf<Reminder>()
    for (tx in transactions) {
        val offset = tx.remind.offsetDays() ?: continue
        for (due in occurrencesBetween(tx, from, to)) {
            out.add(
                Reminder(
                    tx.title, tx.amount, due,
                    due.minusDays(offset.toLong()).atTime(9, 0),
                    "tx-${tx.id}"
                )
            )
        }
    }
    for (inst in installments) {
        val offset = inst.remind.offsetDays() ?: continue
        for (k in 0 until inst.count) {
            if (k in inst.paid) continue
            val due = dueDate(inst, k)
            if (due in from..to) {
                out.add(
                    Reminder(
                        inst.title, inst.amountPerMonth, due,
                        due.minusDays(offset.toLong()).atTime(9, 0),
                        "inst-${inst.id}-$k"
                    )
                )
            }
        }
    }
    return out
}

/**
 * Reminders to fire from [now] until [daysAhead] days out, earliest first.
 * A reminder whose fireAt is already past is skipped.
 */
fun nextReminders(
    transactions: List<Transaction>,
    installments: List<Installment>,
    now: LocalDateTime,
    daysAhead: Int = 90,
): List<Reminder> {
    val today = now.toLocalDate()
    return collectReminders(transactions, installments, today, today.plusDays(daysAhead.toLong()))
        .filter { it.fireAt > now }
        .sortedBy { it.fireAt }
}
