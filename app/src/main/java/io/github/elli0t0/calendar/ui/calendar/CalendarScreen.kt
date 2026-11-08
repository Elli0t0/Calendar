package io.github.elli0t0.calendar.ui.calendar

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.elli0t0.calendar.R
import io.github.elli0t0.calendar.data.MarkerProvider
import io.github.elli0t0.calendar.jalali.Jalali
import io.github.elli0t0.calendar.jalali.PersianNames
import io.github.elli0t0.calendar.jalali.toPersianDigits
import java.time.LocalDate
import kotlinx.coroutines.launch

/** Gregorian months overlapped by a Jalali month, e.g. «اکتبر – نوامبر ۲۰۲۶». */
private fun gregorianOverlap(year: Int, month: Int): String {
    val first = Jalali.toGregorian(io.github.elli0t0.calendar.jalali.JalaliDate(year, month, 1))
    val last = Jalali.toGregorian(
        io.github.elli0t0.calendar.jalali.JalaliDate(year, month, Jalali.monthLength(year, month))
    )
    val firstName = PersianNames.gregorianMonthName(first.monthValue)
    val lastName = PersianNames.gregorianMonthName(last.monthValue)
    val yearText = last.year.toPersianDigits()
    return if (first.monthValue == last.monthValue) {
        "$firstName $yearText"
    } else {
        "$firstName – $lastName $yearText"
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CalendarScreen(
    markerProvider: MarkerProvider,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val today = remember { LocalDate.now() }
    var selected by remember { mutableStateOf(today) }
    val baseJalali = remember { Jalali.toJalali(today) }
    val pagerState = rememberPagerState(initialPage = 600, pageCount = { 1201 })
    val scope = rememberCoroutineScope()
    val displayed = addJalaliMonths(baseJalali.year, baseJalali.month, pagerState.currentPage - 600)

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(bottom = 120.dp)
    ) {
        // Header: big Shamsi month + year, Gregorian overlap, prev/next, today.
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${PersianNames.jalaliMonthName(displayed.second)} ${displayed.first.toPersianDigits()}",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = scheme.onSurface
                )
                Text(
                    text = gregorianOverlap(displayed.first, displayed.second),
                    fontSize = 14.sp,
                    color = scheme.onSurfaceVariant
                )
            }
            TextButton(onClick = {
                selected = today
                scope.launch { pagerState.animateScrollToPage(600) }
            }) {
                Text(text = stringResource(R.string.today), color = scheme.primary)
            }
            IconButton(onClick = {
                scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) }
            }) {
                Icon(
                    imageVector = Icons.Filled.KeyboardArrowRight,
                    contentDescription = stringResource(R.string.previous_month)
                )
            }
            IconButton(onClick = {
                scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
            }) {
                Icon(
                    imageVector = Icons.Filled.KeyboardArrowLeft,
                    contentDescription = stringResource(R.string.next_month)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Weekday row, week starts Saturday. Friday letter in red.
        Row(modifier = Modifier.fillMaxWidth()) {
            PersianNames.weekdayLetters.forEachIndexed { index, letter ->
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = letter,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (index == 6) Color(0xFFE53935) else scheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Month grid with swipe.
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth()
        ) { page ->
            val month = addJalaliMonths(baseJalali.year, baseJalali.month, page - 600)
            MonthGrid(
                year = month.first,
                month = month.second,
                today = today,
                selected = selected,
                markerProvider = markerProvider,
                onSelect = { selected = it }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Selected-day card.
        AnimatedContent(
            targetState = selected,
            transitionSpec = { fadeIn(tween(200)) togetherWith fadeOut(tween(200)) },
            label = "selectedDay"
        ) { date ->
            val jalali = Jalali.toJalali(date)
            val markers = markerProvider.markersFor(date)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = scheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "${PersianNames.weekdayName(date)} ${jalali.day.toPersianDigits()} " +
                            "${PersianNames.jalaliMonthName(jalali.month)} ${jalali.year.toPersianDigits()}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = scheme.onSurface
                    )
                    Text(
                        text = "${date.dayOfMonth.toPersianDigits()} " +
                            "${PersianNames.gregorianMonthName(date.monthValue)} ${date.year.toPersianDigits()}",
                        fontSize = 14.sp,
                        color = scheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    if (markers.isEmpty()) {
                        Text(
                            text = stringResource(R.string.no_events),
                            fontSize = 15.sp,
                            color = scheme.onSurfaceVariant
                        )
                    } else {
                        markers.forEach { marker ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .width(4.dp)
                                        .height(20.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(markerColor(marker.type))
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = marker.title,
                                    fontSize = 15.sp,
                                    color = scheme.onSurface,
                                    modifier = Modifier.weight(1f)
                                )
                                if (marker.amount != null) {
                                    Text(
                                        text = "${marker.amount.toPersianDigits()} تومان",
                                        fontSize = 14.sp,
                                        color = scheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
