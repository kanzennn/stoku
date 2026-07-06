package com.example.stoku.ui.stock

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stoku.domain.model.Product
import com.example.stoku.domain.model.Transaction
import com.example.stoku.domain.model.User
import com.example.stoku.domain.model.UserRole
import com.example.stoku.domain.usecase.auth.GetLoggedInUserUseCase
import com.example.stoku.domain.usecase.product.GetProductBySkuUseCase
import com.example.stoku.domain.usecase.product.UpdateProductDetailsUseCase
import com.example.stoku.domain.usecase.transaction.GetTransactionsBySkuUseCase
import com.example.stoku.ui.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class StockDetailUiState(
    val role: UserRole? = null,
    val product: Product? = null,
    val transactions: List<Transaction> = emptyList(),
    val isEditing: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
)

@HiltViewModel
class StockDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getLoggedInUserUseCase: GetLoggedInUserUseCase,
    private val getProductBySkuUseCase: GetProductBySkuUseCase,
    private val getTransactionsBySkuUseCase: GetTransactionsBySkuUseCase,
    private val updateProductDetailsUseCase: UpdateProductDetailsUseCase,
) : ViewModel() {

    private val sku: String = checkNotNull(savedStateHandle[Routes.STOCK_DETAIL_ARG_SKU])

    private val _uiState = MutableStateFlow(StockDetailUiState())
    val uiState: StateFlow<StockDetailUiState> = _uiState.asStateFlow()

    private var cachedUser: User? = null

    init {
        viewModelScope.launch {
            val user = currentUser()
            _uiState.update { it.copy(role = user.role) }
            refreshProduct()
            getTransactionsBySkuUseCase(sku, user.role).collect { transactions ->
                _uiState.update { it.copy(transactions = transactions) }
            }
        }
    }

    fun startEdit() {
        _uiState.update { it.copy(isEditing = true, errorMessage = null) }
    }

    fun cancelEdit() {
        _uiState.update { it.copy(isEditing = false, errorMessage = null) }
    }

    fun saveEdit(
        brandName: String,
        productName: String,
        category: String,
        costPrice: Long,
        sellingPrice: Long,
        lowStockThreshold: Int,
    ) {
        _uiState.update { it.copy(isSaving = true, errorMessage = null) }
        viewModelScope.launch {
            val user = currentUser()
            updateProductDetailsUseCase(
                role = user.role,
                sku = sku,
                brandName = brandName,
                productName = productName,
                category = category,
                costPrice = costPrice,
                sellingPrice = sellingPrice,
                lowStockThreshold = lowStockThreshold,
                userId = user.id,
            ).fold(
                onSuccess = {
                    refreshProduct()
                    _uiState.update {
                        it.copy(isSaving = false, isEditing = false, successMessage = "Produk berhasil diperbarui")
                    }
                },
                onFailure = { error ->
                    _uiState.update { it.copy(isSaving = false, errorMessage = error.message ?: "Gagal menyimpan") }
                },
            )
        }
    }

    private suspend fun refreshProduct() {
        val role = currentUser().role
        val product = getProductBySkuUseCase(sku, role)
        _uiState.update { it.copy(product = product) }
    }

    private suspend fun currentUser(): User {
        cachedUser?.let { return it }
        val user = requireNotNull(getLoggedInUserUseCase().first()) { "No logged-in user" }
        cachedUser = user
        return user
    }
}
