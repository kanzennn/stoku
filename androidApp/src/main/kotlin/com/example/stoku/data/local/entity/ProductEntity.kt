package com.example.stoku.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey
    val sku: String,
    @ColumnInfo(name = "brand_name")
    val brandName: String,
    @ColumnInfo(name = "product_name")
    val productName: String,
    val category: String,
    val stock: Int,
    @ColumnInfo(name = "cost_price")
    val costPrice: Long,
    @ColumnInfo(name = "selling_price")
    val sellingPrice: Long,
    @ColumnInfo(name = "low_stock_threshold")
    val lowStockThreshold: Int = 5,
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long,
)
