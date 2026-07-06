package com.example.stoku.ui.manual

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

sealed interface ManualMasukMode {
    data object EnterSku : ManualMasukMode
    data object Loading : ManualMasukMode
    data class NewProduct(val sku: String) : ManualMasukMode
    data class Restock(val product: Product) : ManualMasukMode
}

data class ManualMasukUiState(
    val mode: ManualMasukMode = ManualMasukMode.EnterSku,
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
)

@HiltViewModel
class ManualMasukViewModel @Inject constructor(
    private val getLoggedInUserUseCase: GetLoggedInUserUseCase,
    private val getProductBySkuUseCase: GetProductBySkuUseCase,
    private val addNewProductUseCase: AddNewProductUseCase,
    private val restockProductUseCase: RestockProductUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ManualMasukUiState())
    val uiState: StateFlow<ManualMasukUiState> = _uiState.asStateFlow()

    private var cachedUser: User? = null

    fun checkProduct(sku: String) {
        if (sku.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Masukkan SKU terlebih dahulu") }
            return
        }
        _uiState.update { it.copy(mode = ManualMasukMode.Loading, errorMessage = null) }
        viewModelScope.launch {
            val user = currentUser()
            val product = getProductBySkuUseCase(sku, user.role)
            _uiState.update {
                it.copy(mode = if (product == null) ManualMasukMode.NewProduct(sku) else ManualMasukMode.Restock(product))
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
                source = TransactionSource.MANUAL,
                userId = user.id,
                notes = notes,
            ).fold(
                onSuccess = {
                    _uiState.update {
                        ManualMasukUiState(successMessage = "Produk baru ditambahkan, stok $quantity")
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
                source = TransactionSource.MANUAL,
                userId = user.id,
                notes = notes,
            ).fold(
                onSuccess = {
                    _uiState.update {
                        ManualMasukUiState(successMessage = "Stok berhasil ditambahkan: $quantity")
                    }
                },
                onFailure = { error ->
                    _uiState.update { it.copy(isSubmitting = false, errorMessage = error.message ?: "Gagal menyimpan restock") }
                },
            )
        }
    }

    fun inputLagi() {
        _uiState.update { ManualMasukUiState() }
    }

    private suspend fun currentUser(): User {
        cachedUser?.let { return it }
        val user = requireNotNull(getLoggedInUserUseCase().first()) { "No logged-in user" }
        cachedUser = user
        return user
    }
}
