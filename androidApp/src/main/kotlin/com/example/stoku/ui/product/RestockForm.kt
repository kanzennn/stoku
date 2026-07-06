package com.example.stoku.ui.product

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stoku.domain.model.Product
import com.example.stoku.ui.theme.BorderInput
import com.example.stoku.ui.theme.BorderSubtle
import com.example.stoku.ui.theme.DangerRed
import com.example.stoku.ui.theme.GreenBg
import com.example.stoku.ui.theme.GreenDark
import com.example.stoku.ui.theme.GreenPrimary
import com.example.stoku.ui.theme.Ink
import com.example.stoku.ui.theme.SurfaceAlt
import com.example.stoku.ui.theme.TextMuted
import com.example.stoku.ui.theme.TextPlaceholder
import com.example.stoku.ui.theme.TextSecondary
import com.example.stoku.util.RupiahFormatter

/** Shared by Scan Masuk and Manual Masuk for products that already exist. */
@Composable
fun RestockForm(
    product: Product,
    showNotesField: Boolean,
    errorMessage: String?,
    isSubmitting: Boolean,
    onSubmit: (costPrice: Long, sellingPrice: Long, quantity: Int, notes: String?) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var costPrice by remember(product.sku) { mutableStateOf((product.costPrice ?: 0L).toString()) }
    var sellingPrice by remember(product.sku) { mutableStateOf(product.sellingPrice.toString()) }
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
                    Column {
                        Text("Harga Modal", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                        Text(RupiahFormatter.format(product.costPrice ?: 0L), style = MaterialTheme.typography.bodyMedium, color = Ink)
                    }
                }
            }
        }

        // Price fields
        OutlinedTextField(
            value = costPrice,
            onValueChange = { costPrice = it.filter(Char::isDigit) },
            label = { Text("Harga Modal (Rp)", style = MaterialTheme.typography.labelMedium) },
            enabled = !isSubmitting,
            singleLine = true,
            prefix = { Text("Rp ", color = TextPlaceholder) },
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
            prefix = { Text("Rp ", color = TextPlaceholder) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(13.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GreenPrimary, unfocusedBorderColor = BorderInput),
        )

        // Quantity stepper
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Quantity ditambah", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
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
                    cost == null || sell == null -> "Harga tidak valid"
                    else -> null
                }
                if (validationError == null && cost != null && sell != null) {
                    onSubmit(cost, sell, quantity, notes.ifBlank { null })
                }
            },
            enabled = !isSubmitting,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(13.dp),
            colors = ButtonDefaults.filledTonalButtonColors(containerColor = GreenPrimary, contentColor = Color.White),
        ) {
            Text("↓  Tambah Stok", style = MaterialTheme.typography.labelLarge)
        }

        Surface(
            onClick = onCancel,
            enabled = !isSubmitting,
            color = SurfaceAlt,
            shape = RoundedCornerShape(13.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = "Scan Lagi",
                style = MaterialTheme.typography.labelLarge,
                color = TextSecondary,
                modifier = Modifier.padding(vertical = 13.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
        }
    }
}

@Composable
internal fun QtyStepperRow(qty: Int, onDecrement: () -> Unit, onIncrement: () -> Unit, maxQty: Int? = null) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth(),
    ) {
        StepperButton("-", onClick = onDecrement, enabled = qty > 1)
        Text(
            text = qty.toString(),
            style = MaterialTheme.typography.displaySmall,
            color = Ink,
            modifier = Modifier.width(80.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )
        StepperButton("+", onClick = onIncrement, enabled = maxQty == null || qty < maxQty)
    }
}

@Composable
private fun StepperButton(label: String, onClick: () -> Unit, enabled: Boolean = true) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        color = SurfaceAlt,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.size(48.dp).border(1.dp, BorderInput, RoundedCornerShape(12.dp)),
    ) {
        androidx.compose.foundation.layout.Box(contentAlignment = Alignment.Center) {
            Text(text = label, style = MaterialTheme.typography.headlineLarge.copy(fontSize = 20.sp), color = if (enabled) Ink else TextPlaceholder)
        }
    }
}
