package com.example.stoku.domain.usecase.transaction

import com.example.stoku.domain.model.InsufficientStockException
import com.example.stoku.domain.model.Transaction
import com.example.stoku.domain.model.TransactionSource
import com.example.stoku.domain.model.TransactionType
import com.example.stoku.domain.model.UnauthorizedActionException
import com.example.stoku.domain.model.UserRole
import com.example.stoku.domain.repository.ProductRepository
import com.example.stoku.domain.repository.TransactionRepository

class RecordTransactionUseCase(
    private val productRepository: ProductRepository,
    private val transactionRepository: TransactionRepository,
) {
    suspend operator fun invoke(
        role: UserRole,
        sku: String,
        type: TransactionType,
        source: TransactionSource,
        quantity: Int,
        notes: String?,
        userId: Long,
    ): Result<Transaction> {
        if (quantity <= 0) {
            return Result.failure(IllegalArgumentException("Quantity must be greater than zero"))
        }
        if (source == TransactionSource.MANUAL && role == UserRole.KASIR) {
            return Result.failure(UnauthorizedActionException("Manual input is restricted to owner and admin"))
        }
        if (role == UserRole.KASIR && type != TransactionType.OUT) {
            return Result.failure(UnauthorizedActionException("Kasir can only record OUT transactions"))
        }

        val product = productRepository.getBySku(sku)
            ?: return Result.failure(NoSuchElementException("Product with sku $sku not found"))

        if (type == TransactionType.OUT && quantity > product.stock) {
            return Result.failure(
                InsufficientStockException(
                    "Requested quantity $quantity exceeds available stock ${product.stock}",
                ),
            )
        }

        val now = System.currentTimeMillis()
        val delta = if (type == TransactionType.IN) quantity else -quantity
        productRepository.adjustStock(sku, delta, now)

        // Snapshot the true cost price regardless of who performed the transaction —
        // the audit trail must retain it even though kasir-facing reads later mask it.
        val transaction = Transaction(
            id = 0,
            sku = sku,
            type = type,
            source = source,
            quantity = quantity,
            costPriceSnapshot = product.costPrice,
            sellingPriceSnapshot = product.sellingPrice,
            notes = notes,
            userId = userId,
            createdAt = now,
        )
        val insertedId = transactionRepository.insert(transaction)
        return Result.success(transaction.copy(id = insertedId))
    }
}
