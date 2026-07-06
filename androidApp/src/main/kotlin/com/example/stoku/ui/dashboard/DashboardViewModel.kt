package com.example.stoku.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stoku.domain.model.Product
import com.example.stoku.domain.model.Transaction
import com.example.stoku.domain.model.TransactionType
import com.example.stoku.domain.model.UserRole
import com.example.stoku.domain.usecase.auth.GetLoggedInUserUseCase
import com.example.stoku.domain.usecase.auth.LoginUseCase
import com.example.stoku.domain.usecase.auth.LogoutUseCase
import com.example.stoku.domain.usecase.product.GetLowStockProductsUseCase
import com.example.stoku.domain.usecase.product.GetProductsUseCase
import com.example.stoku.domain.usecase.transaction.GetTransactionHistoryUseCase
import com.example.stoku.domain.usecase.transaction.GetTodaysTransactionCountUseCase
import com.example.stoku.ui.navigation.RouteAccess
import com.example.stoku.ui.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RecentTxnRow(
    val transactionId: Long,
    val productName: String,
    val sku: String,
    val dateLabel: String,
    val isIn: Boolean,
    val qtyLabel: String,
)

data class DashboardUiState(
    val role: UserRole? = null,
    val userName: String = "",
    val userInitials: String = "",
    val totalSku: Int = 0,
    val totalStock: Int = 0,
    val lowStockCount: Int = 0,
    val todaysTransactionCount: Int = 0,
    val showScanMasuk: Boolean = false,
    val showInputManual: Boolean = false,
    val recentTransactions: List<RecentTxnRow> = emptyList(),
    val showProfileSheet: Boolean = false,
) {
    val hasLowStockAlert: Boolean get() = lowStockCount > 0
}

private val txnDateFormat = SimpleDateFormat("dd/MM · HH:mm", Locale.getDefault())

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class DashboardViewModel @Inject constructor(
    getLoggedInUserUseCase: GetLoggedInUserUseCase,
    getProductsUseCase: GetProductsUseCase,
    getLowStockProductsUseCase: GetLowStockProductsUseCase,
    getTodaysTransactionCountUseCase: GetTodaysTransactionCountUseCase,
    getTransactionHistoryUseCase: GetTransactionHistoryUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val loginUseCase: LoginUseCase,
) : ViewModel() {

    private val _showProfileSheet = MutableStateFlow(false)

    val uiState: StateFlow<DashboardUiState> = getLoggedInUserUseCase()
        .flatMapLatest { user ->
            if (user == null) {
                flowOf(DashboardUiState())
            } else {
                combine(
                    statsFlow(
                        user.role,
                        user.username,
                        getProductsUseCase,
                        getLowStockProductsUseCase,
                        getTodaysTransactionCountUseCase,
                        getTransactionHistoryUseCase,
                    ),
                    _showProfileSheet,
                ) { stats, showSheet -> stats.copy(showProfileSheet = showSheet) }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DashboardUiState())

    fun openProfileSheet() { _showProfileSheet.update { true } }
    fun dismissProfileSheet() { _showProfileSheet.update { false } }

    fun logout() {
        viewModelScope.launch {
            _showProfileSheet.update { false }
            logoutUseCase()
        }
    }

    fun switchRole(targetRole: UserRole) {
        if (targetRole == uiState.value.role) {
            _showProfileSheet.update { false }
            return
        }
        // Do NOT call logoutUseCase first — it sets session to LoggedOut which triggers
        // NavGraph to navigate to login, destroying this ViewModel and cancelling the
        // viewModelScope before loginUseCase can run. Calling loginUseCase directly
        // overwrites the stored session in place so the session stays LoggedIn throughout.
        viewModelScope.launch {
            _showProfileSheet.update { false }
            val (username, password) = when (targetRole) {
                UserRole.OWNER -> "owner" to "owner123"
                UserRole.ADMIN -> "admin" to "admin123"
                UserRole.KASIR -> "kasir" to "kasir123"
            }
            loginUseCase(username, password)
        }
    }

    private fun statsFlow(
        role: UserRole,
        username: String,
        getProductsUseCase: GetProductsUseCase,
        getLowStockProductsUseCase: GetLowStockProductsUseCase,
        getTodaysTransactionCountUseCase: GetTodaysTransactionCountUseCase,
        getTransactionHistoryUseCase: GetTransactionHistoryUseCase,
    ): Flow<DashboardUiState> = combine(
        getProductsUseCase(role),
        getLowStockProductsUseCase(role),
        getTodaysTransactionCountUseCase(),
        getTransactionHistoryUseCase(role),
    ) { products, lowStockProducts, todaysCount, transactions ->
        DashboardUiState(
            role = role,
            userName = username.replaceFirstChar { it.uppercase() },
            userInitials = username.take(2).uppercase(),
            totalSku = products.size,
            totalStock = products.sumOf { it.stock },
            lowStockCount = lowStockProducts.size,
            todaysTransactionCount = todaysCount,
            showScanMasuk = RouteAccess.isAllowed(Routes.SCAN_IN, role),
            showInputManual = RouteAccess.isAllowed(Routes.MANUAL_INPUT, role),
            recentTransactions = buildRecentRows(transactions.take(5), products),
        )
    }

    private fun buildRecentRows(transactions: List<Transaction>, products: List<Product>): List<RecentTxnRow> {
        val productMap = products.associateBy { it.sku }
        return transactions.map { txn ->
            val isIn = txn.type == TransactionType.IN
            RecentTxnRow(
                transactionId = txn.id,
                productName = productMap[txn.sku]?.productName ?: txn.sku,
                sku = txn.sku,
                dateLabel = txnDateFormat.format(Date(txn.createdAt)),
                isIn = isIn,
                qtyLabel = if (isIn) "+${txn.quantity}" else "-${txn.quantity}",
            )
        }
    }
}
