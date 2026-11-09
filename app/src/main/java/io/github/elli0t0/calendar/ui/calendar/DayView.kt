package io.github.elli0t0.calendar.ui.calendar

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import io.github.elli0t0.calendar.ui.theme.Dimens
import io.github.elli0t0.calendar.ui.theme.LocalDarkTheme
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
            horizontalArrangement = Arrangement.spacedBy(Dimens.GridGap)
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
    // Tile color: surfaceContainerLow in dark, surfaceContainer in light.
    val tileColor = if (LocalDarkTheme.current) {
        scheme.surfaceContainerLow
    } else {
        scheme.surfaceContainer
    }
    var cellModifier = modifier
        .clip(RoundedCornerShape(Dimens.DayTileCorner))
        .background(tileColor)
        .clickable(onClick = onClick)
    // Today and the selected day get a 1.5dp outline around the tile.
    if (isToday || isSelected) {
        cellModifier = cellModifier.border(
            Dimens.DayTileOutline,
            scheme.onSurfaceVariant,
            RoundedCornerShape(Dimens.DayTileCorner)
        )
    }
    Column(
        modifier = cellModifier.padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = PersianNames.weekdayLetter(date),
            fontSize = Dimens.StripLetter,
            color = if (PersianNames.isFriday(date)) {
                Color(0xFFE53935)
            } else {
                scheme.onSurfaceVariant
            }
        )
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .size(Dimens.StripNumberBox)
                .background(
                    // Today: the number sits in a small filled rounded square.
                    if (isToday) scheme.onSurface else Color.Transparent,
                    RoundedCornerShape(Dimens.DayNumberBoxCorner)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = jalali.day.toPersianDigits(),
                fontSize = Dimens.StripNumber,
                fontWeight = FontWeight.SemiBold,
                color = if (isToday) scheme.background else scheme.onSurface
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
