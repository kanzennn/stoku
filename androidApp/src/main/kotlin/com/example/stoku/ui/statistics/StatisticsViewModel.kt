package com.example.stoku.ui.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stoku.domain.model.TransactionSource
import com.example.stoku.domain.model.TransactionType
import com.example.stoku.domain.model.UserRole
import com.example.stoku.domain.usecase.auth.GetLoggedInUserUseCase
import com.example.stoku.domain.usecase.product.GetProductsUseCase
import com.example.stoku.domain.usecase.transaction.GetTransactionHistoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
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

enum class StatsDateRangePreset(val label: String) {
    TODAY("Hari Ini"),
    THIS_WEEK("Minggu Ini"),
    THIS_MONTH("Bulan Ini"),
    CUSTOM("Custom"),
}

data class StatisticsFilters(
    val preset: StatsDateRangePreset = StatsDateRangePreset.THIS_WEEK,
    val customFromMillis: Long? = null,
    val customToMillis: Long? = null,
)

data class DailyQuantity(val dayIndex: Int, val dateLabel: String, val qtyIn: Int, val qtyOut: Int)

data class TopProduct(val sku: String, val productName: String, val totalQtyOut: Int)

data class StatisticsUiState(
    val filters: StatisticsFilters = StatisticsFilters(),
    val dailyQuantities: List<DailyQuantity> = emptyList(),
    val totalQtyIn: Int = 0,
    val totalQtyOut: Int = 0,
    val scanCount: Int = 0,
    val manualCount: Int = 0,
    val topProducts: List<TopProduct> = emptyList(),
)

private val dayLabelFormat = SimpleDateFormat("dd/MM", Locale.getDefault())

private fun startOfDay(millis: Long): Long = Calendar.getInstance().apply {
    timeInMillis = millis
    set(Calendar.HOUR_OF_DAY, 0)
    set(Calendar.MINUTE, 0)
    set(Calendar.SECOND, 0)
    set(Calendar.MILLISECOND, 0)
}.timeInMillis

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class StatisticsViewModel @Inject constructor(
    getLoggedInUserUseCase: GetLoggedInUserUseCase,
    getTransactionHistoryUseCase: GetTransactionHistoryUseCase,
    getProductsUseCase: GetProductsUseCase,
) : ViewModel() {

    private val _filters = MutableStateFlow(StatisticsFilters())

    private val roleFlow = getLoggedInUserUseCase().map { it?.role ?: UserRole.OWNER }
    private val transactionsFlow = roleFlow.flatMapLatest { role -> getTransactionHistoryUseCase(role) }
    private val productsFlow = roleFlow.flatMapLatest { role -> getProductsUseCase(role) }

    val uiState: StateFlow<StatisticsUiState> = combine(
        transactionsFlow,
        productsFlow,
        _filters,
    ) { transactions, products, filters ->
        val (rangeStart, rangeEnd) = effectiveRange(filters)
        val inRange = transactions.filter { it.createdAt in rangeStart..rangeEnd }

        val dayCount = ((startOfDay(rangeEnd) - startOfDay(rangeStart)) / DAY_MILLIS).toInt() + 1
        val dailyQuantities = (0 until dayCount).map { index ->
            val dayStart = startOfDay(rangeStart) + index * DAY_MILLIS
            val dayEnd = dayStart + DAY_MILLIS
            val dayTransactions = inRange.filter { it.createdAt in dayStart until dayEnd }
            DailyQuantity(
                dayIndex = index,
                dateLabel = dayLabelFormat.format(Date(dayStart)),
                qtyIn = dayTransactions.filter { it.type == TransactionType.IN }.sumOf { it.quantity },
                qtyOut = dayTransactions.filter { it.type == TransactionType.OUT }.sumOf { it.quantity },
            )
        }

        val productNameBySku = products.associate { it.sku to it.productName }
        val topProducts = inRange
            .filter { it.type == TransactionType.OUT }
            .groupBy { it.sku }
            .map { (sku, txns) -> TopProduct(sku, productNameBySku[sku] ?: sku, txns.sumOf { it.quantity }) }
            .sortedByDescending { it.totalQtyOut }
            .take(5)

        StatisticsUiState(
            filters = filters,
            dailyQuantities = dailyQuantities,
            totalQtyIn = inRange.filter { it.type == TransactionType.IN }.sumOf { it.quantity },
            totalQtyOut = inRange.filter { it.type == TransactionType.OUT }.sumOf { it.quantity },
            scanCount = inRange.count { it.source == TransactionSource.SCAN },
            manualCount = inRange.count { it.source == TransactionSource.MANUAL },
            topProducts = topProducts,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), StatisticsUiState())

    fun onPresetChange(preset: StatsDateRangePreset) {
        _filters.update { it.copy(preset = preset) }
    }

    fun onCustomRangeChange(fromMillis: Long?, toMillis: Long?) {
        _filters.update { it.copy(preset = StatsDateRangePreset.CUSTOM, customFromMillis = fromMillis, customToMillis = toMillis) }
    }

    private fun effectiveRange(filters: StatisticsFilters): Pair<Long, Long> {
        val now = System.currentTimeMillis()
        return when (filters.preset) {
            StatsDateRangePreset.TODAY -> startOfDay(now) to now
            StatsDateRangePreset.THIS_WEEK -> {
                val start = Calendar.getInstance().apply {
                    timeInMillis = now
                    set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis
                start to now
            }
            StatsDateRangePreset.THIS_MONTH -> {
                val start = Calendar.getInstance().apply {
                    timeInMillis = now
                    set(Calendar.DAY_OF_MONTH, 1)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis
                start to now
            }
            StatsDateRangePreset.CUSTOM -> {
                val from = filters.customFromMillis ?: startOfDay(now)
                val to = filters.customToMillis ?: now
                from to to
            }
        }
    }

    private companion object {
        const val DAY_MILLIS = 24 * 60 * 60 * 1000L
    }
}
