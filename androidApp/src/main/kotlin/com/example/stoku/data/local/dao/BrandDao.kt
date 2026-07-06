package com.example.stoku.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.stoku.data.local.entity.BrandEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BrandDao {
    @Query("SELECT * FROM brands ORDER BY name ASC")
    fun getAll(): Flow<List<BrandEntity>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(brand: BrandEntity): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertAll(brands: List<BrandEntity>)

    @Update
    suspend fun update(brand: BrandEntity)

    @Delete
    suspend fun delete(brand: BrandEntity)
}
