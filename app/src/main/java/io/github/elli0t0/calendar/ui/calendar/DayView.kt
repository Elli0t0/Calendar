package io.github.elli0t0.calendar.ui.calendar

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.elli0t0.calendar.data.AppSettings
import io.github.elli0t0.calendar.data.DayMarker
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

/** Days either side of today the day pager covers (10 years). */
private const val DAY_WINDOW = 3650

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DayView(
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
    fun dateForPage(page: Int): LocalDate = today.plusDays((page - DAY_WINDOW).toLong())
    fun pageForDate(date: LocalDate): Int =
        (DAY_WINDOW + ChronoUnit.DAYS.between(today, date)).toInt()
            .coerceIn(0, DAY_WINDOW * 2)
    // The pager opens on the selected date.
    val initialSelected = remember { selected }
    val dayPagerState = rememberPagerState(
        initialPage = pageForDate(initialSelected),
        pageCount = { DAY_WINDOW * 2 + 1 }
    )
    // Swiping the pager moves the selected day (and the strip).
    LaunchedEffect(dayPagerState.currentPage) {
        val date = dateForPage(dayPagerState.currentPage)
        if (date != selected) onSelect(date)
    }
    // Selecting a day (strip tap, today badge) scrolls the pager to it.
    LaunchedEffect(selected) {
        val page = pageForDate(selected)
        if (page != dayPagerState.currentPage) {
            dayPagerState.animateScrollToPage(page)
        }
    }
    // The week containing the selected day, starting on the first day.
    val weekStart = remember(selected, settings.firstDay) {
        val offset = (
            PersianNames.weekdayIndex(selected.dayOfWeek) -
                firstDayIndex(settings.firstDay) + 7
            ) % 7
        selected.minusDays(offset.toLong())
    }
    val weekDays = remember(weekStart) {
        (0 until 7).map { weekStart.plusDays(it.toLong()) }
    }
    Column(modifier = modifier) {
        // 7-day strip.
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            weekDays.forEach { date ->
                DayStripCell(
                    date = date,
                    isSelected = date == selected,
                    isToday = date == today,
                    markers = markerProvider.markersFor(date),
                    markerStyle = settings.markerStyle,
                    onClick = { onSelect(date) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        HorizontalPager(
            state = dayPagerState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) { page ->
            val date = dateForPage(page)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                DayEvents(
                    date = date,
                    transactions = transactions,
                    installments = installments,
                    markerStyle = settings.markerStyle,
                    onEditTx = onEditTx,
                    onEditInst = onEditInst
                )
            }
        }
    }
}

/** One cell of the 7-day strip: weekday letter, number, tiny marker dots. */
@Composable
fun DayStripCell(
    date: LocalDate,
    isSelected: Boolean,
    isToday: Boolean,
    markers: List<DayMarker>,
    markerStyle: MarkerStyle,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val jalali = Jalali.toJalali(date)
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = PersianNames.weekdayLetter(date),
            fontSize = 13.sp,
            color = if (PersianNames.isFriday(date)) {
                Color(0xFFE53935)
            } else {
                scheme.onSurfaceVariant
            }
        )
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(
                    when {
                        isSelected -> scheme.onSurface
                        isToday -> scheme.primary
                        else -> Color.Transparent
                    },
                    RoundedCornerShape(8.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = jalali.day.toPersianDigits(),
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = when {
                    isSelected -> scheme.background
                    isToday -> scheme.onPrimary
                    else -> scheme.onSurface
                }
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier.height(4.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            markers.take(3).forEach { marker ->
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(
                            markerColor(marker.type, marker.colorIndex, markerStyle)
                        )
                )
            }
        }
    }
}
