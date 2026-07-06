package com.example.stoku.ui.manual

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.stoku.ui.product.AddNewProductForm
import com.example.stoku.ui.product.RestockForm
import com.example.stoku.ui.scan.ScanSubHeader
import com.example.stoku.ui.theme.BorderInput
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

@Composable
fun ManualMasukScreen(
    navController: NavHostController,
    viewModel: ManualMasukViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var skuInput by remember { mutableStateOf("") }

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            when (val mode = uiState.mode) {
                is ManualMasukMode.EnterSku -> Column(modifier = Modifier.fillMaxSize()) {
                    ScanSubHeader(title = "Input Masuk", onBack = { navController.popBackStack() })
                    Column(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        OutlinedTextField(
                            value = skuInput,
                            onValueChange = { skuInput = it },
                            label = { Text("SKU Produk", style = MaterialTheme.typography.labelMedium) },
                            placeholder = { Text("Masukkan SKU…", color = TextPlaceholder) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(13.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GreenPrimary, unfocusedBorderColor = BorderInput),
                        )
                        uiState.errorMessage?.let {
                            Text(text = it, color = DangerRed, style = MaterialTheme.typography.bodySmall)
                        }
                        FilledTonalButton(
                            onClick = { viewModel.checkProduct(skuInput) },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            shape = RoundedCornerShape(13.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(containerColor = GreenPrimary, contentColor = Color.White),
                        ) {
                            Text("Cek Produk", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }

                is ManualMasukMode.Loading -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = GreenPrimary)
                }

                is ManualMasukMode.NewProduct -> Column(modifier = Modifier.fillMaxSize()) {
                    ScanSubHeader(title = "Input Masuk", onBack = { skuInput = ""; viewModel.inputLagi() })
                    AddNewProductForm(
                        sku = mode.sku,
                        skuEditable = true,
                        showNotesField = true,
                        errorMessage = uiState.errorMessage,
                        isSubmitting = uiState.isSubmitting,
                        onSubmit = { sku, brand, productName, category, cost, sell, qty, notes ->
                            viewModel.submitNewProduct(sku, brand, productName, category, cost, sell, qty, notes)
                        },
                        onCancel = viewModel::inputLagi,
                        modifier = Modifier.fillMaxSize(),
                    )
                }

                is ManualMasukMode.Restock -> Column(modifier = Modifier.fillMaxSize()) {
                    ScanSubHeader(title = "Input Masuk", onBack = viewModel::inputLagi)
                    RestockForm(
                        product = mode.product,
                        showNotesField = true,
                        errorMessage = uiState.errorMessage,
                        isSubmitting = uiState.isSubmitting,
                        onSubmit = { cost, sell, qty, notes -> viewModel.submitRestock(mode.product.sku, cost, sell, qty, notes) },
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
                    onAction = { skuInput = ""; viewModel.inputLagi() },
                    onSelesai = { navController.popBackStack() },
                    isIn = true,
                )
            }
        }
    }
}

@Composable
internal fun SuccessOverlay(
    message: String,
    actionLabel: String,
    onAction: () -> Unit,
    onSelesai: () -> Unit,
    isIn: Boolean,
) {
    Surface(
        color = MaterialTheme.colorScheme.background,
        modifier = Modifier.fillMaxSize(),
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Surface(
                    color = if (isIn) GreenBg else DangerBg,
                    shape = CircleShape,
                    modifier = Modifier.size(72.dp).border(2.dp, if (isIn) GreenDark else DangerRed, CircleShape),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "✓",
                            color = if (isIn) GreenDark else DangerRed,
                            style = MaterialTheme.typography.headlineLarge.copy(fontSize = 28.sp),
                        )
                    }
                }
                Text("Berhasil!", style = MaterialTheme.typography.headlineLarge, color = Ink)
                Text(text = message, style = MaterialTheme.typography.bodyMedium, color = TextMuted, textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(8.dp))
                FilledTonalButton(
                    onClick = onAction,
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(13.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(containerColor = GreenPrimary, contentColor = Color.White),
                ) {
                    Text(actionLabel, style = MaterialTheme.typography.labelLarge)
                }
                Surface(onClick = onSelesai, color = SurfaceAlt, shape = RoundedCornerShape(13.dp), modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Selesai",
                        style = MaterialTheme.typography.labelLarge,
                        color = TextSecondary,
                        modifier = Modifier.padding(vertical = 13.dp),
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}
