package com.example.stoku.ui.stock

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.stoku.domain.model.Product
import com.example.stoku.domain.model.Transaction
import com.example.stoku.domain.model.TransactionType
import com.example.stoku.domain.model.UserRole
import com.example.stoku.domain.model.canViewCostPrice
import com.example.stoku.ui.product.BrandCategoryOptionsViewModel
import com.example.stoku.ui.product.NamePickerChipRow
import com.example.stoku.ui.theme.AmberWarning
import com.example.stoku.ui.theme.BorderInput
import com.example.stoku.ui.theme.BorderSubtle
import com.example.stoku.ui.theme.DangerBg
import com.example.stoku.ui.theme.DangerRed
import com.example.stoku.ui.theme.Divider
import com.example.stoku.ui.theme.GreenBg
import com.example.stoku.ui.theme.GreenDark
import com.example.stoku.ui.theme.GreenPrimary
import com.example.stoku.ui.theme.Ink
import com.example.stoku.ui.theme.TextMuted
import com.example.stoku.ui.theme.TextPlaceholder
import com.example.stoku.ui.theme.TextSecondary
import com.example.stoku.util.RupiahFormatter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val dateFormat = SimpleDateFormat("dd MMM", Locale("id", "ID"))

@Composable
fun StockDetailScreen(
    navController: NavHostController,
    viewModel: StockDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val product = uiState.product ?: return
    val role = uiState.role ?: return
    val canManage = role != UserRole.KASIR
    val canSeeCost = role.canViewCostPrice

    if (uiState.isEditing) {
        EditProductForm(
            navController = navController,
            product = product,
            errorMessage = uiState.errorMessage,
            isSaving = uiState.isSaving,
            onSave = viewModel::saveEdit,
            onCancel = viewModel::cancelEdit,
        )
        return
    }

    val isLowStock = product.stock < product.lowStockThreshold
    val stockColor = when {
        product.stock == 0 -> DangerRed
        isLowStock -> AmberWarning
        else -> Ink
    }

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
                Text("Detail Produk", style = MaterialTheme.typography.headlineLarge, color = Ink)
            }

            // Main product card
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp)
                    .border(1.dp, BorderSubtle, RoundedCornerShape(18.dp)),
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    // Category badge + stock number
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top,
                    ) {
                        Surface(
                            color = GreenBg,
                            shape = RoundedCornerShape(8.dp),
                        ) {
                            Text(
                                text = product.category,
                                style = MaterialTheme.typography.labelSmall,
                                color = GreenDark,
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = product.stock.toString(),
                                style = MaterialTheme.typography.displaySmall,
                                color = stockColor,
                            )
                            Text(text = "stok", style = MaterialTheme.typography.labelSmall, color = TextPlaceholder)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = product.productName,
                        style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp),
                        color = Ink,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = "${product.brandName} · ${product.sku}",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextMuted,
                        modifier = Modifier.padding(top = 2.dp),
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = Divider)
                    Spacer(modifier = Modifier.height(14.dp))

                    // Price grid 2×2
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        PriceCell(label = "Harga Jual", value = RupiahFormatter.format(product.sellingPrice), valueColor = GreenDark, modifier = Modifier.weight(1f))
                        if (canSeeCost) {
                            PriceCell(label = "Harga Modal", value = RupiahFormatter.format(product.costPrice ?: 0L), modifier = Modifier.weight(1f))
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        PriceCell(label = "Ambang Minimum", value = "${product.lowStockThreshold} unit", modifier = Modifier.weight(1f))
                        PriceCell(
                            label = "Diperbarui",
                            value = dateFormat.format(Date(product.updatedAt)),
                            modifier = Modifier.weight(1f),
                        )
                    }

                    if (canManage) {
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedButton(
                            onClick = viewModel::startEdit,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BorderInput),
                        ) {
                            Text(
                                "✎  Edit Info Produk",
                                style = MaterialTheme.typography.labelLarge,
                                color = TextSecondary,
                            )
                        }
                    }
                }
            }

            // Transaction history
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Riwayat Transaksi",
                style = MaterialTheme.typography.titleMedium,
                color = Ink,
                modifier = Modifier.padding(horizontal = 18.dp),
            )
            Spacer(modifier = Modifier.height(10.dp))

            if (uiState.transactions.isEmpty()) {
                Text(
                    text = "Belum ada transaksi",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPlaceholder,
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp),
                )
            } else {
                Column(
                    modifier = Modifier.padding(horizontal = 18.dp),
                    verticalArrangement = Arrangement.spacedBy(9.dp),
                ) {
                    uiState.transactions.take(6).forEach { txn ->
                        DetailTxnCard(transaction = txn)
                    }
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun PriceCell(label: String, value: String, modifier: Modifier = Modifier, valueColor: Color = Ink) {
    Column(modifier = modifier) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = TextMuted)
        Text(text = value, style = MaterialTheme.typography.bodyLarge, color = valueColor, modifier = Modifier.padding(top = 2.dp))
    }
}

