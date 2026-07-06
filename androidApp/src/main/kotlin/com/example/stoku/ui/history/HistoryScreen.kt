package com.example.stoku.ui.history

import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.stoku.domain.model.TransactionSource
import com.example.stoku.domain.model.TransactionType
import com.example.stoku.domain.model.UserRole
import com.example.stoku.ui.navigation.Routes
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.launch

private val rowDateFormat = SimpleDateFormat("dd/MM · HH:mm", Locale.getDefault())

@Composable
fun HistoryScreen(
    navController: NavHostController,
    viewModel: HistoryViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp),
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 14.dp, bottom = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text = "Riwayat", style = MaterialTheme.typography.headlineLarge, color = Ink)
                if (uiState.showPriceColumns) {
                    Surface(
                        onClick = {
                            scope.launch {
                                viewModel.exportCsv().fold(
                                    onSuccess = { snackbarHostState.showSnackbar("Berhasil diekspor ke Downloads") },
                                    onFailure = { snackbarHostState.showSnackbar("Gagal mengekspor: ${it.message}") },
                                )
                            }
                        },
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(9.dp)
                    ) {
                        Text(
                            text = "↓ CSV",
                            style = MaterialTheme.typography.labelMedium,
                            color = TextSecondary,
                            modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp),
                        )
                    }
                }
            }

            // Search
            OutlinedTextField(
                value = uiState.filters.query,
                onValueChange = viewModel::onQueryChange,
                placeholder = { Text("Cari nama atau SKU", color = TextPlaceholder) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(13.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GreenPrimary,
                    unfocusedBorderColor = BorderInput,
                ),
                leadingIcon = {
                    Text("⌕", color = TextPlaceholder, fontSize = 15.sp, modifier = Modifier.padding(start = 4.dp))
                },
            )

            // Filter chips — "Masuk" disembunyikan untuk Kasir karena mereka tidak bisa scan masuk
            val isKasir = uiState.role == UserRole.KASIR
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(vertical = 11.dp),
                horizontalArrangement = Arrangement.spacedBy(7.dp),
            ) {
                FilterChip(
                    label = "Semua",
                    selected = uiState.filters.typeFilter == null && uiState.filters.sourceFilter == null,
                    onClick = { viewModel.onTypeFilterChange(null); viewModel.onSourceFilterChange(null) },
                )
                if (!isKasir) {
                    FilterChip(
                        label = "Masuk",
                        selected = uiState.filters.typeFilter == TransactionType.IN,
                        onClick = { viewModel.onTypeFilterChange(TransactionType.IN); viewModel.onSourceFilterChange(null) },
                    )
                }
                FilterChip(
                    label = "Keluar",
                    selected = uiState.filters.typeFilter == TransactionType.OUT,
                    onClick = { viewModel.onTypeFilterChange(TransactionType.OUT); viewModel.onSourceFilterChange(null) },
                )
                FilterChip(
                    label = "Scan",
                    selected = uiState.filters.sourceFilter == TransactionSource.SCAN,
                    onClick = { viewModel.onSourceFilterChange(TransactionSource.SCAN); viewModel.onTypeFilterChange(null) },
                )
                if (!isKasir) {
                    FilterChip(
                        label = "Manual",
                        selected = uiState.filters.sourceFilter == TransactionSource.MANUAL,
                        onClick = { viewModel.onSourceFilterChange(TransactionSource.MANUAL); viewModel.onTypeFilterChange(null) },
                    )
                }
            }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(9.dp),
                contentPadding = PaddingValues(bottom = 16.dp),
            ) {
                items(uiState.rows, key = { it.transaction.id }) { row ->
                    HistoryCard(
                        row = row,
                        onClick = { navController.navigate(Routes.historyDetail(row.transaction.id)) },
                    )
                }
            }
        }
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

@Composable
private fun FilterChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        color = if (selected) Ink else MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(20.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) Color.White else TextSecondary,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
        )
    }
}

@Composable
private fun HistoryCard(row: HistoryRow, onClick: () -> Unit) {
    val txn = row.transaction
    val isIn = txn.type.value == "IN"

    Surface(
        onClick = onClick,
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, BorderSubtle, RoundedCornerShape(14.dp)),
    ) {
        Row(
            modifier = Modifier.padding(13.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            val dotBg = if (isIn) GreenBg else DangerBg
            val dotColor = if (isIn) GreenDark else DangerRed
            Surface(color = dotBg, shape = RoundedCornerShape(10.dp), modifier = Modifier.size(34.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = if (isIn) "↓" else "↑",
                        color = dotColor,
                        style = MaterialTheme.typography.titleSmall.copy(fontSize = 15.sp),
                    )
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = row.productName,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Ink,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Row(
                    modifier = Modifier.padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    SourcePill(txn.source.value)
                    Text(
                        text = "${rowDateFormat.format(Date(txn.createdAt))} · ${row.username}",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextPlaceholder,
                    )
                }
            }
            Text(
                text = if (isIn) "+${txn.quantity}" else "-${txn.quantity}",
                style = MaterialTheme.typography.titleSmall.copy(fontSize = 14.sp),
                color = if (isIn) GreenDark else DangerRed,
            )
        }
    }
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
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
        )
    }
}
