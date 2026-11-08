package io.github.elli0t0.calendar.ui.nav

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.elli0t0.calendar.R

enum class Tab(val titleRes: Int) {
    CALENDAR(R.string.tab_calendar),
    TRANSACTIONS(R.string.tab_transactions),
    INSTALLMENTS(R.string.tab_installments),
    STATS(R.string.tab_stats)
}

/** Placeholder for tabs that are not built yet: a centered title. */
@Composable
fun PlaceholderScreen(tab: Tab) {
    Box(
        modifier = Modifier.fillMaxSize().padding(bottom = 120.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(tab.titleRes),
            style = MaterialTheme.typography.headlineLarge,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
