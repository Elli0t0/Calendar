package io.github.elli0t0.calendar.ui.calendar

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.elli0t0.calendar.R
import io.github.elli0t0.calendar.data.AppSettings
import io.github.elli0t0.calendar.data.FirstDay
import io.github.elli0t0.calendar.data.Installment
import io.github.elli0t0.calendar.data.InstallmentRepository
import io.github.elli0t0.calendar.data.MarkerProvider
import io.github.elli0t0.calendar.data.MarkerStyle
import io.github.elli0t0.calendar.data.Transaction
import io.github.elli0t0.calendar.data.TxRepository
import io.github.elli0t0.calendar.jalali.Jalali
import io.github.elli0t0.calendar.jalali.PersianNames
import io.github.elli0t0.calendar.jalali.toPersianDigits
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlinx.coroutines.launch

/** Weeks either side of the current week the week pager covers (10 years). */
private const val WEEK_WINDOW = 520

/** The first day of the week containing [date], per the first-day setting. */
fun weekStartFor(date: LocalDate, firstDay: FirstDay): LocalDate {
    val offset = (
        PersianNames.weekdayIndex(date.dayOfWeek) -
            firstDayIndex(firstDay) + 7
        ) % 7
    return date.minusDays(offset.toLong())
}

/** The week's Shamsi range ("16 – 22 Aban" / "۲۸ آبان تا ۴ آذر"). */
@Composable
fun weekRangeText(start: LocalDate, end: LocalDate): String {
    val startJalali = Jalali.toJalali(start)
    val endJalali = Jalali.toJalali(end)
    return if (startJalali.month == endJalali.month) {
        stringResource(
            R.string.week_range_same_month,
            startJalali.day.toPersianDigits(),
            endJalali.day.toPersianDigits(),
            PersianNames.jalaliMonthName(startJalali.month)
        )
    } else {
        stringResource(
            R.string.week_range_cross_month,
            startJalali.day.toPersianDigits(),
            PersianNames.jalaliMonthName(startJalali.month),
            endJalali.day.toPersianDigits(),
            PersianNames.jalaliMonthName(endJalali.month)
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WeekView(
    selected: LocalDate,
    onSelect: (LocalDate) -> Unit,
    markerProvider: MarkerProvider,
    onEditTx: (Transaction) -> Unit,
    onEditInst: (Installment) -> Unit,
    modifier: Modifier = Modifier,
) {
    val settings by AppSettings.settings.collectAsState()
    val today = remember { LocalDate.now() }
    fun weekStartForPage(page: Int): LocalDate {
        val currentWeekStart = weekStartFor(today, settings.firstDay)
        return currentWeekStart.plusDays(((page - WEEK_WINDOW) * 7).toLong())
    }
    fun pageForWeekStart(weekStart: LocalDate): Int {
        val currentWeekStart = weekStartFor(today, settings.firstDay)
        return (
            WEEK_WINDOW + ChronoUnit.WEEKS.between(currentWeekStart, weekStart)
            ).toInt().coerceIn(0, WEEK_WINDOW * 2)
    }
    // The pager opens on the selected day's week.
    val initialSelected = remember { selected }
    val weekPagerState = rememberPagerState(
        initialPage = pageForWeekStart(
            weekStartFor(initialSelected, settings.firstDay)
        ),
        pageCount = { WEEK_WINDOW * 2 + 1 }
    )
    // Swiping to a new week moves the selected day to the same weekday.
    LaunchedEffect(weekPagerState.currentPage) {
        val oldWeekStart = weekStartFor(selected, settings.firstDay)
        val offset = ChronoUnit.DAYS.between(oldWeekStart, selected)
        val newSelected = weekStartForPage(weekPagerState.currentPage)
            .plusDays(offset)
        if (newSelected != selected) onSelect(newSelected)
    }
    // Selecting a day scrolls the pager to its week.
    LaunchedEffect(selected) {
        val page = pageForWeekStart(weekStartFor(selected, settings.firstDay))
        if (page != weekPagerState.currentPage) {
            weekPagerState.animateScrollToPage(page)
        }
    }
    HorizontalPager(
        state = weekPagerState,
        modifier = modifier
    ) { page ->
        WeekPage(
            weekStart = weekStartForPage(page),
            selected = selected,
            onSelect = onSelect,
            markerProvider = markerProvider,
            onEditTx = onEditTx,
            onEditInst = onEditInst,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
private fun WeekPage(
    weekStart: LocalDate,
    selected: LocalDate,
    onSelect: (LocalDate) -> Unit,
    markerProvider: MarkerProvider,
    onEditTx: (Transaction) -> Unit,
    onEditInst: (Installment) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val settings by AppSettings.settings.collectAsState()
    val transactions by TxRepository.items.collectAsState()
    val installments by InstallmentRepository.items.collectAsState()
    val today = remember { LocalDate.now() }
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val days = remember(weekStart) {
        (0 until 7).map { weekStart.plusDays(it.toLong()) }
    }
    Column(modifier = modifier) {
        // 7-day strip for this week.
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            days.forEachIndexed { index, date ->
                DayStripCell(
                    date = date,
                    isSelected = date == selected,
                    isToday = date == today,
                    markers = markerProvider.markersFor(date),
                    markerStyle = settings.markerStyle,
                    onClick = {
                        onSelect(date)
                        scope.launch { listState.animateScrollToItem(index) }
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        // The 7 days as sections.
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(days, key = { it.toEpochDay() }) { date ->
                WeekDaySection(
                    date = date,
                    transactions = transactions,
                    installments = installments,
                    markerProvider = markerProvider,
                    markerStyle = settings.markerStyle,
                    onEditTx = onEditTx,
                    onEditInst = onEditInst
                )
            }
        }
    }
}

@Composable
private fun WeekDaySection(
    date: LocalDate,
    transactions: List<Transaction>,
    installments: List<Installment>,
    markerProvider: MarkerProvider,
    markerStyle: MarkerStyle,
    onEditTx: (Transaction) -> Unit,
    onEditInst: (Installment) -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val jalali = Jalali.toJalali(date)
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = jalali.day.toPersianDigits(),
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = scheme.onSurface
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = PersianNames.weekdayName(date),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = scheme.onSurface
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        if (markerProvider.markersFor(date).isEmpty()) {
            Text(
                text = stringResource(R.string.no_events),
                fontSize = 14.sp,
                color = scheme.onSurfaceVariant
            )
        } else {
            DayEventCards(
                date = date,
                transactions = transactions,
                installments = installments,
                markerStyle = markerStyle,
                onEditTx = onEditTx,
                onEditInst = onEditInst
            )
        }
    }
}
