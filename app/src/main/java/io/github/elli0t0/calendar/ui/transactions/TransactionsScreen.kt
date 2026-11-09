package io.github.elli0t0.calendar.ui.transactions

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.elli0t0.calendar.R
import io.github.elli0t0.calendar.data.Transaction
import io.github.elli0t0.calendar.data.TxRepository
import io.github.elli0t0.calendar.data.TxType
import io.github.elli0t0.calendar.jalali.Jalali
import io.github.elli0t0.calendar.jalali.PersianNames
import io.github.elli0t0.calendar.ui.common.formatAmount
import io.github.elli0t0.calendar.ui.common.toPersianDigits
import java.time.LocalDate

private val IncomeGreen = Color(0xFF43A047)
private val ExpenseRed = Color(0xFFE53935)

private fun groupHeader(epochDay: Long): String {
    val date = LocalDate.ofEpochDay(epochDay)
    val j = Jalali.toJalali(date)
    return "${PersianNames.weekdayName(date)} ${toPersianDigits(j.day.toString())} " +
        "${PersianNames.jalaliMonthName(j.month)} ${toPersianDigits(j.year.toString())}"
}

@Composable
fun TransactionsScreen(onMenuClick: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val items by TxRepository.items.collectAsState()
    var showSheet by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<Transaction?>(null) }
    val today = remember { LocalDate.now() }

    val income = items.filter { it.type == TxType.INCOME }.sumOf { it.amount }
    val expense = items.filter { it.type == TxType.EXPENSE }.sumOf { it.amount }
    val balance = income - expense
    val groups = items.groupBy { it.epochDay }.toSortedMap(compareByDescending { it })

    Box(
        modifier = Modifier
            .fillMaxSize()
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
                IconButton(onClick = onMenuClick) {
                    Icon(
                        painter = painterResource(R.drawable.ic_menu),
                        contentDescription = stringResource(R.string.menu)
                    )
                }
                Text(
                    text = stringResource(R.string.tab_transactions),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = scheme.onSurface,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.width(48.dp))
            }
            Spacer(modifier = Modifier.height(16.dp))
            SummaryCard(income = income, expense = expense, balance = balance)
            Spacer(modifier = Modifier.height(16.dp))
            if (items.isEmpty()) {
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.tx_empty),
                        fontSize = 16.sp,
                        color = scheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    groups.forEach { (epochDay, txs) ->
                        item(key = "header-$epochDay") {
                            Text(
                                text = groupHeader(epochDay),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = scheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 8.dp, bottom = 2.dp)
                            )
                        }
                        items(txs.sortedByDescending { it.id }, key = { it.id }) { tx ->
                            TxRow(tx = tx, onClick = { editing = tx; showSheet = true })
                        }
                    }
                }
            }
        }
        FloatingActionButton(
            onClick = { editing = null; showSheet = true },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp),
            containerColor = scheme.primary,
            contentColor = Color.White
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_add),
                contentDescription = stringResource(R.string.tx_add)
            )
        }
    }
    if (showSheet) {
        TxSheet(
            initial = editing,
            initialDate = today,
            onDismiss = { showSheet = false }
        )
    }
}

@Composable
private fun SummaryCard(income: Long, expense: Long, balance: Long) {
    val scheme = MaterialTheme.colorScheme
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = scheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.padding(20.dp)) {
            SummaryItem(
                label = stringResource(R.string.tx_income),
                value = income,
                color = IncomeGreen,
                modifier = Modifier.weight(1f)
            )
            SummaryItem(
                label = stringResource(R.string.tx_expense),
                value = expense,
                color = ExpenseRed,
                modifier = Modifier.weight(1f)
            )
            SummaryItem(
                label = stringResource(R.string.tx_balance),
                value = balance,
                color = if (balance >= 0) IncomeGreen else ExpenseRed,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun RowScope.SummaryItem(label: String, value: Long, color: Color, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = formatAmount(value),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
private fun TxRow(tx: Transaction, onClick: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val color = if (tx.type == TxType.INCOME) IncomeGreen else ExpenseRed
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = scheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(40.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(color)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = tx.title,
                fontSize = 16.sp,
                color = scheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = formatAmount(tx.amount),
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}
