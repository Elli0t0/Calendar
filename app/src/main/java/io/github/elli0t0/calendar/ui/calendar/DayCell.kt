package io.github.elli0t0.calendar.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
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
import java.time.LocalDate

fun markerColor(type: MarkerType): Color = when (type) {
    MarkerType.INCOME -> Color(0xFF43A047)
    MarkerType.EXPENSE -> Color(0xFFE53935)
    MarkerType.INSTALLMENT -> Color(0xFFFB8C00)
}

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
) {
    val scheme = MaterialTheme.colorScheme
    var cellModifier = Modifier
        .aspectRatio(0.85f)
        .clip(RoundedCornerShape(14.dp))
        .clickable(onClick = onClick)
    if (isSelected) {
        cellModifier = cellModifier.border(2.dp, scheme.primary, RoundedCornerShape(14.dp))
    }
    if (!inCurrentMonth) {
        cellModifier = cellModifier.alpha(0.35f)
    }
    val numberColor = when {
        isToday -> Color.White
        isFriday -> Color(0xFFE53935)
        else -> scheme.onSurface
    }
    Column(
        modifier = cellModifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
    ) {
        if (isToday) {
            Box(
                modifier = Modifier.size(40.dp).background(scheme.primary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = jalali.day.toPersianDigits(),
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        } else {
            Box(
                modifier = Modifier.size(40.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = jalali.day.toPersianDigits(),
                    color = numberColor,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Text(
            text = gregorian.dayOfMonth.toPersianDigits(),
            color = scheme.onSurfaceVariant,
            fontSize = 11.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier.height(3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            markers.take(3).forEachIndexed { index, marker ->
                if (index > 0) Spacer(modifier = Modifier.width(3.dp))
                Box(
                    modifier = Modifier
                        .width(10.dp)
                        .height(3.dp)
                        .clip(CircleShape)
                        .background(markerColor(marker.type))
                )
            }
        }
    }
}
