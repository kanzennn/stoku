 package com.example.stoku.ui.product

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.stoku.ui.theme.BlueScan
import com.example.stoku.ui.theme.BlueScanBg
import com.example.stoku.ui.theme.BorderInput
import com.example.stoku.ui.theme.DangerRed
import com.example.stoku.ui.theme.GreenPrimary
import com.example.stoku.ui.theme.Ink
import com.example.stoku.ui.theme.SurfaceAlt
import com.example.stoku.ui.theme.TextSecondary

/** Shared by Scan Masuk and Manual Masuk for products with no existing SKU match. */
@Composable
fun AddNewProductForm(
    sku: String,
    skuEditable: Boolean,
    showNotesField: Boolean,
    errorMessage: String?,
    isSubmitting: Boolean,
    onSubmit: (
        sku: String,
        brandName: String,
        productName: String,
        category: String,
        costPrice: Long,
        sellingPrice: Long,
        quantity: Int,
        notes: String?,
    ) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
    optionsViewModel: BrandCategoryOptionsViewModel = hiltViewModel(),
) {
    val brands by optionsViewModel.brands.collectAsStateWithLifecycle()
    val categories by optionsViewModel.categories.collectAsStateWithLifecycle()

    var skuValue by remember(sku) { mutableStateOf(sku) }
    var brandName by remember { mutableStateOf("") }
    var productName by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("") }
    var costPrice by remember { mutableStateOf("") }
    var sellingPrice by remember { mutableStateOf("") }
    var quantity by remember { mutableIntStateOf(1) }
    var notes by remember { mutableStateOf("") }
    var validationError by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        // Blue "SKU belum terdaftar" banner
        Surface(
            color = BlueScanBg,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .border(1.dp, BlueScan, RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                ) {
                    Text("SKU", style = MaterialTheme.typography.labelSmall, color = BlueScan)
                }
                Column {
                    Text("SKU belum terdaftar", style = MaterialTheme.typography.bodyMedium, color = BlueScan)
                    Text("Isi data produk baru di bawah ini.", style = MaterialTheme.typography.labelSmall, color = BlueScan.copy(alpha = 0.7f))
                }
            }
        }

        OutlinedTextField(
            value = skuValue,
            onValueChange = { skuValue = it },
            label = { Text("SKU", style = MaterialTheme.typography.labelMedium) },
            enabled = skuEditable && !isSubmitting,
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(13.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GreenPrimary, unfocusedBorderColor = BorderInput),
        )
        OutlinedTextField(
            value = brandName,
            onValueChange = { brandName = it },
            label = { Text("Nama Brand", style = MaterialTheme.typography.labelMedium) },
            enabled = !isSubmitting,
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(13.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GreenPrimary, unfocusedBorderColor = BorderInput),
        )
        if (brands.isNotEmpty()) {
            NamePickerChipRow(
                names = brands.map { it.name },
                selected = brandName,
                onSelect = { brandName = it },
                enabled = !isSubmitting,
            )
        }
        OutlinedTextField(
            value = productName,
            onValueChange = { productName = it },
            label = { Text("Nama Produk", style = MaterialTheme.typography.labelMedium) },
            enabled = !isSubmitting,
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(13.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GreenPrimary, unfocusedBorderColor = BorderInput),
        )

        // Category chip-select — sourced from the managed Category table (Settings > Manajemen Data)
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Kategori", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
            NamePickerChipRow(
                names = categories.map { it.name },
                selected = selectedCategory,
                onSelect = { selectedCategory = it },
                enabled = !isSubmitting,
            )
        }

        OutlinedTextField(
            value = costPrice,
            onValueChange = { costPrice = it.filter(Char::isDigit) },
            label = { Text("Harga Modal (Rp)", style = MaterialTheme.typography.labelMedium) },
            enabled = !isSubmitting,
            singleLine = true,
            prefix = { Text("Rp ", color = com.example.stoku.ui.theme.TextPlaceholder) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(13.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GreenPrimary, unfocusedBorderColor = BorderInput),
        )
        OutlinedTextField(
            value = sellingPrice,
            onValueChange = { sellingPrice = it.filter(Char::isDigit) },
            label = { Text("Harga Jual (Rp)", style = MaterialTheme.typography.labelMedium) },
            enabled = !isSubmitting,
            singleLine = true,
            prefix = { Text("Rp ", color = com.example.stoku.ui.theme.TextPlaceholder) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(13.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GreenPrimary, unfocusedBorderColor = BorderInput),
        )

        // Quantity stepper
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Quantity Awal", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
            QtyStepperRow(
                qty = quantity,
                onDecrement = { if (quantity > 1) quantity-- },
                onIncrement = { quantity++ },
            )
        }

        if (showNotesField) {
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Keterangan (opsional)", style = MaterialTheme.typography.labelMedium) },
                enabled = !isSubmitting,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(13.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GreenPrimary, unfocusedBorderColor = BorderInput),
            )
        }

        (validationError ?: errorMessage)?.let { message ->
            Text(text = message, color = DangerRed, style = MaterialTheme.typography.bodySmall)
        }

        FilledTonalButton(
            onClick = {
                val cost = costPrice.toLongOrNull()
                val sell = sellingPrice.toLongOrNull()
                validationError = when {
                    skuValue.isBlank() || brandName.isBlank() || productName.isBlank() -> "Semua kolom wajib diisi"
                    selectedCategory.isBlank() -> "Pilih kategori produk"
                    cost == null || sell == null -> "Harga tidak valid"
                    else -> null
                }
                if (validationError == null && cost != null && sell != null) {
                    optionsViewModel.ensureBrandExists(brandName)
                    onSubmit(skuValue, brandName, productName, selectedCategory, cost, sell, quantity, notes.ifBlank { null })
                }
            },
            enabled = !isSubmitting,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(13.dp),
            colors = ButtonDefaults.filledTonalButtonColors(containerColor = GreenPrimary, contentColor = Color.White),
        ) {
            Text("Simpan Produk Baru", style = MaterialTheme.typography.labelLarge)
        }

        Surface(
            onClick = onCancel,
            enabled = !isSubmitting,
            color = SurfaceAlt,
            shape = RoundedCornerShape(13.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = "Batal",
                style = MaterialTheme.typography.labelLarge,
                color = TextSecondary,
                modifier = Modifier.padding(vertical = 13.dp),
                textAlign = TextAlign.Center,
            )
        }
    }
}
