package io.github.elli0t0.calendar.ui.calendar

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.elli0t0.calendar.R
import io.github.elli0t0.calendar.data.Installment
import io.github.elli0t0.calendar.data.InstallmentRepository
import io.github.elli0t0.calendar.data.MarkerProvider
import io.github.elli0t0.calendar.data.MarkerType
import io.github.elli0t0.calendar.data.Transaction
import io.github.elli0t0.calendar.data.TxRepository
import io.github.elli0t0.calendar.data.TxType
import io.github.elli0t0.calendar.data.dueDate
import io.github.elli0t0.calendar.data.occursOn
import io.github.elli0t0.calendar.jalali.Jalali
import io.github.elli0t0.calendar.jalali.PersianNames
import io.github.elli0t0.calendar.jalali.toPersianDigits
import io.github.elli0t0.calendar.ui.common.formatAmount
import io.github.elli0t0.calendar.ui.installments.InstallmentSheet
import io.github.elli0t0.calendar.ui.search.SearchScreen
import io.github.elli0t0.calendar.ui.transactions.TxSheet
import java.time.LocalDate
import kotlinx.coroutines.launch

/** Gregorian months overlapped by a Jalali month, e.g. «اکتبر – نوامبر ۲۰۲۶». */
@Composable
private fun gregorianOverlap(year: Int, month: Int): String {
    val first = Jalali.toGregorian(io.github.elli0t0.calendar.jalali.JalaliDate(year, month, 1))
    val last = Jalali.toGregorian(
        io.github.elli0t0.calendar.jalali.JalaliDate(year, month, Jalali.monthLength(year, month))
    )
    val firstName = PersianNames.gregorianMonthName(first.monthValue)
    val lastName = PersianNames.gregorianMonthName(last.monthValue)
    val yearText = last.year.toPersianDigits()
    return if (first.monthValue == last.monthValue) {
        "$firstName $yearText"
    } else {
        "$firstName – $lastName $yearText"
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CalendarScreen(
    markerProvider: MarkerProvider,
    onMenuClick: () -> Unit,
    modifier: Modifier = Modifier,
    /** Due date to jump to when opened from a reminder notification. */
    initialSelectedDate: LocalDate? = null,
) {
    val scheme = MaterialTheme.colorScheme
    // Collecting the flow here makes the screen recompose whenever
    // transactions change, so cells and the card below re-read fresh markers.
    val transactions by TxRepository.items.collectAsState()
    val installments by InstallmentRepository.items.collectAsState()
    val today = remember { LocalDate.now() }
    var selected by remember { mutableStateOf(today) }
    var showSheet by remember { mutableStateOf(false) }
    var editingTx by remember { mutableStateOf<Transaction?>(null) }
    var showInstSheet by remember { mutableStateOf(false) }
    var editingInst by remember { mutableStateOf<Installment?>(null) }
    var showSearch by remember { mutableStateOf(false) }
    val baseJalali = remember { Jalali.toJalali(today) }
    val pagerState = rememberPagerState(initialPage = 600, pageCount = { 1201 })
    // Pager page for a date's Jalali month.
    fun pageFor(date: LocalDate): Int {
        val target = Jalali.toJalali(date)
        return (
            600 + (target.year * 12 + target.month) -
                (baseJalali.year * 12 + baseJalali.month)
            ).coerceIn(0, 1200)
    }
    // A reminder tap jumps the calendar to the due date.
    LaunchedEffect(initialSelectedDate) {
        if (initialSelectedDate != null) {
            selected = initialSelectedDate
            pagerState.scrollToPage(pageFor(initialSelectedDate))
        }
    }
    val scope = rememberCoroutineScope()
    val displayed = addJalaliMonths(baseJalali.year, baseJalali.month, pagerState.currentPage - 600)

    if (showSearch) {
        SearchScreen(
            onBack = { showSearch = false },
            onSelectDate = { date ->
                showSearch = false
                selected = date
                scope.launch { pagerState.scrollToPage(pageFor(date)) }
            }
        )
    } else {
    Box(modifier = modifier.fillMaxSize().background(scheme.background)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 96.dp)
        ) {
        // Header: menu, month + year + Gregorian line, today badge.
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onMenuClick) {
                Icon(
                    painter = painterResource(R.drawable.ic_menu),
                    contentDescription = stringResource(R.string.menu)
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = PersianNames.jalaliMonthName(displayed.second),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = scheme.onSurface,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "${displayed.first.toPersianDigits()} · ${gregorianOverlap(displayed.first, displayed.second)}",
                    fontSize = 12.sp,
                    color = scheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
            IconButton(onClick = { showSearch = true }) {
                Icon(
                    painter = painterResource(R.drawable.ic_search),
                    contentDescription = stringResource(R.string.search_hint),
                    tint = scheme.onSurface
                )
            }
            Box(
                modifier = Modifier.size(48.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .border(2.dp, scheme.onSurface, RoundedCornerShape(8.dp))
                        .clickable {
                            selected = today
                            scope.launch { pagerState.animateScrollToPage(600) }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = Jalali.toJalali(today).day.toPersianDigits(),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = scheme.onSurface
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Weekday row, week starts Saturday. Friday letter in red.
        Row(modifier = Modifier.fillMaxWidth()) {
            PersianNames.weekdayLetters().forEachIndexed { index, letter ->
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = letter,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Normal,
                        color = if (index == 6) Color(0xFFE53935) else scheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Month grid with swipe.
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth()
        ) { page ->
            val month = addJalaliMonths(baseJalali.year, baseJalali.month, page - 600)
            MonthGrid(
                year = month.first,
                month = month.second,
                today = today,
                selected = selected,
                markerProvider = markerProvider,
                onSelect = { selected = it },
                onLongClick = { date ->
                    selected = date
                    editingTx = null
                    showSheet = true
                }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Selected-day card.
        AnimatedContent(
            targetState = selected,
            transitionSpec = { fadeIn(tween(200)) togetherWith fadeOut(tween(200)) },
            label = "selectedDay"
        ) { date ->
            val jalali = Jalali.toJalali(date)
            val dayTxs = transactions.filter { occursOn(it, date) }
            val dayInstallments = installments.flatMap { inst ->
                (0 until inst.count)
                    .filter { k -> dueDate(inst, k) == date }
                    .map { k -> inst to k }
            }
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = jalali.day.toPersianDigits(),
                        fontSize = 32.sp,
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
                            text = "${PersianNames.jalaliMonthName(jalali.month)} ${jalali.year.toPersianDigits()}",
                            fontSize = 14.sp,
                            color = scheme.onSurfaceVariant
                        )
                    }
                }
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
                // One muted line with the day's income/expense totals
                // (only the non-zero parts).
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
                    Text(
                        text = stringResource(R.string.no_events),
                        fontSize = 15.sp,
                        color = scheme.onSurfaceVariant
                    )
                } else {
                    // Installments first, then expenses, then incomes.
                    dayInstallments.forEach { (inst, k) ->
                        val paid = k in inst.paid
                        DayEventRow(
                            barColor = markerColor(MarkerType.INSTALLMENT),
                            title = inst.title,
                            note = null,
                            amountText = stringResource(
                                R.string.amount_with_currency,
                                formatAmount(inst.amountPerMonth)
                            ),
                            amountColor = Color(0xFFFB8C00),
                            alpha = if (paid) 0.5f else 1f,
                            strikethroughTitle = paid,
                            onClick = { editingInst = inst; showInstSheet = true },
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
                            barColor = markerColor(MarkerType.EXPENSE, tx.colorIndex),
                            title = tx.title,
                            note = tx.note.ifBlank { null },
                            amountText = stringResource(
                                R.string.amount_with_currency,
                                formatAmount(tx.amount)
                            ),
                            amountColor = Color(0xFFE53935),
                            onClick = { editingTx = tx; showSheet = true }
                        )
                    }
                    dayTxs.filter { it.type == TxType.INCOME }.forEach { tx ->
                        DayEventRow(
                            barColor = markerColor(MarkerType.INCOME, tx.colorIndex),
                            title = tx.title,
                            note = tx.note.ifBlank { null },
                            amountText = stringResource(
                                R.string.amount_with_currency,
                                formatAmount(tx.amount)
                            ),
                            amountColor = Color(0xFF43A047),
                            onClick = { editingTx = tx; showSheet = true }
                        )
                    }
                }
            }
        }
    }
        // Add pill: anchored to the bottom of the screen, over the scrolling content.
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
                .height(56.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(scheme.surfaceVariant)
                .clickable { editingTx = null; showSheet = true }
                .padding(horizontal = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(
                        R.string.tx_add_on_day,
                        Jalali.toJalali(selected).day.toPersianDigits(),
                        PersianNames.jalaliMonthName(Jalali.toJalali(selected).month)
                    ),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = scheme.onSurface
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    painter = painterResource(R.drawable.ic_add),
                    contentDescription = null,
                    tint = scheme.onSurface
                )
            }
        }
    }
    }
    if (showSheet) {
        TxSheet(
            initial = editingTx,
            initialDate = selected,
            onDismiss = { showSheet = false }
        )
    }
    if (showInstSheet) {
        InstallmentSheet(
            initial = editingInst,
            onDismiss = { showInstSheet = false }
        )
    }
}

/** One event card in the selected-day section: color bar, title, one-line
 * note, amount. An optional round button (e.g. the paid toggle) goes last. */
@Composable
private fun DayEventRow(
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
            Text(
                text = title,
                fontSize = 15.sp,
                color = scheme.onSurface,
                textDecoration = if (strikethroughTitle) TextDecoration.LineThrough else null
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
