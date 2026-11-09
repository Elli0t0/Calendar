package io.github.elli0t0.calendar.ui.installments

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.elli0t0.calendar.R
import io.github.elli0t0.calendar.data.Installment
import io.github.elli0t0.calendar.data.InstallmentRepository
import io.github.elli0t0.calendar.data.Remind
import io.github.elli0t0.calendar.jalali.Jalali
import io.github.elli0t0.calendar.jalali.PersianNames
import io.github.elli0t0.calendar.ui.common.SheetSaveButton
import io.github.elli0t0.calendar.ui.common.SheetTextField
import io.github.elli0t0.calendar.ui.common.formatGroupedDigits
import io.github.elli0t0.calendar.ui.common.normalizeDigits
import io.github.elli0t0.calendar.ui.common.toPersianDigits
import io.github.elli0t0.calendar.ui.transactions.remindLabelRes
import java.time.LocalDate

private const val MIN_YEAR = 1380
private const val MAX_YEAR = 1450

private val DeleteRed = Color(0xFFE53935)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InstallmentSheet(initial: Installment?, onDismiss: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val nowJalali = remember { Jalali.toJalali(LocalDate.now()) }
    var title by remember { mutableStateOf(initial?.title ?: "") }
    var digits by remember { mutableStateOf(initial?.amountPerMonth?.toString() ?: "") }
    var dayDigits by remember { mutableStateOf(initial?.dayOfMonth?.toString() ?: "1") }
    var countDigits by remember { mutableStateOf(initial?.count?.toString() ?: "12") }
    var startYear by remember { mutableStateOf(initial?.startYear ?: nowJalali.year) }
    var startMonth by remember { mutableStateOf(initial?.startMonth ?: nowJalali.month) }
    var remind by remember { mutableStateOf(initial?.remind ?: Remind.NONE) }
    var showRemindDialog by remember { mutableStateOf(false) }
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
    val amount = digits.toLongOrNull() ?: 0L
    val day = dayDigits.toIntOrNull() ?: 0
    val count = countDigits.toIntOrNull() ?: 0
    val canSave = title.isNotBlank() && amount > 0 && day in 1..31 && count > 0

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp)
                .imePadding()
        ) {
            Text(
                text = stringResource(if (initial == null) R.string.inst_new else R.string.inst_edit),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = scheme.onSurface
            )
            Spacer(modifier = Modifier.height(16.dp))
            SheetTextField(
                value = title,
                onValueChange = { title = it },
                label = stringResource(R.string.tx_title)
            )
            Spacer(modifier = Modifier.height(12.dp))
            SheetTextField(
                value = formatGroupedDigits(digits),
                onValueChange = { digits = normalizeDigits(it) },
                label = stringResource(R.string.inst_amount),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                SheetTextField(
                    value = dayDigits,
                    onValueChange = { dayDigits = normalizeDigits(it).take(2) },
                    label = stringResource(R.string.inst_day),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(12.dp))
                SheetTextField(
                    value = countDigits,
                    onValueChange = { countDigits = normalizeDigits(it).take(3) },
                    label = stringResource(R.string.inst_count),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.inst_first_month),
                fontSize = 14.sp,
                color = scheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                MonthPickerColumn(
                    value = PersianNames.jalaliMonthName(startMonth),
                    onUp = {
                        if (startMonth == 12) {
                            startMonth = 1
                            startYear = minOf(startYear + 1, MAX_YEAR)
                        } else {
                            startMonth++
                        }
                    },
                    onDown = {
                        if (startMonth == 1) {
                            startMonth = 12
                            startYear = maxOf(startYear - 1, MIN_YEAR)
                        } else {
                            startMonth--
                        }
                    }
                )
                MonthPickerColumn(
                    value = toPersianDigits(startYear.toString()),
                    onUp = { if (startYear < MAX_YEAR) startYear++ },
                    onDown = { if (startYear > MIN_YEAR) startYear-- }
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
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
            Spacer(modifier = Modifier.height(16.dp))
            SheetSaveButton(
                onClick = {
                    if (initial == null) {
                        InstallmentRepository.add(
                            title.trim(), amount, day, count, startYear, startMonth, remind
                        )
                    } else {
                        InstallmentRepository.update(
                            initial.copy(
                                title = title.trim(),
                                amountPerMonth = amount,
                                dayOfMonth = day,
                                count = count,
                                startYear = startYear,
                                startMonth = startMonth,
                                paid = initial.paid.filter { it < count }.toSet(),
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
                TextButton(
                    onClick = {
                        InstallmentRepository.delete(initial.id)
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.tx_delete), color = DeleteRed)
                }
            }
        }
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
private fun MonthPickerColumn(value: String, onUp: () -> Unit, onDown: () -> Unit) {
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
