package io.github.elli0t0.calendar.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.elli0t0.calendar.R
import io.github.elli0t0.calendar.data.AppLanguage
import io.github.elli0t0.calendar.data.AppSettings
import io.github.elli0t0.calendar.data.FirstDay
import io.github.elli0t0.calendar.data.ThemeMode
import io.github.elli0t0.calendar.notify.ReminderScheduler
import io.github.elli0t0.calendar.ui.common.toPersianDigits
import io.github.elli0t0.calendar.ui.theme.Dimens

private fun firstDayLabelRes(firstDay: FirstDay): Int = when (firstDay) {
    FirstDay.SATURDAY -> R.string.first_day_saturday
    FirstDay.SUNDAY -> R.string.first_day_sunday
    FirstDay.MONDAY -> R.string.first_day_monday
}

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onLanguageChanged: () -> Unit = {},
) {
    val scheme = MaterialTheme.colorScheme
    val settings by AppSettings.settings.collectAsState()
    var showFirstDayDialog by remember { mutableStateOf(false) }
    var showTimeDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var showStyle by remember { mutableStateOf(false) }
    if (showStyle) {
        CalendarStyleScreen(onBack = { showStyle = false })
        return
    }
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
                text = stringResource(R.string.settings_title),
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
            SettingsSection(label = stringResource(R.string.settings_appearance)) {
                ValueRow(
                    title = stringResource(R.string.settings_theme),
                    value = stringResource(themeModeLabelRes(settings.themeMode)),
                    onClick = { showThemeDialog = true }
                )
                SectionDivider()
                ValueRow(
                    title = stringResource(R.string.settings_language),
                    value = languageName(settings.language),
                    onClick = { showLanguageDialog = true }
                )
            }
            SettingsSection(label = stringResource(R.string.settings_calendar)) {
                ValueRow(
                    title = stringResource(R.string.settings_first_day),
                    value = stringResource(firstDayLabelRes(settings.firstDay)),
                    onClick = { showFirstDayDialog = true }
                )
                SectionDivider()
                SwitchRow(
                    title = stringResource(R.string.settings_show_gregorian_tiles),
                    checked = settings.showGregorianInTiles,
                    onCheckedChange = { AppSettings.setShowGregorianInTiles(it) }
                )
                SectionDivider()
                SwitchRow(
                    title = stringResource(R.string.settings_show_gregorian_header),
                    checked = settings.showGregorianInHeader,
                    onCheckedChange = { AppSettings.setShowGregorianInHeader(it) }
                )
            }
            SettingsSection(label = stringResource(R.string.settings_alerts)) {
                ValueRow(
                    title = stringResource(R.string.settings_reminder_time),
                    value = toPersianDigits(
                        "%02d:%02d".format(
                            settings.reminderHour, settings.reminderMinute
                        )
                    ),
                    onClick = { showTimeDialog = true }
                )
            }
            SettingsSection(label = stringResource(R.string.settings_calendar_style)) {
                NavRow(
                    title = stringResource(R.string.settings_calendar_style),
                    onClick = { showStyle = true }
                )
            }
        }
    }
    if (showThemeDialog) {
        ThemeDialog(
            current = settings.themeMode,
            onSelect = { mode ->
                showThemeDialog = false
                AppSettings.setThemeMode(mode)
            },
            onDismiss = { showThemeDialog = false }
        )
    }
    if (showLanguageDialog) {
        LanguageDialog(
            current = settings.language,
            onSelect = { lang ->
                showLanguageDialog = false
                if (lang != settings.language) {
                    AppSettings.setLanguage(lang)
                    onLanguageChanged()
                }
            },
            onDismiss = { showLanguageDialog = false }
        )
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

/** A section: small label, then one rounded container holding the rows. */
@Composable
fun SettingsSection(
    label: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontSize = Dimens.SettingsLabel,
            color = scheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(Dimens.SettingsContainerCorner))
                .background(scheme.surface),
            content = content
        )
    }
}

/** A 1dp divider between rows, inset 16dp from the container edges. */
@Composable
fun SectionDivider() {
    val scheme = MaterialTheme.colorScheme
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 16.dp),
        thickness = 1.dp,
        color = scheme.outlineVariant
    )
}

/** A row with a value: title, then the current value in primary below it. */
@Composable
fun ValueRow(title: String, value: String, onClick: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    Column(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Text(title, fontSize = Dimens.SettingsRowTitle, color = scheme.onSurface)
        Text(value, fontSize = Dimens.SettingsRowValue, color = scheme.primary)
    }
}

/** A row with a Switch: title + optional muted description, Switch at the end. */
@Composable
fun SwitchRow(
    title: String,
    description: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = Dimens.SettingsRowTitle, color = scheme.onSurface)
            if (description != null) {
                Text(
                    description,
                    fontSize = Dimens.SettingsRowDescription,
                    color = scheme.onSurfaceVariant
                )
            }
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

/** A row that opens a page: title + a chevron at the end. */
@Composable
fun NavRow(title: String, onClick: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = Dimens.SettingsRowTitle,
            color = scheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        Icon(
            painter = painterResource(R.drawable.ic_arrow_right),
            contentDescription = null,
            tint = scheme.onSurfaceVariant
        )
    }
}

/** String resource for a theme mode. */
private fun themeModeLabelRes(mode: ThemeMode): Int = when (mode) {
    ThemeMode.SYSTEM -> R.string.theme_system
    ThemeMode.LIGHT -> R.string.theme_light
    ThemeMode.DARK -> R.string.theme_dark
}

/** A language's name in its own language (never translated). */
private fun languageName(lang: AppLanguage): String = when (lang) {
    AppLanguage.FA -> "فارسی"
    AppLanguage.EN -> "English"
}

/** Theme picker: each option is a radio row. */
@Composable
private fun ThemeDialog(
    current: ThemeMode,
    onSelect: (ThemeMode) -> Unit,
    onDismiss: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.settings_theme)) },
        text = {
            Column {
                ThemeMode.entries.forEach { option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(option) }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = current == option,
                            onClick = { onSelect(option) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(themeModeLabelRes(option)),
                            fontSize = Dimens.SettingsRowTitle,
                            color = scheme.onSurface
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.tx_dismiss))
            }
        }
    )
}

/** Language picker: each option is always shown in its own language. */
@Composable
private fun LanguageDialog(
    current: AppLanguage,
    onSelect: (AppLanguage) -> Unit,
    onDismiss: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.settings_language)) },
        text = {
            Column {
                AppLanguage.entries.forEach { option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(option) }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = current == option,
                            onClick = { onSelect(option) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = languageName(option),
                            fontSize = Dimens.SettingsRowTitle,
                            color = scheme.onSurface
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.tx_dismiss))
            }
        }
    )
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
                            fontSize = Dimens.SettingsRowTitle,
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
    val scheme = MaterialTheme.colorScheme
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        IconButton(onClick = onUp) {
            Icon(
                painter = painterResource(R.drawable.ic_arrow_up),
                contentDescription = null,
                tint = scheme.onSurface
            )
        }
        Text(text = value, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        IconButton(onClick = onDown) {
            Icon(
                painter = painterResource(R.drawable.ic_arrow_down),
                contentDescription = null,
                tint = scheme.onSurface
            )
        }
    }
}
