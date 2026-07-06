package com.example.stoku.data.repository

import com.example.stoku.data.local.dao.PriceHistoryDao
import com.example.stoku.data.local.entity.PriceHistoryEntity
import com.example.stoku.domain.model.PriceHistory
import com.example.stoku.domain.repository.PriceHistoryRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PriceHistoryRepositoryImpl @Inject constructor(
    private val priceHistoryDao: PriceHistoryDao,
) : PriceHistoryRepository {
    override fun getBySku(sku: String): Flow<List<PriceHistory>> =
        priceHistoryDao.getBySku(sku).map { entries -> entries.map { it.toDomain() } }

    override suspend fun insert(priceHistory: PriceHistory): Long = priceHistoryDao.insert(priceHistory.toEntity())
}

private fun PriceHistoryEntity.toDomain(): PriceHistory = PriceHistory(
    id = id,
    sku = sku,
    costPrice = costPrice,
    sellingPrice = sellingPrice,
    changedByUserId = changedByUserId,
    createdAt = createdAt,
)

private fun PriceHistory.toEntity(): PriceHistoryEntity = PriceHistoryEntity(
    id = id,
    sku = sku,
    costPrice = requireNotNull(costPrice) { "costPrice must not be null when persisting price history" },
    sellingPrice = sellingPrice,
    changedByUserId = changedByUserId,
    createdAt = createdAt,
)
