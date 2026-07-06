package com.example.stoku.ui.history

import android.content.Context
import android.net.Uri
import com.example.stoku.domain.model.Transaction
import com.example.stoku.domain.model.TransactionSource
import com.example.stoku.domain.model.TransactionType
import com.example.stoku.domain.model.User
import com.example.stoku.domain.model.UserRole
import com.example.stoku.domain.usecase.auth.GetLoggedInUserUseCase
import com.example.stoku.domain.usecase.product.GetProductsUseCase
import com.example.stoku.domain.usecase.transaction.GetTransactionHistoryUseCase
import com.example.stoku.domain.usecase.user.GetUsersUseCase
import com.example.stoku.util.CsvExporter
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
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

data class HistoryRow(
    val transaction: Transaction,
    val productName: String,
    val username: String,
)

data class HistoryFilters(
    val typeFilter: TransactionType? = null,
    val sourceFilter: TransactionSource? = null,
    val userIdFilter: Long? = null,
    val dateFromMillis: Long? = null,
    val dateToMillis: Long? = null,
    val query: String = "",
)

data class HistoryUiState(
    val role: UserRole? = null,
    val filters: HistoryFilters = HistoryFilters(),
    val users: List<User> = emptyList(),
    val rows: List<HistoryRow> = emptyList(),
    val showPriceColumns: Boolean = false,
)

private val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HistoryViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context,
    getLoggedInUserUseCase: GetLoggedInUserUseCase,
    getTransactionHistoryUseCase: GetTransactionHistoryUseCase,
    getProductsUseCase: GetProductsUseCase,
    getUsersUseCase: GetUsersUseCase,
) : ViewModel() {

    private val _filters = MutableStateFlow(HistoryFilters())

    private val roleFlow = getLoggedInUserUseCase().map { it?.role }
    private val transactionsFlow = roleFlow.flatMapLatest { role ->
        role?.let { getTransactionHistoryUseCase(it) } ?: flowOf(emptyList())
    }
    private val productsFlow = roleFlow.flatMapLatest { role ->
        role?.let { getProductsUseCase(it) } ?: flowOf(emptyList())
    }
    private val usersFlow = getUsersUseCase()

    val uiState: StateFlow<HistoryUiState> = combine(
        roleFlow,
        transactionsFlow,
        productsFlow,
        usersFlow,
        _filters,
    ) { role, transactions, products, users, filters ->
        val productNameBySku = products.associate { it.sku to it.productName }
        val usernameById = users.associate { it.id to it.username }
        val filtered = transactions.filter { t ->
            (filters.typeFilter == null || t.type == filters.typeFilter) &&
                (filters.sourceFilter == null || t.source == filters.sourceFilter) &&
                (filters.userIdFilter == null || t.userId == filters.userIdFilter) &&
                (filters.dateFromMillis == null || t.createdAt >= filters.dateFromMillis) &&
                (filters.dateToMillis == null || t.createdAt <= filters.dateToMillis) &&
                (
                    filters.query.isBlank() ||
                        t.sku.contains(filters.query, ignoreCase = true) ||
                        productNameBySku[t.sku]?.contains(filters.query, ignoreCase = true) == true
                    )
        }
        HistoryUiState(
            role = role,
            filters = filters,
            users = users,
            rows = filtered.map { t ->
                HistoryRow(
                    transaction = t,
                    productName = productNameBySku[t.sku] ?: t.sku,
                    username = usernameById[t.userId] ?: "?",
                )
            },
            showPriceColumns = role != null && role != UserRole.KASIR,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HistoryUiState())

    fun onQueryChange(query: String) {
        _filters.update { it.copy(query = query) }
    }

    fun onTypeFilterChange(type: TransactionType?) {
        _filters.update { it.copy(typeFilter = type) }
    }

    fun onSourceFilterChange(source: TransactionSource?) {
        _filters.update { it.copy(sourceFilter = source) }
    }

    fun onUserFilterChange(userId: Long?) {
        _filters.update { it.copy(userIdFilter = userId) }
    }

    fun onDateRangeChange(fromMillis: Long?, toMillis: Long?) {
        _filters.update { it.copy(dateFromMillis = fromMillis, dateToMillis = toMillis) }
    }

    fun exportCsv(): Result<Uri> {
        val state = uiState.value
        val showPrices = state.showPriceColumns
        val header = buildList {
            add("Tanggal")
            add("SKU")
            add("Produk")
            add("Tipe")
            add("Sumber")
            add("Jumlah")
            if (showPrices) {
                add("Harga Modal")
                add("Harga Jual")
            }
            add("Catatan")
            add("User")
        }
        val rows = state.rows.map { row ->
            val t = row.transaction
            buildList {
                add(dateFormat.format(Date(t.createdAt)))
                add(t.sku)
                add(row.productName)
                add(t.type.value)
                add(t.source.value)
                add(t.quantity.toString())
                if (showPrices) {
                    add(t.costPriceSnapshot?.toString() ?: "")
                    add(t.sellingPriceSnapshot.toString())
                }
                add(t.notes ?: "")
                add(row.username)
            }
        }
        val fileName = "stoku_history_${System.currentTimeMillis()}.csv"
        return CsvExporter.export(appContext, fileName, header, rows)
    }
}
