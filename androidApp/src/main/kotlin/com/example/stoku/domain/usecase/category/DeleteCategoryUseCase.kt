package com.example.stoku.domain.usecase.category

import com.example.stoku.domain.model.Category
import com.example.stoku.domain.repository.CategoryRepository

class DeleteCategoryUseCase(private val categoryRepository: CategoryRepository) {
    suspend operator fun invoke(category: Category) = categoryRepository.delete(category)
}
