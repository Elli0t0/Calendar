package io.github.elli0t0.calendar.ui.calendar

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import io.github.elli0t0.calendar.data.MarkerProvider
import io.github.elli0t0.calendar.jalali.Jalali
import io.github.elli0t0.calendar.jalali.PersianNames
import io.github.elli0t0.calendar.jalali.toPersianDigits
import java.time.LocalDate

/**
 * The 12 mini months of one Shamsi year. The grid slides when the year
 * changes. Only visible mini months are composed (LazyVerticalGrid), so
 * markers are computed lazily per month.
 */
@Composable
fun YearView(
    year: Int,
    markerProvider: MarkerProvider,
    onSelectMonth: (year: Int, month: Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    AnimatedContent(
        targetState = year,
        transitionSpec = {
            val direction = if (targetState > initialState) 1 else -1
            slideInHorizontally { width -> direction * width } +
                fadeIn(tween(200)) togetherWith
                slideOutHorizontally { width -> -direction * width } +
                fadeOut(tween(200))
        },
        label = "yearGrid",
        modifier = modifier
    ) { y ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 20.dp, top = 8.dp, end = 20.dp, bottom = 96.dp
            ),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(12, key = { it }) { monthIndex ->
                MiniMonth(
                    year = y,
                    month = monthIndex + 1,
                    markerProvider = markerProvider,
                    onClick = { onSelectMonth(y, monthIndex + 1) }
                )
            }
        }
    }
}

@Composable
private fun MiniMonth(
    year: Int,
    month: Int,
    markerProvider: MarkerProvider,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val settings by AppSettings.settings.collectAsState()
    val today = remember { LocalDate.now() }
    val cells = remember(year, month, settings.firstDay) {
        monthCells(year, month, settings.firstDay)
    }
    val letters = orderedWeekdayLetters(
        PersianNames.weekdayLetters(), settings.firstDay
    )
    val friday = fridayIndex(settings.firstDay)
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(8.dp)
    ) {
        Text(
            text = PersianNames.jalaliMonthName(month),
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = scheme.onSurface
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            letters.forEachIndexed { index, letter ->
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = letter,
                        fontSize = 9.sp,
                        color = if (index == friday) {
                            Color(0xFFE53935)
                        } else {
                            scheme.onSurfaceVariant
                        }
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(2.dp))
        cells.chunked(7).forEach { row ->
            Row(modifier = Modifier.fillMaxWidth()) {
                row.forEach { date ->
                    val jalali = Jalali.toJalali(date)
                    val inMonth = jalali.month == month
                    val isToday = date == today
                    val dotColor = if (inMonth) {
                        markerProvider.markersFor(date).firstOrNull()?.let {
                            markerColor(it.type, it.colorIndex, settings.markerStyle)
                        }
                    } else {
                        null
                    }
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(18.dp)
                                    .background(
                                        if (isToday) scheme.primary else Color.Transparent,
                                        RoundedCornerShape(4.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = jalali.day.toPersianDigits(),
                                    fontSize = 9.sp,
                                    color = when {
                                        isToday -> scheme.onPrimary
                                        !inMonth -> scheme.onSurface.copy(alpha = 0.35f)
                                        PersianNames.isFriday(date) -> Color(0xFFE53935)
                                        else -> scheme.onSurface
                                    }
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .size(3.dp)
                                    .clip(CircleShape)
                                    .background(dotColor ?: Color.Transparent)
                            )
                        }
                    }
                }
            }
        }
    }
}
