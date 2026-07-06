package com.example.stoku.domain.usecase.product

import com.example.stoku.domain.model.PriceHistory
import com.example.stoku.domain.model.Product
import com.example.stoku.domain.model.Transaction
import com.example.stoku.domain.model.TransactionSource
import com.example.stoku.domain.model.TransactionType
import com.example.stoku.domain.model.UnauthorizedActionException
import com.example.stoku.domain.model.UserRole
import com.example.stoku.domain.repository.PriceHistoryRepository
import com.example.stoku.domain.repository.ProductRepository
import com.example.stoku.domain.repository.TransactionRepository
import com.example.stoku.domain.usecase.settings.GetGlobalLowStockThresholdUseCase
import kotlinx.coroutines.flow.first

/**
 * Used by both Scan Masuk and Manual Masuk when a scanned/entered SKU has no existing product:
 * creates the product, logs its initial price as the first price_histories entry, and records
 * the initial stock as an IN transaction in one cohesive operation.
 */
class AddNewProductUseCase(
    private val productRepository: ProductRepository,
    private val priceHistoryRepository: PriceHistoryRepository,
    private val transactionRepository: TransactionRepository,
    private val getGlobalLowStockThresholdUseCase: GetGlobalLowStockThresholdUseCase,
) {
    suspend operator fun invoke(
        role: UserRole,
        sku: String,
        brandName: String,
        productName: String,
        category: String,
        costPrice: Long,
        sellingPrice: Long,
        initialQuantity: Int,
        source: TransactionSource,
        userId: Long,
        notes: String? = null,
    ): Result<Product> {
        if (role == UserRole.KASIR) {
            return Result.failure(UnauthorizedActionException("Kasir cannot add new products"))
        }
        if (sku.isBlank() || brandName.isBlank() || productName.isBlank() || category.isBlank()) {
            return Result.failure(IllegalArgumentException("All product fields are required"))
        }
        if (costPrice < 0 || sellingPrice < 0 || initialQuantity < 0) {
            return Result.failure(IllegalArgumentException("Prices and quantity must not be negative"))
        }
        if (productRepository.getBySku(sku) != null) {
            return Result.failure(IllegalStateException("Product with sku $sku already exists"))
        }

        val now = System.currentTimeMillis()
        val product = Product(
            sku = sku,
            brandName = brandName,
            productName = productName,
            category = category,
            stock = initialQuantity,
            costPrice = costPrice,
            sellingPrice = sellingPrice,
            lowStockThreshold = getGlobalLowStockThresholdUseCase().first(),
            createdAt = now,
            updatedAt = now,
        )

        return runCatching {
            productRepository.insert(product)
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
            transactionRepository.insert(
                Transaction(
                    id = 0,
                    sku = sku,
                    type = TransactionType.IN,
                    source = source,
                    quantity = initialQuantity,
                    costPriceSnapshot = costPrice,
                    sellingPriceSnapshot = sellingPrice,
                    notes = notes,
                    userId = userId,
                    createdAt = now,
                ),
            )
            product
        }
    }
}
