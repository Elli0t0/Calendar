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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.ReceiptLong
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.elli0t0.calendar.data.EmptyMarkerProvider
import io.github.elli0t0.calendar.data.FakeMarkerProvider
import io.github.elli0t0.calendar.ui.calendar.CalendarScreen
import io.github.elli0t0.calendar.ui.nav.PlaceholderScreen
import io.github.elli0t0.calendar.ui.nav.Tab
import io.github.elli0t0.calendar.ui.theme.CalendarTheme

private fun tabIcon(tab: Tab): ImageVector = when (tab) {
    Tab.CALENDAR -> Icons.Filled.DateRange
    Tab.TRANSACTIONS -> Icons.Filled.ReceiptLong
    Tab.INSTALLMENTS -> Icons.Filled.CreditCard
    Tab.STATS -> Icons.Filled.BarChart
}

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CalendarTheme {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    var tab by remember { mutableStateOf(Tab.CALENDAR) }
                    val markerProvider = remember {
                        if (BuildConfig.DEBUG) FakeMarkerProvider() else EmptyMarkerProvider()
                    }
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
                        imageVector = tabIcon(tab),
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
