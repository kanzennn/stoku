package com.example.stoku.ui.settings

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.stoku.domain.model.UserRole
import com.example.stoku.ui.navigation.Routes
import com.example.stoku.ui.theme.BorderSubtle
import com.example.stoku.ui.theme.DangerRed
import com.example.stoku.ui.theme.GreenPrimary
import com.example.stoku.ui.theme.Ink
import com.example.stoku.ui.theme.TextMuted
import com.example.stoku.ui.theme.TextSecondary

@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val versionName = remember {
        runCatching { context.packageManager.getPackageInfo(context.packageName, 0).versionName }.getOrNull() ?: "-"
    }

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 14.dp, bottom = 24.dp),
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
                Text(
                    text = "Pengaturan",
                    style = MaterialTheme.typography.headlineLarge,
                    color = Ink,
                )
            }

            // Low stock threshold card
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, BorderSubtle, RoundedCornerShape(18.dp)),
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Ambang Stok Minimum Global",
                        style = MaterialTheme.typography.titleMedium,
                        color = Ink,
                    )
                    Text(
                        text = "Produk dengan stok di bawah nilai ini akan ditandai sebagai \"menipis\".",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextMuted,
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        StepperButton(
                            symbol = "−",
                            enabled = uiState.globalLowStockThreshold > 1,
                            onClick = { viewModel.saveThreshold(uiState.globalLowStockThreshold - 1) },
                        )
                        Text(
                            text = uiState.globalLowStockThreshold.toString(),
                            style = MaterialTheme.typography.displaySmall,
                            color = Ink,
                            modifier = Modifier.padding(horizontal = 32.dp),
                        )
                        StepperButton(
                            symbol = "+",
                            enabled = true,
                            onClick = { viewModel.saveThreshold(uiState.globalLowStockThreshold + 1) },
                        )
                    }

                    uiState.errorMessage?.let {
                        Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                    uiState.successMessage?.let {
                        Text(it, color = GreenPrimary, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 20.dp), color = MaterialTheme.colorScheme.outlineVariant)

            // Brand & Category management
            Text(
                text = "Manajemen Data",
                style = MaterialTheme.typography.titleMedium,
                color = Ink,
                modifier = Modifier.padding(bottom = 12.dp),
            )
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, BorderSubtle, RoundedCornerShape(18.dp)),
            ) {
                Column {
                    SettingsNavRow(
                        label = "Brand",
                        onClick = { navController.navigate(Routes.BRAND_LIST) },
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    SettingsNavRow(
                        label = "Kategori",
                        onClick = { navController.navigate(Routes.CATEGORY_LIST) },
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 20.dp), color = MaterialTheme.colorScheme.outlineVariant)

            val roleLabel = when (uiState.role) {
                UserRole.OWNER -> "Owner · Akses Penuh"
                UserRole.ADMIN -> "Admin · Kelola Inventori"
                UserRole.KASIR -> "Kasir · Scan Keluar"
                null -> ""
            }
            Text(
                text = "StokVape · v$versionName · $roleLabel",
                style = MaterialTheme.typography.bodyMedium,
                color = TextMuted,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 20.dp), color = MaterialTheme.colorScheme.outlineVariant)

            OutlinedButton(
                onClick = viewModel::logout,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = DangerRed),
                border = androidx.compose.foundation.BorderStroke(1.dp, DangerRed),
                shape = RoundedCornerShape(13.dp),
            ) {
                Text("Keluar Akun", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Composable
private fun SettingsNavRow(label: String, onClick: () -> Unit) {
    Surface(onClick = onClick, color = Color.Transparent) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = label, style = MaterialTheme.typography.bodyLarge, color = Ink)
            Text(text = "›", style = MaterialTheme.typography.titleLarge, color = TextSecondary)
        }
    }
}

@Composable
private fun StepperButton(symbol: String, enabled: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        color = if (enabled) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.size(48.dp),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = symbol,
                style = MaterialTheme.typography.headlineSmall,
                color = if (enabled) Ink else TextMuted,
            )
        }
    }
}
