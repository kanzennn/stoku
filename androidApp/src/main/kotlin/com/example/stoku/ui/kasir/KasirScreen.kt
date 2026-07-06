package com.example.stoku.ui.kasir

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.stoku.domain.model.Product
import com.example.stoku.ui.navigation.Routes
import com.example.stoku.ui.scan.ScanCameraStage
import com.example.stoku.ui.theme.AmberWarning
import com.example.stoku.ui.theme.BorderInput
import com.example.stoku.ui.theme.BorderSubtle
import com.example.stoku.ui.theme.DangerBg
import com.example.stoku.ui.theme.DangerRed
import com.example.stoku.ui.theme.GreenBg
import com.example.stoku.ui.theme.GreenDark
import com.example.stoku.ui.theme.GreenPrimary
import com.example.stoku.ui.theme.Ink
import com.example.stoku.ui.theme.TextMuted
import com.example.stoku.ui.theme.TextPlaceholder
import com.example.stoku.ui.theme.TextSecondary
import com.example.stoku.util.ReceiptPdfExporter
import com.example.stoku.util.RupiahFormatter
import kotlinx.coroutines.launch

@Composable
fun KasirScreen(
    navController: NavHostController,
    viewModel: KasirViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when (uiState.stage) {
        KasirStage.CATALOG -> CatalogStage(uiState, viewModel, navController)
        KasirStage.PAYMENT -> PaymentStage(uiState, viewModel)
        KasirStage.RECEIPT -> ReceiptStage(uiState, viewModel, navController)
        KasirStage.SCAN -> KasirScanStage(viewModel)
    }
}

@Composable
private fun KasirScanStage(viewModel: KasirViewModel) {
    ScanCameraStage(
        title = "Scan ke Keranjang",
        onBack = viewModel::cancelScan,
        onBarcodeScanned = viewModel::onBarcodeScanned,
    )
}

// ─── CATALOG ────────────────────────────────────────────────────────────────

@Composable
private fun CatalogStage(
    uiState: KasirUiState,
    viewModel: KasirViewModel,
    navController: NavHostController,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(uiState.scanMessage) {
        uiState.scanMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.dismissScanMessage()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    onClick = { navController.navigateUp() },
                    color = Color.Transparent,
                ) {
                    Text("‹", style = MaterialTheme.typography.headlineMedium, color = Ink)
                }
                Text("Kasir", style = MaterialTheme.typography.headlineSmall, color = Ink)
                Surface(
                    onClick = viewModel::startScan,
                    color = Ink,
                    shape = RoundedCornerShape(11.dp),
                    modifier = Modifier.size(38.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("⊡", color = Color.White, fontSize = 17.sp)
                    }
                }
            }

            // Search
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = viewModel::onSearchChange,
                placeholder = { Text("Cari produk…", color = TextPlaceholder) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp),
                shape = RoundedCornerShape(13.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GreenPrimary,
                    unfocusedBorderColor = BorderInput,
                ),
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Product grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(uiState.filteredProducts, key = { it.sku }) { product ->
                    val cartQty = uiState.cart[product.sku]?.quantity ?: 0
                    ProductCard(
                        product = product,
                        cartQty = cartQty,
                        onAdd = { viewModel.addToCart(product) },
                        onDecrement = { viewModel.decrementCart(product.sku) },
                    )
                }
            }

            // Cart bar
            if (uiState.cart.isNotEmpty()) {
                CartBar(
                    itemCount = uiState.cartItemCount,
                    total = uiState.cartTotal,
                    onCheckout = viewModel::goToPayment,
                )
            }
        }
    }
}

