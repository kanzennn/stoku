package com.example.stoku.ui.manual

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stoku.domain.model.Product
import com.example.stoku.domain.model.TransactionSource
import com.example.stoku.domain.model.TransactionType
import com.example.stoku.domain.model.User
import com.example.stoku.domain.model.UserRole
import com.example.stoku.domain.model.canViewCostPrice
import com.example.stoku.domain.usecase.auth.GetLoggedInUserUseCase
import com.example.stoku.domain.usecase.product.SearchProductsUseCase
import com.example.stoku.domain.usecase.transaction.RecordTransactionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface ManualKeluarMode {
    data object Search : ManualKeluarMode
    data class Found(val product: Product) : ManualKeluarMode
}

data class ManualKeluarUiState(
    val mode: ManualKeluarMode = ManualKeluarMode.Search,
    val role: UserRole? = null,
    val searchResults: List<Product> = emptyList(),
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val notFoundAlert: Boolean = false,
    val outOfStockAlert: Boolean = false,
)

@HiltViewModel
class ManualKeluarViewModel @Inject constructor(
    private val getLoggedInUserUseCase: GetLoggedInUserUseCase,
    private val searchProductsUseCase: SearchProductsUseCase,
    private val recordTransactionUseCase: RecordTransactionUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ManualKeluarUiState())
    val uiState: StateFlow<ManualKeluarUiState> = _uiState.asStateFlow()

    private var cachedUser: User? = null

    fun search(query: String) {
        if (query.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Masukkan SKU atau nama produk") }
            return
        }
        viewModelScope.launch {
            val user = currentUser()
            val results = searchProductsUseCase(query, user.role).first()
            _uiState.update {
                it.copy(searchResults = results, role = user.role, errorMessage = null, notFoundAlert = results.isEmpty())
            }
        }
    }

    fun selectProduct(product: Product) {
        _uiState.update {
            if (product.stock <= 0) {
                it.copy(outOfStockAlert = true)
            } else {
                it.copy(mode = ManualKeluarMode.Found(product))
            }
        }
    }

    fun submitOut(quantity: Int, notes: String?) {
        val product = (_uiState.value.mode as? ManualKeluarMode.Found)?.product ?: return
        _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }
        viewModelScope.launch {
            val user = currentUser()
            recordTransactionUseCase(
                role = user.role,
                sku = product.sku,
                type = TransactionType.OUT,
                source = TransactionSource.MANUAL,
                quantity = quantity,
                notes = notes,
                userId = user.id,
            ).fold(
                onSuccess = {
                    val remainingStock = product.stock - quantity
                    _uiState.update {
                        ManualKeluarUiState(role = user.role, successMessage = "Stok keluar $quantity, sisa stok $remainingStock")
                    }
                },
                onFailure = { error ->
                    _uiState.update { it.copy(isSubmitting = false, errorMessage = error.message ?: "Gagal menyimpan transaksi") }
                },
            )
        }
    }

    fun dismissAlert() {
        _uiState.update { it.copy(notFoundAlert = false, outOfStockAlert = false) }
    }

    fun inputLagi() {
        _uiState.update { ManualKeluarUiState() }
    }

    fun canViewCostPrice(): Boolean = _uiState.value.role?.canViewCostPrice ?: false

    private suspend fun currentUser(): User {
        cachedUser?.let { return it }
        val user = requireNotNull(getLoggedInUserUseCase().first()) { "No logged-in user" }
        cachedUser = user
        return user
    }
}
