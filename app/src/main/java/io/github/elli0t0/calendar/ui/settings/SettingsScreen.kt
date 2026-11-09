package io.github.elli0t0.calendar.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.elli0t0.calendar.R
import io.github.elli0t0.calendar.data.AppSettings
import io.github.elli0t0.calendar.data.FirstDay
import io.github.elli0t0.calendar.data.MarkerStyle
import io.github.elli0t0.calendar.data.MarkerType
import io.github.elli0t0.calendar.notify.ReminderScheduler
import io.github.elli0t0.calendar.ui.calendar.markerColor
import io.github.elli0t0.calendar.ui.common.toPersianDigits

private fun firstDayLabelRes(firstDay: FirstDay): Int = when (firstDay) {
    FirstDay.SATURDAY -> R.string.first_day_saturday
    FirstDay.SUNDAY -> R.string.first_day_sunday
    FirstDay.MONDAY -> R.string.first_day_monday
}

private fun markerStyleLabelRes(style: MarkerStyle): Int = when (style) {
    MarkerStyle.VERY_MUTED -> R.string.marker_very_muted
    MarkerStyle.MUTED -> R.string.marker_muted
    MarkerStyle.BRIGHT -> R.string.marker_bright
    MarkerStyle.VERY_BRIGHT -> R.string.marker_very_bright
}

@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val settings by AppSettings.settings.collectAsState()
    var showFirstDayDialog by remember { mutableStateOf(false) }
    var showTimeDialog by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(scheme.background)
            .padding(bottom = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
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
                    text = stringResource(R.string.settings_title),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = scheme.onSurface,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.width(48.dp))
            }
            Spacer(modifier = Modifier.height(16.dp))
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SettingsCard(title = stringResource(R.string.settings_calendar)) {
                    SettingsRow(
                        title = stringResource(R.string.settings_first_day),
                        value = stringResource(firstDayLabelRes(settings.firstDay)),
                        onClick = { showFirstDayDialog = true }
                    )
                    HorizontalDivider(color = scheme.outlineVariant)
                    SettingsRowSwitch(
                        title = stringResource(R.string.settings_show_gregorian_tiles),
                        checked = settings.showGregorianInTiles,
                        onCheckedChange = { AppSettings.setShowGregorianInTiles(it) }
                    )
                    HorizontalDivider(color = scheme.outlineVariant)
                    SettingsRowSwitch(
                        title = stringResource(R.string.settings_show_gregorian_header),
                        checked = settings.showGregorianInHeader,
                        onCheckedChange = { AppSettings.setShowGregorianInHeader(it) }
                    )
                }
                SettingsCard(title = stringResource(R.string.settings_event_colors)) {
                    MarkerStyle.entries.forEachIndexed { index, style ->
                        if (index > 0) {
                            HorizontalDivider(color = scheme.outlineVariant)
                        }
                        MarkerStyleRow(
                            style = style,
                            selected = settings.markerStyle == style,
                            onClick = { AppSettings.setMarkerStyle(style) }
                        )
                    }
                }
                SettingsCard(title = stringResource(R.string.settings_reminders)) {
                    SettingsRow(
                        title = stringResource(R.string.settings_reminder_time),
                        value = toPersianDigits(
                            "%02d:%02d".format(
                                settings.reminderHour, settings.reminderMinute
                            )
                        ),
                        onClick = { showTimeDialog = true }
                    )
                }
            }
        }
    }
    if (showFirstDayDialog) {
        FirstDayDialog(
            current = settings.firstDay,
            onDismiss = { showFirstDayDialog = false }
        )
    }
    if (showTimeDialog) {
        TimePickerDialog(
            hour = settings.reminderHour,
            minute = settings.reminderMinute,
            onDismiss = { showTimeDialog = false }
        )
    }
}

@Composable
private fun SettingsCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    val scheme = MaterialTheme.colorScheme
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = scheme.onSurface,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = scheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                content = content
            )
        }
    }
}

@Composable
private fun SettingsRow(title: String, value: String, onClick: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 16.sp,
            color = scheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            fontSize = 14.sp,
            color = scheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SettingsRowSwitch(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 16.sp,
            color = scheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun MarkerStyleRow(
    style: MarkerStyle,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = stringResource(markerStyleLabelRes(style)),
            fontSize = 16.sp,
            color = scheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            MarkerType.entries.forEach { type ->
                Box(
                    modifier = Modifier
                        .width(24.dp)
                        .height(3.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(markerColor(type, -1, style))
                )
            }
        }
    }
}

@Composable
private fun FirstDayDialog(current: FirstDay, onDismiss: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.settings_first_day)) },
        text = {
            Column {
                FirstDay.entries.forEach { option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                AppSettings.setFirstDay(option)
                                onDismiss()
                            }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = current == option,
                            onClick = {
                                AppSettings.setFirstDay(option)
                                onDismiss()
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(firstDayLabelRes(option)),
                            fontSize = 16.sp,
                            color = scheme.onSurface
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.tx_dismiss))
            }
        }
    )
}

@Composable
private fun TimePickerDialog(hour: Int, minute: Int, onDismiss: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val context = LocalContext.current
    var pickedHour by remember { mutableStateOf(hour) }
    var pickedMinute by remember { mutableStateOf(minute) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.settings_reminder_time)) },
        text = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                UpDownNumber(
                    value = toPersianDigits("%02d".format(pickedHour)),
                    onUp = { pickedHour = (pickedHour + 1) % 24 },
                    onDown = { pickedHour = (pickedHour + 23) % 24 }
                )
                Text(
                    text = ":",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = scheme.onSurface,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                UpDownNumber(
                    value = toPersianDigits("%02d".format(pickedMinute)),
                    onUp = { pickedMinute = (pickedMinute + 1) % 60 },
                    onDown = { pickedMinute = (pickedMinute + 59) % 60 }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                AppSettings.setReminderTime(pickedHour, pickedMinute)
                ReminderScheduler.reschedule(context)
                onDismiss()
            }) {
                Text(stringResource(R.string.tx_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.tx_dismiss))
            }
        }
    )
}

@Composable
private fun UpDownNumber(value: String, onUp: () -> Unit, onDown: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        IconButton(onClick = onUp) {
            Icon(
                painter = painterResource(R.drawable.ic_arrow_up),
                contentDescription = null
            )
        }
        Text(text = value, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        IconButton(onClick = onDown) {
            Icon(
                painter = painterResource(R.drawable.ic_arrow_down),
                contentDescription = null
            )
        }
    }
}