@Composable
private fun ProductCard(
    product: Product,
    cartQty: Int,
    onAdd: () -> Unit,
    onDecrement: () -> Unit,
) {
    val isOutOfStock = product.stock == 0

    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp)),
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // Image placeholder
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = product.category.take(4).uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = TextPlaceholder,
                    )
                    if (cartQty > 0) {
                        Surface(
                            color = GreenPrimary,
                            shape = RoundedCornerShape(50),
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(6.dp)
                                .size(22.dp),
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = cartQty.toString(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White,
                                )
                            }
                        }
                    }
                    if (isOutOfStock) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.78f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "STOK HABIS",
                                style = MaterialTheme.typography.labelMedium,
                                color = DangerRed,
                            )
                        }
                    }
                }
            }

            Text(
                text = product.productName,
                style = MaterialTheme.typography.bodyMedium,
                color = Ink,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = RupiahFormatter.format(product.sellingPrice),
                style = MaterialTheme.typography.labelMedium,
                color = GreenDark,
            )

            val stockLabel = when {
                isOutOfStock -> "STOK HABIS"
                product.stock < 5 -> "Sisa ${product.stock}"
                else -> "Stok: ${product.stock}"
            }
            Text(
                text = stockLabel,
                style = MaterialTheme.typography.labelSmall,
                color = if (isOutOfStock) DangerRed else if (product.stock < 5) AmberWarning else TextPlaceholder,
            )

            if (isOutOfStock) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().height(36.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("Habis", style = MaterialTheme.typography.labelMedium, color = TextPlaceholder)
                    }
                }
            } else if (cartQty == 0) {
                Surface(
                    onClick = onAdd,
                    color = GreenPrimary,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().height(36.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("+ Tambah", style = MaterialTheme.typography.labelMedium, color = Color.White)
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Surface(
                        onClick = onDecrement,
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.size(36.dp),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("−", style = MaterialTheme.typography.titleMedium, color = Ink)
                        }
                    }
                    Text(
                        text = cartQty.toString(),
                        style = MaterialTheme.typography.titleSmall,
                        color = Ink,
                    )
                    Surface(
                        onClick = onAdd,
                        color = GreenBg,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.size(36.dp),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("+", style = MaterialTheme.typography.titleMedium, color = GreenDark)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CartBar(itemCount: Int, total: Long, onCheckout: () -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 6.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    text = "$itemCount item",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextMuted,
                )
                Text(
                    text = RupiahFormatter.format(total),
                    style = MaterialTheme.typography.titleLarge,
                    color = Ink,
                )
            }
            Surface(
                onClick = onCheckout,
                color = GreenPrimary,
                shape = RoundedCornerShape(13.dp),
            ) {
                Text(
                    text = "Bayar →",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                )
            }
        }
    }
}

@Composable
private fun DisabledPaymentMethodRow(symbol: String, title: String, subtitle: String) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(13.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, BorderSubtle, RoundedCornerShape(13.dp))
            .alpha(0.55f),
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(11.dp), modifier = Modifier.size(38.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Text(symbol, color = TextPlaceholder, style = MaterialTheme.typography.titleMedium)
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.bodyLarge, color = Ink)
                Text(subtitle, style = MaterialTheme.typography.labelSmall, color = TextPlaceholder)
            }
            Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(7.dp)) {
                Text(
                    "Segera",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextPlaceholder,
                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                )
            }
        }
    }
}

// ─── PAYMENT ────────────────────────────────────────────────────────────────

