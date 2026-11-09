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
        // 1405/08/01 is Friday 2026-10-23, so the Saturday-first grid
        // starts on Saturday 2026-10-17 and the month starts at index 6.
        val cells = monthCells(1405, 8)
        assertEquals(42, cells.size)
        assertEquals(LocalDate.of(2026, 10, 17), cells[0])
        assertEquals(LocalDate.of(2026, 10, 23), cells[6])
        assertEquals(JalaliDate(1405, 8, 1), Jalali.toJalali(cells[6]))
        assertEquals(LocalDate.of(2026, 11, 27), cells[41])
    }

    @Test
    fun mondayFirst() {
        // Monday-first: the same month starts on Monday 2026-10-19 and
        // the month starts at index 4.
        val cells = monthCells(1405, 8, FirstDay.MONDAY)
        assertEquals(42, cells.size)
        assertEquals(LocalDate.of(2026, 10, 19), cells[0])
        assertEquals(LocalDate.of(2026, 10, 23), cells[4])
        assertEquals(JalaliDate(1405, 8, 1), Jalali.toJalali(cells[4]))
        assertEquals(LocalDate.of(2026, 11, 29), cells[41])
    }
}
