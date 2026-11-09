package io.github.elli0t0.calendar.ui.settings

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.elli0t0.calendar.R
import io.github.elli0t0.calendar.data.AppSettings
import io.github.elli0t0.calendar.data.DayMarker
import io.github.elli0t0.calendar.data.MarkerProvider
import io.github.elli0t0.calendar.data.MarkerStyle
import io.github.elli0t0.calendar.data.MarkerType
import io.github.elli0t0.calendar.jalali.Jalali
import io.github.elli0t0.calendar.jalali.PersianNames
import io.github.elli0t0.calendar.ui.calendar.DayCell
import io.github.elli0t0.calendar.ui.calendar.markerColor
import io.github.elli0t0.calendar.ui.calendar.monthCells
import io.github.elli0t0.calendar.ui.theme.Dimens
import java.time.LocalDate

private fun markerStyleLabelRes(style: MarkerStyle): Int = when (style) {
    MarkerStyle.VERY_MUTED -> R.string.marker_very_muted
    MarkerStyle.MUTED -> R.string.marker_muted
    MarkerStyle.BRIGHT -> R.string.marker_bright
    MarkerStyle.VERY_BRIGHT -> R.string.marker_very_bright
}

/** Fake markers so the style preview shows event colors. */
private class PreviewMarkerProvider : MarkerProvider {
    override fun markersFor(date: LocalDate): List<DayMarker> {
        return when (date.dayOfMonth % 4) {
            0 -> listOf(DayMarker(MarkerType.INCOME, "Pay", 1000L))
            1 -> listOf(DayMarker(MarkerType.EXPENSE, "Food", 500L))
            2 -> listOf(
                DayMarker(MarkerType.INSTALLMENT, "Rent", 2000L),
                DayMarker(MarkerType.INCOME, "Pay", 1000L)
            )
            else -> emptyList()
        }
    }
}

/** The Calendar style page: live preview, event colors, display toggles. */
@Composable
fun CalendarStyleScreen(onBack: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val settings by AppSettings.settings.collectAsState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(scheme.background)
            .padding(bottom = 20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    painter = painterResource(R.drawable.ic_arrow_left),
                    contentDescription = stringResource(R.string.back),
                    tint = scheme.onSurface
                )
            }
            Text(
                text = stringResource(R.string.settings_calendar_style),
                fontSize = Dimens.HeaderTitle,
                fontWeight = FontWeight.Bold,
                color = scheme.onSurface
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            StylePreviewCard()
            SettingsSection(label = stringResource(R.string.settings_event_colors)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MarkerStyle.entries.forEach { style ->
                        EventColorOption(
                            style = style,
                            selected = settings.markerStyle == style,
                            onClick = { AppSettings.setMarkerStyle(style) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(Dimens.SettingsContainerCorner))
                    .background(scheme.surface)
            ) {
                SwitchRow(
                    title = stringResource(R.string.settings_wrap_titles),
                    description = stringResource(R.string.settings_wrap_titles_desc),
                    checked = settings.wrapEventTitles,
                    onCheckedChange = { AppSettings.setWrapEventTitles(it) }
                )
                SectionDivider()
                SwitchRow(
                    title = stringResource(R.string.settings_show_gregorian_tiles),
                    description = stringResource(
                        R.string.settings_show_gregorian_tiles_desc
                    ),
                    checked = settings.showGregorianInTiles,
                    onCheckedChange = { AppSettings.setShowGregorianInTiles(it) }
                )
            }
        }
    }
}

/** A mini 2-week grid of real day cells with fake markers. It reads the
 * live marker style and Gregorian toggle, so it updates as they change. */
@Composable
private fun StylePreviewCard() {
    val scheme = MaterialTheme.colorScheme
    val settings by AppSettings.settings.collectAsState()
    val today = remember { LocalDate.now() }
    val todayJalali = Jalali.toJalali(today)
    val cells = remember(today, settings.firstDay) {
        monthCells(todayJalali.year, todayJalali.month, settings.firstDay)
    }
    // The 2 weeks starting with the week that holds today.
    val todayIndex = cells.indexOf(today).coerceAtLeast(0)
    val weekStart = ((todayIndex / 7) * 7).coerceAtMost(28)
    val shown = cells.drop(weekStart).take(14)
    val fakeProvider = remember { PreviewMarkerProvider() }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Dimens.SettingsContainerCorner))
            .background(scheme.surface)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(Dimens.GridGap)
    ) {
        shown.chunked(7).forEach { week ->
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
                            isSelected = date == today,
                            isFriday = PersianNames.isFriday(date),
                            inCurrentMonth = jalali.month == todayJalali.month,
                            markers = fakeProvider.markersFor(date),
                            onClick = {},
                            onLongClick = {},
                            showGregorian = settings.showGregorianInTiles,
                            markerStyle = settings.markerStyle
                        )
                    }
                }
            }
        }
    }
}

/** Six stripes showing how one event-color style renders colors. */
private fun styleStripes(style: MarkerStyle): List<Color> = listOf(
    markerColor(MarkerType.INCOME, -1, style),
    markerColor(MarkerType.EXPENSE, -1, style),
    markerColor(MarkerType.INSTALLMENT, -1, style),
    markerColor(MarkerType.INCOME, 0, style),
    markerColor(MarkerType.INCOME, 2, style),
    markerColor(MarkerType.INCOME, 4, style),
)

/** One event-color option: stripes, label, then a RadioButton. */
@Composable
private fun EventColorOption(
    style: MarkerStyle,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    Column(
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            styleStripes(style).forEach { color ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(Dimens.StyleStripeHeight)
                        .clip(RoundedCornerShape(Dimens.StyleStripeCorner))
                        .background(color)
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(markerStyleLabelRes(style)),
            fontSize = Dimens.StyleOptionLabel,
            color = scheme.onSurface,
            textAlign = TextAlign.Center
        )
        RadioButton(selected = selected, onClick = onClick)
    }
}