@Composable
private fun PaymentStage(uiState: KasirUiState, viewModel: KasirViewModel) {
    Scaffold(containerColor = MaterialTheme.colorScheme.background) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(onClick = viewModel::backToCatalog, color = Color.Transparent) {
                    Text("‹", style = MaterialTheme.typography.headlineMedium, color = Ink)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text("Pembayaran", style = MaterialTheme.typography.headlineSmall, color = Ink)
            }

            // Scrollable content — everything except the pinned confirm button below,
            // so the button stays reachable even when payment methods + cash chips
            // push total content taller than the screen (e.g. keyboard open).
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 18.dp),
            ) {

            // Total tagihan card
            Surface(
                color = Ink,
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Total Tagihan", style = MaterialTheme.typography.labelLarge, color = Color.White.copy(alpha = 0.7f))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = RupiahFormatter.format(uiState.cartTotal),
                        style = MaterialTheme.typography.displaySmall.copy(fontSize = 32.sp),
                        color = Color.White,
                    )
                    Text(
                        text = "${uiState.cartItemCount} item",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.6f),
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Payment method
            Text("Metode Pembayaran", style = MaterialTheme.typography.titleSmall, color = TextSecondary)
            Spacer(modifier = Modifier.height(8.dp))
            Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                Surface(
                    color = GreenBg,
                    shape = RoundedCornerShape(13.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, GreenPrimary, RoundedCornerShape(13.dp)),
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Surface(color = Color.White, shape = RoundedCornerShape(11.dp), modifier = Modifier.size(38.dp)) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("Rp", color = GreenDark, style = MaterialTheme.typography.labelMedium)
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Tunai", style = MaterialTheme.typography.bodyLarge, color = Ink)
                            Text("Bayar dengan uang tunai", style = MaterialTheme.typography.labelSmall, color = GreenDark)
                        }
                        Surface(color = GreenPrimary, shape = CircleShape, modifier = Modifier.size(22.dp)) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("✓", color = Color.White, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
                DisabledPaymentMethodRow(symbol = "▦", title = "QRIS", subtitle = "Scan untuk bayar")
                DisabledPaymentMethodRow(symbol = "▭", title = "Kartu Debit / Kredit", subtitle = "EDC / mesin gesek")
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Cash input
            Text("Uang Diterima", style = MaterialTheme.typography.titleSmall, color = TextSecondary)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = uiState.cashInput,
                onValueChange = viewModel::onCashInput,
                placeholder = { Text("0", color = TextPlaceholder) },
                prefix = { Text("Rp ", color = TextSecondary, style = MaterialTheme.typography.bodyLarge) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(13.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GreenPrimary,
                    unfocusedBorderColor = BorderInput,
                ),
            )

            // Quick cash chips — mirrors design's suggestion generator: exact total,
            // rounded up to nearest 5rb/50rb, plus common bill denominations, deduped,
            // filtered to >= total, sorted ascending, capped at 5.
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                val cartTotal = uiState.cartTotal
                fun ceilTo(x: Long, step: Long) = ((x + step - 1) / step) * step
                val suggestions = listOf(cartTotal, ceilTo(cartTotal, 5_000L), ceilTo(cartTotal, 50_000L), 50_000L, 100_000L, 150_000L, 200_000L)
                    .distinct()
                    .filter { it >= cartTotal && it > 0 }
                    .sorted()
                    .take(5)

                suggestions.forEachIndexed { index, amount ->
                    val label = if (index == 0 && amount == cartTotal) "Uang Pas" else RupiahFormatter.format(amount)
                    Surface(
                        onClick = { viewModel.setCashQuick(amount) },
                        color = if (uiState.cashLong == amount) Ink else MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(20.dp),
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (uiState.cashLong == amount) Color.White else TextSecondary,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Change
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Kembalian", style = MaterialTheme.typography.titleSmall, color = TextSecondary)
                Text(
                    text = if (uiState.change >= 0) RupiahFormatter.format(uiState.change) else "—",
                    style = MaterialTheme.typography.titleLarge,
                    color = if (uiState.change >= 0) GreenPrimary else TextPlaceholder,
                )
            }

            uiState.errorMessage?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(it, color = DangerRed, style = MaterialTheme.typography.bodySmall)
            }

            Spacer(modifier = Modifier.height(16.dp))
            } // end scrollable content Column

            // Pinned confirm button — stays reachable regardless of scroll position.
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 6.dp,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Surface(
                    onClick = viewModel::confirmPayment,
                    color = if (uiState.canConfirmPayment) GreenPrimary else MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(13.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 12.dp)
                        .height(52.dp),
                    enabled = uiState.canConfirmPayment && !uiState.isProcessing,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (uiState.isProcessing) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                        } else {
                            Text(
                                text = "Konfirmasi Pembayaran",
                                style = MaterialTheme.typography.labelLarge,
                                color = if (uiState.canConfirmPayment) Color.White else TextPlaceholder,
                            )
                        }
                    }
                }
            }
        }
    }
}

// ─── RECEIPT ────────────────────────────────────────────────────────────────

