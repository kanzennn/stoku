package com.example.stoku.domain.usecase.category

import com.example.stoku.domain.model.Category
import com.example.stoku.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow

class GetCategoriesUseCase(private val categoryRepository: CategoryRepository) {
    operator fun invoke(): Flow<List<Category>> = categoryRepository.getAll()
}
