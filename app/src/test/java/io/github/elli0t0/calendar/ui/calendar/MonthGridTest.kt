package io.github.elli0t0.calendar.ui.calendar

import io.github.elli0t0.calendar.data.FirstDay
import io.github.elli0t0.calendar.jalali.Jalali
import io.github.elli0t0.calendar.jalali.JalaliDate
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class MonthGridTest {

    @Test
    fun defaultSaturdayFirst() {
        // 1405/08/01 is Thursday 2025-10-23, so the Saturday-first grid
        // starts on Saturday 2025-10-18 and the month starts at index 5.
        val cells = monthCells(1405, 8)
        assertEquals(42, cells.size)
        assertEquals(LocalDate.of(2025, 10, 18), cells[0])
        assertEquals(LocalDate.of(2025, 10, 23), cells[5])
        assertEquals(JalaliDate(1405, 8, 1), Jalali.toJalali(cells[5]))
        assertEquals(LocalDate.of(2025, 11, 28), cells[41])
    }

    @Test
    fun mondayFirst() {
        // Monday-first: the same month starts on Monday 2025-10-20 and
        // the month starts at index 3.
        val cells = monthCells(1405, 8, FirstDay.MONDAY)
        assertEquals(42, cells.size)
        assertEquals(LocalDate.of(2025, 10, 20), cells[0])
        assertEquals(LocalDate.of(2025, 10, 23), cells[3])
        assertEquals(JalaliDate(1405, 8, 1), Jalali.toJalali(cells[3]))
        assertEquals(LocalDate.of(2025, 11, 30), cells[41])
    }
}
