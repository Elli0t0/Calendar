package io.github.elli0t0.calendar.data

import io.github.elli0t0.calendar.jalali.Jalali
import io.github.elli0t0.calendar.jalali.JalaliDate
import org.junit.Assert.assertEquals
import org.junit.Test

class InstallmentTest {

    private fun plan(startYear: Int, startMonth: Int, day: Int, count: Int) =
        Installment(
            id = 1,
            title = "وام",
            amountPerMonth = 1_000_000L,
            dayOfMonth = day,
            count = count,
            startYear = startYear,
            startMonth = startMonth,
            paid = emptySet()
        )

    @Test
    fun `day 31 clamps to 30 in 7th month`() {
        val p = plan(1404, 1, 31, 12)
        assertEquals(JalaliDate(1404, 7, 30), Jalali.toJalali(dueDate(p, 6)))
    }

    @Test
    fun `day 31 clamps to 29 in Esfand of non-leap 1404`() {
        val p = plan(1404, 1, 31, 12)
        assertEquals(JalaliDate(1404, 12, 29), Jalali.toJalali(dueDate(p, 11)))
    }

    @Test
    fun `day 31 clamps to 30 in Esfand of leap 1403`() {
        val p = plan(1403, 6, 31, 12)
        assertEquals(JalaliDate(1403, 12, 30), Jalali.toJalali(dueDate(p, 6)))
    }

    @Test
    fun `plan crosses year boundary`() {
        val p = plan(1404, 10, 15, 6)
        assertEquals(JalaliDate(1404, 10, 15), Jalali.toJalali(dueDate(p, 0)))
        assertEquals(JalaliDate(1404, 12, 15), Jalali.toJalali(dueDate(p, 2)))
        assertEquals(JalaliDate(1405, 1, 15), Jalali.toJalali(dueDate(p, 3)))
        assertEquals(JalaliDate(1405, 3, 15), Jalali.toJalali(dueDate(p, 5)))
    }
}
