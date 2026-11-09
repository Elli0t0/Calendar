package io.github.elli0t0.calendar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.annotation.DrawableRes
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.elli0t0.calendar.data.TransactionMarkerProvider
import io.github.elli0t0.calendar.data.TxRepository
import io.github.elli0t0.calendar.ui.calendar.CalendarScreen
import io.github.elli0t0.calendar.ui.nav.PlaceholderScreen
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

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        TxRepository.init(applicationContext)
        setContent {
            CalendarTheme {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    var tab by remember { mutableStateOf(Tab.CALENDAR) }
                    val markerProvider = remember { TransactionMarkerProvider() }
                    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
                    Scaffold(
                        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
                        topBar = {
                            LargeTopAppBar(
                                title = {
                                    Text(
                                        text = stringResource(tab.titleRes),
                                        fontWeight = FontWeight.Bold
                                    )
                                },
                                scrollBehavior = scrollBehavior
                            )
                        }
                    ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            when (tab) {
                                Tab.CALENDAR -> CalendarScreen(markerProvider = markerProvider)
                                Tab.TRANSACTIONS -> TransactionsScreen()
                                else -> PlaceholderScreen(tab = tab)
                            }
                            PillNav(
                                selected = tab,
                                onSelect = { tab = it },
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(bottom = 16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PillNav(
    selected: Tab,
    onSelect: (Tab) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(32.dp),
        color = scheme.surfaceContainerHigh,
        shadowElevation = 8.dp
    ) {
        Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)) {
            Tab.entries.forEach { tab ->
                val isSelected = tab == selected
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(24.dp))
                        .then(
                            if (isSelected) {
                                Modifier.background(scheme.primary)
                            } else {
                                Modifier
                            }
                        )
                        .clickable { onSelect(tab) }
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(tabIcon(tab)),
                        contentDescription = null,
                        tint = if (isSelected) scheme.onPrimary else scheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(tab.titleRes),
                        fontSize = 14.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) scheme.onPrimary else scheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
