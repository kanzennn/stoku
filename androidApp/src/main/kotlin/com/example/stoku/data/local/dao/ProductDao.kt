package com.example.stoku.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.stoku.data.local.entity.ProductEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(product: ProductEntity)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertAll(products: List<ProductEntity>)

    @Update
    suspend fun update(product: ProductEntity)

    @Delete
    suspend fun delete(product: ProductEntity)

    @Query("SELECT * FROM products WHERE sku = :sku LIMIT 1")
    suspend fun getBySku(sku: String): ProductEntity?

    @Query("SELECT * FROM products ORDER BY product_name ASC")
    fun getAll(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE category = :category ORDER BY product_name ASC")
    fun getByCategory(category: String): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE stock <= low_stock_threshold ORDER BY stock ASC")
    fun getLowStock(): Flow<List<ProductEntity>>

    @Query("UPDATE products SET stock = stock + :delta, updated_at = :updatedAt WHERE sku = :sku")
    suspend fun adjustStock(sku: String, delta: Int, updatedAt: Long)

    @Query(
        "SELECT * FROM products WHERE product_name LIKE '%' || :query || '%' OR sku LIKE '%' || :query || '%' " +
            "ORDER BY product_name ASC",
    )
    fun search(query: String): Flow<List<ProductEntity>>
}
