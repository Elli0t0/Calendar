package io.github.elli0t0.calendar.ui.calendar

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.elli0t0.calendar.data.DayMarker
import io.github.elli0t0.calendar.data.MarkerStyle
import io.github.elli0t0.calendar.ui.theme.Dimens
import io.github.elli0t0.calendar.data.MarkerType
import io.github.elli0t0.calendar.jalali.JalaliDate
import io.github.elli0t0.calendar.jalali.toPersianDigits
import io.github.elli0t0.calendar.ui.theme.EventPalette
import java.time.LocalDate

/**
 * Color for a marker: the chosen palette color when one is set,
 * otherwise the color of its type. The marker style scales the
 * saturation via HSV (very muted 0.45, muted 0.7, bright 1.0,
 * very bright 1.0 with 10% higher value).
 */
fun markerColor(
    type: MarkerType,
    colorIndex: Int = -1,
    style: MarkerStyle = MarkerStyle.MUTED,
): Color {
    val base = if (colorIndex in EventPalette.indices) {
        EventPalette[colorIndex]
    } else {
        when (type) {
            MarkerType.INCOME -> Color(0xFF43A047)
            MarkerType.EXPENSE -> Color(0xFFE53935)
            MarkerType.INSTALLMENT -> Color(0xFFFB8C00)
        }
    }
    val saturationFactor = when (style) {
        MarkerStyle.VERY_MUTED -> 0.45f
        MarkerStyle.MUTED -> 0.7f
        MarkerStyle.BRIGHT -> 1f
        MarkerStyle.VERY_BRIGHT -> 1f
    }
    val valueFactor = if (style == MarkerStyle.VERY_BRIGHT) 1.1f else 1f
    val hsv = FloatArray(3)
    android.graphics.Color.colorToHSV(base.toArgb(), hsv)
    hsv[1] = (hsv[1] * saturationFactor).coerceIn(0f, 1f)
    hsv[2] = (hsv[2] * valueFactor).coerceIn(0f, 1f)
    return Color(android.graphics.Color.HSVToColor(hsv))
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
    showGregorian: Boolean = true,
    markerStyle: MarkerStyle = MarkerStyle.MUTED,
) {
    val scheme = MaterialTheme.colorScheme
    var cellModifier = Modifier
        .aspectRatio(0.7f)
        .combinedClickable(onClick = onClick, onLongClick = onLongClick)
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
        // Without the Gregorian number the Shamsi number and the markers
        // are centered vertically.
        verticalArrangement = if (showGregorian) {
            Arrangement.Top
        } else {
            Arrangement.Center
        },
    ) {
        if (isSelected) {
            Box(
                modifier = Modifier.size(Dimens.DayNumberBox)
                    .background(scheme.onSurface, RoundedCornerShape(Dimens.DayNumberBoxCorner)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = jalali.day.toPersianDigits(),
                    color = scheme.background,
                    fontSize = Dimens.DayNumber,
                    fontWeight = FontWeight.SemiBold
                )
            }
        } else if (isToday) {
            Box(
                modifier = Modifier.size(Dimens.DayNumberBox)
                    .background(scheme.primary, RoundedCornerShape(Dimens.DayNumberBoxCorner)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = jalali.day.toPersianDigits(),
                    color = scheme.onPrimary,
                    fontSize = Dimens.DayNumber,
                    fontWeight = FontWeight.SemiBold
                )
            }
        } else {
            Box(
                modifier = Modifier.size(Dimens.DayNumberBox),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = jalali.day.toPersianDigits(),
                    color = numberColor,
                    fontSize = Dimens.DayNumber,
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
                        .height(Dimens.MarkerHeight)
                        .clip(RoundedCornerShape(Dimens.MarkerCorner))
                        .background(markerColor(marker.type, marker.colorIndex, markerStyle))
                )
            }
        }
        if (showGregorian) {
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = gregorian.dayOfMonth.toPersianDigits(),
                color = scheme.onSurfaceVariant,
                fontSize = Dimens.GregorianNumber
            )
        }
    }
}
