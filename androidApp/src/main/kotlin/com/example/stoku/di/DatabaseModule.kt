package com.example.stoku.di

import android.content.Context
import com.example.stoku.data.local.AppDatabase
import com.example.stoku.data.local.dao.BrandDao
import com.example.stoku.data.local.dao.CategoryDao
import com.example.stoku.data.local.dao.PriceHistoryDao
import com.example.stoku.data.local.dao.ProductDao
import com.example.stoku.data.local.dao.TransactionDao
import com.example.stoku.data.local.dao.UserDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideApplicationScope(): CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context,
        applicationScope: CoroutineScope,
    ): AppDatabase = AppDatabase.create(context, applicationScope)

    @Provides
    fun provideUserDao(database: AppDatabase): UserDao = database.userDao()

    @Provides
    fun provideProductDao(database: AppDatabase): ProductDao = database.productDao()

    @Provides
    fun provideTransactionDao(database: AppDatabase): TransactionDao = database.transactionDao()

    @Provides
    fun providePriceHistoryDao(database: AppDatabase): PriceHistoryDao = database.priceHistoryDao()

    @Provides
    fun provideBrandDao(database: AppDatabase): BrandDao = database.brandDao()

    @Provides
    fun provideCategoryDao(database: AppDatabase): CategoryDao = database.categoryDao()
}
