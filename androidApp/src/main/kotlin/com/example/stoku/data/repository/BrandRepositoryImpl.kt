package com.example.stoku.data.repository

import com.example.stoku.data.local.dao.BrandDao
import com.example.stoku.data.local.entity.BrandEntity
import com.example.stoku.domain.model.Brand
import com.example.stoku.domain.repository.BrandRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class BrandRepositoryImpl @Inject constructor(
    private val brandDao: BrandDao,
) : BrandRepository {
    override fun getAll(): Flow<List<Brand>> =
        brandDao.getAll().map { list -> list.map { it.toDomain() } }

    override suspend fun insert(brand: Brand) { brandDao.insert(brand.toEntity()) }

    override suspend fun update(brand: Brand) = brandDao.update(brand.toEntity())

    override suspend fun delete(brand: Brand) = brandDao.delete(brand.toEntity())
}

private fun BrandEntity.toDomain() = Brand(id = id, name = name, createdAt = createdAt)
private fun Brand.toEntity() = BrandEntity(id = id, name = name, createdAt = createdAt)
