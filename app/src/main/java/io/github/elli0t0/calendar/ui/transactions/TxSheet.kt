package io.github.elli0t0.calendar.ui.transactions

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.platform.LocalContext
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.elli0t0.calendar.R
import io.github.elli0t0.calendar.data.AppSettings
import io.github.elli0t0.calendar.data.MarkerType
import io.github.elli0t0.calendar.data.Remind
import io.github.elli0t0.calendar.data.Repeat
import io.github.elli0t0.calendar.data.Transaction
import io.github.elli0t0.calendar.data.TxRepository
import io.github.elli0t0.calendar.data.TxType
import io.github.elli0t0.calendar.ui.calendar.markerColor
import io.github.elli0t0.calendar.ui.common.formatGroupedDigits
import io.github.elli0t0.calendar.ui.common.fullJalaliDate
import io.github.elli0t0.calendar.ui.common.SheetSaveButton
import io.github.elli0t0.calendar.ui.common.SheetTextField
import io.github.elli0t0.calendar.ui.common.normalizeDigits
import io.github.elli0t0.calendar.ui.theme.EventPalette
import java.time.LocalDate

private val IncomeGreen = Color(0xFF43A047)
private val ExpenseRed = Color(0xFFE53935)

/** String resource for a repeat choice. */
fun repeatLabelRes(repeat: Repeat): Int = when (repeat) {
    Repeat.NONE -> R.string.repeat_none
    Repeat.DAILY -> R.string.repeat_daily
    Repeat.WEEKLY -> R.string.repeat_weekly
    Repeat.MONTHLY -> R.string.repeat_monthly
    Repeat.YEARLY -> R.string.repeat_yearly
}

