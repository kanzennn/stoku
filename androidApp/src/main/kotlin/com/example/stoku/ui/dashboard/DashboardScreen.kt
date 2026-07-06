package com.example.stoku.ui.dashboard

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.stoku.domain.model.UserRole
import com.example.stoku.ui.navigation.RouteAccess
import com.example.stoku.ui.navigation.Routes
import com.example.stoku.ui.theme.AmberBg
import com.example.stoku.ui.theme.AmberBorder
import com.example.stoku.ui.theme.AmberIcon
import com.example.stoku.ui.theme.AmberIconText
import com.example.stoku.ui.theme.AmberText
import com.example.stoku.ui.theme.AmberWarning
import com.example.stoku.ui.theme.BorderInput
import com.example.stoku.ui.theme.BorderSubtle
import com.example.stoku.ui.theme.CashAccent
import com.example.stoku.ui.theme.CashBg
import com.example.stoku.ui.theme.DangerBg
import com.example.stoku.ui.theme.DangerRed
import com.example.stoku.ui.theme.GreenBg
import com.example.stoku.ui.theme.GreenDark
import com.example.stoku.ui.theme.GreenPrimary
import com.example.stoku.ui.theme.Ink
import com.example.stoku.ui.theme.SurfaceAlt
import com.example.stoku.ui.theme.TextMuted
import com.example.stoku.ui.theme.TextPlaceholder
import com.example.stoku.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navController: NavHostController,
    viewModel: DashboardViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    if (uiState.showProfileSheet) {
        ModalBottomSheet(
            onDismissRequest = viewModel::dismissProfileSheet,
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = MaterialTheme.colorScheme.surface,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 36.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                // Avatar + name + role
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Surface(color = GreenBg, shape = CircleShape, modifier = Modifier.size(52.dp)) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = uiState.userInitials,
                                color = GreenDark,
                                style = MaterialTheme.typography.titleMedium,
                            )
                        }
                    }
                    Column {
                        Text(
                            text = uiState.userName,
                            style = MaterialTheme.typography.titleMedium,
                            color = Ink,
                        )
                        Text(
                            text = when (uiState.role) {
                                UserRole.OWNER -> "Owner · Akses Penuh"
                                UserRole.ADMIN -> "Admin · Kelola Inventori"
                                UserRole.KASIR -> "Kasir · Scan Keluar"
                                null -> ""
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted,
                        )
                    }
                }

                // Role switcher
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "GANTI PERAN",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextPlaceholder,
                        letterSpacing = 0.8.sp,
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(
                            UserRole.OWNER to "Owner",
                            UserRole.ADMIN to "Admin",
                            UserRole.KASIR to "Kasir",
                        ).forEach { (role, label) ->
                            val selected = uiState.role == role
                            Surface(
                                onClick = { viewModel.switchRole(role) },
                                color = if (selected) Ink else MaterialTheme.colorScheme.surface,
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (selected) Color.White else TextSecondary,
                                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 9.dp),
                                )
                            }
                        }
                    }
                }

                // Settings entry point (owner/admin only)
                if (RouteAccess.isAllowed(Routes.SETTINGS, uiState.role ?: UserRole.KASIR)) {
                    Surface(
                        onClick = {
                            viewModel.dismissProfileSheet()
                            navController.navigate(Routes.SETTINGS)
                        },
                        color = SurfaceAlt,
                        shape = RoundedCornerShape(13.dp),
                        modifier = Modifier.fillMaxWidth().border(1.dp, BorderSubtle, RoundedCornerShape(13.dp)),
                    ) {
                        Row(
                            modifier = Modifier.padding(13.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(11.dp),
                        ) {
                            Text("⚙", style = MaterialTheme.typography.titleMedium)
                            Text("Pengaturan", style = MaterialTheme.typography.bodyLarge, color = Ink)
                        }
                    }
                }

                // Logout
                Surface(
                    onClick = viewModel::logout,
                    color = DangerBg,
                    shape = RoundedCornerShape(13.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = "Keluar Akun",
                        style = MaterialTheme.typography.labelLarge,
                        color = DangerRed,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 14.dp),
                    )
                }
            }
        }
    }

    Surface(color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 6.dp),
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(text = "Selamat datang,", style = MaterialTheme.typography.bodyMedium, color = TextMuted)
                    Text(
                        text = uiState.userName.ifEmpty { "—" },
                        style = MaterialTheme.typography.headlineLarge,
                        color = Ink,
                    )
                }
                Surface(
                    onClick = viewModel::openProfileSheet,
                    shape = RoundedCornerShape(13.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier
                        .size(42.dp)
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(13.dp)),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = uiState.userInitials,
                            style = MaterialTheme.typography.labelLarge,
                            color = GreenDark,
                        )
                    }
                }
            }

            // Stat cards 2×2
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(11.dp)) {
                StatCard(
                    title = "Total SKU",
                    value = uiState.totalSku.toString(),
                    modifier = Modifier.weight(1f),
                )
                StatCard(
                    title = "Total Stok",
                    value = uiState.totalStock.toString(),
                    modifier = Modifier.weight(1f),
                )
            }
            Spacer(modifier = Modifier.height(11.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(11.dp)) {
                StatCard(
                    title = "Stok Menipis",
                    value = uiState.lowStockCount.toString(),
                    valueColor = if (uiState.hasLowStockAlert) AmberWarning else Ink,
                    modifier = Modifier.weight(1f),
                )
                StatCard(
                    title = "Transaksi Hari Ini",
                    value = uiState.todaysTransactionCount.toString(),
                    modifier = Modifier.weight(1f),
                )
            }

            // Low stock alert
            if (uiState.hasLowStockAlert) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    onClick = { navController.navigate(Routes.STOCK_LIST) },
                    color = AmberBg,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, AmberBorder, RoundedCornerShape(16.dp)),
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(11.dp),
                    ) {
                        Surface(color = AmberIcon, shape = RoundedCornerShape(9.dp), modifier = Modifier.size(30.dp)) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("!", color = AmberIconText, style = MaterialTheme.typography.titleSmall.copy(fontSize = 14.sp))
                            }
                        }
                        Text(
                            text = "${uiState.lowStockCount} produk di bawah ambang stok minimum",
                            style = MaterialTheme.typography.bodyMedium,
                            color = AmberText,
                            modifier = Modifier.weight(1f),
                        )
                        Text("›", color = AmberText)
                    }
                }
            }

            // Quick actions
            Spacer(modifier = Modifier.height(24.dp))
            Text(text = "Aksi Cepat", style = MaterialTheme.typography.titleMedium, color = Ink)
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(11.dp)) {
                if (uiState.showScanMasuk) {
                    QuickActionButton(
                        symbol = "↓",
                        symbolColor = GreenDark,
                        symbolBg = GreenBg,
                        label = "Scan Masuk",
                        modifier = Modifier.weight(1f),
                        onClick = { navController.navigate(Routes.SCAN_IN) },
                    )
                }
                if (uiState.showInputManual) {
                    QuickActionButton(
                        symbol = "✎",
                        symbolColor = TextSecondary,
                        symbolBg = SurfaceAlt,
                        label = "Input Manual",
                        modifier = Modifier.weight(1f),
                        onClick = { navController.navigate(Routes.MANUAL_INPUT) },
                    )
                }
                QuickActionButton(
                    symbol = "Rp",
                    symbolColor = CashAccent,
                    symbolBg = CashBg,
                    symbolIsText = true,
                    label = "Kasir",
                    modifier = Modifier.weight(1f),
                    onClick = { navController.navigate(Routes.KASIR) },
                )
            }

            // Recent transactions
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text = "Transaksi Terakhir", style = MaterialTheme.typography.titleMedium, color = Ink)
                Surface(onClick = { navController.navigate(Routes.HISTORY) }, color = Color.Transparent) {
                    Text(
                        text = "Semua ›",
                        style = MaterialTheme.typography.bodyMedium,
                        color = GreenPrimary,
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))

            if (uiState.recentTransactions.isEmpty()) {
                Text(
                    text = "Belum ada transaksi",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPlaceholder,
                    modifier = Modifier.padding(vertical = 8.dp),
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                    uiState.recentTransactions.forEach { txn ->
                        RecentTxnCard(
                            row = txn,
                            onClick = { navController.navigate(Routes.historyDetail(txn.transactionId)) },
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color = Ink,
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(18.dp),
        modifier = modifier.border(1.dp, BorderSubtle, RoundedCornerShape(18.dp)),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, style = MaterialTheme.typography.labelMedium, color = TextMuted)
            Text(
                text = value,
                style = MaterialTheme.typography.displaySmall,
                color = valueColor,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}

@Composable
private fun QuickActionButton(
    symbol: String,
    symbolColor: Color,
    symbolBg: Color,
    label: String,
    modifier: Modifier = Modifier,
    symbolIsText: Boolean = false,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(16.dp),
        modifier = modifier.border(1.dp, BorderSubtle, RoundedCornerShape(16.dp)),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(9.dp),
        ) {
            Surface(color = symbolBg, shape = RoundedCornerShape(12.dp), modifier = Modifier.size(40.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = symbol,
                        color = symbolColor,
                        style = if (symbolIsText) {
                            MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp)
                        } else {
                            MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp)
                        },
                    )
                }
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = Ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun RecentTxnCard(row: RecentTxnRow, onClick: () -> Unit) {
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
            val dotBg = if (row.isIn) GreenBg else DangerBg
            val dotColor = if (row.isIn) GreenDark else DangerRed
            Surface(color = dotBg, shape = RoundedCornerShape(10.dp), modifier = Modifier.size(34.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = if (row.isIn) "↓" else "↑",
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
                Text(
                    text = "${row.sku} · ${row.dateLabel}",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextPlaceholder,
                    modifier = Modifier.padding(top = 1.dp),
                )
            }
            Text(
                text = row.qtyLabel,
                style = MaterialTheme.typography.titleSmall.copy(fontSize = 14.sp),
                color = if (row.isIn) GreenDark else DangerRed,
            )
        }
    }
}
