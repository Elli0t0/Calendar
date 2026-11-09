package io.github.elli0t0.calendar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.annotation.DrawableRes
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import io.github.elli0t0.calendar.data.CompositeMarkerProvider
import io.github.elli0t0.calendar.data.InstallmentMarkerProvider
import io.github.elli0t0.calendar.data.InstallmentRepository
import io.github.elli0t0.calendar.data.TransactionMarkerProvider
import io.github.elli0t0.calendar.data.TxRepository
import io.github.elli0t0.calendar.ui.calendar.CalendarScreen
import io.github.elli0t0.calendar.ui.installments.InstallmentsScreen
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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        TxRepository.init(applicationContext)
        InstallmentRepository.init(applicationContext)
        setContent {
            CalendarTheme {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    var tab by remember { mutableStateOf(Tab.CALENDAR) }
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
                            ModalDrawerSheet {
                                Text(
                                    text = stringResource(R.string.app_name),
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = scheme.onSurface,
                                    modifier = Modifier.padding(20.dp)
                                )
                                Tab.entries.forEach { item ->
                                    NavigationDrawerItem(
                                        icon = {
                                            Icon(
                                                painter = painterResource(tabIcon(item)),
                                                contentDescription = null
                                            )
                                        },
                                        label = { Text(stringResource(item.titleRes)) },
                                        selected = item == tab,
                                        onClick = {
                                            tab = item
                                            scope.launch { drawerState.close() }
                                        },
                                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                                    )
                                }
                            }
                        }
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .statusBarsPadding()
                                .navigationBarsPadding()
                        ) {
                            when (tab) {
                                Tab.CALENDAR -> CalendarScreen(
                                    markerProvider = markerProvider,
                                    onMenuClick = openDrawer
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

