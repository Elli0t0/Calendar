package io.github.elli0t0.calendar.data

import io.github.elli0t0.calendar.jalali.Jalali
import io.github.elli0t0.calendar.jalali.JalaliDate
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Jalali (year, month, day) as a Gregorian date. */
private fun j(year: Int, month: Int, day: Int): LocalDate =
    Jalali.toGregorian(JalaliDate(year, month, day))

private fun tx(start: LocalDate, repeat: Repeat, until: LocalDate? = null) =
    Transaction(
        id = 1, type = TxType.EXPENSE, title = "t", amount = 1000,
        epochDay = start.toEpochDay(), repeat = repeat,
        repeatUntilEpochDay = until?.toEpochDay()
    )

class OccurrencesTest {

    @Test
    fun `none occurs only on its own date`() {
        val t = tx(j(1405, 8, 18), Repeat.NONE)
        assertTrue(occursOn(t, j(1405, 8, 18)))
        assertFalse(occursOn(t, j(1405, 8, 17)))
        assertFalse(occursOn(t, j(1405, 8, 19)))
    }

    @Test
    fun `daily occurs every day from the start`() {
        val t = tx(j(1405, 8, 18), Repeat.DAILY)
        assertFalse(occursOn(t, j(1405, 8, 17)))
        assertEquals(
            listOf(j(1405, 8, 18), j(1405, 8, 19), j(1405, 8, 20)),
            occurrencesBetween(t, j(1405, 8, 18), j(1405, 8, 20))
        )
    }

    @Test
    fun `monthly on the 31st clamps across Mordad to Esfand`() {
        val t = tx(j(1405, 1, 31), Repeat.MONTHLY)
        assertTrue(occursOn(t, j(1405, 1, 31)))
        assertTrue(occursOn(t, j(1405, 2, 31)))
        assertEquals(
            listOf(
                j(1405, 7, 30), j(1405, 8, 30), j(1405, 9, 30),
                j(1405, 10, 30), j(1405, 11, 30), j(1405, 12, 29)
            ),
            occurrencesBetween(t, j(1405, 7, 1), j(1405, 12, 29))
        )
    }

    @Test
    fun `monthly on the 30th through Esfand in a leap year`() {
        val t = tx(j(1403, 1, 30), Repeat.MONTHLY)
        assertEquals(
            listOf(j(1403, 12, 30)),
            occurrencesBetween(t, j(1403, 12, 1), j(1403, 12, 30))
        )
    }

    @Test
    fun `monthly on the 30th through Esfand in a non-leap year`() {
        val t = tx(j(1404, 1, 30), Repeat.MONTHLY)
        assertEquals(
            listOf(j(1404, 12, 29)),
            occurrencesBetween(t, j(1404, 12, 1), j(1404, 12, 29))
        )
    }

    @Test
    fun `yearly 30 Esfand clamps to 29 in common years`() {
        // 1404-1407 are common years (Esfand has 29 days); the next leap
        // year after 1403 is 1408, so the 30th returns then.
        val t = tx(j(1403, 12, 30), Repeat.YEARLY)
        assertEquals(
            listOf(
                j(1404, 12, 29), j(1405, 12, 29), j(1406, 12, 29),
                j(1407, 12, 29), j(1408, 12, 30)
            ),
            occurrencesBetween(t, j(1404, 12, 1), j(1408, 12, 30))
        )
    }

    @Test
    fun `end date is inclusive`() {
        val t = tx(j(1405, 1, 1), Repeat.DAILY, until = j(1405, 1, 3))
        assertEquals(
            listOf(j(1405, 1, 1), j(1405, 1, 2), j(1405, 1, 3)),
            occurrencesBetween(t, j(1405, 1, 1), j(1405, 1, 10))
        )
        assertFalse(occursOn(t, j(1405, 1, 4)))
    }

    @Test
    fun `weekly across a year boundary`() {
        // 2026-03-20 is a Friday (29 Esfand 1404); Nowruz 1405 is 2026-03-21.
        val friday = LocalDate.of(2026, 3, 20)
        val t = tx(friday, Repeat.WEEKLY)
        assertEquals(
            listOf(friday, friday.plusDays(7), friday.plusDays(14)),
            occurrencesBetween(t, friday, friday.plusDays(14))
        )
        assertEquals(1404, Jalali.toJalali(friday).year)
        assertEquals(1405, Jalali.toJalali(friday.plusDays(7)).year)
    }
}
