package io.github.elli0t0.calendar.ui.calendar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import io.github.elli0t0.calendar.data.AppSettings
import io.github.elli0t0.calendar.data.FirstDay
import io.github.elli0t0.calendar.data.InstallmentRepository
import io.github.elli0t0.calendar.data.MarkerProvider
import io.github.elli0t0.calendar.ui.theme.Dimens
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

/** Index of the first day in the Saturday-first week (Saturday = 0). */
fun firstDayIndex(firstDay: FirstDay): Int = when (firstDay) {
    FirstDay.SATURDAY -> 0
    FirstDay.SUNDAY -> 1
    FirstDay.MONDAY -> 2
}

/** Weekday letters in the Saturday-first order, rotated to [firstDay]. */
fun orderedWeekdayLetters(letters: List<String>, firstDay: FirstDay): List<String> {
    val start = firstDayIndex(firstDay)
    return letters.drop(start) + letters.take(start)
}

/** Index of Friday in a week starting on [firstDay]. */
fun fridayIndex(firstDay: FirstDay): Int = (6 - firstDayIndex(firstDay) + 7) % 7

/**
 * 42 Gregorian cells for a Jalali month: the Gregorian date of day 1, backed
 * up to the first day of that week, then filled forward.
 */
fun monthCells(
    year: Int,
    month: Int,
    firstDay: FirstDay = FirstDay.SATURDAY,
): List<LocalDate> {
    val first = Jalali.toGregorian(JalaliDate(year, month, 1))
    val offset = (
        PersianNames.weekdayIndex(first.dayOfWeek) - firstDayIndex(firstDay) + 7
        ) % 7
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
    onLongClick: (LocalDate) -> Unit = {},
) {
    // Subscribe to the transaction and installment flows so the grid
    // recomposes (and the provider's markers are re-read) whenever one is
    // added, edited or deleted. Without these reads, the pager item
    // content would be skipped because none of MonthGrid's parameters change.
    TxRepository.items.collectAsState()
    InstallmentRepository.items.collectAsState()
    val settings by AppSettings.settings.collectAsState()
    val cells = monthCells(year, month, settings.firstDay)
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Dimens.GridGap)
    ) {
        cells.chunked(7).forEach { week ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Dimens.GridGap)
            ) {
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
                            onClick = { onSelect(date) },
                            onLongClick = { onLongClick(date) },
                            showGregorian = settings.showGregorianInTiles,
                            markerStyle = settings.markerStyle
                        )
                    }
                }
            }
        }
    }
}

/** One row of DayCells for the selected week (the collapsed month). */
@Composable
fun CollapsedWeekRow(
    selected: LocalDate,
    today: LocalDate,
    firstDay: FirstDay,
    markerProvider: MarkerProvider,
    onSelect: (LocalDate) -> Unit,
    onLongClick: (LocalDate) -> Unit,
) {
    val settings by AppSettings.settings.collectAsState()
    val weekStart = weekStartFor(selected, firstDay)
    val selectedJalali = Jalali.toJalali(selected)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Dimens.GridGap)
    ) {
        (0..6).forEach { offset ->
            val date = weekStart.plusDays(offset.toLong())
            Box(modifier = Modifier.weight(1f)) {
                val jalali = Jalali.toJalali(date)
                DayCell(
                    jalali = jalali,
                    gregorian = date,
                    isToday = date == today,
                    isSelected = date == selected,
                    isFriday = PersianNames.isFriday(date),
                    inCurrentMonth = jalali.month == selectedJalali.month,
                    markers = markerProvider.markersFor(date),
                    onClick = { onSelect(date) },
                    onLongClick = { onLongClick(date) },
                    showGregorian = settings.showGregorianInTiles,
                    markerStyle = settings.markerStyle
                )
            }
        }
    }
}
