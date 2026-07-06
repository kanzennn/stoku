package com.example.stoku.domain.usecase.product

import com.example.stoku.domain.model.Product
import com.example.stoku.domain.model.UserRole
import com.example.stoku.domain.model.maskFor
import com.example.stoku.domain.repository.ProductRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Contains-match search over sku or product_name, used by Manual Keluar and Stock List. */
class SearchProductsUseCase(
    private val productRepository: ProductRepository,
) {
    operator fun invoke(query: String, role: UserRole): Flow<List<Product>> =
        productRepository.search(query).map { products -> products.map { it.maskFor(role) } }
}
