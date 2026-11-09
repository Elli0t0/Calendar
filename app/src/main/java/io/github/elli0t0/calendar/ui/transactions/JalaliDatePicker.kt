package io.github.elli0t0.calendar.ui.transactions

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import io.github.elli0t0.calendar.R
import io.github.elli0t0.calendar.data.AppSettings
import io.github.elli0t0.calendar.jalali.Jalali
import io.github.elli0t0.calendar.jalali.PersianNames
import io.github.elli0t0.calendar.jalali.toPersianDigits
import io.github.elli0t0.calendar.ui.calendar.addJalaliMonths
import io.github.elli0t0.calendar.ui.calendar.fridayIndex
import io.github.elli0t0.calendar.ui.calendar.monthCells
import io.github.elli0t0.calendar.ui.calendar.orderedWeekdayLetters
import java.time.LocalDate

/**
 * A month-grid date picker. The shown month starts at [current]; the user
 * picks a day, jumps to today, or cancels. Confirm returns the picked day.
 */
@Composable
fun JalaliDatePicker(
    current: LocalDate,
    onDismiss: () -> Unit,
    onConfirm: (LocalDate) -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val settings by AppSettings.settings.collectAsState()
    val today = remember { LocalDate.now() }
    var selected by remember { mutableStateOf(current) }
    val start = Jalali.toJalali(current)
    var year by remember { mutableStateOf(start.year) }
    var month by remember { mutableStateOf(start.month) }

    fun goToToday() {
        selected = today
        val t = Jalali.toJalali(today)
        year = t.year
        month = t.month
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = scheme.surface
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${PersianNames.jalaliMonthName(month)} ${year.toPersianDigits()}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = scheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = {
                        val prev = addJalaliMonths(year, month, -1)
                        year = prev.first
                        month = prev.second
                    }) {
                        Icon(
                            painter = painterResource(R.drawable.ic_arrow_left),
                            contentDescription = stringResource(R.string.previous_month)
                        )
                    }
                    IconButton(onClick = {
                        val next = addJalaliMonths(year, month, 1)
                        year = next.first
                        month = next.second
                    }) {
                        Icon(
                            painter = painterResource(R.drawable.ic_arrow_right),
                            contentDescription = stringResource(R.string.next_month)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                    orderedWeekdayLetters(
                        PersianNames.weekdayLetters(), settings.firstDay
                    ).forEachIndexed { index, letter ->
                        Box(
                            modifier = Modifier.size(36.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = letter,
                                fontSize = 13.sp,
                                color = if (index == fridayIndex(settings.firstDay)) {
                                    Color(0xFFE53935)
                                } else {
                                    scheme.onSurfaceVariant
                                }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    monthCells(year, month, settings.firstDay).chunked(7).forEach { week ->
                        Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                            week.forEach { date ->
                                val jalali = Jalali.toJalali(date)
                                PickerDay(
                                    day = jalali.day,
                                    isToday = date == today,
                                    isSelected = date == selected,
                                    isFriday = PersianNames.isFriday(date),
                                    inCurrentMonth = jalali.month == month,
                                    onClick = { selected = date }
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = { goToToday() }) {
                        Text(stringResource(R.string.today))
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.tx_dismiss))
                    }
                    TextButton(onClick = { onConfirm(selected) }) {
                        Text(stringResource(R.string.tx_confirm))
                    }
                }
            }
        }
    }
}

@Composable
private fun PickerDay(
    day: Int,
    isToday: Boolean,
    isSelected: Boolean,
    isFriday: Boolean,
    inCurrentMonth: Boolean,
    onClick: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    var boxModifier = Modifier
        .size(36.dp)
        .clip(RoundedCornerShape(8.dp))
        .clickable(onClick = onClick)
    if (isSelected) {
        boxModifier = boxModifier.background(scheme.primary)
    } else if (isToday) {
        boxModifier = boxModifier.border(2.dp, scheme.primary, RoundedCornerShape(8.dp))
    }
    if (!inCurrentMonth) {
        boxModifier = boxModifier.alpha(0.35f)
    }
    val numberColor = when {
        isSelected -> scheme.onPrimary
        isFriday -> Color(0xFFE53935)
        else -> scheme.onSurface
    }
    Box(
        modifier = boxModifier,
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = day.toPersianDigits(),
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = numberColor
        )
    }
}