@Composable
private fun ReceiptStage(
    uiState: KasirUiState,
    viewModel: KasirViewModel,
    navController: NavHostController,
) {
    val receipt = uiState.receipt ?: return
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val receiptGraphicsLayer = rememberGraphicsLayer()

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 18.dp),
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // Receipt card — captured into receiptGraphicsLayer for PDF export below.
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, BorderSubtle, RoundedCornerShape(18.dp))
                    .drawWithContent {
                        receiptGraphicsLayer.record { this@drawWithContent.drawContent() }
                        drawLayer(receiptGraphicsLayer)
                    },
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = "STOKVAPE",
                        style = MaterialTheme.typography.titleMedium.copy(fontFamily = FontFamily.Monospace),
                        color = Ink,
                    )
                    Text(
                        text = "Jl. Contoh No. 1, Kota",
                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                        color = TextMuted,
                    )
                    Text(
                        text = "Telp: 081234567890",
                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                        color = TextMuted,
                    )
                    Text(
                        text = "--------------------------------",
                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                        color = TextPlaceholder,
                    )
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(
                            text = "No: ${receipt.trxId}",
                            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                            color = TextSecondary,
                        )
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(
                            text = receipt.time,
                            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                            color = TextSecondary,
                        )
                        Text(
                            text = receipt.cashierName,
                            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                            color = TextSecondary,
                        )
                    }
                    Text(
                        text = "--------------------------------",
                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                        color = TextPlaceholder,
                    )
                    receipt.items.forEach { item ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(
                                text = "${item.quantity}x ${item.product.productName.take(14)}",
                                style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                                color = TextSecondary,
                                modifier = Modifier.weight(1f),
                                overflow = TextOverflow.Ellipsis,
                                maxLines = 1,
                            )
                            Text(
                                text = RupiahFormatter.format(item.product.sellingPrice * item.quantity),
                                style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                                color = Ink,
                            )
                        }
                    }
                    Text(
                        text = "--------------------------------",
                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                        color = TextPlaceholder,
                    )
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(
                            text = "TOTAL",
                            style = MaterialTheme.typography.titleSmall.copy(fontFamily = FontFamily.Monospace),
                            color = Ink,
                        )
                        Text(
                            text = RupiahFormatter.format(receipt.total),
                            style = MaterialTheme.typography.titleSmall.copy(fontFamily = FontFamily.Monospace),
                            color = Ink,
                        )
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(
                            text = "Tunai",
                            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                            color = TextSecondary,
                        )
                        Text(
                            text = RupiahFormatter.format(receipt.cash),
                            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                            color = TextSecondary,
                        )
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(
                            text = "Kembalian",
                            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                            color = TextSecondary,
                        )
                        Text(
                            text = RupiahFormatter.format(receipt.change),
                            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                            color = TextSecondary,
                        )
                    }
                    Text(
                        text = "--------------------------------",
                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                        color = TextPlaceholder,
                    )
                    Text(
                        text = "Terima kasih atas kunjungan Anda!",
                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                        color = TextMuted,
                        textAlign = TextAlign.Center,
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Surface(
                onClick = {
                    scope.launch {
                        val bitmap = receiptGraphicsLayer.toImageBitmap().asAndroidBitmap()
                        ReceiptPdfExporter.export(context, receipt.trxId, bitmap).fold(
                            onSuccess = { uri ->
                                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "application/pdf"
                                    putExtra(Intent.EXTRA_STREAM, uri)
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(Intent.createChooser(sendIntent, "Simpan / bagikan struk"))
                            },
                            onFailure = {
                                Toast.makeText(context, "Gagal menyimpan struk PDF", Toast.LENGTH_SHORT).show()
                            },
                        )
                    }
                },
                color = Ink,
                shape = RoundedCornerShape(13.dp),
                modifier = Modifier.fillMaxWidth().height(50.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("⎙ Simpan Struk (PDF)", style = MaterialTheme.typography.labelLarge, color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                onClick = viewModel::startNewTransaction,
                color = GreenPrimary,
                shape = RoundedCornerShape(13.dp),
                modifier = Modifier.fillMaxWidth().height(50.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("Transaksi Baru", style = MaterialTheme.typography.labelLarge, color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                onClick = { navController.navigate(Routes.DASHBOARD) { popUpTo(Routes.KASIR) { inclusive = true } } },
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(13.dp),
                modifier = Modifier.fillMaxWidth().height(50.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("Kembali ke Beranda", style = MaterialTheme.typography.labelLarge, color = TextSecondary)
                }
            }
        }
    }
}
