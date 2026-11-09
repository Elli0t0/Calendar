package io.github.elli0t0.calendar.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.elli0t0.calendar.R
import io.github.elli0t0.calendar.data.AppSettings
import io.github.elli0t0.calendar.data.MarkerStyle
import io.github.elli0t0.calendar.data.MarkerType
import io.github.elli0t0.calendar.ui.calendar.markerColor
import io.github.elli0t0.calendar.ui.theme.Dimens

private fun markerStyleLabelRes(style: MarkerStyle): Int = when (style) {
    MarkerStyle.VERY_MUTED -> R.string.marker_very_muted
    MarkerStyle.MUTED -> R.string.marker_muted
    MarkerStyle.BRIGHT -> R.string.marker_bright
    MarkerStyle.VERY_BRIGHT -> R.string.marker_very_bright
}

/** The Calendar style page: event appearance. (4d adds the rest.) */
@Composable
fun CalendarStyleScreen(onBack: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val settings by AppSettings.settings.collectAsState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(scheme.background)
            .padding(bottom = 20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    painter = painterResource(R.drawable.ic_arrow_left),
                    contentDescription = stringResource(R.string.back),
                    tint = scheme.onSurface
                )
            }
            Text(
                text = stringResource(R.string.settings_calendar_style),
                fontSize = Dimens.HeaderTitle,
                fontWeight = FontWeight.Bold,
                color = scheme.onSurface
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SettingsSection(label = stringResource(R.string.settings_event_colors)) {
                MarkerStyle.entries.forEachIndexed { index, style ->
                    if (index > 0) {
                        SectionDivider()
                    }
                    MarkerStyleRow(
                        style = style,
                        selected = settings.markerStyle == style,
                        onClick = { AppSettings.setMarkerStyle(style) }
                    )
                }
            }
        }
    }
}

@Composable
private fun MarkerStyleRow(
    style: MarkerStyle,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = stringResource(markerStyleLabelRes(style)),
            fontSize = Dimens.SettingsRowTitle,
            color = scheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            MarkerType.entries.forEach { type ->
                Box(
                    modifier = Modifier
                        .width(24.dp)
                        .height(3.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(markerColor(type, -1, style))
                )
            }
        }
    }
}
