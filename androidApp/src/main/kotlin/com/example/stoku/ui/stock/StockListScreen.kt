package com.example.stoku.ui.stock

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.stoku.domain.model.Product
import com.example.stoku.domain.model.canViewCostPrice
import com.example.stoku.ui.navigation.Routes
import com.example.stoku.ui.theme.AmberWarning
import com.example.stoku.ui.theme.BorderInput
import com.example.stoku.ui.theme.BorderSubtle
import com.example.stoku.ui.theme.GreenDark
import com.example.stoku.ui.theme.GreenPrimary
import com.example.stoku.ui.theme.Ink
import com.example.stoku.ui.theme.TextMuted
import com.example.stoku.ui.theme.TextPlaceholder
import com.example.stoku.ui.theme.TextSecondary
import com.example.stoku.util.RupiahFormatter

@Composable
fun StockListScreen(
    navController: NavHostController,
    viewModel: StockListViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val canSeeCost = uiState.role?.canViewCostPrice ?: false

    Surface(color = MaterialTheme.colorScheme.background) {
        Column(modifier = Modifier.fillMaxSize().padding(horizontal = 18.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 14.dp, bottom = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text = "Daftar Stok", style = MaterialTheme.typography.headlineLarge, color = Ink)
            }

            // Search
            OutlinedTextField(
                value = uiState.query,
                onValueChange = viewModel::onQueryChange,
                placeholder = { Text("Cari produk atau SKU", color = TextPlaceholder) },
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

            // Category chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(vertical = 11.dp),
                horizontalArrangement = Arrangement.spacedBy(7.dp),
            ) {
                CategoryChip(
                    label = "Semua",
                    selected = uiState.categoryFilter == null,
                    onClick = { viewModel.onCategoryFilterChange(null) },
                )
                uiState.categories.forEach { cat ->
                    CategoryChip(
                        label = cat,
                        selected = uiState.categoryFilter == cat,
                        onClick = { viewModel.onCategoryFilterChange(cat) },
                    )
                }
            }

            // Count + sort
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 11.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "${uiState.products.size} produk",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextMuted,
                )
                Surface(
                    onClick = { viewModel.onSortOptionChange(uiState.sortOption.next()) },
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(9.dp)
                ) {
                    Text(
                        text = "⇅ ${uiState.sortOption.label}",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextSecondary,
                        modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp),
                    )
                }
            }

            LazyColumn(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                items(uiState.products, key = { it.sku }) { product ->
                    StockCard(
                        product = product,
                        showCostPrice = canSeeCost,
                        onClick = { navController.navigate(Routes.stockDetail(product.sku)) },
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        color = if (selected) Ink else MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(20.dp),
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
private fun StockCard(product: Product, showCostPrice: Boolean, onClick: () -> Unit) {
    val isLowStock = product.stock < product.lowStockThreshold
    Surface(
        onClick = onClick,
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(15.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, BorderSubtle, RoundedCornerShape(15.dp)),
    ) {
        Row(
            modifier = Modifier.padding(13.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.size(44.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = product.category.take(4).uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        color = TextSecondary,
                    )
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = product.productName,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Ink,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "${product.brandName} · ${product.sku}",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextPlaceholder,
                    modifier = Modifier.padding(top = 2.dp),
                )
                Text(
                    text = RupiahFormatter.format(product.sellingPrice),
                    style = MaterialTheme.typography.labelMedium,
                    color = GreenDark,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = product.stock.toString(),
                    style = MaterialTheme.typography.titleLarge,
                    color = if (isLowStock) AmberWarning else Ink,
                )
                Text(
                    text = if (isLowStock) "menipis" else "stok",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isLowStock) AmberWarning else TextPlaceholder,
                )
            }
        }
    }
}

private fun StockSortOption.next(): StockSortOption {
    val values = StockSortOption.entries
    val nextIndex = (values.indexOf(this) + 1) % values.size
    return values[nextIndex]
}
