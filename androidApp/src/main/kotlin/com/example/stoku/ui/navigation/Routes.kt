package com.example.stoku.ui.navigation

import com.example.stoku.domain.model.UserRole

object Routes {
    const val LOGIN = "login"
    const val DASHBOARD = "dashboard"
    const val SCAN_IN = "scan_in"
    const val MANUAL_INPUT = "manual_input"
    const val MANUAL_IN = "manual_in"
    const val MANUAL_OUT = "manual_out"
    const val STOCK_LIST = "stock_list"
    const val STOCK_DETAIL = "stock_detail"
    const val HISTORY = "history"
    const val HISTORY_DETAIL = "history_detail"
    const val STATISTICS = "statistics"
    const val SETTINGS = "settings"
    const val KASIR = "kasir"
    const val BRAND_LIST = "brand_list"
    const val CATEGORY_LIST = "category_list"

    const val STOCK_DETAIL_ARG_SKU = "sku"
    const val STOCK_DETAIL_PATTERN = "$STOCK_DETAIL/{$STOCK_DETAIL_ARG_SKU}"

    const val HISTORY_DETAIL_ARG_ID = "transactionId"
    const val HISTORY_DETAIL_PATTERN = "$HISTORY_DETAIL/{$HISTORY_DETAIL_ARG_ID}"

    fun stockDetail(sku: String): String = "$STOCK_DETAIL/$sku"
    fun historyDetail(transactionId: Long): String = "$HISTORY_DETAIL/$transactionId"
}

/**
 * Per-route role guards. Owner has full access; admin covers scan in/out, manual input, and
 * statistics; kasir is scan-out + kasir only and never sees manual input, statistics, or settings.
 */
object RouteAccess {
    private val allowedRoles: Map<String, Set<UserRole>> = mapOf(
        Routes.DASHBOARD to UserRole.entries.toSet(),
        Routes.SCAN_IN to setOf(UserRole.OWNER, UserRole.ADMIN),
        Routes.MANUAL_INPUT to setOf(UserRole.OWNER, UserRole.ADMIN),
        Routes.MANUAL_IN to setOf(UserRole.OWNER, UserRole.ADMIN),
        Routes.MANUAL_OUT to setOf(UserRole.OWNER, UserRole.ADMIN),
        Routes.STOCK_LIST to UserRole.entries.toSet(),
        Routes.STOCK_DETAIL_PATTERN to UserRole.entries.toSet(),
        Routes.HISTORY to UserRole.entries.toSet(),
        Routes.HISTORY_DETAIL_PATTERN to UserRole.entries.toSet(),
        Routes.STATISTICS to UserRole.entries.toSet(),
        Routes.SETTINGS to setOf(UserRole.OWNER, UserRole.ADMIN),
        Routes.KASIR to UserRole.entries.toSet(),
        Routes.BRAND_LIST to setOf(UserRole.OWNER, UserRole.ADMIN),
        Routes.CATEGORY_LIST to setOf(UserRole.OWNER, UserRole.ADMIN),
    )

    fun isAllowed(route: String, role: UserRole): Boolean = allowedRoles[route]?.contains(role) ?: true
}