@Composable
private fun DetailTxnCard(transaction: Transaction) {
    val isIn = transaction.type == TransactionType.IN
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
            Surface(
                color = if (isIn) GreenBg else DangerBg,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.size(30.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(if (isIn) "↓" else "↑", color = if (isIn) GreenDark else DangerRed, fontSize = 13.sp)
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isIn) "Masuk ${transaction.quantity} unit" else "Keluar ${transaction.quantity} unit",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Ink,
                )
                val txnDateFmt = remember { SimpleDateFormat("dd/MM · HH:mm", Locale.getDefault()) }
                Text(
                    text = txnDateFmt.format(Date(transaction.createdAt)),
                    style = MaterialTheme.typography.labelSmall,
                    color = TextPlaceholder,
                )
            }
            val isManual = transaction.source.value == "MANUAL"
            Surface(
                color = if (isManual) com.example.stoku.ui.theme.PurpleManualBg else com.example.stoku.ui.theme.BlueScanBg,
                shape = RoundedCornerShape(20.dp),
            ) {
                Text(
                    text = if (isManual) "Manual" else "Scan",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isManual) com.example.stoku.ui.theme.PurpleManual else com.example.stoku.ui.theme.BlueScan,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                )
            }
        }
    }
}

@Composable
private fun EditProductForm(
    navController: NavHostController,
    product: Product,
    errorMessage: String?,
    isSaving: Boolean,
    onSave: (brandName: String, productName: String, category: String, costPrice: Long, sellingPrice: Long, lowStockThreshold: Int) -> Unit,
    onCancel: () -> Unit,
    optionsViewModel: BrandCategoryOptionsViewModel = hiltViewModel(),
) {
    val brands by optionsViewModel.brands.collectAsStateWithLifecycle()
    val categories by optionsViewModel.categories.collectAsStateWithLifecycle()

    var brandName by remember(product.sku) { mutableStateOf(product.brandName) }
    var productName by remember(product.sku) { mutableStateOf(product.productName) }
    var selectedCategory by remember(product.sku) { mutableStateOf(product.category) }
    var costPrice by remember(product.sku) { mutableStateOf((product.costPrice ?: 0L).toString()) }
    var sellingPrice by remember(product.sku) { mutableStateOf(product.sellingPrice.toString()) }
    var lowStockThreshold by remember(product.sku) { mutableStateOf(product.lowStockThreshold.toString()) }
    var validationError by remember { mutableStateOf<String?>(null) }

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Surface(onClick = onCancel, color = Color.Transparent, shape = RoundedCornerShape(8.dp)) {
                    Text("‹", style = MaterialTheme.typography.headlineLarge, color = Ink, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                }
                Text("Edit Produk", style = MaterialTheme.typography.headlineLarge, color = Ink)
            }

            StyledTextField(value = brandName, onValueChange = { brandName = it }, label = "Nama Brand", enabled = !isSaving)
            if (brands.isNotEmpty()) {
                NamePickerChipRow(
                    names = brands.map { it.name },
                    selected = brandName,
                    onSelect = { brandName = it },
                    enabled = !isSaving,
                )
            }
            StyledTextField(value = productName, onValueChange = { productName = it }, label = "Nama Produk", enabled = !isSaving)

            // Category chips — sourced from the managed Category table (Settings > Manajemen Data)
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Kategori", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                NamePickerChipRow(
                    names = categories.map { it.name },
                    selected = selectedCategory,
                    onSelect = { selectedCategory = it },
                    enabled = !isSaving,
                )
            }

            StyledTextField(value = costPrice, onValueChange = { costPrice = it.filter(Char::isDigit) }, label = "Harga Modal (Rp)", keyboardType = KeyboardType.Number, enabled = !isSaving)
            StyledTextField(value = sellingPrice, onValueChange = { sellingPrice = it.filter(Char::isDigit) }, label = "Harga Jual (Rp)", keyboardType = KeyboardType.Number, enabled = !isSaving)
            StyledTextField(value = lowStockThreshold, onValueChange = { lowStockThreshold = it.filter(Char::isDigit) }, label = "Ambang Stok Minimum", keyboardType = KeyboardType.Number, enabled = !isSaving)

            (validationError ?: errorMessage)?.let {
                Text(text = it, color = DangerRed, style = MaterialTheme.typography.bodySmall)
            }

            Button(
                onClick = {
                    val cost = costPrice.toLongOrNull()
                    val sell = sellingPrice.toLongOrNull()
                    val threshold = lowStockThreshold.toIntOrNull()
                    validationError = when {
                        brandName.isBlank() || productName.isBlank() || selectedCategory.isBlank() -> "Semua kolom wajib diisi"
                        cost == null || sell == null -> "Harga tidak valid"
                        threshold == null || threshold < 0 -> "Ambang stok tidak valid"
                        else -> null
                    }
                    if (validationError == null && cost != null && sell != null && threshold != null) {
                        optionsViewModel.ensureBrandExists(brandName)
                        onSave(brandName, productName, selectedCategory, cost, sell, threshold)
                    }
                },
                enabled = !isSaving,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(13.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
            ) {
                Text("Simpan Perubahan", style = MaterialTheme.typography.labelLarge, color = Color.White)
            }
            OutlinedButton(
                onClick = onCancel,
                enabled = !isSaving,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(13.dp),
            ) {
                Text("Batal", style = MaterialTheme.typography.labelLarge, color = TextSecondary)
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun StyledTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    enabled: Boolean = true,
    keyboardType: KeyboardType = KeyboardType.Text,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, style = MaterialTheme.typography.labelMedium) },
        enabled = enabled,
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(13.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = GreenPrimary,
            unfocusedBorderColor = BorderInput,
            focusedLabelColor = GreenPrimary,
        ),
    )
}
