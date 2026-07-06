package com.example.stoku.ui.stock

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stoku.domain.model.Product
import com.example.stoku.domain.model.UserRole
import com.example.stoku.domain.usecase.auth.GetLoggedInUserUseCase
import com.example.stoku.domain.usecase.product.GetProductsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
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

enum class StockSortOption(val label: String) {
    NAME("Nama"),
    STOCK("Stok"),
    LAST_UPDATED("Terakhir Diperbarui"),
}

data class StockListUiState(
    val role: UserRole? = null,
    val query: String = "",
    val categoryFilter: String? = null,
    val sortOption: StockSortOption = StockSortOption.NAME,
    val categories: List<String> = emptyList(),
    val products: List<Product> = emptyList(),
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class StockListViewModel @Inject constructor(
    getLoggedInUserUseCase: GetLoggedInUserUseCase,
    getProductsUseCase: GetProductsUseCase,
) : ViewModel() {

    private val _query = MutableStateFlow("")
    private val _categoryFilter = MutableStateFlow<String?>(null)
    private val _sortOption = MutableStateFlow(StockSortOption.NAME)

    private val roleFlow = getLoggedInUserUseCase().map { it?.role }
    private val productsFlow = roleFlow.flatMapLatest { role ->
        role?.let { getProductsUseCase(it) } ?: flowOf(emptyList())
    }

    val uiState: StateFlow<StockListUiState> = combine(
        roleFlow,
        productsFlow,
        _query,
        _categoryFilter,
        _sortOption,
    ) { role, products, query, categoryFilter, sortOption ->
        val filtered = products
            .filter { categoryFilter == null || it.category == categoryFilter }
            .filter {
                query.isBlank() ||
                    it.productName.contains(query, ignoreCase = true) ||
                    it.sku.contains(query, ignoreCase = true)
            }
        val sorted = when (sortOption) {
            StockSortOption.NAME -> filtered.sortedBy { it.productName.lowercase() }
            StockSortOption.STOCK -> filtered.sortedBy { it.stock }
            StockSortOption.LAST_UPDATED -> filtered.sortedByDescending { it.updatedAt }
        }
        StockListUiState(
            role = role,
            query = query,
            categoryFilter = categoryFilter,
            sortOption = sortOption,
            categories = products.map { it.category }.distinct().sorted(),
            products = sorted,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), StockListUiState())

    fun onQueryChange(query: String) {
        _query.value = query
    }

    fun onCategoryFilterChange(category: String?) {
        _categoryFilter.value = category
    }

    fun onSortOptionChange(option: StockSortOption) {
        _sortOption.value = option
    }
}
