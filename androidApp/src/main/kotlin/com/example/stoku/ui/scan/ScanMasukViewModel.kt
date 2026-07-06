package com.example.stoku.ui.scan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stoku.domain.model.Product
import com.example.stoku.domain.model.TransactionSource
import com.example.stoku.domain.model.User
import com.example.stoku.domain.usecase.auth.GetLoggedInUserUseCase
import com.example.stoku.domain.usecase.product.AddNewProductUseCase
import com.example.stoku.domain.usecase.product.GetProductBySkuUseCase
import com.example.stoku.domain.usecase.product.RestockProductUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface ScanMasukMode {
    data object Scanning : ScanMasukMode
    data object Loading : ScanMasukMode
    data class NewProduct(val sku: String) : ScanMasukMode
    data class Restock(val product: Product) : ScanMasukMode
}

data class ScanMasukUiState(
    val mode: ScanMasukMode = ScanMasukMode.Scanning,
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
)

@HiltViewModel
class ScanMasukViewModel @Inject constructor(
    private val getLoggedInUserUseCase: GetLoggedInUserUseCase,
    private val getProductBySkuUseCase: GetProductBySkuUseCase,
    private val addNewProductUseCase: AddNewProductUseCase,
    private val restockProductUseCase: RestockProductUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScanMasukUiState())
    val uiState: StateFlow<ScanMasukUiState> = _uiState.asStateFlow()

    private var cachedUser: User? = null

    fun onBarcodeScanned(sku: String) {
        if (_uiState.value.mode !is ScanMasukMode.Scanning) return
        _uiState.update { it.copy(mode = ScanMasukMode.Loading, errorMessage = null, successMessage = null) }
        viewModelScope.launch {
            val user = currentUser()
            val product = getProductBySkuUseCase(sku, user.role)
            _uiState.update {
                it.copy(mode = if (product == null) ScanMasukMode.NewProduct(sku) else ScanMasukMode.Restock(product))
            }
        }
    }

    fun submitNewProduct(
        sku: String,
        brandName: String,
        productName: String,
        category: String,
        costPrice: Long,
        sellingPrice: Long,
        quantity: Int,
        notes: String?,
    ) {
        _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }
        viewModelScope.launch {
            val user = currentUser()
            addNewProductUseCase(
                role = user.role,
                sku = sku,
                brandName = brandName,
                productName = productName,
                category = category,
                costPrice = costPrice,
                sellingPrice = sellingPrice,
                initialQuantity = quantity,
                source = TransactionSource.SCAN,
                userId = user.id,
                notes = notes,
            ).fold(
                onSuccess = {
                    _uiState.update {
                        ScanMasukUiState(successMessage = "Produk baru ditambahkan, stok $quantity")
                    }
                },
                onFailure = { error ->
                    _uiState.update { it.copy(isSubmitting = false, errorMessage = error.message ?: "Gagal menyimpan produk") }
                },
            )
        }
    }

    fun submitRestock(sku: String, costPrice: Long, sellingPrice: Long, quantity: Int, notes: String?) {
        _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }
        viewModelScope.launch {
            val user = currentUser()
            restockProductUseCase(
                role = user.role,
                sku = sku,
                newCostPrice = costPrice,
                newSellingPrice = sellingPrice,
                quantity = quantity,
                source = TransactionSource.SCAN,
                userId = user.id,
                notes = notes,
            ).fold(
                onSuccess = {
                    _uiState.update {
                        ScanMasukUiState(successMessage = "Stok berhasil ditambahkan: $quantity")
                    }
                },
                onFailure = { error ->
                    _uiState.update { it.copy(isSubmitting = false, errorMessage = error.message ?: "Gagal menyimpan restock") }
                },
            )
        }
    }

    fun scanAgain() {
        _uiState.update { ScanMasukUiState() }
    }

    private suspend fun currentUser(): User {
        cachedUser?.let { return it }
        val user = requireNotNull(getLoggedInUserUseCase().first()) { "No logged-in user" }
        cachedUser = user
        return user
    }
}
