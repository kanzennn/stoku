package com.example.stoku.domain.usecase.brand

import com.example.stoku.domain.model.Brand
import com.example.stoku.domain.repository.BrandRepository
import kotlinx.coroutines.flow.Flow

class GetBrandsUseCase(private val brandRepository: BrandRepository) {
    operator fun invoke(): Flow<List<Brand>> = brandRepository.getAll()
}
