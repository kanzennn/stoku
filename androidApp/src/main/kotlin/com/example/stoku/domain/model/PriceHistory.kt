package com.example.stoku.domain.model

data class PriceHistory(
    val id: Long,
    val sku: String,
    /** Null when masked for a role without [canViewCostPrice]. */
    val costPrice: Long?,
    val sellingPrice: Long,
    val changedByUserId: Long,
    val createdAt: Long,
)

fun PriceHistory.maskFor(role: UserRole): PriceHistory =
    if (role.canViewCostPrice) this else copy(costPrice = null)
