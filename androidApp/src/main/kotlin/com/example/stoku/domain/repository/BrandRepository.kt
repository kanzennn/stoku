package com.example.stoku.domain.repository

import com.example.stoku.domain.model.Brand
import kotlinx.coroutines.flow.Flow

interface BrandRepository {
    fun getAll(): Flow<List<Brand>>
    suspend fun insert(brand: Brand)
    suspend fun update(brand: Brand)
    suspend fun delete(brand: Brand)
}
