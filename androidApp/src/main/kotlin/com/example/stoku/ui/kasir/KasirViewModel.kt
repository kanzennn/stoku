package com.example.stoku.ui.kasir

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stoku.domain.model.Product
import com.example.stoku.domain.model.TransactionSource
import com.example.stoku.domain.model.TransactionType
import com.example.stoku.domain.model.UserRole
import com.example.stoku.domain.usecase.auth.GetLoggedInUserUseCase
import com.example.stoku.domain.usecase.product.GetProductsUseCase
import com.example.stoku.domain.usecase.transaction.RecordTransactionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class KasirStage { CATALOG, PAYMENT, RECEIPT, SCAN }

data class CartItem(
    val product: Product,
    val quantity: Int,
)

data class ReceiptData(
    val trxId: String,
    val cashierName: String,
    val time: String,
    val items: List<CartItem>,
    val total: Long,
    val cash: Long,
    val change: Long,
)

data class KasirUiState(
    val stage: KasirStage = KasirStage.CATALOG,
    val role: UserRole? = null,
    val userId: Long = 0,
    val userName: String = "",
    val allProducts: List<Product> = emptyList(),
    val searchQuery: String = "",
    val cart: Map<String, CartItem> = emptyMap(),
    val cashInput: String = "",
    val isProcessing: Boolean = false,
    val errorMessage: String? = null,
    val receipt: ReceiptData? = null,
    val scanMessage: String? = null,
) {
    val filteredProducts: List<Product>
        get() = if (searchQuery.isBlank()) allProducts
        else allProducts.filter {
            it.productName.contains(searchQuery, ignoreCase = true) ||
                it.sku.contains(searchQuery, ignoreCase = true) ||
                it.brandName.contains(searchQuery, ignoreCase = true)
        }

    val cartTotal: Long get() = cart.values.sumOf { it.product.sellingPrice * it.quantity }
    val cartItemCount: Int get() = cart.values.sumOf { it.quantity }

    val cashLong: Long get() = cashInput.toLongOrNull() ?: 0L
    val change: Long get() = cashLong - cartTotal
    val canConfirmPayment: Boolean get() = cashLong >= cartTotal && cart.isNotEmpty()
}

private val receiptTimeFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class KasirViewModel @Inject constructor(
    getLoggedInUserUseCase: GetLoggedInUserUseCase,
    getProductsUseCase: GetProductsUseCase,
    private val recordTransactionUseCase: RecordTransactionUseCase,
) : ViewModel() {

    private val _localState = MutableStateFlow(KasirUiState())

    private val userFlow = getLoggedInUserUseCase()

    val uiState: StateFlow<KasirUiState> = userFlow
        .flatMapLatest { user ->
            if (user == null) {
                flowOf(KasirUiState())
            } else {
                combine(getProductsUseCase(user.role), _localState) { products, local ->
                    local.copy(
                        role = user.role,
                        userId = user.id,
                        userName = user.username.replaceFirstChar { it.uppercase() },
                        allProducts = products,
                    )
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), KasirUiState())

    fun onSearchChange(query: String) {
        _localState.update { it.copy(searchQuery = query) }
    }

    fun addToCart(product: Product) {
        _localState.update { state ->
            val existing = state.cart[product.sku]
            val currentQty = existing?.quantity ?: 0
            if (currentQty >= product.stock) return@update state
            val newItem = CartItem(product, currentQty + 1)
            state.copy(cart = state.cart + (product.sku to newItem))
        }
    }

    fun decrementCart(sku: String) {
        _localState.update { state ->
            val existing = state.cart[sku] ?: return@update state
            val newQty = existing.quantity - 1
            if (newQty <= 0) {
                state.copy(cart = state.cart - sku)
            } else {
                state.copy(cart = state.cart + (sku to existing.copy(quantity = newQty)))
            }
        }
    }

    fun removeFromCart(sku: String) {
        _localState.update { it.copy(cart = it.cart - sku) }
    }

    fun startScan() {
        _localState.update { it.copy(stage = KasirStage.SCAN) }
    }

    fun cancelScan() {
        _localState.update { it.copy(stage = KasirStage.CATALOG) }
    }

    fun onBarcodeScanned(sku: String) {
        // Guard against the analyzer firing repeatedly for the same frame/barcode
        // before we've had a chance to navigate away from the camera stage.
        if (_localState.value.stage != KasirStage.SCAN) return
        val product = uiState.value.allProducts.find { it.sku == sku }
        val message = when {
            product == null -> "Produk tidak ditemukan"
            product.stock <= 0 -> "Stok habis"
            else -> {
                addToCart(product)
                "${product.productName} ditambahkan"
            }
        }
        _localState.update { it.copy(stage = KasirStage.CATALOG, scanMessage = message) }
    }

    fun dismissScanMessage() {
        _localState.update { it.copy(scanMessage = null) }
    }

    fun onCashInput(value: String) {
        _localState.update { it.copy(cashInput = value.filter(Char::isDigit)) }
    }

    fun setCashQuick(amount: Long) {
        _localState.update { it.copy(cashInput = amount.toString()) }
    }

    fun goToPayment() {
        _localState.update { it.copy(stage = KasirStage.PAYMENT, errorMessage = null) }
    }

    fun backToCatalog() {
        _localState.update { it.copy(stage = KasirStage.CATALOG, errorMessage = null) }
    }

    fun confirmPayment() {
        // Must read from uiState (not _localState) — role/userId/userName are only
        // populated on the merged flow (combined with the logged-in user), never on
        // _localState directly. Reading _localState.value here always saw role == null
        // and silently no-op'd the button.
        val state = uiState.value
        val role = state.role ?: return
        if (!state.canConfirmPayment) return

        _localState.update { it.copy(isProcessing = true, errorMessage = null) }

        viewModelScope.launch {
            val cartItems = state.cart.values.toList()
            val errors = mutableListOf<String>()

            for (item in cartItems) {
                recordTransactionUseCase(
                    role = role,
                    sku = item.product.sku,
                    type = TransactionType.OUT,
                    source = TransactionSource.MANUAL,
                    quantity = item.quantity,
                    notes = "Kasir POS",
                    userId = state.userId,
                ).onFailure { errors.add("${item.product.productName}: ${it.message}") }
            }

            if (errors.isNotEmpty()) {
                _localState.update {
                    it.copy(isProcessing = false, errorMessage = errors.joinToString("\n"))
                }
                return@launch
            }

            val trxId = "TRX-${(100000..999999).random()}"
            val receipt = ReceiptData(
                trxId = trxId,
                cashierName = state.userName,
                time = receiptTimeFormat.format(Date()),
                items = cartItems,
                total = state.cartTotal,
                cash = state.cashLong,
                change = state.change,
            )
            _localState.update {
                it.copy(
                    isProcessing = false,
                    stage = KasirStage.RECEIPT,
                    receipt = receipt,
                )
            }
        }
    }

    fun startNewTransaction() {
        _localState.update {
            KasirUiState(
                stage = KasirStage.CATALOG,
                searchQuery = "",
                cart = emptyMap(),
                cashInput = "",
            )
        }
    }
}
