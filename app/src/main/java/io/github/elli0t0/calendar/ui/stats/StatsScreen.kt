package io.github.elli0t0.calendar.ui.stats

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.elli0t0.calendar.R
import io.github.elli0t0.calendar.data.InstallmentRepository
import io.github.elli0t0.calendar.data.TxRepository
import io.github.elli0t0.calendar.data.dueDate
import io.github.elli0t0.calendar.jalali.Jalali
import io.github.elli0t0.calendar.jalali.PersianNames
import io.github.elli0t0.calendar.ui.calendar.addJalaliMonths
import io.github.elli0t0.calendar.ui.common.formatAmount
import io.github.elli0t0.calendar.ui.common.toPersianDigits
import java.time.LocalDate

private val IncomeGreen = Color(0xFF43A047)
private val ExpenseRed = Color(0xFFE53935)
private val DueOrange = Color(0xFFFB8C00)

@Composable
fun StatsScreen(onMenuClick: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val transactions by TxRepository.items.collectAsState()
    val installments by InstallmentRepository.items.collectAsState()
    val todayJalali = remember { Jalali.toJalali(LocalDate.now()) }
    var selectedYear by remember { mutableStateOf(todayJalali.year) }
    var selectedMonth by remember { mutableStateOf(todayJalali.month) }

    val totals = monthlyTotals(transactions, selectedYear, selectedMonth)
    val balance = totals.income - totals.expense
    val dueAmounts = installments.flatMap { inst ->
        (0 until inst.count).filter { k ->
            k !in inst.paid && isJalaliMonth(dueDate(inst, k), selectedYear, selectedMonth)
        }.map { inst.amountPerMonth }
    }
    val months = lastSixMonths(selectedYear, selectedMonth)
    val chartTotals = months.map { (y, m) -> monthlyTotals(transactions, y, m) }
    val hasData = totals.income != 0L || totals.expense != 0L

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(scheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(bottom = 20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onMenuClick) {
                Icon(
                    painter = painterResource(R.drawable.ic_menu),
                    contentDescription = stringResource(R.string.menu)
                )
            }
            Text(
                text = stringResource(R.string.tab_stats),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = scheme.onSurface,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.width(48.dp))
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                val prev = addJalaliMonths(selectedYear, selectedMonth, -1)
                selectedYear = prev.first
                selectedMonth = prev.second
            }) {
                Icon(
                    painter = painterResource(R.drawable.ic_arrow_right),
                    contentDescription = stringResource(R.string.previous_month)
                )
            }
            Text(
                text = "${PersianNames.jalaliMonthName(selectedMonth)} " +
                    toPersianDigits(selectedYear.toString()),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = scheme.onSurface,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
            IconButton(onClick = {
                val next = addJalaliMonths(selectedYear, selectedMonth, 1)
                selectedYear = next.first
                selectedMonth = next.second
            }) {
                Icon(
                    painter = painterResource(R.drawable.ic_arrow_left),
                    contentDescription = stringResource(R.string.next_month)
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SummaryMiniCard(
                label = stringResource(R.string.tx_income),
                value = totals.income,
                color = IncomeGreen,
                modifier = Modifier.weight(1f)
            )
            SummaryMiniCard(
                label = stringResource(R.string.tx_expense),
                value = totals.expense,
                color = ExpenseRed,
                modifier = Modifier.weight(1f)
            )
            SummaryMiniCard(
                label = stringResource(R.string.tx_balance),
                value = balance,
                color = if (balance >= 0) IncomeGreen else ExpenseRed,
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = scheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = stringResource(R.string.stats_due_title),
                    fontSize = 13.sp,
                    color = scheme.onSurfaceVariant
                )
                Text(
                    text = formatAmount(dueAmounts.sum()),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = DueOrange
                )
                Text(
                    text = "${toPersianDigits(dueAmounts.size.toString())} " +
                        stringResource(R.string.stats_installment_word),
                    fontSize = 14.sp,
                    color = DueOrange
                )
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
        if (!hasData) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.stats_empty),
                    fontSize = 16.sp,
                    color = scheme.onSurfaceVariant
                )
            }
        } else {
            BarChart(months = months, totals = chartTotals)
        }
    }
}

@Composable
private fun RowScope.SummaryMiniCard(
    label: String,
    value: Long,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = formatAmount(value),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
private fun BarChart(months: List<Pair<Int, Int>>, totals: List<MonthTotal>) {
    val max = (totals.flatMap { listOf(it.income, it.expense) }.maxOrNull() ?: 0L)
        .toFloat().coerceAtLeast(1f)
    val animatedIncome = totals.map { t ->
        animateFloatAsState(
            targetValue = t.income.toFloat() / max,
            animationSpec = tween(400),
            label = "incomeBar"
        ).value
    }
    val animatedExpense = totals.map { t ->
        animateFloatAsState(
            targetValue = t.expense.toFloat() / max,
            animationSpec = tween(400),
            label = "expenseBar"
        ).value
    }
    Column {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
        ) {
            val groupWidth = size.width / months.size
            val barWidth = 10.dp.toPx()
            val gap = 4.dp.toPx()
            totals.forEachIndexed { i, _ ->
                val cx = groupWidth * i + groupWidth / 2f
                val incomeH = size.height * animatedIncome[i]
                val expenseH = size.height * animatedExpense[i]
                drawRoundRect(
                    color = IncomeGreen,
                    topLeft = Offset(cx - barWidth - gap / 2f, size.height - incomeH),
                    size = Size(barWidth, incomeH),
                    cornerRadius = CornerRadius(5.dp.toPx())
                )
                drawRoundRect(
                    color = ExpenseRed,
                    topLeft = Offset(cx + gap / 2f, size.height - expenseH),
                    size = Size(barWidth, expenseH),
                    cornerRadius = CornerRadius(5.dp.toPx())
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            months.forEach { (_, m) ->
                Text(
                    text = PersianNames.jalaliMonthName(m),
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
