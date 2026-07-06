package com.example.stoku.domain.usecase.product

import com.example.stoku.domain.model.Product
import com.example.stoku.domain.model.UserRole
import com.example.stoku.domain.model.maskFor
import com.example.stoku.domain.repository.ProductRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GetProductsUseCase(
    private val productRepository: ProductRepository,
) {
    operator fun invoke(role: UserRole): Flow<List<Product>> =
        productRepository.getAll().map { products -> products.map { it.maskFor(role) } }
}
