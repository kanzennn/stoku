package com.example.stoku.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "price_histories",
    foreignKeys = [
        ForeignKey(
            entity = ProductEntity::class,
            parentColumns = ["sku"],
            childColumns = ["sku"],
        ),
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["changed_by_user_id"],
        ),
    ],
    indices = [Index("sku"), Index("changed_by_user_id")],
)
data class PriceHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sku: String,
    @ColumnInfo(name = "cost_price")
    val costPrice: Long,
    @ColumnInfo(name = "selling_price")
    val sellingPrice: Long,
    @ColumnInfo(name = "changed_by_user_id")
    val changedByUserId: Long,
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
)
