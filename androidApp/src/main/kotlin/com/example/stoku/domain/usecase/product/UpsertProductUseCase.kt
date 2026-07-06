package com.example.stoku.domain.usecase.product

import com.example.stoku.domain.model.Product
import com.example.stoku.domain.model.UnauthorizedActionException
import com.example.stoku.domain.model.UserRole
import com.example.stoku.domain.repository.ProductRepository

class UpsertProductUseCase(
    private val productRepository: ProductRepository,
) {
    suspend operator fun invoke(role: UserRole, product: Product): Result<Unit> {
        if (role == UserRole.KASIR) {
            return Result.failure(UnauthorizedActionException("Kasir cannot create or modify products"))
        }
        return runCatching {
            val existing = productRepository.getBySku(product.sku)
            if (existing == null) productRepository.insert(product) else productRepository.update(product)
        }
    }
}
