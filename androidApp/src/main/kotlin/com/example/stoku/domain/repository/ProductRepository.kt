package com.example.stoku.domain.repository

import com.example.stoku.domain.model.Product
import kotlinx.coroutines.flow.Flow

interface ProductRepository {
    fun getAll(): Flow<List<Product>>
    fun getLowStock(): Flow<List<Product>>
    fun search(query: String): Flow<List<Product>>
    suspend fun getBySku(sku: String): Product?
    suspend fun insert(product: Product)
    suspend fun update(product: Product)
    suspend fun adjustStock(sku: String, delta: Int, updatedAt: Long)
}
