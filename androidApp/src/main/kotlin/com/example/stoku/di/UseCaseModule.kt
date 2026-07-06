package com.example.stoku.di

import com.example.stoku.data.local.AppPreferences
import com.example.stoku.domain.repository.BrandRepository
import com.example.stoku.domain.repository.CategoryRepository
import com.example.stoku.domain.repository.PriceHistoryRepository
import com.example.stoku.domain.repository.ProductRepository
import com.example.stoku.domain.repository.TransactionRepository
import com.example.stoku.domain.repository.UserRepository
import com.example.stoku.domain.usecase.auth.GetLoggedInUserUseCase
import com.example.stoku.domain.usecase.auth.LoginUseCase
import com.example.stoku.domain.usecase.auth.LogoutUseCase
import com.example.stoku.domain.usecase.brand.AddBrandUseCase
import com.example.stoku.domain.usecase.brand.DeleteBrandUseCase
import com.example.stoku.domain.usecase.brand.GetBrandsUseCase
import com.example.stoku.domain.usecase.brand.UpdateBrandUseCase
import com.example.stoku.domain.usecase.category.AddCategoryUseCase
import com.example.stoku.domain.usecase.category.DeleteCategoryUseCase
import com.example.stoku.domain.usecase.category.GetCategoriesUseCase
import com.example.stoku.domain.usecase.category.UpdateCategoryUseCase
import com.example.stoku.domain.usecase.pricing.GetPriceHistoryUseCase
import com.example.stoku.domain.usecase.pricing.UpdateProductPriceUseCase
import com.example.stoku.domain.usecase.product.AddNewProductUseCase
import com.example.stoku.domain.usecase.product.GetLowStockProductsUseCase
import com.example.stoku.domain.usecase.product.GetProductBySkuUseCase
import com.example.stoku.domain.usecase.product.GetProductsUseCase
import com.example.stoku.domain.usecase.product.RestockProductUseCase
import com.example.stoku.domain.usecase.product.SearchProductsUseCase
import com.example.stoku.domain.usecase.product.UpdateProductDetailsUseCase
import com.example.stoku.domain.usecase.product.UpsertProductUseCase
import com.example.stoku.domain.usecase.settings.GetGlobalLowStockThresholdUseCase
import com.example.stoku.domain.usecase.settings.SetGlobalLowStockThresholdUseCase
import com.example.stoku.domain.usecase.transaction.GetTodaysTransactionCountUseCase
import com.example.stoku.domain.usecase.transaction.GetTransactionByIdUseCase
import com.example.stoku.domain.usecase.transaction.GetTransactionHistoryUseCase
import com.example.stoku.domain.usecase.transaction.GetTransactionsBySkuUseCase
import com.example.stoku.domain.usecase.transaction.RecordTransactionUseCase
import com.example.stoku.domain.usecase.user.GetUsersUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {

    @Provides
    fun provideLoginUseCase(
        userRepository: UserRepository,
        appPreferences: AppPreferences,
    ): LoginUseCase = LoginUseCase(userRepository, appPreferences)

    @Provides
    fun provideLogoutUseCase(appPreferences: AppPreferences): LogoutUseCase =
        LogoutUseCase(appPreferences)

    @Provides
    fun provideGetLoggedInUserUseCase(
        userRepository: UserRepository,
        appPreferences: AppPreferences,
    ): GetLoggedInUserUseCase = GetLoggedInUserUseCase(userRepository, appPreferences)

    @Provides
    fun provideGetProductsUseCase(productRepository: ProductRepository): GetProductsUseCase =
        GetProductsUseCase(productRepository)

    @Provides
    fun provideGetProductBySkuUseCase(productRepository: ProductRepository): GetProductBySkuUseCase =
        GetProductBySkuUseCase(productRepository)

    @Provides
    fun provideGetLowStockProductsUseCase(
        productRepository: ProductRepository,
    ): GetLowStockProductsUseCase = GetLowStockProductsUseCase(productRepository)

    @Provides
    fun provideUpsertProductUseCase(productRepository: ProductRepository): UpsertProductUseCase =
        UpsertProductUseCase(productRepository)

    @Provides
    fun provideAddNewProductUseCase(
        productRepository: ProductRepository,
        priceHistoryRepository: PriceHistoryRepository,
        transactionRepository: TransactionRepository,
        getGlobalLowStockThresholdUseCase: GetGlobalLowStockThresholdUseCase,
    ): AddNewProductUseCase = AddNewProductUseCase(
        productRepository,
        priceHistoryRepository,
        transactionRepository,
        getGlobalLowStockThresholdUseCase,
    )

    @Provides
    fun provideRestockProductUseCase(
        productRepository: ProductRepository,
        priceHistoryRepository: PriceHistoryRepository,
        transactionRepository: TransactionRepository,
    ): RestockProductUseCase = RestockProductUseCase(productRepository, priceHistoryRepository, transactionRepository)

    @Provides
    fun provideSearchProductsUseCase(productRepository: ProductRepository): SearchProductsUseCase =
        SearchProductsUseCase(productRepository)

    @Provides
    fun provideUpdateProductDetailsUseCase(
        productRepository: ProductRepository,
        priceHistoryRepository: PriceHistoryRepository,
    ): UpdateProductDetailsUseCase = UpdateProductDetailsUseCase(productRepository, priceHistoryRepository)

    @Provides
    fun provideRecordTransactionUseCase(
        productRepository: ProductRepository,
        transactionRepository: TransactionRepository,
    ): RecordTransactionUseCase = RecordTransactionUseCase(productRepository, transactionRepository)

    @Provides
    fun provideGetTransactionHistoryUseCase(
        transactionRepository: TransactionRepository,
    ): GetTransactionHistoryUseCase = GetTransactionHistoryUseCase(transactionRepository)

    @Provides
    fun provideGetTransactionsBySkuUseCase(
        transactionRepository: TransactionRepository,
    ): GetTransactionsBySkuUseCase = GetTransactionsBySkuUseCase(transactionRepository)

    @Provides
    fun provideGetTodaysTransactionCountUseCase(
        transactionRepository: TransactionRepository,
    ): GetTodaysTransactionCountUseCase = GetTodaysTransactionCountUseCase(transactionRepository)

    @Provides
    fun provideGetTransactionByIdUseCase(
        transactionRepository: TransactionRepository,
    ): GetTransactionByIdUseCase = GetTransactionByIdUseCase(transactionRepository)

    @Provides
    fun provideGetUsersUseCase(userRepository: UserRepository): GetUsersUseCase =
        GetUsersUseCase(userRepository)

    @Provides
    fun provideUpdateProductPriceUseCase(
        productRepository: ProductRepository,
        priceHistoryRepository: PriceHistoryRepository,
    ): UpdateProductPriceUseCase = UpdateProductPriceUseCase(productRepository, priceHistoryRepository)

    @Provides
    fun provideGetPriceHistoryUseCase(
        priceHistoryRepository: PriceHistoryRepository,
    ): GetPriceHistoryUseCase = GetPriceHistoryUseCase(priceHistoryRepository)

    @Provides
    fun provideGetGlobalLowStockThresholdUseCase(
        appPreferences: AppPreferences,
    ): GetGlobalLowStockThresholdUseCase = GetGlobalLowStockThresholdUseCase(appPreferences)

    @Provides
    fun provideSetGlobalLowStockThresholdUseCase(
        appPreferences: AppPreferences,
    ): SetGlobalLowStockThresholdUseCase = SetGlobalLowStockThresholdUseCase(appPreferences)

    @Provides
    fun provideGetBrandsUseCase(brandRepository: BrandRepository): GetBrandsUseCase =
        GetBrandsUseCase(brandRepository)

    @Provides
    fun provideAddBrandUseCase(brandRepository: BrandRepository): AddBrandUseCase =
        AddBrandUseCase(brandRepository)

    @Provides
    fun provideUpdateBrandUseCase(brandRepository: BrandRepository): UpdateBrandUseCase =
        UpdateBrandUseCase(brandRepository)

    @Provides
    fun provideDeleteBrandUseCase(brandRepository: BrandRepository): DeleteBrandUseCase =
        DeleteBrandUseCase(brandRepository)

    @Provides
    fun provideGetCategoriesUseCase(categoryRepository: CategoryRepository): GetCategoriesUseCase =
        GetCategoriesUseCase(categoryRepository)

    @Provides
    fun provideAddCategoryUseCase(categoryRepository: CategoryRepository): AddCategoryUseCase =
        AddCategoryUseCase(categoryRepository)

    @Provides
    fun provideUpdateCategoryUseCase(categoryRepository: CategoryRepository): UpdateCategoryUseCase =
        UpdateCategoryUseCase(categoryRepository)

    @Provides
    fun provideDeleteCategoryUseCase(categoryRepository: CategoryRepository): DeleteCategoryUseCase =
        DeleteCategoryUseCase(categoryRepository)
}
