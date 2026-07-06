package com.example.stoku.domain.repository

import com.example.stoku.domain.model.PriceHistory
import kotlinx.coroutines.flow.Flow

interface PriceHistoryRepository {
    fun getBySku(sku: String): Flow<List<PriceHistory>>
    suspend fun insert(priceHistory: PriceHistory): Long
}
