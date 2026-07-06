package com.example.stoku.ui.history

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stoku.domain.model.Transaction
import com.example.stoku.domain.model.UserRole
import com.example.stoku.domain.usecase.auth.GetLoggedInUserUseCase
import com.example.stoku.domain.usecase.product.GetProductBySkuUseCase
import com.example.stoku.domain.usecase.transaction.GetTransactionByIdUseCase
import com.example.stoku.domain.usecase.user.GetUsersUseCase
import com.example.stoku.ui.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HistoryDetailUiState(
    val role: UserRole? = null,
    val transaction: Transaction? = null,
    val productName: String? = null,
    val username: String? = null,
)

@HiltViewModel
class HistoryDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    getLoggedInUserUseCase: GetLoggedInUserUseCase,
    private val getTransactionByIdUseCase: GetTransactionByIdUseCase,
    private val getProductBySkuUseCase: GetProductBySkuUseCase,
    private val getUsersUseCase: GetUsersUseCase,
) : ViewModel() {

    private val transactionId: Long = checkNotNull(savedStateHandle[Routes.HISTORY_DETAIL_ARG_ID])

    private val _uiState = MutableStateFlow(HistoryDetailUiState())
    val uiState: StateFlow<HistoryDetailUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val role = getLoggedInUserUseCase().first()?.role
            val transaction = role?.let { getTransactionByIdUseCase(transactionId, it) }
            val productName = transaction?.let { getProductBySkuUseCase(it.sku, role)?.productName }
            val username = transaction?.let { t -> getUsersUseCase().first().firstOrNull { it.id == t.userId }?.username }
            _uiState.update {
                it.copy(role = role, transaction = transaction, productName = productName, username = username)
            }
        }
    }
}
