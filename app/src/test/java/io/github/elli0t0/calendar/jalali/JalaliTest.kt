package io.github.elli0t0.calendar.jalali

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class JalaliTest {

    @Test
    fun `2026-11-09 is 1405-08-18`() {
        assertEquals(JalaliDate(1405, 8, 18), Jalali.toJalali(LocalDate.of(2026, 11, 9)))
    }

    @Test
    fun `1404-01-01 is 2025-03-21`() {
        assertEquals(LocalDate.of(2025, 3, 21), Jalali.toGregorian(JalaliDate(1404, 1, 1)))
    }

    @Test
    fun `1403-01-01 is 2024-03-20`() {
        assertEquals(LocalDate.of(2024, 3, 20), Jalali.toGregorian(JalaliDate(1403, 1, 1)))
    }

    @Test
    fun `1403 is leap and 1403-12-30 is 2025-03-20`() {
        assertTrue(Jalali.isLeap(1403))
        assertEquals(30, Jalali.monthLength(1403, 12))
        assertEquals(LocalDate.of(2025, 3, 20), Jalali.toGregorian(JalaliDate(1403, 12, 30)))
    }

    @Test
    fun `1404 is not leap`() {
        assertFalse(Jalali.isLeap(1404))
        assertEquals(29, Jalali.monthLength(1404, 12))
    }

    @Test
    fun `round trip for every day from 2000-01-01 to 2060-12-31`() {
        var date = LocalDate.of(2000, 1, 1)
        val end = LocalDate.of(2060, 12, 31)
        while (!date.isAfter(end)) {
            assertEquals(date, Jalali.toGregorian(Jalali.toJalali(date)))
            date = date.plusDays(1)
        }
    }
}
