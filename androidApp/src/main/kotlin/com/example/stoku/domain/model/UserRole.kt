package com.example.stoku.domain.model

enum class UserRole(val value: String) {
    OWNER("owner"),
    ADMIN("admin"),
    KASIR("kasir"),
    ;

    companion object {
        fun fromValue(value: String): UserRole = entries.first { it.value == value }
    }
}

/** cost_price & cost_price_snapshot are hidden from kasir on ALL screens. */
val UserRole.canViewCostPrice: Boolean
    get() = this != UserRole.KASIR
