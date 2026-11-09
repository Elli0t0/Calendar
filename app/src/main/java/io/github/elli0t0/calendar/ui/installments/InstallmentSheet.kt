package io.github.elli0t0.calendar.ui.installments

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
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
import io.github.elli0t0.calendar.jalali.Jalali
import io.github.elli0t0.calendar.jalali.PersianNames
import io.github.elli0t0.calendar.ui.common.formatGroupedDigits
import io.github.elli0t0.calendar.ui.common.normalizeDigits
import io.github.elli0t0.calendar.ui.common.toPersianDigits
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
                label = { Text(stringResource(R.string.inst_amount)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = dayDigits,
                    onValueChange = { dayDigits = normalizeDigits(it).take(2) },
                    label = { Text(stringResource(R.string.inst_day)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(12.dp))
                OutlinedTextField(
                    value = countDigits,
                    onValueChange = { countDigits = normalizeDigits(it).take(3) },
                    label = { Text(stringResource(R.string.inst_count)) },
                    singleLine = true,
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
            Button(
                onClick = {
                    if (initial == null) {
                        InstallmentRepository.add(
                            title.trim(), amount, day, count, startYear, startMonth
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
                                paid = initial.paid.filter { it < count }.toSet()
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
}

@Composable
private fun MonthPickerColumn(value: String, onUp: () -> Unit, onDown: () -> Unit) {
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
