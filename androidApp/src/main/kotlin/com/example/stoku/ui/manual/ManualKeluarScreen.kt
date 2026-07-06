package com.example.stoku.ui.manual

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.stoku.ui.product.ProductOutForm
import com.example.stoku.ui.scan.ScanSubHeader
import com.example.stoku.ui.theme.AmberWarning
import com.example.stoku.ui.theme.BorderInput
import com.example.stoku.ui.theme.BorderSubtle
import com.example.stoku.ui.theme.DangerRed
import com.example.stoku.ui.theme.GreenPrimary
import com.example.stoku.ui.theme.Ink
import com.example.stoku.ui.theme.TextMuted
import com.example.stoku.ui.theme.TextPlaceholder
import com.example.stoku.ui.theme.TextSecondary

@Composable
fun ManualKeluarScreen(
    navController: NavHostController,
    viewModel: ManualKeluarViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var query by remember { mutableStateOf("") }

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            when (val mode = uiState.mode) {
                is ManualKeluarMode.Search -> Column(modifier = Modifier.fillMaxSize()) {
                    ScanSubHeader(title = "Input Keluar", onBack = { navController.popBackStack() })
                    Column(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        OutlinedTextField(
                            value = query,
                            onValueChange = { query = it },
                            label = { Text("SKU atau Nama Produk", style = MaterialTheme.typography.labelMedium) },
                            placeholder = { Text("Cari produk…", color = TextPlaceholder) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(13.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GreenPrimary, unfocusedBorderColor = BorderInput),
                        )
                        uiState.errorMessage?.let {
                            Text(text = it, color = DangerRed, style = MaterialTheme.typography.bodySmall)
                        }
                        FilledTonalButton(
                            onClick = { viewModel.search(query) },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            shape = RoundedCornerShape(13.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(containerColor = GreenPrimary, contentColor = Color.White),
                        ) {
                            Text("Cari", style = MaterialTheme.typography.labelLarge)
                        }

                        if (uiState.searchResults.isNotEmpty()) {
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                                items(uiState.searchResults, key = { it.sku }) { product ->
                                    Surface(
                                        onClick = { viewModel.selectProduct(product) },
                                        color = MaterialTheme.colorScheme.surface,
                                        shape = RoundedCornerShape(14.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .border(1.dp, BorderSubtle, RoundedCornerShape(14.dp)),
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(14.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically,
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = product.productName,
                                                    style = MaterialTheme.typography.bodyLarge,
                                                    color = Ink,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis,
                                                )
                                                Text(
                                                    text = "${product.sku} · ${product.brandName}",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = TextMuted,
                                                )
                                            }
                                            val stockColor = when {
                                                product.stock == 0 -> DangerRed
                                                product.stock < product.lowStockThreshold -> AmberWarning
                                                else -> TextSecondary
                                            }
                                            Text(
                                                text = "Stok ${product.stock}",
                                                style = MaterialTheme.typography.labelMedium,
                                                color = stockColor,
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        if (uiState.notFoundAlert) {
                            Text(
                                text = "Tidak ada produk yang cocok.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = DangerRed,
                            )
                        }
                        if (uiState.outOfStockAlert) {
                            Text(
                                text = "Produk ini tidak memiliki stok tersedia.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = DangerRed,
                            )
                        }
                    }
                }

                is ManualKeluarMode.Found -> Column(modifier = Modifier.fillMaxSize()) {
                    ScanSubHeader(title = "Input Keluar", onBack = viewModel::inputLagi)
                    ProductOutForm(
                        product = mode.product,
                        showCostPrice = viewModel.canViewCostPrice(),
                        showNotesField = true,
                        errorMessage = uiState.errorMessage,
                        isSubmitting = uiState.isSubmitting,
                        onSubmit = { quantity, notes -> viewModel.submitOut(quantity, notes) },
                        onCancel = viewModel::inputLagi,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }

            // Success overlay
            uiState.successMessage?.let { message ->
                SuccessOverlay(
                    message = message,
                    actionLabel = "Input Lagi",
                    onAction = { query = ""; viewModel.inputLagi() },
                    onSelesai = { navController.popBackStack() },
                    isIn = false,
                )
            }
        }
    }
}
