package com.example.stoku.domain.usecase.brand

import com.example.stoku.domain.model.Brand
import com.example.stoku.domain.repository.BrandRepository

class UpdateBrandUseCase(private val brandRepository: BrandRepository) {
    suspend operator fun invoke(brand: Brand) = brandRepository.update(brand)
}
