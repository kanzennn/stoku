package com.example.stoku.ui.brand

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.stoku.domain.model.Brand
import com.example.stoku.ui.theme.BorderInput
import com.example.stoku.ui.theme.BorderSubtle
import com.example.stoku.ui.theme.DangerRed
import com.example.stoku.ui.theme.GreenPrimary
import com.example.stoku.ui.theme.Ink
import com.example.stoku.ui.theme.TextMuted
import com.example.stoku.ui.theme.TextSecondary

@Composable
fun BrandListScreen(
    navController: NavController,
    viewModel: BrandListViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "Brand",
                    style = MaterialTheme.typography.headlineLarge,
                    color = Ink,
                )
                FilledTonalButton(
                    onClick = viewModel::showAddDialog,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = GreenPrimary,
                        contentColor = Color.White,
                    ),
                ) {
                    Text("+ Tambah", style = MaterialTheme.typography.labelMedium)
                }
            }

            if (uiState.brands.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text("Belum ada brand", style = MaterialTheme.typography.bodyMedium, color = TextMuted)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 18.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(uiState.brands, key = { it.id }) { brand ->
                        BrandRow(
                            brand = brand,
                            onEdit = { viewModel.showEditDialog(brand) },
                            onDelete = { viewModel.showDeleteDialog(brand) },
                        )
                    }
                }
            }
        }
    }

    when (val dialog = uiState.dialog) {
        is BrandDialog.Add -> AddEditBrandDialog(
            title = "Tambah Brand",
            initialName = "",
            onConfirm = { viewModel.addBrand(it) },
            onDismiss = viewModel::dismissDialog,
        )
        is BrandDialog.Edit -> AddEditBrandDialog(
            title = "Edit Brand",
            initialName = dialog.brand.name,
            onConfirm = { viewModel.updateBrand(dialog.brand, it) },
            onDismiss = viewModel::dismissDialog,
        )
        is BrandDialog.Delete -> DeleteBrandDialog(
            brand = dialog.brand,
            onConfirm = { viewModel.deleteBrand(dialog.brand) },
            onDismiss = viewModel::dismissDialog,
        )
        is BrandDialog.None -> Unit
    }
}

@Composable
private fun BrandRow(brand: Brand, onEdit: () -> Unit, onDelete: () -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, BorderSubtle, RoundedCornerShape(14.dp)),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = brand.name,
                style = MaterialTheme.typography.bodyLarge,
                color = Ink,
                modifier = Modifier.weight(1f),
            )
            TextButton(onClick = onEdit) {
                Text("Edit", color = GreenPrimary, style = MaterialTheme.typography.labelMedium)
            }
            TextButton(onClick = onDelete) {
                Text("Hapus", color = DangerRed, style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

@Composable
private fun AddEditBrandDialog(
    title: String,
    initialName: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var name by remember(initialName) { mutableStateOf(initialName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, style = MaterialTheme.typography.titleLarge, color = Ink) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nama Brand", style = MaterialTheme.typography.labelMedium) },
                singleLine = true,
                shape = RoundedCornerShape(13.dp),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GreenPrimary,
                    unfocusedBorderColor = BorderInput,
                ),
                modifier = Modifier.fillMaxWidth(),
            )
        },
        confirmButton = {
            FilledTonalButton(
                onClick = { onConfirm(name) },
                enabled = name.isNotBlank(),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = GreenPrimary,
                    contentColor = Color.White,
                ),
                shape = RoundedCornerShape(10.dp),
            ) {
                Text("Simpan", style = MaterialTheme.typography.labelMedium)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal", color = TextSecondary)
            }
        },
    )
}

@Composable
private fun DeleteBrandDialog(brand: Brand, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Hapus Brand?", style = MaterialTheme.typography.titleLarge, color = Ink) },
        text = {
            Text(
                text = "Brand \"${brand.name}\" akan dihapus. Tindakan ini tidak dapat dibatalkan.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
            )
        },
        confirmButton = {
            FilledTonalButton(
                onClick = onConfirm,
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = DangerRed,
                    contentColor = Color.White,
                ),
                shape = RoundedCornerShape(10.dp),
            ) {
                Text("Hapus", style = MaterialTheme.typography.labelMedium)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal", color = TextSecondary)
            }
        },
    )
}
