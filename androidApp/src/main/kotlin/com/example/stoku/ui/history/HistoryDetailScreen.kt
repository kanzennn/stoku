package com.example.stoku.ui.history

import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.stoku.domain.model.UserRole
import com.example.stoku.domain.model.canViewCostPrice
import com.example.stoku.ui.theme.BlueScan
import com.example.stoku.ui.theme.BlueScanBg
import com.example.stoku.ui.theme.BorderSubtle
import com.example.stoku.ui.theme.DangerBg
import com.example.stoku.ui.theme.DangerRed
import com.example.stoku.ui.theme.Divider
import com.example.stoku.ui.theme.GreenBg
import com.example.stoku.ui.theme.GreenDark
import com.example.stoku.ui.theme.Ink
import com.example.stoku.ui.theme.PurpleManual
import com.example.stoku.ui.theme.PurpleManualBg
import com.example.stoku.ui.theme.TextMuted
import com.example.stoku.ui.theme.TextPlaceholder
import com.example.stoku.ui.theme.TextSecondary
import com.example.stoku.util.RupiahFormatter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryDetailScreen(
    navController: NavHostController,
    viewModel: HistoryDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val transaction = uiState.transaction ?: return
    val role = uiState.role

    val dateFormat = remember { SimpleDateFormat("dd MMM · HH:mm", Locale("id", "ID")) }
    val isIn = transaction.type.value == "IN"
    val showCostPrice = role != null && role != UserRole.KASIR
    val isManual = transaction.source.value == "MANUAL"

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Surface(
                    onClick = { navController.popBackStack() },
                    color = Color.Transparent,
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Text("‹", style = MaterialTheme.typography.headlineLarge, color = Ink, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                }
                Text("Detail Transaksi", style = MaterialTheme.typography.headlineLarge, color = Ink)
            }

            // Hero section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Surface(
                    color = if (isIn) GreenBg else DangerBg,
                    shape = CircleShape,
                    modifier = Modifier.size(56.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = if (isIn) "↓" else "↑",
                            color = if (isIn) GreenDark else DangerRed,
                            style = MaterialTheme.typography.headlineLarge.copy(fontSize = 22.sp),
                        )
                    }
                }
                Text(
                    text = "${if (isIn) "+" else "-"}${transaction.quantity} unit",
                    style = MaterialTheme.typography.displaySmall,
                    color = if (isIn) GreenDark else DangerRed,
                )
                uiState.productName?.let {
                    Text(text = it, style = MaterialTheme.typography.bodyLarge, color = Ink)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    SourcePill(source = transaction.source.value)
                    TypePill(type = transaction.type.value)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Detail rows card
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp, bottomStart = 0.dp, bottomEnd = 0.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp)
                    .border(1.dp, BorderSubtle, RoundedCornerShape(18.dp)),
            ) {
                Column(modifier = Modifier.padding(4.dp)) {
                    DetailRow("SKU", transaction.sku)
                    RowDivider()
                    uiState.productName?.let {
                        DetailRow("Produk", it)
                        RowDivider()
                    }
                    DetailRow("Tanggal", dateFormat.format(Date(transaction.createdAt)))
                    RowDivider()
                    DetailRow("Jumlah", "${transaction.quantity} unit")
                    RowDivider()
                    DetailRow("Sumber", if (isManual) "Manual" else "Scan")
                    RowDivider()
                    uiState.username?.let {
                        DetailRow("User", it.replaceFirstChar { c -> c.uppercase() })
                        RowDivider()
                    }
                    if (showCostPrice) {
                        transaction.costPriceSnapshot?.let {
                            DetailRow("Harga Modal", RupiahFormatter.format(it))
                            RowDivider()
                        }
                        DetailRow("Harga Jual", RupiahFormatter.format(transaction.sellingPriceSnapshot))
                    }
                    transaction.notes?.let {
                        if (it.isNotBlank()) {
                            RowDivider()
                            DetailRow("Keterangan", it, valueStyle = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    valueStyle: TextStyle = MaterialTheme.typography.bodyMedium,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 13.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = label, style = MaterialTheme.typography.labelMedium, color = TextMuted)
        Text(text = value, style = valueStyle, color = Ink)
    }
}

@Composable
private fun RowDivider() {
    HorizontalDivider(color = Divider, thickness = 1.dp)
}

@Composable
private fun SourcePill(source: String) {
    val isManual = source == "MANUAL"
    val bg = if (isManual) PurpleManualBg else BlueScanBg
    val color = if (isManual) PurpleManual else BlueScan
    val label = if (isManual) "Manual" else "Scan"
    Surface(color = bg, shape = RoundedCornerShape(20.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
        )
    }
}

@Composable
private fun TypePill(type: String) {
    val isIn = type == "IN"
    val bg = if (isIn) GreenBg else DangerBg
    val color = if (isIn) GreenDark else DangerRed
    val label = if (isIn) "Masuk" else "Keluar"
    Surface(color = bg, shape = RoundedCornerShape(20.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
        )
    }
}
