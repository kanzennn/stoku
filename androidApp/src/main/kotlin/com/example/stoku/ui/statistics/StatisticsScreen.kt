package com.example.stoku.ui.statistics

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.stoku.ui.theme.BlueScan
import com.example.stoku.ui.theme.BlueScanBg
import com.example.stoku.ui.theme.BorderInput
import com.example.stoku.ui.theme.BorderSubtle
import com.example.stoku.ui.theme.DangerBg
import com.example.stoku.ui.theme.DangerRed
import com.example.stoku.ui.theme.GreenBg
import com.example.stoku.ui.theme.GreenDark
import com.example.stoku.ui.theme.GreenPrimary
import com.example.stoku.ui.theme.Ink
import com.example.stoku.ui.theme.PurpleManual
import com.example.stoku.ui.theme.PurpleManualBg
import com.example.stoku.ui.theme.TextMuted
import com.example.stoku.ui.theme.TextPlaceholder
import com.example.stoku.ui.theme.TextSecondary

private val ChartBarIn = Color(0xFF0E9F6E)
private val ChartBarOut = Color(0xFFE2756F)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(viewModel: StatisticsViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showFromPicker by remember { mutableStateOf(false) }
    var showToPicker by remember { mutableStateOf(false) }
    var pendingFrom by remember { mutableStateOf<Long?>(null) }

    if (showFromPicker) {
        val state = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showFromPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pendingFrom = state.selectedDateMillis
                    showFromPicker = false
                    showToPicker = true
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showFromPicker = false }) { Text("Batal") } },
        ) { DatePicker(state = state) }
    }
    if (showToPicker) {
        val state = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showToPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.onCustomRangeChange(pendingFrom, state.selectedDateMillis)
                    showToPicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showToPicker = false }) { Text("Batal") } },
        ) { DatePicker(state = state) }
    }

    Surface(color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp),
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 14.dp, bottom = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Statistik", style = MaterialTheme.typography.headlineLarge, color = Ink)
            }

            // Date range chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(bottom = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(7.dp),
            ) {
                StatsDateRangePreset.entries.forEach { preset ->
                    val selected = uiState.filters.preset == preset
                    Surface(
                        onClick = {
                            if (preset == StatsDateRangePreset.CUSTOM) showFromPicker = true
                            else viewModel.onPresetChange(preset)
                        },
                        color = if (selected) Ink else MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(20.dp),
                    ) {
                        Text(
                            text = preset.label,
                            style = MaterialTheme.typography.labelMedium,
                            color = if (selected) Color.White else TextSecondary,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                        )
                    }
                }
            }

            // Total Masuk + Total Keluar cards
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(11.dp)) {
                SummaryCard(
                    label = "Total Masuk",
                    value = uiState.totalQtyIn.toString(),
                    bg = GreenBg,
                    valueColor = GreenDark,
                    modifier = Modifier.weight(1f),
                )
                SummaryCard(
                    label = "Total Keluar",
                    value = uiState.totalQtyOut.toString(),
                    bg = DangerBg,
                    valueColor = DangerRed,
                    modifier = Modifier.weight(1f),
                )
            }

            // Bar chart
            Spacer(modifier = Modifier.height(18.dp))
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, BorderSubtle, RoundedCornerShape(18.dp)),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("Masuk vs Keluar / hari", style = MaterialTheme.typography.bodyLarge, color = Ink)
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            LegendDot(color = ChartBarIn, label = "Masuk")
                            LegendDot(color = ChartBarOut, label = "Keluar")
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    StokuBarChart(data = uiState.dailyQuantities, modifier = Modifier.fillMaxWidth().height(140.dp))
                }
            }

            // Sumber transaksi
            Spacer(modifier = Modifier.height(18.dp))
            Text("Sumber Transaksi", style = MaterialTheme.typography.titleMedium, color = Ink)
            Spacer(modifier = Modifier.height(10.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(11.dp)) {
                SourceBox(count = uiState.scanCount, label = "Scan", bg = BlueScanBg, color = BlueScan, modifier = Modifier.weight(1f))
                SourceBox(count = uiState.manualCount, label = "Manual", bg = PurpleManualBg, color = PurpleManual, modifier = Modifier.weight(1f))
            }

            // Top 5 products
            Spacer(modifier = Modifier.height(18.dp))
            Text("Top 5 Produk Terlaris", style = MaterialTheme.typography.titleMedium, color = Ink)
            Spacer(modifier = Modifier.height(10.dp))

            if (uiState.topProducts.isEmpty()) {
                Text(
                    text = "Belum ada data transaksi keluar",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPlaceholder,
                    modifier = Modifier.padding(vertical = 8.dp),
                )
            } else {
                val maxQty = uiState.topProducts.firstOrNull()?.totalQtyOut?.coerceAtLeast(1) ?: 1
                Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                    uiState.topProducts.forEachIndexed { index, product ->
                        TopProductRow(rank = index + 1, product = product, maxQty = maxQty)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SummaryCard(label: String, value: String, bg: Color, valueColor: Color, modifier: Modifier = Modifier) {
    Surface(
        color = bg,
        shape = RoundedCornerShape(18.dp),
        modifier = modifier,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = label, style = MaterialTheme.typography.labelMedium, color = valueColor.copy(alpha = 0.7f))
            Text(text = value, style = MaterialTheme.typography.displaySmall, color = valueColor, modifier = Modifier.padding(top = 2.dp))
        }
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(modifier = Modifier.size(8.dp).clip(RoundedCornerShape(2.dp)).background(color))
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = TextMuted)
    }
}

