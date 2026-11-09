package io.github.elli0t0.calendar.stats

import io.github.elli0t0.calendar.data.Transaction
import io.github.elli0t0.calendar.data.TxType
import io.github.elli0t0.calendar.jalali.Jalali
import io.github.elli0t0.calendar.jalali.JalaliDate
import io.github.elli0t0.calendar.ui.stats.monthlyTotals
import org.junit.Assert.assertEquals
import org.junit.Test

class StatsDataTest {

    @Test
    fun `transactions on 1405-07-30 and 1405-08-01 are in different months`() {
        val items = listOf(
            Transaction(
                1, TxType.INCOME, "a", 1_000L,
                Jalali.toGregorian(JalaliDate(1405, 7, 30)).toEpochDay()
            ),
            Transaction(
                2, TxType.EXPENSE, "b", 2_000L,
                Jalali.toGregorian(JalaliDate(1405, 8, 1)).toEpochDay()
            )
        )
        val mehr = monthlyTotals(items, 1405, 7)
        assertEquals(1_000L, mehr.income)
        assertEquals(0L, mehr.expense)
        val aban = monthlyTotals(items, 1405, 8)
        assertEquals(0L, aban.income)
        assertEquals(2_000L, aban.expense)
    }
}
