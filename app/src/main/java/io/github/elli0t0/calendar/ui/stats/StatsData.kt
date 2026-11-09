package io.github.elli0t0.calendar.ui.stats

import io.github.elli0t0.calendar.data.Transaction
import io.github.elli0t0.calendar.data.TxType
import io.github.elli0t0.calendar.jalali.Jalali
import io.github.elli0t0.calendar.ui.calendar.addJalaliMonths
import java.time.LocalDate

data class MonthTotal(val income: Long, val expense: Long)

/** Income/expense totals of [items] in one Shamsi month. */
fun monthlyTotals(items: List<Transaction>, year: Int, month: Int): MonthTotal {
    var income = 0L
    var expense = 0L
    for (tx in items) {
        val j = Jalali.toJalali(LocalDate.ofEpochDay(tx.epochDay))
        if (j.year == year && j.month == month) {
            if (tx.type == TxType.INCOME) income += tx.amount else expense += tx.amount
        }
    }
    return MonthTotal(income, expense)
}

/** True when [date] falls in the given Shamsi month. */
fun isJalaliMonth(date: LocalDate, year: Int, month: Int): Boolean {
    val j = Jalali.toJalali(date)
    return j.year == year && j.month == month
}

/** The 6 Shamsi months ending at (year, month), oldest first. */
fun lastSixMonths(year: Int, month: Int): List<Pair<Int, Int>> =
    (5 downTo 0).map { addJalaliMonths(year, month, -it) }
