package io.github.elli0t0.calendar

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import java.time.LocalDate
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.annotation.DrawableRes
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import kotlinx.coroutines.launch
import io.github.elli0t0.calendar.data.AppSettings
import io.github.elli0t0.calendar.data.CalendarViewMode
import io.github.elli0t0.calendar.data.ThemeMode
import io.github.elli0t0.calendar.data.withAppLocale
import io.github.elli0t0.calendar.data.CompositeMarkerProvider
import io.github.elli0t0.calendar.data.InstallmentMarkerProvider
import io.github.elli0t0.calendar.data.InstallmentRepository
import io.github.elli0t0.calendar.data.TransactionMarkerProvider
import io.github.elli0t0.calendar.data.TxRepository
import io.github.elli0t0.calendar.notify.EXTRA_EPOCH_DAY
import io.github.elli0t0.calendar.notify.ReminderScheduler
import io.github.elli0t0.calendar.ui.calendar.CalendarScreen
import io.github.elli0t0.calendar.ui.installments.InstallmentsScreen
import io.github.elli0t0.calendar.ui.settings.SettingsScreen
import io.github.elli0t0.calendar.ui.stats.StatsScreen
import io.github.elli0t0.calendar.ui.transactions.TransactionsScreen
import io.github.elli0t0.calendar.ui.nav.Tab
import io.github.elli0t0.calendar.ui.theme.CalendarTheme
import io.github.elli0t0.calendar.ui.theme.Dimens

@DrawableRes
private fun tabIcon(tab: Tab): Int = when (tab) {
    Tab.CALENDAR -> R.drawable.ic_date_range
    Tab.TRANSACTIONS -> R.drawable.ic_receipt
    Tab.INSTALLMENTS -> R.drawable.ic_credit_card
    Tab.STATS -> R.drawable.ic_bar_chart
}

@DrawableRes
private fun viewIcon(mode: CalendarViewMode): Int = when (mode) {
    CalendarViewMode.YEAR -> R.drawable.ic_year
    CalendarViewMode.MONTH -> R.drawable.ic_date_range
    CalendarViewMode.WEEK -> R.drawable.ic_week
    CalendarViewMode.DAY -> R.drawable.ic_day
    CalendarViewMode.SCHEDULE -> R.drawable.ic_schedule
}

private fun viewTitleRes(mode: CalendarViewMode): Int = when (mode) {
    CalendarViewMode.YEAR -> R.string.view_year
    CalendarViewMode.MONTH -> R.string.view_month
    CalendarViewMode.WEEK -> R.string.view_week
    CalendarViewMode.DAY -> R.string.view_day
    CalendarViewMode.SCHEDULE -> R.string.view_schedule
}

/** A drawer row: 52dp tall, 24dp icon + 16sp label. The selected row is a
 * pill of secondaryContainer at 60% alpha, inset 12dp from the edges. */
@Composable
private fun DrawerItem(
    @DrawableRes iconRes: Int,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.DrawerItemPadding)
            .height(Dimens.DrawerItemHeight)
            .clip(RoundedCornerShape(Dimens.DrawerItemCorner))
            .background(
                if (selected) {
                    scheme.secondaryContainer.copy(alpha = 0.6f)
                } else {
                    Color.Transparent
                }
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            tint = if (selected) scheme.primary else scheme.onSurfaceVariant,
            modifier = Modifier.size(Dimens.DrawerItemIcon)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = label,
            fontSize = Dimens.DrawerItemLabel,
            color = if (selected) scheme.onSurface else scheme.onSurfaceVariant
        )
    }
}

/** A 1dp dashed horizontal line (6dp dash, 6dp gap). */
@Composable
private fun DashedDivider(modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(1.dp)
            .drawBehind {
                drawLine(
                    color = scheme.outlineVariant,
                    start = Offset(0f, size.height / 2),
                    end = Offset(size.width, size.height / 2),
                    strokeWidth = 1.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f))
                )
            }
    )
}

class MainActivity : ComponentActivity() {
    // Due date from a tapped reminder notification, if any.
    private val deepLinkEpochDay = mutableStateOf<Long?>(null)

