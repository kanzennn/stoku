package com.example.stoku.domain.model

data class Transaction(
    val id: Long,
    val sku: String,
    val type: TransactionType,
    val source: TransactionSource,
    val quantity: Int,
    /** Null when masked for a role without [canViewCostPrice]. */
    val costPriceSnapshot: Long?,
    val sellingPriceSnapshot: Long,
    val notes: String?,
    val userId: Long,
    val createdAt: Long,
)

fun Transaction.maskFor(role: UserRole): Transaction =
    if (role.canViewCostPrice) this else copy(costPriceSnapshot = null)
