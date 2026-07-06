package com.example.stoku.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = ProductEntity::class,
            parentColumns = ["sku"],
            childColumns = ["sku"],
        ),
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["user_id"],
        ),
    ],
    indices = [Index("sku"), Index("user_id")],
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sku: String,
    /** "IN" or "OUT" */
    val type: String,
    /** "SCAN" or "MANUAL" */
    val source: String,
    val quantity: Int,
    @ColumnInfo(name = "cost_price_snapshot")
    val costPriceSnapshot: Long,
    @ColumnInfo(name = "selling_price_snapshot")
    val sellingPriceSnapshot: Long,
    val notes: String? = null,
    @ColumnInfo(name = "user_id")
    val userId: Long,
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
)
