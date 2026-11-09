package io.github.elli0t0.calendar.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.elli0t0.calendar.R
import io.github.elli0t0.calendar.data.AppSettings
import io.github.elli0t0.calendar.data.Installment
import io.github.elli0t0.calendar.data.InstallmentRepository
import io.github.elli0t0.calendar.data.MarkerStyle
import io.github.elli0t0.calendar.data.MarkerType
import io.github.elli0t0.calendar.data.Transaction
import io.github.elli0t0.calendar.data.TxType
import io.github.elli0t0.calendar.data.dueDate
import io.github.elli0t0.calendar.data.occursOn
import io.github.elli0t0.calendar.ui.common.formatAmount
import java.time.LocalDate

/** One event card: color bar, title, one-line note, amount. An optional
 * round button (e.g. the paid toggle) goes last. */
@Composable
fun DayEventRow(
    barColor: Color,
    title: String,
    note: String?,
    amountText: String,
    amountColor: Color,
    alpha: Float = 1f,
    strikethroughTitle: Boolean = false,
    onClick: (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null,
) {
    val scheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(alpha)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(40.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(barColor)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            // "Let event titles wrap" off keeps titles on one line.
            val settings by AppSettings.settings.collectAsState()
            Text(
                text = title,
                fontSize = 15.sp,
                color = scheme.onSurface,
                textDecoration = if (strikethroughTitle) TextDecoration.LineThrough else null,
                maxLines = if (settings.wrapEventTitles) Int.MAX_VALUE else 1,
                overflow = if (settings.wrapEventTitles) {
                    TextOverflow.Clip
                } else {
                    TextOverflow.Ellipsis
                }
            )
            if (!note.isNullOrBlank()) {
                Text(
                    text = note,
                    fontSize = 13.sp,
                    color = scheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        Text(
            text = amountText,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = amountColor
        )
        if (trailing != null) {
            Spacer(modifier = Modifier.width(12.dp))
            trailing()
        }
    }
}

/** The day's event cards, grouped installments, expenses, incomes. */
@Composable
fun DayEventCards(
    date: LocalDate,
    transactions: List<Transaction>,
    installments: List<Installment>,
    markerStyle: MarkerStyle,
    onEditTx: (Transaction) -> Unit,
    onEditInst: (Installment) -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val dayTxs = transactions.filter { occursOn(it, date) }
    val dayInstallments = installments.flatMap { inst ->
        (0 until inst.count)
            .filter { k -> dueDate(inst, k) == date }
            .map { k -> inst to k }
    }
    // Installments first, then expenses, then incomes.
    dayInstallments.forEach { (inst, k) ->
        val paid = k in inst.paid
        DayEventRow(
            barColor = markerColor(
                MarkerType.INSTALLMENT, -1, markerStyle
            ),
            title = inst.title,
            note = null,
            amountText = stringResource(
                R.string.amount_with_currency,
                formatAmount(inst.amountPerMonth)
            ),
            amountColor = Color(0xFFFB8C00),
            alpha = if (paid) 0.5f else 1f,
            strikethroughTitle = paid,
            onClick = { onEditInst(inst) },
            trailing = {
                val paidLabel = stringResource(R.string.inst_paid)
                val markPaidLabel = stringResource(R.string.inst_mark_paid)
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(
                            if (paid) scheme.primary else Color.Transparent
                        )
                        .border(
                            2.dp,
                            if (paid) scheme.primary else scheme.outline,
                            CircleShape
                        )
                        .clickable {
                            InstallmentRepository.togglePaid(inst.id, k)
                        }
                        .semantics {
                            contentDescription =
                                if (paid) paidLabel else markPaidLabel
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (paid) {
                        Icon(
                            painter = painterResource(R.drawable.ic_check),
                            contentDescription = null,
                            tint = scheme.onPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        )
    }
    dayTxs.filter { it.type == TxType.EXPENSE }.forEach { tx ->
        DayEventRow(
            barColor = markerColor(
                MarkerType.EXPENSE, tx.colorIndex, markerStyle
            ),
            title = tx.title,
            note = tx.note.ifBlank { null },
            amountText = stringResource(
                R.string.amount_with_currency,
                formatAmount(tx.amount)
            ),
            amountColor = Color(0xFFE53935),
            onClick = { onEditTx(tx) }
        )
    }
    dayTxs.filter { it.type == TxType.INCOME }.forEach { tx ->
        DayEventRow(
            barColor = markerColor(
                MarkerType.INCOME, tx.colorIndex, markerStyle
            ),
            title = tx.title,
            note = tx.note.ifBlank { null },
            amountText = stringResource(
                R.string.amount_with_currency,
                formatAmount(tx.amount)
            ),
            amountColor = Color(0xFF43A047),
            onClick = { onEditTx(tx) }
        )
    }
}

/**
 * A day's content: a muted income/expense summary line (non-zero parts
 * only), then the event cards, or the empty state when there is nothing.
 */
@Composable
fun DayEvents(
    date: LocalDate,
    transactions: List<Transaction>,
    installments: List<Installment>,
    markerStyle: MarkerStyle,
    onEditTx: (Transaction) -> Unit,
    onEditInst: (Installment) -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val dayTxs = transactions.filter { occursOn(it, date) }
    val dayInstallments = installments.flatMap { inst ->
        (0 until inst.count)
            .filter { k -> dueDate(inst, k) == date }
            .map { k -> inst to k }
    }
    val dayIncome = dayTxs
        .filter { it.type == TxType.INCOME }.sumOf { it.amount }
    val dayExpense = dayTxs
        .filter { it.type == TxType.EXPENSE }.sumOf { it.amount }
    val summaryText = when {
        dayIncome > 0 && dayExpense > 0 -> "${
            stringResource(R.string.tx_income)
        } ${stringResource(R.string.amount_with_currency, formatAmount(dayIncome))} · ${
            stringResource(R.string.tx_expense)
        } ${stringResource(R.string.amount_with_currency, formatAmount(dayExpense))}"
        dayIncome > 0 -> "${stringResource(R.string.tx_income)} ${
            stringResource(R.string.amount_with_currency, formatAmount(dayIncome))
        }"
        dayExpense > 0 -> "${stringResource(R.string.tx_expense)} ${
            stringResource(R.string.amount_with_currency, formatAmount(dayExpense))
        }"
        else -> null
    }
    if (summaryText != null) {
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = summaryText,
            fontSize = 13.sp,
            color = scheme.onSurfaceVariant
        )
    }
    Spacer(modifier = Modifier.height(12.dp))
    if (dayInstallments.isEmpty() && dayTxs.isEmpty()) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_date_range),
                contentDescription = null,
                tint = scheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.no_events),
                fontSize = 15.sp,
                color = scheme.onSurfaceVariant
            )
        }
    } else {
        DayEventCards(
            date = date,
            transactions = transactions,
            installments = installments,
            markerStyle = markerStyle,
            onEditTx = onEditTx,
            onEditInst = onEditInst
        )
    }
}
