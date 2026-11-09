package io.github.elli0t0.calendar.ui.calendar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import io.github.elli0t0.calendar.data.Transaction
import io.github.elli0t0.calendar.data.TxRepository
import io.github.elli0t0.calendar.data.dueDate
import io.github.elli0t0.calendar.data.occurrencesBetween
import io.github.elli0t0.calendar.jalali.Jalali
import io.github.elli0t0.calendar.jalali.JalaliDate
import io.github.elli0t0.calendar.jalali.PersianNames
import io.github.elli0t0.calendar.jalali.toPersianDigits
import java.time.LocalDate

sealed interface ScheduleItem {
    val key: String

    data class Month(val year: Int, val month: Int) : ScheduleItem {
        override val key: String get() = "month-$year-$month"
    }

    data class Day(val date: LocalDate) : ScheduleItem {
        override val key: String get() = "day-${date.toEpochDay()}"
    }
}

/** Days with events in [from, to] (inclusive), sorted. Repeats and unpaid
 * installments are included via the occurrence engine. */
fun daysWithEvents(
    transactions: List<Transaction>,
    installments: List<Installment>,
    from: LocalDate,
    to: LocalDate,
): List<LocalDate> {
    val days = mutableSetOf<LocalDate>()
    for (tx in transactions) {
        days.addAll(occurrencesBetween(tx, from, to))
    }
    for (inst in installments) {
        for (k in 0 until inst.count) {
            if (k in inst.paid) continue
            val due = dueDate(inst, k)
            if (due in from..to) days.add(due)
        }
    }
    return days.sorted()
}

/** Schedule items for a list of Jalali months: a month header, then one
 * item per day with events. Empty months are skipped. */
fun itemsForMonths(
    months: List<Pair<Int, Int>>,
    transactions: List<Transaction>,
    installments: List<Installment>,
): List<ScheduleItem> {
    val out = mutableListOf<ScheduleItem>()
    for ((year, month) in months) {
        val first = Jalali.toGregorian(JalaliDate(year, month, 1))
        val last = Jalali.toGregorian(
            JalaliDate(year, month, Jalali.monthLength(year, month))
        )
        val days = daysWithEvents(transactions, installments, first, last)
        if (days.isEmpty()) continue
        out.add(ScheduleItem.Month(year, month))
        for (day in days) out.add(ScheduleItem.Day(day))
    }
    return out
}

/**
 * The next 3-month windows after [fromMonth] (forward) or before it
 * (backward) that contain events, up to 4 windows (12 months). Null when
 * nothing exists that far, so loading stops.
 */
fun monthsToAdd(
    fromMonth: Pair<Int, Int>,
    transactions: List<Transaction>,
    installments: List<Installment>,
    forward: Boolean,
): List<Pair<Int, Int>>? {
    var candidate = fromMonth
    var added = emptyList<Pair<Int, Int>>()
    repeat(4) {
        val next = if (forward) {
            (1..3).map { addJalaliMonths(candidate.first, candidate.second, it) }
        } else {
            (3 downTo 1).map {
                addJalaliMonths(candidate.first, candidate.second, -it)
            }
        }
        if (itemsForMonths(next, transactions, installments).isNotEmpty()) {
            return added + next
        }
        added = added + next
        candidate = if (forward) next.last() else next.first()
    }
    return null
}

@Composable
fun ScheduleView(
    selected: LocalDate,
    onEditTx: (Transaction) -> Unit,
    onEditInst: (Installment) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val settings by AppSettings.settings.collectAsState()
    val transactions by TxRepository.items.collectAsState()
    val installments by InstallmentRepository.items.collectAsState()
    val listState = rememberLazyListState()
    // The loaded months, starting at the selected date's month (3 months).
    val startMonth = remember(selected) {
        Jalali.toJalali(selected).let { it.year to it.month }
    }
    var months by remember(selected) {
        mutableStateOf(
            (0 until 3).map { addJalaliMonths(startMonth.first, startMonth.second, it) }
        )
    }
    var endReached by remember(selected) { mutableStateOf(false) }
    var topReached by remember(selected) { mutableStateOf(false) }
    // The top window only loads after the user has scrolled down once,
    // so opening the schedule does not immediately prepend months.
    var hasScrolledDown by remember(selected) { mutableStateOf(false) }
    // (index, offset) to restore after prepending months at the top.
    var pendingTopScroll by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    val items = remember(months, transactions, installments) {
        itemsForMonths(months, transactions, installments)
    }
    // Load the next window near the end, the previous near the top.
    LaunchedEffect(listState, items) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo }
            .collect { visible ->
                if (visible.isEmpty()) return@collect
                val last = visible.last().index
                val first = visible.first().index
                if (first > 0 || listState.firstVisibleItemScrollOffset > 0) {
                    hasScrolledDown = true
                }
                if (!endReached && last >= items.size - 5) {
                    val next = monthsToAdd(
                        months.last(), transactions, installments, forward = true
                    )
                    if (next == null) {
                        endReached = true
                    } else {
                        months = months + next
                    }
                } else if (!topReached && hasScrolledDown && first <= 5) {
                    val prev = monthsToAdd(
                        months.first(), transactions, installments, forward = false
                    )
                    if (prev == null) {
                        topReached = true
                    } else {
                        val addedCount = itemsForMonths(
                            prev, transactions, installments
                        ).size
                        pendingTopScroll = listState.firstVisibleItemIndex + addedCount to
                            listState.firstVisibleItemScrollOffset
                        months = prev + months
                    }
                }
            }
    }
    LaunchedEffect(months) {
        pendingTopScroll?.let { (index, offset) ->
            listState.scrollToItem(index, offset)
            pendingTopScroll = null
        }
    }
    if (items.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(R.string.no_events),
                fontSize = 16.sp,
                color = scheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
    } else {
        LazyColumn(
            state = listState,
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 20.dp, top = 8.dp, end = 20.dp, bottom = 96.dp
            ),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(items, key = { it.key }) { item ->
                when (item) {
                    is ScheduleItem.Month -> {
                        Text(
                            text = "${PersianNames.jalaliMonthName(item.month)} " +
                                item.year.toPersianDigits(),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = scheme.onSurface,
                            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                        )
                    }
                    is ScheduleItem.Day -> {
                        ScheduleDayRow(
                            date = item.date,
                            transactions = transactions,
                            installments = installments,
                            markerStyle = settings.markerStyle,
                            onEditTx = onEditTx,
                            onEditInst = onEditInst
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ScheduleDayRow(
    date: LocalDate,
    transactions: List<Transaction>,
    installments: List<Installment>,
    markerStyle: MarkerStyle,
    onEditTx: (Transaction) -> Unit,
    onEditInst: (Installment) -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val jalali = Jalali.toJalali(date)
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = jalali.day.toPersianDigits(),
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = scheme.onSurface
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = PersianNames.weekdayName(date),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = scheme.onSurface
                )
                Text(
                    text = stringResource(
                        R.string.date_gregorian,
                        date.dayOfMonth.toPersianDigits(),
                        PersianNames.gregorianMonthName(date.monthValue),
                        date.year.toPersianDigits()
                    ),
                    fontSize = 14.sp,
                    color = scheme.onSurfaceVariant
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
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
