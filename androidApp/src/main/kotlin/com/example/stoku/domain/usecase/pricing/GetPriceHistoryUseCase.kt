package com.example.stoku.domain.usecase.pricing

import com.example.stoku.domain.model.PriceHistory
import com.example.stoku.domain.model.UserRole
import com.example.stoku.domain.model.maskFor
import com.example.stoku.domain.repository.PriceHistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GetPriceHistoryUseCase(
    private val priceHistoryRepository: PriceHistoryRepository,
) {
    operator fun invoke(sku: String, role: UserRole): Flow<List<PriceHistory>> =
        priceHistoryRepository.getBySku(sku).map { entries -> entries.map { it.maskFor(role) } }
}
