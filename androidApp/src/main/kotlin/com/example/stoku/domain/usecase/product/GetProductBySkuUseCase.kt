package com.example.stoku.domain.usecase.product

import com.example.stoku.domain.model.Product
import com.example.stoku.domain.model.UserRole
import com.example.stoku.domain.model.maskFor
import com.example.stoku.domain.repository.ProductRepository

class GetProductBySkuUseCase(
    private val productRepository: ProductRepository,
) {
    suspend operator fun invoke(sku: String, role: UserRole): Product? =
        productRepository.getBySku(sku)?.maskFor(role)
}
