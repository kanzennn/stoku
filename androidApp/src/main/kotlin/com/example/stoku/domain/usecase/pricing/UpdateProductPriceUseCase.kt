package com.example.stoku.domain.usecase.pricing

import com.example.stoku.domain.model.PriceHistory
import com.example.stoku.domain.model.UnauthorizedActionException
import com.example.stoku.domain.model.UserRole
import com.example.stoku.domain.repository.PriceHistoryRepository
import com.example.stoku.domain.repository.ProductRepository

class UpdateProductPriceUseCase(
    private val productRepository: ProductRepository,
    private val priceHistoryRepository: PriceHistoryRepository,
) {
    suspend operator fun invoke(
        role: UserRole,
        sku: String,
        newCostPrice: Long,
        newSellingPrice: Long,
        changedByUserId: Long,
    ): Result<Unit> {
        if (role == UserRole.KASIR) {
            return Result.failure(UnauthorizedActionException("Kasir cannot change prices"))
        }
        val product = productRepository.getBySku(sku)
            ?: return Result.failure(NoSuchElementException("Product with sku $sku not found"))

        val now = System.currentTimeMillis()
        return runCatching {
            productRepository.update(
                product.copy(costPrice = newCostPrice, sellingPrice = newSellingPrice, updatedAt = now),
            )
            priceHistoryRepository.insert(
                PriceHistory(
                    id = 0,
                    sku = sku,
                    costPrice = newCostPrice,
                    sellingPrice = newSellingPrice,
                    changedByUserId = changedByUserId,
                    createdAt = now,
                ),
            )
        }
    }
}
