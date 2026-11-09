package io.github.elli0t0.calendar.notify

import io.github.elli0t0.calendar.data.Installment
import io.github.elli0t0.calendar.data.Remind
import io.github.elli0t0.calendar.data.Repeat
import io.github.elli0t0.calendar.data.Transaction
import io.github.elli0t0.calendar.data.TxType
import io.github.elli0t0.calendar.jalali.Jalali
import io.github.elli0t0.calendar.jalali.JalaliDate
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Jalali (year, month, day) as a Gregorian date. */
private fun j(year: Int, month: Int, day: Int): LocalDate =
    Jalali.toGregorian(JalaliDate(year, month, day))

private fun tx(start: LocalDate, repeat: Repeat, remind: Remind) =
    Transaction(
        id = 1, type = TxType.EXPENSE, title = "rent", amount = 5000,
        epochDay = start.toEpochDay(), repeat = repeat, remind = remind
    )

class RemindersTest {

    @Test
    fun `monthly repeat fires before each occurrence`() {
        val t = tx(j(1405, 8, 18), Repeat.MONTHLY, Remind.ONE_DAY_BEFORE)
        val now = j(1405, 8, 17).atTime(8, 0)
        val reminders = nextReminders(listOf(t), emptyList(), now)
        assertTrue(reminders.size >= 2)
        assertEquals(j(1405, 8, 18), reminders[0].dueDate)
        assertEquals(j(1405, 8, 17).atTime(9, 0), reminders[0].fireAt)
        assertEquals(j(1405, 9, 18), reminders[1].dueDate)
        assertEquals(j(1405, 9, 17).atTime(9, 0), reminders[1].fireAt)
    }

    @Test
    fun `one week before crosses a month boundary`() {
        val t = tx(j(1405, 9, 1), Repeat.NONE, Remind.ONE_WEEK_BEFORE)
        val now = j(1405, 8, 20).atTime(8, 0)
        val reminders = nextReminders(listOf(t), emptyList(), now)
        assertEquals(1, reminders.size)
        assertEquals(j(1405, 9, 1), reminders[0].dueDate)
        assertEquals(j(1405, 8, 24).atTime(9, 0), reminders[0].fireAt)
        assertEquals(8, Jalali.toJalali(reminders[0].fireAt.toLocalDate()).month)
        assertEquals(9, Jalali.toJalali(reminders[0].dueDate).month)
    }

    @Test
    fun `paid installments are skipped`() {
        val inst = Installment(
            id = 2, title = "loan", amountPerMonth = 100000, dayOfMonth = 15,
            count = 3, startYear = 1405, startMonth = 8, paid = setOf(0),
            remind = Remind.ON_DAY
        )
        val now = j(1405, 8, 1).atTime(8, 0)
        val reminders = nextReminders(emptyList(), listOf(inst), now)
        assertEquals(
            listOf(j(1405, 9, 15), j(1405, 10, 15)),
            reminders.map { it.dueDate }
        )
        assertTrue(reminders.all { it.fireAt.toLocalDate() == it.dueDate })
        assertTrue(reminders.all { it.fireAt.hour == 9 })
    }

    @Test
    fun `past fire times are skipped`() {
        val today = tx(j(1405, 8, 18), Repeat.NONE, Remind.ON_DAY)
        val tomorrow = tx(j(1405, 8, 19), Repeat.NONE, Remind.ONE_DAY_BEFORE)
        // 10:00, after the 09:00 fire time of both.
        val now = j(1405, 8, 18).atTime(10, 0)
        assertEquals(emptyList<Reminder>(), nextReminders(listOf(today), emptyList(), now))
        assertEquals(emptyList<Reminder>(), nextReminders(listOf(tomorrow), emptyList(), now))
    }

    @Test
    fun `reminders beyond the window are not returned`() {
        val far = tx(j(1405, 8, 18).plusDays(100), Repeat.NONE, Remind.ON_DAY)
        val now = j(1405, 8, 18).atTime(8, 0)
        assertEquals(emptyList<Reminder>(), nextReminders(listOf(far), emptyList(), now))
        assertEquals(
            1,
            nextReminders(listOf(far), emptyList(), now, daysAhead = 101).size
        )
    }
}
