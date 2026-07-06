package com.example.stoku.domain.usecase.brand

import com.example.stoku.domain.model.Brand
import com.example.stoku.domain.repository.BrandRepository

class AddBrandUseCase(private val brandRepository: BrandRepository) {
    suspend operator fun invoke(name: String) =
        brandRepository.insert(Brand(name = name, createdAt = System.currentTimeMillis()))
}
