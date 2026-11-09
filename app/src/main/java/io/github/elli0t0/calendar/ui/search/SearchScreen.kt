package io.github.elli0t0.calendar.ui.search

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.elli0t0.calendar.R
import io.github.elli0t0.calendar.data.AppSettings
import io.github.elli0t0.calendar.data.Installment
import io.github.elli0t0.calendar.data.InstallmentRepository
import io.github.elli0t0.calendar.data.MarkerStyle
import io.github.elli0t0.calendar.data.MarkerType
import io.github.elli0t0.calendar.data.Repeat
import io.github.elli0t0.calendar.data.Transaction
import io.github.elli0t0.calendar.data.TxRepository
import io.github.elli0t0.calendar.data.TxType
import io.github.elli0t0.calendar.data.dueDate
import io.github.elli0t0.calendar.data.occurrencesBetween
import io.github.elli0t0.calendar.ui.calendar.markerColor
import io.github.elli0t0.calendar.ui.common.formatAmount
import io.github.elli0t0.calendar.ui.common.fullJalaliDate
import java.time.LocalDate

data class SearchResult(
    val title: String,
    val amount: Long,
    val date: LocalDate,
    val barColor: Color,
    val amountColor: Color,
)

/** Normalize for matching: Arabic ي/ك to Persian ی/ک, Persian/Arabic digits
 * to English digits, lowercased. */
fun normalizeForSearch(s: String): String {
    val sb = StringBuilder()
    for (c in s) {
        when (c) {
            'ي' -> sb.append('ی')
            'ك' -> sb.append('ک')
            in '۰'..'۹' -> sb.append('0' + (c - '۰'))
            in '٠'..'٩' -> sb.append('0' + (c - '٠'))
            else -> sb.append(c)
        }
    }
    return sb.toString().lowercase()
}

/**
 * Rows matching [query] in title or note. A repeating event shows its next
 * occurrence from [today] (or its start date when none is left); each unpaid
 * installment due date is its own row (max 12 per installment). Sorted by
 * date ascending starting from today, past ones after the upcoming ones.
 */
fun searchResults(
    query: String,
    transactions: List<Transaction>,
    installments: List<Installment>,
    today: LocalDate,
    markerStyle: MarkerStyle = MarkerStyle.MUTED,
): List<SearchResult> {
    val q = normalizeForSearch(query)
    if (q.isEmpty()) return emptyList()
    val out = mutableListOf<SearchResult>()
    for (tx in transactions) {
        if (!normalizeForSearch(tx.title).contains(q) &&
            !normalizeForSearch(tx.note).contains(q)
        ) continue
        val start = LocalDate.ofEpochDay(tx.epochDay)
        val date = if (tx.repeat == Repeat.NONE) {
            start
        } else {
            occurrencesBetween(tx, today, today.plusDays(366)).firstOrNull() ?: start
        }
        val isIncome = tx.type == TxType.INCOME
        out.add(
            SearchResult(
                tx.title, tx.amount, date,
                markerColor(
                    if (isIncome) MarkerType.INCOME else MarkerType.EXPENSE,
                    tx.colorIndex, markerStyle
                ),
                if (isIncome) Color(0xFF43A047) else Color(0xFFE53935)
            )
        )
    }
    for (inst in installments) {
        if (!normalizeForSearch(inst.title).contains(q)) continue
        val dueDates = (0 until inst.count)
            .filter { k -> k !in inst.paid }
            .map { k -> dueDate(inst, k) }
            .sortedWith(compareBy<LocalDate> { it < today }.thenBy { it })
            .take(12)
        for (due in dueDates) {
            out.add(
                SearchResult(
                    inst.title, inst.amountPerMonth, due,
                    Color(0xFFFB8C00), Color(0xFFFB8C00)
                )
            )
        }
    }
    return out.sortedWith(compareBy<SearchResult> { it.date < today }.thenBy { it.date })
}

@Composable
fun SearchScreen(onBack: () -> Unit, onSelectDate: (LocalDate) -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val transactions by TxRepository.items.collectAsState()
    val installments by InstallmentRepository.items.collectAsState()
    val settings by AppSettings.settings.collectAsState()
    var query by remember { mutableStateOf("") }
    val today = remember { LocalDate.now() }
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }
    val results = remember(query, transactions, installments, settings.markerStyle) {
        searchResults(query, transactions, installments, today, settings.markerStyle)
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(scheme.background)
            .imePadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    painter = painterResource(R.drawable.ic_arrow_left),
                    contentDescription = stringResource(R.string.back),
                    tint = scheme.onSurface
                )
            }
            TextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 56.dp)
                    .focusRequester(focusRequester),
                placeholder = { Text(stringResource(R.string.search_hint)) },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = scheme.surfaceContainerHigh,
                    unfocusedContainerColor = scheme.surfaceContainerHigh,
                    disabledContainerColor = scheme.surfaceContainerHigh,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor = scheme.primary,
                    focusedTextColor = scheme.onSurface,
                    unfocusedTextColor = scheme.onSurface,
                    focusedPlaceholderColor = scheme.onSurfaceVariant,
                    unfocusedPlaceholderColor = scheme.onSurfaceVariant
                )
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        if (query.isNotEmpty() && results.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.no_results),
                    fontSize = 16.sp,
                    color = scheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(results) { result ->
                    SearchRow(result = result, onClick = { onSelectDate(result.date) })
                }
            }
        }
    }
}

@Composable
private fun SearchRow(result: SearchResult, onClick: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
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
                    .background(result.barColor)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = result.title,
                    fontSize = 16.sp,
                    color = scheme.onSurface
                )
                Text(
                    text = fullJalaliDate(result.date),
                    fontSize = 13.sp,
                    color = scheme.onSurfaceVariant
                )
            }
            Text(
                text = stringResource(
                    R.string.amount_with_currency,
                    formatAmount(result.amount)
                ),
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = result.amountColor
            )
        }
    }
}
