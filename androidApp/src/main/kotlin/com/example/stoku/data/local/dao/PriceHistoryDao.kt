package com.example.stoku.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.stoku.data.local.entity.PriceHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PriceHistoryDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(priceHistory: PriceHistoryEntity): Long

    @Query("SELECT * FROM price_histories ORDER BY created_at DESC")
    fun getAll(): Flow<List<PriceHistoryEntity>>

    @Query("SELECT * FROM price_histories WHERE sku = :sku ORDER BY created_at DESC")
    fun getBySku(sku: String): Flow<List<PriceHistoryEntity>>
}
