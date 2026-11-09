package io.github.elli0t0.calendar.ui.installments

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.elli0t0.calendar.R
import io.github.elli0t0.calendar.data.Installment
import io.github.elli0t0.calendar.data.InstallmentRepository
import io.github.elli0t0.calendar.data.dueDate
import io.github.elli0t0.calendar.ui.common.formatAmount
import io.github.elli0t0.calendar.ui.common.fullJalaliDate
import io.github.elli0t0.calendar.ui.common.toPersianDigits

@Composable
fun InstallmentsScreen() {
    val scheme = MaterialTheme.colorScheme
    val items by InstallmentRepository.items.collectAsState()
    var showSheet by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<Installment?>(null) }
    var expandedId by remember { mutableStateOf<Long?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 120.dp)
    ) {
        if (items.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.inst_empty),
                    fontSize = 16.sp,
                    color = scheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 20.dp, top = 4.dp, bottom = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(items.sortedByDescending { it.id }, key = { it.id }) { inst ->
                    InstallmentCard(
                        inst = inst,
                        expanded = expandedId == inst.id,
                        onToggleExpand = {
                            expandedId = if (expandedId == inst.id) null else inst.id
                        },
                        onEdit = { editing = inst; showSheet = true }
                    )
                }
            }
        }
        FloatingActionButton(
            onClick = { editing = null; showSheet = true },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp),
            containerColor = scheme.primary,
            contentColor = Color.White
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_add),
                contentDescription = stringResource(R.string.tx_add)
            )
        }
    }
    if (showSheet) {
        InstallmentSheet(
            initial = editing,
            onDismiss = { showSheet = false }
        )
    }
}

@Composable
private fun InstallmentCard(
    inst: Installment,
    expanded: Boolean,
    onToggleExpand: () -> Unit,
    onEdit: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val paidCount = inst.paid.count { it < inst.count }
    val nextDue = (0 until inst.count).firstOrNull { it !in inst.paid }?.let { dueDate(inst, it) }
    Card(
        onClick = onToggleExpand,
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = scheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = inst.title,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = scheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = formatAmount(inst.amountPerMonth),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = scheme.primary
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(
                    R.string.inst_progress,
                    toPersianDigits(paidCount.toString()),
                    toPersianDigits(inst.count.toString())
                ),
                fontSize = 14.sp,
                color = scheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = if (inst.count > 0) paidCount.toFloat() / inst.count else 0f,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = scheme.primary,
                trackColor = scheme.surfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (nextDue != null) {
                    "${stringResource(R.string.inst_next)}: ${fullJalaliDate(nextDue)}"
                } else {
                    stringResource(R.string.inst_all_paid)
                },
                fontSize = 14.sp,
                color = scheme.onSurfaceVariant
            )
            if (expanded) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = scheme.surfaceVariant)
                Spacer(modifier = Modifier.height(4.dp))
                (0 until inst.count).forEach { k ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = fullJalaliDate(dueDate(inst, k)),
                            fontSize = 15.sp,
                            color = scheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        Checkbox(
                            checked = k in inst.paid,
                            onCheckedChange = { InstallmentRepository.togglePaid(inst.id, k) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(
                    onClick = onEdit,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.inst_edit))
                }
            }
        }
    }
}
