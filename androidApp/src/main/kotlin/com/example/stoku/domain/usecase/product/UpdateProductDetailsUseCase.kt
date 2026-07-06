package com.example.stoku.domain.usecase.product

import com.example.stoku.domain.model.PriceHistory
import com.example.stoku.domain.model.Product
import com.example.stoku.domain.model.UnauthorizedActionException
import com.example.stoku.domain.model.UserRole
import com.example.stoku.domain.repository.PriceHistoryRepository
import com.example.stoku.domain.repository.ProductRepository

/**
 * Used by Stock Detail's edit form: updates any product field, and logs a price_histories
 * entry only if cost_price or selling_price actually changed.
 */
class UpdateProductDetailsUseCase(
    private val productRepository: ProductRepository,
    private val priceHistoryRepository: PriceHistoryRepository,
) {
    suspend operator fun invoke(
        role: UserRole,
        sku: String,
        brandName: String,
        productName: String,
        category: String,
        costPrice: Long,
        sellingPrice: Long,
        lowStockThreshold: Int,
        userId: Long,
    ): Result<Unit> {
        if (role == UserRole.KASIR) {
            return Result.failure(UnauthorizedActionException("Kasir cannot edit products"))
        }
        val existing = productRepository.getBySku(sku)
            ?: return Result.failure(NoSuchElementException("Product with sku $sku not found"))

        val now = System.currentTimeMillis()
        val priceChanged = costPrice != existing.costPrice || sellingPrice != existing.sellingPrice

        return runCatching {
            if (priceChanged) {
                priceHistoryRepository.insert(
                    PriceHistory(
                        id = 0,
                        sku = sku,
                        costPrice = costPrice,
                        sellingPrice = sellingPrice,
                        changedByUserId = userId,
                        createdAt = now,
                    ),
                )
            }
            productRepository.update(
                Product(
                    sku = sku,
                    brandName = brandName,
                    productName = productName,
                    category = category,
                    stock = existing.stock,
                    costPrice = costPrice,
                    sellingPrice = sellingPrice,
                    lowStockThreshold = lowStockThreshold,
                    createdAt = existing.createdAt,
                    updatedAt = now,
                ),
            )
        }
    }
}
