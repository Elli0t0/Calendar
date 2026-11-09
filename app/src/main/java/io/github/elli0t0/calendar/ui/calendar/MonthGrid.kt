package io.github.elli0t0.calendar.ui.calendar

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import io.github.elli0t0.calendar.data.InstallmentRepository
import io.github.elli0t0.calendar.data.MarkerProvider
import io.github.elli0t0.calendar.data.TxRepository
import io.github.elli0t0.calendar.jalali.Jalali
import io.github.elli0t0.calendar.jalali.JalaliDate
import io.github.elli0t0.calendar.jalali.PersianNames
import java.time.LocalDate

/** Add [delta] months to a Jalali year/month. Returns (year, month). */
fun addJalaliMonths(year: Int, month: Int, delta: Int): Pair<Int, Int> {
    val total = year * 12 + (month - 1) + delta
    return Pair(Math.floorDiv(total, 12), Math.floorMod(total, 12) + 1)
}

/**
 * 42 Gregorian cells for a Jalali month: the Gregorian date of day 1, backed
 * up to the Saturday starting that week (Saturday = 0), then filled forward.
 */
fun monthCells(year: Int, month: Int): List<LocalDate> {
    val first = Jalali.toGregorian(JalaliDate(year, month, 1))
    val offset = PersianNames.weekdayIndex(first.dayOfWeek)
    val start = first.minusDays(offset.toLong())
    return (0 until 42).map { start.plusDays(it.toLong()) }
}

@Composable
fun MonthGrid(
    year: Int,
    month: Int,
    today: LocalDate,
    selected: LocalDate,
    markerProvider: MarkerProvider,
    onSelect: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    // Subscribe to the transaction and installment flows so the grid
    // recomposes (and the provider's markers are re-read) whenever one is
    // added, edited or deleted. Without these reads, the pager item
    // content would be skipped because none of MonthGrid's parameters change.
    TxRepository.items.collectAsState()
    InstallmentRepository.items.collectAsState()
    val cells = monthCells(year, month)
    Column(modifier = modifier.fillMaxWidth()) {
        cells.chunked(7).forEach { week ->
            Row(modifier = Modifier.fillMaxWidth()) {
                week.forEach { date ->
                    Box(modifier = Modifier.weight(1f)) {
                        val jalali = Jalali.toJalali(date)
                        DayCell(
                            jalali = jalali,
                            gregorian = date,
                            isToday = date == today,
                            isSelected = date == selected,
                            isFriday = PersianNames.isFriday(date),
                            inCurrentMonth = jalali.month == month,
                            markers = markerProvider.markersFor(date),
                            onClick = { onSelect(date) }
                        )
                    }
                }
            }
        }
    }
}