/** String resource for a reminder choice. */
fun remindLabelRes(remind: Remind): Int = when (remind) {
    Remind.NONE -> R.string.remind_none
    Remind.ON_DAY -> R.string.remind_on_day
    Remind.ONE_DAY_BEFORE -> R.string.remind_one_day_before
    Remind.TWO_DAYS_BEFORE -> R.string.remind_two_days_before
    Remind.ONE_WEEK_BEFORE -> R.string.remind_one_week_before
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TxSheet(
    initial: Transaction?,
    initialDate: LocalDate,
    onDismiss: () -> Unit,
) {
    val settings by AppSettings.settings.collectAsState()
    var type by remember { mutableStateOf(initial?.type ?: TxType.INCOME) }
    var title by remember { mutableStateOf(initial?.title ?: "") }
    var digits by remember { mutableStateOf(initial?.amount?.toString() ?: "") }
    var date by remember { mutableStateOf(initial?.let { LocalDate.ofEpochDay(it.epochDay) } ?: initialDate) }
    var note by remember { mutableStateOf(initial?.note ?: "") }
    var colorIndex by remember { mutableStateOf(initial?.colorIndex ?: -1) }
    var repeat by remember { mutableStateOf(initial?.repeat ?: Repeat.NONE) }
    var repeatUntil by remember {
        mutableStateOf(initial?.repeatUntilEpochDay?.let { LocalDate.ofEpochDay(it) })
    }
    var remind by remember { mutableStateOf(initial?.remind ?: Remind.NONE) }
    var showDateDialog by remember { mutableStateOf(false) }
    var showRepeatDialog by remember { mutableStateOf(false) }
    var showEndDialog by remember { mutableStateOf(false) }
    var showRemindDialog by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    // Notification permission is asked only when the user picks a reminder,
    // never at app start. Below Android 13 it is granted at install time.
    val context = LocalContext.current
    var hasNotificationPermission by remember {
        mutableStateOf(
            Build.VERSION.SDK_INT < 33 ||
                ContextCompat.checkSelfPermission(
                    context, Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> hasNotificationPermission = granted }
    val scheme = MaterialTheme.colorScheme
    val amount = digits.toLongOrNull() ?: 0L
    val canSave = title.isNotBlank() && amount > 0
    // A one-off event never ends; the end date is never before the start date.
    val untilEpochDay =
        if (repeat == Repeat.NONE) null else repeatUntil?.let { maxOf(it, date) }?.toEpochDay()
    val titleFocus = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        if (initial == null) titleFocus.requestFocus()
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        windowInsets = WindowInsets(0)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
        ) {
            Column(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
            ) {
            Text(
                text = stringResource(if (initial == null) R.string.tx_new else R.string.tx_edit),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = scheme.onSurface
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                TypeChip(
                    label = stringResource(R.string.tx_income),
                    selected = type == TxType.INCOME,
                    selectedColor = IncomeGreen,
                    onClick = { type = TxType.INCOME }
                )
                Spacer(modifier = Modifier.width(12.dp))
                TypeChip(
                    label = stringResource(R.string.tx_expense),
                    selected = type == TxType.EXPENSE,
                    selectedColor = ExpenseRed,
                    onClick = { type = TxType.EXPENSE }
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            SheetTextField(
                value = title,
                onValueChange = { title = it },
                label = stringResource(R.string.tx_title),
                modifier = Modifier.focusRequester(titleFocus)
            )
            Spacer(modifier = Modifier.height(12.dp))
            SheetTextField(
                value = formatGroupedDigits(digits),
                onValueChange = { digits = normalizeDigits(it) },
                label = stringResource(R.string.tx_amount),
                suffix = { Text(stringResource(R.string.tx_toman)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .border(1.dp, scheme.outline, RoundedCornerShape(14.dp))
                    .clickable { showDateDialog = true }
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_date_range),
                    contentDescription = null,
                    tint = scheme.primary
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(text = fullJalaliDate(date), fontSize = 16.sp, color = scheme.onSurface)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .border(1.dp, scheme.outline, RoundedCornerShape(14.dp))
                    .clickable { showRepeatDialog = true }
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_repeat),
                    contentDescription = null,
                    tint = scheme.primary
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(text = stringResource(R.string.repeat_title), fontSize = 16.sp, color = scheme.onSurface)
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = stringResource(repeatLabelRes(repeat)),
                    fontSize = 14.sp,
                    color = scheme.onSurfaceVariant
                )
            }
            if (repeat != Repeat.NONE) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .border(1.dp, scheme.outline, RoundedCornerShape(14.dp))
                        .clickable { showEndDialog = true }
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_date_range),
                        contentDescription = null,
                        tint = scheme.primary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(text = stringResource(R.string.repeat_ends), fontSize = 16.sp, color = scheme.onSurface)
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = repeatUntil?.let { fullJalaliDate(it) }
                            ?: stringResource(R.string.repeat_never),
                        fontSize = 14.sp,
                        color = scheme.onSurfaceVariant
                    )
                    if (repeatUntil != null) {
                        IconButton(onClick = { repeatUntil = null }) {
                            Icon(
                                painter = painterResource(R.drawable.ic_close),
                                contentDescription = stringResource(R.string.repeat_clear),
                                tint = scheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .border(1.dp, scheme.outline, RoundedCornerShape(14.dp))
                    .clickable { showRemindDialog = true }
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_notification),
                    contentDescription = null,
                    tint = scheme.primary
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(text = stringResource(R.string.remind_title), fontSize = 16.sp, color = scheme.onSurface)
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = stringResource(remindLabelRes(remind)),
                    fontSize = 14.sp,
                    color = scheme.onSurfaceVariant
                )
            }
            if (remind != Remind.NONE && !hasNotificationPermission) {
                Text(
                    text = stringResource(R.string.remind_permission_hint),
                    fontSize = 13.sp,
                    color = scheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            // The 9 color circles spread across the full width; if they do
            // not fit, the row scrolls instead of clipping.
            val circles: @Composable RowScope.() -> Unit = {
                ColorCircle(
                    color = markerColor(
                        if (type == TxType.INCOME) MarkerType.INCOME else MarkerType.EXPENSE,
                        -1, settings.markerStyle
                    ),
                    selected = colorIndex == -1,
                    onClick = { colorIndex = -1 }
                )
                EventPalette.forEachIndexed { index, paletteColor ->
                    ColorCircle(
                        color = paletteColor,
                        selected = colorIndex == index,
                        onClick = { colorIndex = index }
                    )
                }
            }
            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                if (28.dp * (1 + EventPalette.size) <= maxWidth) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        circles()
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        circles()
                        // 24dp of space after the last circle, inside the scroll.
                        Spacer(modifier = Modifier.width(24.dp))
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            SheetTextField(
                value = note,
                onValueChange = { note = it },
                label = stringResource(R.string.tx_note),
                singleLine = false,
                maxLines = 4
            )
            }
            // Pinned to the bottom, always visible above the keyboard.
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(top = 16.dp, bottom = 24.dp)
            ) {
            SheetSaveButton(
                onClick = {
                    if (initial == null) {
                        TxRepository.add(
                            type, title.trim(), amount, date, note.trim(), colorIndex,
                            repeat, untilEpochDay, remind
                        )
                    } else {
                        TxRepository.update(
                            initial.copy(
                                type = type,
                                title = title.trim(),
                                amount = amount,
                                epochDay = date.toEpochDay(),
                                note = note.trim(),
                                colorIndex = colorIndex,
                                repeat = repeat,
                                repeatUntilEpochDay = untilEpochDay,
                                remind = remind
                            )
                        )
                    }
                    onDismiss()
                },
                enabled = canSave,
                label = stringResource(R.string.tx_save)
            )
            if (initial != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    TextButton(
                        onClick = {
                            TxRepository.add(
                                type, title.trim(), amount, date, note.trim(), colorIndex,
                                repeat, untilEpochDay, remind
                            )
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(stringResource(R.string.tx_copy))
                    }
                    TextButton(
                        onClick = { showDeleteConfirm = true },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(stringResource(R.string.tx_delete), color = ExpenseRed)
                    }
                }
                if (showDeleteConfirm) {
                    AlertDialog(
                        onDismissRequest = { showDeleteConfirm = false },
                        // Deleting a repeating event deletes the whole series.
                        title = {
                            Text(
                                stringResource(
                                    if (initial.repeat != Repeat.NONE) {
                                        R.string.repeat_delete_confirm
                                    } else {
                                        R.string.tx_delete_confirm
                                    }
                                )
                            )
                        },
                        confirmButton = {
                            TextButton(onClick = {
                                TxRepository.delete(initial.id)
                                showDeleteConfirm = false
                                onDismiss()
                            }) {
                                Text(stringResource(R.string.tx_delete), color = ExpenseRed)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showDeleteConfirm = false }) {
                                Text(stringResource(R.string.tx_dismiss))
                            }
                        }
                    )
                }
            }
            }
        }
    }
    if (showDateDialog) {
        JalaliDatePicker(
            current = date,
            onDismiss = { showDateDialog = false },
            onConfirm = { date = it; showDateDialog = false }
        )
    }
    if (showRepeatDialog) {
        AlertDialog(
            onDismissRequest = { showRepeatDialog = false },
            title = { Text(stringResource(R.string.repeat_title)) },
            text = {
                Column {
                    Repeat.entries.forEach { option ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    repeat = option
                                    if (option == Repeat.NONE) repeatUntil = null
                                    showRepeatDialog = false
                                }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = repeat == option,
                                onClick = {
                                    repeat = option
                                    if (option == Repeat.NONE) repeatUntil = null
                                    showRepeatDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(repeatLabelRes(option)),
                                fontSize = 16.sp,
                                color = scheme.onSurface
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showRepeatDialog = false }) {
                    Text(stringResource(R.string.tx_dismiss))
                }
            }
        )
    }
    if (showEndDialog) {
        JalaliDatePicker(
            current = repeatUntil ?: date,
            onDismiss = { showEndDialog = false },
            onConfirm = {
                // The end date can't be before the start date.
                repeatUntil = maxOf(it, date)
                showEndDialog = false
            }
        )
    }
    if (showRemindDialog) {
        AlertDialog(
            onDismissRequest = { showRemindDialog = false },
            title = { Text(stringResource(R.string.remind_title)) },
            text = {
                Column {
                    Remind.entries.forEach { option ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    remind = option
                                    showRemindDialog = false
                                    if (option != Remind.NONE && !hasNotificationPermission &&
                                        Build.VERSION.SDK_INT >= 33
                                    ) {
                                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    }
                                }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = remind == option,
                                onClick = {
                                    remind = option
                                    showRemindDialog = false
                                    if (option != Remind.NONE && !hasNotificationPermission &&
                                        Build.VERSION.SDK_INT >= 33
                                    ) {
                                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    }
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(remindLabelRes(option)),
                                fontSize = 16.sp,
                                color = scheme.onSurface
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showRemindDialog = false }) {
                    Text(stringResource(R.string.tx_dismiss))
                }
            }
        )
    }
}

@Composable
private fun RowScope.TypeChip(
    label: String,
    selected: Boolean,
    selectedColor: Color,
    onClick: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(20.dp))
            .background(
                if (selected) selectedColor.copy(alpha = 0.2f) else scheme.surfaceContainerHigh
            )
            .then(
                if (selected) {
                    Modifier.border(1.5.dp, selectedColor, RoundedCornerShape(20.dp))
                } else {
                    Modifier
                }
            )
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (selected) selectedColor else scheme.onSurfaceVariant,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

/** One color choice: a 32dp circle, with a ring and a check when selected. */
@Composable
private fun ColorCircle(color: Color, selected: Boolean, onClick: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(color)
            .then(if (selected) Modifier.border(2.dp, scheme.onSurface, CircleShape) else Modifier)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (selected) {
            Icon(
                painter = painterResource(R.drawable.ic_check),
                contentDescription = null,
                tint = scheme.inverseOnSurface,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}