@Composable
private fun StokuBarChart(data: List<DailyQuantity>, modifier: Modifier = Modifier) {
    if (data.isEmpty()) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text("Belum ada data", style = MaterialTheme.typography.bodyMedium, color = TextPlaceholder)
        }
        return
    }
    val maxVal = data.maxOf { maxOf(it.qtyIn, it.qtyOut) }.coerceAtLeast(1)

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom,
    ) {
        data.forEach { day ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom,
                modifier = Modifier.weight(1f).fillMaxHeight(),
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    verticalAlignment = Alignment.Bottom,
                    modifier = Modifier.weight(1f),
                ) {
                    // IN bar
                    val inFraction = day.qtyIn.toFloat() / maxVal
                    val animIn by animateFloatAsState(targetValue = inFraction, label = "in")
                    Box(
                        modifier = Modifier
                            .width(7.dp)
                            .fillMaxHeight(animIn.coerceAtLeast(0.02f))
                            .clip(RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp))
                            .background(ChartBarIn),
                    )
                    // OUT bar
                    val outFraction = day.qtyOut.toFloat() / maxVal
                    val animOut by animateFloatAsState(targetValue = outFraction, label = "out")
                    Box(
                        modifier = Modifier
                            .width(7.dp)
                            .fillMaxHeight(animOut.coerceAtLeast(0.02f))
                            .clip(RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp))
                            .background(ChartBarOut),
                    )
                }
                Text(
                    text = day.dateLabel.take(5),
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                    color = TextPlaceholder,
                    modifier = Modifier.padding(top = 4.dp),
                    maxLines = 1,
                )
            }
        }
    }
}

@Composable
private fun SourceBox(count: Int, label: String, bg: Color, color: Color, modifier: Modifier = Modifier) {
    Surface(
        color = bg,
        shape = RoundedCornerShape(16.dp),
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(text = count.toString(), style = MaterialTheme.typography.displaySmall, color = color)
            Text(text = label, style = MaterialTheme.typography.labelMedium, color = color.copy(alpha = 0.7f))
        }
    }
}

@Composable
private fun TopProductRow(rank: Int, product: TopProduct, maxQty: Int) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp)),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Surface(color = Ink, shape = RoundedCornerShape(8.dp), modifier = Modifier.size(26.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Text(text = "#$rank", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = Color.White)
                }
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = product.productName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Ink,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(BorderSubtle),
                ) {
                    val fraction = product.totalQtyOut.toFloat() / maxQty
                    val anim by animateFloatAsState(targetValue = fraction, label = "bar")
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(anim)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(2.dp))
                            .background(GreenPrimary),
                    )
                }
            }
            Text(
                text = "${product.totalQtyOut}",
                style = MaterialTheme.typography.labelLarge,
                color = TextSecondary,
            )
        }
    }
}
