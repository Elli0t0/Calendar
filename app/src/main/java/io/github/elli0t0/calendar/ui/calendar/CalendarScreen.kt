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
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.elli0t0.calendar.R
import io.github.elli0t0.calendar.data.AppSettings
import io.github.elli0t0.calendar.data.CalendarViewMode
import io.github.elli0t0.calendar.data.Installment
import io.github.elli0t0.calendar.data.InstallmentRepository
import io.github.elli0t0.calendar.data.MarkerProvider
import io.github.elli0t0.calendar.data.MarkerType
import io.github.elli0t0.calendar.data.Transaction
import io.github.elli0t0.calendar.data.TxRepository
import io.github.elli0t0.calendar.jalali.Jalali
import io.github.elli0t0.calendar.jalali.JalaliDate
import io.github.elli0t0.calendar.jalali.PersianNames
import io.github.elli0t0.calendar.jalali.toPersianDigits
import io.github.elli0t0.calendar.ui.common.shortJalaliDate
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
    val settings by AppSettings.settings.collectAsState()
    val today = remember { LocalDate.now() }
    var selected by remember { mutableStateOf(today) }
    // Month grid collapsed to the selected week (swipe up), or full month.
    var monthCollapsed by remember { mutableStateOf(false) }
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
    // The year the Year view shows; follows the selected date on entry.
    var yearViewYear by remember { mutableStateOf(Jalali.toJalali(today).year) }
    LaunchedEffect(settings.viewMode) {
        if (settings.viewMode == CalendarViewMode.YEAR) {
            yearViewYear = Jalali.toJalali(selected).year
        }
    }

    // Header title and subtitle for the current mode.
    val selectedJalali = Jalali.toJalali(selected)
    val (headerTitle, headerSubtitle, headerTitleSize) = when (settings.viewMode) {
        CalendarViewMode.MONTH -> Triple(
            PersianNames.jalaliMonthName(displayed.second),
            if (settings.showGregorianInHeader) {
                "${displayed.first.toPersianDigits()} · " +
                    gregorianOverlap(displayed.first, displayed.second)
            } else {
                null
            },
            Dimens.HeaderTitle
        )
        CalendarViewMode.DAY -> Triple(
            shortJalaliDate(selected),
            "${selectedJalali.year.toPersianDigits()} · " + stringResource(
                R.string.date_gregorian,
                selected.dayOfMonth.toPersianDigits(),
                PersianNames.gregorianMonthName(selected.monthValue),
                selected.year.toPersianDigits()
            ),
            20.sp
        )
        CalendarViewMode.SCHEDULE -> Triple(
            PersianNames.jalaliMonthName(selectedJalali.month),
            if (settings.showGregorianInHeader) {
                "${selectedJalali.year.toPersianDigits()} · " +
                    gregorianOverlap(selectedJalali.year, selectedJalali.month)
            } else {
                null
            },
            Dimens.HeaderTitle
        )
        CalendarViewMode.WEEK -> Triple(
            "${PersianNames.jalaliMonthName(selectedJalali.month)} " +
                selectedJalali.year.toPersianDigits(),
            weekRangeText(
                weekStartFor(selected, settings.firstDay),
                weekStartFor(selected, settings.firstDay).plusDays(6)
            ),
            Dimens.HeaderTitle
        )
        CalendarViewMode.YEAR -> Triple(
            yearViewYear.toPersianDigits(),
            null,
            Dimens.HeaderTitle
        )
    }

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
        Column(modifier = Modifier.fillMaxSize()) {
            // Header (shared by all modes): menu, title, search, today badge.
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onMenuClick) {
                    Icon(
                        painter = painterResource(R.drawable.ic_menu),
                        contentDescription = stringResource(R.string.menu),
                        tint = scheme.onSurface
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (settings.viewMode == CalendarViewMode.YEAR) {
                        // Year number with prev/next arrows.
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            IconButton(onClick = { yearViewYear-- }) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_arrow_left),
                                    contentDescription = stringResource(R.string.nav_prev),
                                    tint = scheme.onSurface
                                )
                            }
                            Text(
                                text = yearViewYear.toPersianDigits(),
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = scheme.onSurface,
                                textAlign = TextAlign.Center
                            )
                            IconButton(onClick = { yearViewYear++ }) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_arrow_right),
                                    contentDescription = stringResource(R.string.nav_next),
                                    tint = scheme.onSurface
                                )
                            }
                        }
                    } else {
                        Text(
                            text = headerTitle,
                            fontSize = headerTitleSize,
                            fontWeight = FontWeight.Bold,
                            color = scheme.onSurface,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (headerSubtitle != null) {
                            Text(
                                text = headerSubtitle,
                                fontSize = 12.sp,
                                color = scheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
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
            when (settings.viewMode) {
                CalendarViewMode.MONTH -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 20.dp)
                            .padding(bottom = 96.dp)
                    ) {
                        Spacer(modifier = Modifier.height(16.dp))

        // Swipe up collapses the month to the selected week, swipe down
        // expands it again (like the reference).
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .pointerInput(Unit) {
                    var totalDrag = 0f
                    detectVerticalDragGestures(
                        onDragEnd = { totalDrag = 0f },
                        onDragCancel = { totalDrag = 0f },
                        onVerticalDrag = { _, dragAmount ->
                            totalDrag += dragAmount
                            if (totalDrag < -40) {
                                monthCollapsed = true
                                totalDrag = 0f
                            } else if (totalDrag > 40) {
                                monthCollapsed = false
                                totalDrag = 0f
                            }
                        }
                    )
                }
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
        // Weekday row, starting on the chosen first day. Friday letter in red.
        Row(modifier = Modifier.fillMaxWidth()) {
            orderedWeekdayLetters(PersianNames.weekdayLetters(), settings.firstDay)
                .forEachIndexed { index, letter ->
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = letter,
                            fontSize = Dimens.WeekdayLetter,
                            fontWeight = FontWeight.Normal,
                            color = if (index == fridayIndex(settings.firstDay)) {
                                Color(0xFFE53935)
                            } else {
                                scheme.onSurfaceVariant
                            }
                        )
                    }
                }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (monthCollapsed) {
            // One row: the selected week only.
            CollapsedWeekRow(
                selected = selected,
                today = today,
                firstDay = settings.firstDay,
                markerProvider = markerProvider,
                onSelect = { selected = it },
                onLongClick = { date ->
                    selected = date
                    editingTx = null
                    showSheet = true
                }
            )
        } else {
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
        }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Selected-day card.
        AnimatedContent(
            targetState = selected,
            transitionSpec = { fadeIn(tween(200)) togetherWith fadeOut(tween(200)) },
            label = "selectedDay"
        ) { date ->
            val jalali = Jalali.toJalali(date)
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "${jalali.day.toPersianDigits()} ${PersianNames.weekdayName(date)}",
                    fontSize = Dimens.DayHeader,
                    fontWeight = FontWeight.Bold,
                    color = scheme.onSurface
                )
                Text(
                    text = "${PersianNames.jalaliMonthName(jalali.month)} ${jalali.year.toPersianDigits()} · " +
                        stringResource(
                            R.string.date_gregorian,
                            date.dayOfMonth.toPersianDigits(),
                            PersianNames.gregorianMonthName(date.monthValue),
                            date.year.toPersianDigits()
                        ),
                    fontSize = Dimens.DayHeaderMuted,
                    color = scheme.onSurfaceVariant
                )
                DayEvents(
                    date = date,
                    transactions = transactions,
                    installments = installments,
                    markerStyle = settings.markerStyle,
                    onEditTx = { editingTx = it; showSheet = true },
                    onEditInst = { editingInst = it; showInstSheet = true }
                )
            }
        }
                    }
                }
                CalendarViewMode.DAY -> {
                    DayView(
                        selected = selected,
                        onSelect = { selected = it },
                        markerProvider = markerProvider,
                        onEditTx = { editingTx = it; showSheet = true },
                        onEditInst = { editingInst = it; showInstSheet = true },
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 20.dp)
                            .padding(top = 16.dp, bottom = 96.dp)
                    )
                }
                CalendarViewMode.SCHEDULE -> {
                    ScheduleView(
                        selected = selected,
                        onEditTx = { editingTx = it; showSheet = true },
                        onEditInst = { editingInst = it; showInstSheet = true },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                CalendarViewMode.WEEK -> {
                    WeekView(
                        selected = selected,
                        onSelect = { selected = it },
                        markerProvider = markerProvider,
                        onEditTx = { editingTx = it; showSheet = true },
                        onEditInst = { editingInst = it; showInstSheet = true },
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 20.dp)
                            .padding(top = 16.dp)
                    )
                }
                CalendarViewMode.YEAR -> {
                    YearView(
                        year = yearViewYear,
                        markerProvider = markerProvider,
                        onSelectMonth = { year, month ->
                            AppSettings.setViewMode(CalendarViewMode.MONTH)
                            val first = Jalali.toGregorian(JalaliDate(year, month, 1))
                            val last = Jalali.toGregorian(
                                JalaliDate(year, month, Jalali.monthLength(year, month))
                            )
                            selected = if (today in first..last) today else first
                            scope.launch { pagerState.scrollToPage(pageFor(selected)) }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
        // Add pill: anchored to the bottom of the screen, over the scrolling
        // content. Not in Year view, where it would cover the last months.
        // Like the reference it shows the day's first event (tap it to edit)
        // and a + to add; with no event it is just the Add button.
        if (settings.viewMode != CalendarViewMode.YEAR) {
        val firstMarker = markerProvider.markersFor(selected).firstOrNull()
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 16.dp)
                .height(Dimens.PillHeight)
                .clip(RoundedCornerShape(Dimens.PillCorner))
                .background(scheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (firstMarker != null) {
                    Text(
                        text = firstMarker.title,
                        fontSize = Dimens.PillText,
                        fontWeight = FontWeight.Bold,
                        color = scheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                when (firstMarker.type) {
                                    MarkerType.INCOME, MarkerType.EXPENSE -> {
                                        transactions.firstOrNull { it.id == firstMarker.refId }
                                            ?.let { editingTx = it; showSheet = true }
                                    }
                                    MarkerType.INSTALLMENT -> {
                                        installments.firstOrNull { it.id == firstMarker.refId }
                                            ?.let { editingInst = it; showInstSheet = true }
                                    }
                                }
                            }
                    )
                } else {
                    Text(
                        text = stringResource(
                            R.string.tx_add_on_day,
                            Jalali.toJalali(selected).day.toPersianDigits(),
                            PersianNames.jalaliMonthName(Jalali.toJalali(selected).month)
                        ),
                        fontSize = Dimens.PillText,
                        fontWeight = FontWeight.Bold,
                        color = scheme.onSurface,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { editingTx = null; showSheet = true }
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = { editingTx = null; showSheet = true }) {
                    Icon(
                        painter = painterResource(R.drawable.ic_add),
                        contentDescription = null,
                        tint = scheme.onSurface
                    )
                }
            }
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

