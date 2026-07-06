package com.example.stoku.ui.product

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.unit.sp
import com.example.stoku.domain.model.Product
import com.example.stoku.ui.theme.BorderInput
import com.example.stoku.ui.theme.BorderSubtle
import com.example.stoku.ui.theme.DangerRed
import com.example.stoku.ui.theme.GreenDark
import com.example.stoku.ui.theme.GreenPrimary
import com.example.stoku.ui.theme.Ink
import com.example.stoku.ui.theme.SurfaceAlt
import com.example.stoku.ui.theme.TextMuted
import com.example.stoku.ui.theme.TextPlaceholder
import com.example.stoku.ui.theme.TextSecondary
import com.example.stoku.util.RupiahFormatter

/** Shared by Scan Keluar and Manual Keluar to record an OUT transaction for an existing product. */
@Composable
fun ProductOutForm(
    product: Product,
    showCostPrice: Boolean,
    showNotesField: Boolean,
    errorMessage: String?,
    isSubmitting: Boolean,
    onSubmit: (quantity: Int, notes: String?) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var quantity by remember(product.sku) { mutableIntStateOf(1) }
    var notes by remember(product.sku) { mutableStateOf("") }
    var validationError by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        // Product info card
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp)),
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = product.productName, style = MaterialTheme.typography.titleMedium, color = Ink)
                        Text(
                            text = "${product.brandName} · ${product.sku}",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted,
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = product.stock.toString(),
                            style = MaterialTheme.typography.displaySmall.copy(fontSize = 22.sp),
                            color = Ink,
                        )
                        Text("atas kini", style = MaterialTheme.typography.labelSmall, color = TextPlaceholder)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    Column {
                        Text("Harga Jual", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                        Text(RupiahFormatter.format(product.sellingPrice), style = MaterialTheme.typography.bodyMedium, color = GreenDark)
                    }
                    if (showCostPrice && product.costPrice != null) {
                        Column {
                            Text("Harga Modal", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                            Text(RupiahFormatter.format(product.costPrice), style = MaterialTheme.typography.bodyMedium, color = Ink)
                        }
                    }
                }
            }
        }

        // Quantity stepper
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Quantity keluar", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                Text("maks ${product.stock}", style = MaterialTheme.typography.labelSmall, color = TextPlaceholder)
            }
            QtyStepperRow(
                qty = quantity,
                onDecrement = { if (quantity > 1) quantity-- },
                onIncrement = { if (quantity < product.stock) quantity++ },
                maxQty = product.stock,
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
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GreenPrimary,
                    unfocusedBorderColor = BorderInput,
                ),
            )
        }

        (validationError ?: errorMessage)?.let { message ->
            Text(text = message, color = DangerRed, style = MaterialTheme.typography.bodySmall)
        }

        FilledTonalButton(
            onClick = {
                validationError = when {
                    quantity <= 0 -> "Jumlah harus lebih dari 0"
                    quantity > product.stock -> "Jumlah melebihi stok yang tersedia"
                    else -> null
                }
                if (validationError == null) {
                    onSubmit(quantity, notes.ifBlank { null })
                }
            },
            enabled = !isSubmitting && product.stock > 0,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(13.dp),
            colors = ButtonDefaults.filledTonalButtonColors(containerColor = DangerRed, contentColor = Color.White),
        ) {
            Text("Konfirmasi Keluar", style = MaterialTheme.typography.labelLarge)
        }

        Surface(
            onClick = onCancel,
            enabled = !isSubmitting,
            color = SurfaceAlt,
            shape = RoundedCornerShape(13.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = "Scan Produk Lain",
                style = MaterialTheme.typography.labelLarge,
                color = TextSecondary,
                modifier = Modifier.padding(vertical = 13.dp),
                textAlign = TextAlign.Center,
            )
        }
    }
}
