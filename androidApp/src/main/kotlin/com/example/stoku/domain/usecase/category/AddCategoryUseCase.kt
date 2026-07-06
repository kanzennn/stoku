package com.example.stoku.domain.usecase.category

import com.example.stoku.domain.model.Category
import com.example.stoku.domain.repository.CategoryRepository

class AddCategoryUseCase(private val categoryRepository: CategoryRepository) {
    suspend operator fun invoke(name: String) =
        categoryRepository.insert(Category(name = name, createdAt = System.currentTimeMillis()))
}
