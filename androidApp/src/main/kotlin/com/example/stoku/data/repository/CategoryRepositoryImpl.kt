package com.example.stoku.data.repository

import com.example.stoku.data.local.dao.CategoryDao
import com.example.stoku.data.local.entity.CategoryEntity
import com.example.stoku.domain.model.Category
import com.example.stoku.domain.repository.CategoryRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CategoryRepositoryImpl @Inject constructor(
    private val categoryDao: CategoryDao,
) : CategoryRepository {
    override fun getAll(): Flow<List<Category>> =
        categoryDao.getAll().map { list -> list.map { it.toDomain() } }

    override suspend fun insert(category: Category) { categoryDao.insert(category.toEntity()) }

    override suspend fun update(category: Category) = categoryDao.update(category.toEntity())

    override suspend fun delete(category: Category) = categoryDao.delete(category.toEntity())
}

private fun CategoryEntity.toDomain() = Category(id = id, name = name, createdAt = createdAt)
private fun Category.toEntity() = CategoryEntity(id = id, name = name, createdAt = createdAt)
