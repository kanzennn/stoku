package com.example.stoku.di

import com.example.stoku.data.repository.BrandRepositoryImpl
import com.example.stoku.data.repository.CategoryRepositoryImpl
import com.example.stoku.data.repository.PriceHistoryRepositoryImpl
import com.example.stoku.data.repository.ProductRepositoryImpl
import com.example.stoku.data.repository.TransactionRepositoryImpl
import com.example.stoku.data.repository.UserRepositoryImpl
import com.example.stoku.domain.repository.BrandRepository
import com.example.stoku.domain.repository.CategoryRepository
import com.example.stoku.domain.repository.PriceHistoryRepository
import com.example.stoku.domain.repository.ProductRepository
import com.example.stoku.domain.repository.TransactionRepository
import com.example.stoku.domain.repository.UserRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    abstract fun bindUserRepository(impl: UserRepositoryImpl): UserRepository

    @Binds
    abstract fun bindProductRepository(impl: ProductRepositoryImpl): ProductRepository

    @Binds
    abstract fun bindTransactionRepository(impl: TransactionRepositoryImpl): TransactionRepository

    @Binds
    abstract fun bindPriceHistoryRepository(impl: PriceHistoryRepositoryImpl): PriceHistoryRepository

    @Binds
    abstract fun bindBrandRepository(impl: BrandRepositoryImpl): BrandRepository

    @Binds
    abstract fun bindCategoryRepository(impl: CategoryRepositoryImpl): CategoryRepository
}
