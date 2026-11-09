package io.github.elli0t0.calendar

import android.content.Intent
import android.os.Bundle
import java.time.LocalDate
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.annotation.DrawableRes
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import io.github.elli0t0.calendar.data.AppSettings
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

@DrawableRes
private fun tabIcon(tab: Tab): Int = when (tab) {
    Tab.CALENDAR -> R.drawable.ic_date_range
    Tab.TRANSACTIONS -> R.drawable.ic_receipt
    Tab.INSTALLMENTS -> R.drawable.ic_credit_card
    Tab.STATS -> R.drawable.ic_bar_chart
}

class MainActivity : ComponentActivity() {
    // Due date from a tapped reminder notification, if any.
    private val deepLinkEpochDay = mutableStateOf<Long?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        deepLinkEpochDay.value =
            intent.getLongExtra(EXTRA_EPOCH_DAY, -1L).takeIf { it >= 0 }
        AppSettings.init(applicationContext)
        TxRepository.init(applicationContext)
        InstallmentRepository.init(applicationContext)
        ReminderScheduler.reschedule(applicationContext)
        setContent {
            CalendarTheme {
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
                    ModalNavigationDrawer(
                        drawerState = drawerState,
                        drawerContent = {
                            val screenWidth = LocalConfiguration.current.screenWidthDp.dp
                            ModalDrawerSheet(
                                modifier = Modifier
                                    .padding(12.dp)
                                    .width(screenWidth * 0.78f),
                                drawerShape = RoundedCornerShape(28.dp),
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
                                            contentDescription = stringResource(R.string.menu)
                                        )
                                    }
                                    Spacer(modifier = Modifier.weight(1f))
                                    IconButton(onClick = {
                                        showSettings = true
                                        scope.launch { drawerState.close() }
                                    }) {
                                        Icon(
                                            painter = painterResource(R.drawable.ic_settings),
                                            contentDescription = stringResource(R.string.settings_title)
                                        )
                                    }
                                }
                                HorizontalDivider(
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                    color = scheme.outlineVariant
                                )
                                Tab.entries.forEach { item ->
                                    NavigationDrawerItem(
                                        icon = {
                                            Icon(
                                                painter = painterResource(tabIcon(item)),
                                                contentDescription = null
                                            )
                                        },
                                        label = {
                                            Text(
                                                text = stringResource(item.titleRes),
                                                fontSize = 18.sp
                                            )
                                        },
                                        selected = item == tab,
                                        onClick = {
                                            tab = item
                                            scope.launch { drawerState.close() }
                                        },
                                        colors = NavigationDrawerItemDefaults.colors(
                                            selectedContainerColor = scheme.secondaryContainer,
                                            unselectedContainerColor = Color.Transparent,
                                            selectedIconColor = scheme.primary,
                                            unselectedIconColor = scheme.onSurfaceVariant,
                                            selectedTextColor = scheme.onSurface,
                                            unselectedTextColor = scheme.onSurfaceVariant
                                        ),
                                        shape = RoundedCornerShape(16.dp),
                                        modifier = Modifier
                                            .height(56.dp)
                                            .padding(NavigationDrawerItemDefaults.ItemPadding)
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
                                SettingsScreen(onBack = { showSettings = false })
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

    // SingleTop: a notification tap while the app is open re-delivers here.
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        deepLinkEpochDay.value =
            intent.getLongExtra(EXTRA_EPOCH_DAY, -1L).takeIf { it >= 0 }
    }
}

