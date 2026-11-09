package io.github.elli0t0.calendar.ui.transactions

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.elli0t0.calendar.R
import io.github.elli0t0.calendar.data.Transaction
import io.github.elli0t0.calendar.data.TxRepository
import io.github.elli0t0.calendar.data.TxType
import io.github.elli0t0.calendar.jalali.Jalali
import io.github.elli0t0.calendar.jalali.JalaliDate
import io.github.elli0t0.calendar.jalali.PersianNames
import io.github.elli0t0.calendar.ui.common.toPersianDigits
import java.time.LocalDate

private const val MIN_YEAR = 1380
private const val MAX_YEAR = 1450

private val IncomeGreen = Color(0xFF43A047)
private val ExpenseRed = Color(0xFFE53935)

/** Keeps only digits (Persian or English), capped so the value fits in a Long. */
private fun normalizeDigits(input: String): String {
    val out = StringBuilder()
    for (c in input) {
        when (c) {
            in '0'..'9' -> out.append(c)
            in '۰'..'۹' -> out.append('0' + (c - '۰'))
            in '٠'..'٩' -> out.append('0' + (c - '٠'))
        }
    }
    return out.toString().take(15)
}

private fun formatGroupedDigits(digits: String): String {
    if (digits.isEmpty()) return ""
    val grouped = digits.reversed().chunked(3).joinToString("٬").reversed()
    return toPersianDigits(grouped)
}

private fun fullJalaliDate(date: LocalDate): String {
    val j = Jalali.toJalali(date)
    return "${PersianNames.weekdayName(date)} ${toPersianDigits(j.day.toString())} " +
        "${PersianNames.jalaliMonthName(j.month)} ${toPersianDigits(j.year.toString())}"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TxSheet(
    initial: Transaction?,
    initialDate: LocalDate,
    onDismiss: () -> Unit,
) {
    var type by remember { mutableStateOf(initial?.type ?: TxType.INCOME) }
    var title by remember { mutableStateOf(initial?.title ?: "") }
    var digits by remember { mutableStateOf(initial?.amount?.toString() ?: "") }
    var date by remember { mutableStateOf(initial?.let { LocalDate.ofEpochDay(it.epochDay) } ?: initialDate) }
    var showDateDialog by remember { mutableStateOf(false) }
    val scheme = MaterialTheme.colorScheme
    val amount = digits.toLongOrNull() ?: 0L
    val canSave = title.isNotBlank() && amount > 0

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp)
                .imePadding()
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
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text(stringResource(R.string.tx_title)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = formatGroupedDigits(digits),
                onValueChange = { digits = normalizeDigits(it) },
                label = { Text(stringResource(R.string.tx_amount)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
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
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    if (initial == null) {
                        TxRepository.add(type, title.trim(), amount, date)
                    } else {
                        TxRepository.update(
                            initial.copy(
                                type = type,
                                title = title.trim(),
                                amount = amount,
                                epochDay = date.toEpochDay()
                            )
                        )
                    }
                    onDismiss()
                },
                enabled = canSave,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.tx_save))
            }
            if (initial != null) {
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(
                    onClick = {
                        TxRepository.delete(initial.id)
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.tx_delete), color = ExpenseRed)
                }
            }
        }
    }
    if (showDateDialog) {
        JalaliDatePickerDialog(
            current = date,
            onDismiss = { showDateDialog = false },
            onConfirm = { date = it; showDateDialog = false }
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
    Box(
        modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(20.dp))
            .background(if (selected) selectedColor else MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
private fun JalaliDatePickerDialog(
    current: LocalDate,
    onDismiss: () -> Unit,
    onConfirm: (LocalDate) -> Unit,
) {
    val start = Jalali.toJalali(current)
    var year by remember { mutableStateOf(start.year) }
    var month by remember { mutableStateOf(start.month) }
    var day by remember { mutableStateOf(start.day) }
    fun clampDay() {
        day = minOf(day, Jalali.monthLength(year, month))
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.tx_pick_date)) },
        text = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                PickerColumn(
                    value = toPersianDigits(day.toString()),
                    onUp = { if (day < Jalali.monthLength(year, month)) day++ },
                    onDown = { if (day > 1) day-- }
                )
                PickerColumn(
                    value = PersianNames.jalaliMonthName(month),
                    onUp = {
                        if (month == 12) {
                            month = 1
                            year = minOf(year + 1, MAX_YEAR)
                        } else {
                            month++
                        }
                        clampDay()
                    },
                    onDown = {
                        if (month == 1) {
                            month = 12
                            year = maxOf(year - 1, MIN_YEAR)
                        } else {
                            month--
                        }
                        clampDay()
                    }
                )
                PickerColumn(
                    value = toPersianDigits(year.toString()),
                    onUp = {
                        if (year < MAX_YEAR) {
                            year++
                            clampDay()
                        }
                    },
                    onDown = {
                        if (year > MIN_YEAR) {
                            year--
                            clampDay()
                        }
                    }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(Jalali.toGregorian(JalaliDate(year, month, day))) }) {
                Text(stringResource(R.string.tx_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.tx_cancel))
            }
        }
    )
}

@Composable
private fun PickerColumn(value: String, onUp: () -> Unit, onDown: () -> Unit) {
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
