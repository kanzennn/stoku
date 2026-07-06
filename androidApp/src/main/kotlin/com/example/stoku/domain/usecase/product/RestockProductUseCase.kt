package com.example.stoku.domain.usecase.product

import com.example.stoku.domain.model.PriceHistory
import com.example.stoku.domain.model.Transaction
import com.example.stoku.domain.model.TransactionSource
import com.example.stoku.domain.model.TransactionType
import com.example.stoku.domain.model.UnauthorizedActionException
import com.example.stoku.domain.model.UserRole
import com.example.stoku.domain.repository.PriceHistoryRepository
import com.example.stoku.domain.repository.ProductRepository
import com.example.stoku.domain.repository.TransactionRepository

/**
 * Used by both Scan Masuk and Manual Masuk when a scanned/entered SKU matches an existing
 * product: optionally updates cost/selling price (logging a price_histories entry only if a
 * price actually changed), adds the restocked quantity, and records the IN transaction.
 */
class RestockProductUseCase(
    private val productRepository: ProductRepository,
    private val priceHistoryRepository: PriceHistoryRepository,
    private val transactionRepository: TransactionRepository,
) {
    suspend operator fun invoke(
        role: UserRole,
        sku: String,
        newCostPrice: Long,
        newSellingPrice: Long,
        quantity: Int,
        source: TransactionSource,
        userId: Long,
        notes: String? = null,
    ): Result<Transaction> {
        if (role == UserRole.KASIR) {
            return Result.failure(UnauthorizedActionException("Kasir cannot restock products"))
        }
        if (quantity <= 0) {
            return Result.failure(IllegalArgumentException("Quantity must be greater than zero"))
        }
        if (newCostPrice < 0 || newSellingPrice < 0) {
            return Result.failure(IllegalArgumentException("Prices must not be negative"))
        }
        val product = productRepository.getBySku(sku)
            ?: return Result.failure(NoSuchElementException("Product with sku $sku not found"))

        val now = System.currentTimeMillis()
        val priceChanged = newCostPrice != product.costPrice || newSellingPrice != product.sellingPrice

        return runCatching {
            if (priceChanged) {
                priceHistoryRepository.insert(
                    PriceHistory(
                        id = 0,
                        sku = sku,
                        costPrice = newCostPrice,
                        sellingPrice = newSellingPrice,
                        changedByUserId = userId,
                        createdAt = now,
                    ),
                )
            }
            productRepository.update(
                product.copy(costPrice = newCostPrice, sellingPrice = newSellingPrice, updatedAt = now),
            )
            productRepository.adjustStock(sku, quantity, now)

            val transaction = Transaction(
                id = 0,
                sku = sku,
                type = TransactionType.IN,
                source = source,
                quantity = quantity,
                costPriceSnapshot = newCostPrice,
                sellingPriceSnapshot = newSellingPrice,
                notes = notes,
                userId = userId,
                createdAt = now,
            )
            val insertedId = transactionRepository.insert(transaction)
            transaction.copy(id = insertedId)
        }
    }
}
