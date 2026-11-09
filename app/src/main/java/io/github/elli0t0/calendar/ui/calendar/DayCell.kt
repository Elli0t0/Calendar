package io.github.elli0t0.calendar.ui.calendar

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.elli0t0.calendar.data.DayMarker
import io.github.elli0t0.calendar.data.MarkerType
import io.github.elli0t0.calendar.jalali.JalaliDate
import io.github.elli0t0.calendar.jalali.toPersianDigits
import io.github.elli0t0.calendar.ui.theme.EventPalette
import java.time.LocalDate

/**
 * Color for a marker: the chosen palette color when one is set,
 * otherwise the color of its type.
 */
fun markerColor(type: MarkerType, colorIndex: Int = -1): Color =
    if (colorIndex in EventPalette.indices) {
        EventPalette[colorIndex]
    } else {
        when (type) {
            MarkerType.INCOME -> Color(0xFF43A047)
            MarkerType.EXPENSE -> Color(0xFFE53935)
            MarkerType.INSTALLMENT -> Color(0xFFFB8C00)
        }
    }

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DayCell(
    jalali: JalaliDate,
    gregorian: LocalDate,
    isToday: Boolean,
    isSelected: Boolean,
    isFriday: Boolean,
    inCurrentMonth: Boolean,
    markers: List<DayMarker>,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {},
) {
    val scheme = MaterialTheme.colorScheme
    var cellModifier = Modifier
        .aspectRatio(0.7f)
        .clip(RoundedCornerShape(10.dp))
        .background(scheme.surfaceContainerLow)
        .combinedClickable(onClick = onClick, onLongClick = onLongClick)
    if (isSelected) {
        cellModifier = cellModifier.border(
            1.5.dp,
            scheme.onSurface.copy(alpha = 0.6f),
            RoundedCornerShape(10.dp)
        )
    }
    if (!inCurrentMonth) {
        cellModifier = cellModifier.alpha(0.35f)
    }
    val numberColor = when {
        isFriday -> Color(0xFFE53935)
        else -> scheme.onSurface
    }
    Column(
        modifier = cellModifier.padding(top = 4.dp, bottom = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (isSelected) {
            Box(
                modifier = Modifier.size(26.dp)
                    .background(scheme.onSurface, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = jalali.day.toPersianDigits(),
                    color = scheme.background,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        } else if (isToday) {
            Box(
                modifier = Modifier.size(26.dp)
                    .background(scheme.primary, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = jalali.day.toPersianDigits(),
                    color = scheme.onPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        } else {
            Box(
                modifier = Modifier.size(26.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = jalali.day.toPersianDigits(),
                    color = numberColor,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
        Spacer(modifier = Modifier.height(2.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            markers.take(3).forEach { marker ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(markerColor(marker.type, marker.colorIndex))
                )
            }
        }
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = gregorian.dayOfMonth.toPersianDigits(),
            color = scheme.onSurfaceVariant,
            fontSize = 11.sp
        )
    }
}
