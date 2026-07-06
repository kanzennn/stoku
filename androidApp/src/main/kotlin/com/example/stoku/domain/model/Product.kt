package com.example.stoku.domain.model

data class Product(
    val sku: String,
    val brandName: String,
    val productName: String,
    val category: String,
    val stock: Int,
    /** Null when masked for a role without [canViewCostPrice]. */
    val costPrice: Long?,
    val sellingPrice: Long,
    val lowStockThreshold: Int,
    val createdAt: Long,
    val updatedAt: Long,
)

fun Product.maskFor(role: UserRole): Product =
    if (role.canViewCostPrice) this else copy(costPrice = null)