    // The app language applies before anything reads a string: AppSettings
    // is not initialised yet here, so the pref is read directly.
    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base.withAppLocale(AppSettings.readLanguage(base)))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        deepLinkEpochDay.value =
            intent.getLongExtra(EXTRA_EPOCH_DAY, -1L).takeIf { it >= 0 }
        AppSettings.init(applicationContext)
        TxRepository.init(applicationContext)
        InstallmentRepository.init(applicationContext)
        ReminderScheduler.reschedule(applicationContext)
        enableEdgeToEdge()
        setContent {
            val settings by AppSettings.settings.collectAsState()
            val dark = when (settings.themeMode) {
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }
            CalendarTheme(darkTheme = dark) {
                    // The status and navigation bar icons follow the chosen
                    // theme, not the system theme.
                    val view = LocalView.current
                    SideEffect {
                        val w = (view.context as Activity).window
                        WindowCompat.getInsetsController(w, view).apply {
                            isAppearanceLightStatusBars = !dark
                            isAppearanceLightNavigationBars = !dark
                        }
                    }
                    var tab by remember { mutableStateOf(Tab.CALENDAR) }
                    var showSettings by remember { mutableStateOf(false) }
                    val markerProvider = remember {
                        CompositeMarkerProvider(
                            listOf(TransactionMarkerProvider(), InstallmentMarkerProvider())
                        )
                    }
                    val scheme = MaterialTheme.colorScheme
                    val drawerState = rememberDrawerState(DrawerValue.Closed)
                    val scope = rememberCoroutineScope()
                    val openDrawer: () -> Unit = { scope.launch { drawerState.open() } }
                    BackHandler(enabled = drawerState.isOpen) {
                        scope.launch { drawerState.close() }
                    }
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = scheme.background,
                        contentColor = scheme.onBackground
                    ) {
                    ModalNavigationDrawer(
                        drawerState = drawerState,
                        drawerContent = {
                            val screenWidth = LocalConfiguration.current.screenWidthDp.dp
                            ModalDrawerSheet(
                                modifier = Modifier.width(screenWidth * 0.8f),
                                drawerShape = RoundedCornerShape(
                                    topEnd = Dimens.DrawerCorner,
                                    bottomEnd = Dimens.DrawerCorner
                                ),
                                drawerContainerColor = scheme.surface,
                                drawerTonalElevation = 0.dp
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 4.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(onClick = { scope.launch { drawerState.close() } }) {
                                        Icon(
                                            painter = painterResource(R.drawable.ic_menu),
                                            contentDescription = stringResource(R.string.menu),
                                            tint = scheme.onSurface
                                        )
                                    }
                                    Spacer(modifier = Modifier.weight(1f))
                                    IconButton(onClick = {
                                        showSettings = true
                                        scope.launch { drawerState.close() }
                                    }) {
                                        Icon(
                                            painter = painterResource(R.drawable.ic_settings),
                                            contentDescription = stringResource(R.string.settings_title),
                                            tint = scheme.onSurface
                                        )
                                    }
                                }
                                DashedDivider(modifier = Modifier.padding(horizontal = 16.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                // Group 1: calendar views (switch the Calendar screen's mode).
                                CalendarViewMode.entries.forEach { mode ->
                                    DrawerItem(
                                        iconRes = viewIcon(mode),
                                        label = stringResource(viewTitleRes(mode)),
                                        selected = tab == Tab.CALENDAR &&
                                            settings.viewMode == mode,
                                        onClick = {
                                            AppSettings.setViewMode(mode)
                                            tab = Tab.CALENDAR
                                            scope.launch { drawerState.close() }
                                        }
                                    )
                                }
                                DashedDivider(
                                    modifier = Modifier.padding(
                                        horizontal = 16.dp, vertical = 8.dp
                                    )
                                )
                                // Group 2: the other tabs.
                                Tab.entries.filter { it != Tab.CALENDAR }.forEach { item ->
                                    DrawerItem(
                                        iconRes = tabIcon(item),
                                        label = stringResource(item.titleRes),
                                        selected = item == tab,
                                        onClick = {
                                            tab = item
                                            scope.launch { drawerState.close() }
                                        }
                                    )
                                }
                            }
                        }
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(scheme.background)
                                .statusBarsPadding()
                                .navigationBarsPadding()
                        ) {
                            if (showSettings) {
                                SettingsScreen(
                                    onBack = { showSettings = false },
                                    onLanguageChanged = { recreate() }
                                )
                            } else {
                                when (tab) {
                                    Tab.CALENDAR -> CalendarScreen(
                                        markerProvider = markerProvider,
                                        onMenuClick = openDrawer,
                                        initialSelectedDate = deepLinkEpochDay.value?.let {
                                            LocalDate.ofEpochDay(it)
                                        }
                                    )
                                    Tab.TRANSACTIONS -> TransactionsScreen(onMenuClick = openDrawer)
                                    Tab.INSTALLMENTS -> InstallmentsScreen(onMenuClick = openDrawer)
                                    Tab.STATS -> StatsScreen(onMenuClick = openDrawer)
                                }
                            }
                        }
                    }
                    }
            }
        }
    }

    // SingleTop: a notification tap while the app is open re-delivers here.
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        deepLinkEpochDay.value =
            intent.getLongExtra(EXTRA_EPOCH_DAY, -1L).takeIf { it >= 0 }
    }
}

