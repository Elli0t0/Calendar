package io.github.elli0t0.calendar.data

import io.github.elli0t0.calendar.jalali.Jalali
import java.time.LocalDate

/**
 * True when [tx] has an occurrence on [date]. The first occurrence is the
 * transaction's own date; never earlier, never after repeatUntil (inclusive).
 * Monthly repeats use the same Jalali day of month, clamped to the length of
 * that month; yearly repeats use the same Jalali month and day, with
 * 30 Esfand clamped to 29 in non-leap years.
 */
fun occursOn(tx: Transaction, date: LocalDate): Boolean {
    val start = LocalDate.ofEpochDay(tx.epochDay)
    if (date < start) return false
    if (tx.repeatUntilEpochDay != null && date.toEpochDay() > tx.repeatUntilEpochDay) {
        return false
    }
    return when (tx.repeat) {
        Repeat.NONE -> date == start
        Repeat.DAILY -> true
        Repeat.WEEKLY -> date.dayOfWeek == start.dayOfWeek
        Repeat.MONTHLY -> {
            val j = Jalali.toJalali(date)
            val s = Jalali.toJalali(start)
            j.day == minOf(s.day, Jalali.monthLength(j.year, j.month))
        }
        Repeat.YEARLY -> {
            val j = Jalali.toJalali(date)
            val s = Jalali.toJalali(start)
            j.month == s.month && j.day == minOf(s.day, Jalali.monthLength(j.year, j.month))
        }
    }
}

/** All occurrences of [tx] from [from] to [to] (inclusive), clamped to the
 * transaction's own start and end. Empty when [from] is after [to]. */
fun occurrencesBetween(tx: Transaction, from: LocalDate, to: LocalDate): List<LocalDate> {
    val start = LocalDate.ofEpochDay(tx.epochDay)
    val first = maxOf(from, start)
    val last = tx.repeatUntilEpochDay?.let { minOf(to, LocalDate.ofEpochDay(it)) } ?: to
    if (first > last) return emptyList()
    val out = mutableListOf<LocalDate>()
    var date = first
    while (!date.isAfter(last)) {
        if (occursOn(tx, date)) out.add(date)
        date = date.plusDays(1)
    }
    return out
}
