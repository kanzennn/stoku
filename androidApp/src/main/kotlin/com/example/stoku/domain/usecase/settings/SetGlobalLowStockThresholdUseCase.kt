package com.example.stoku.domain.usecase.settings

import com.example.stoku.data.local.AppPreferences
import com.example.stoku.domain.model.UnauthorizedActionException
import com.example.stoku.domain.model.UserRole

class SetGlobalLowStockThresholdUseCase(
    private val appPreferences: AppPreferences,
) {
    suspend operator fun invoke(role: UserRole, threshold: Int): Result<Unit> {
        if (role == UserRole.KASIR) {
            return Result.failure(UnauthorizedActionException("Kasir cannot change settings"))
        }
        if (threshold <= 0) {
            return Result.failure(IllegalArgumentException("Threshold must be greater than zero"))
        }
        return runCatching { appPreferences.setGlobalLowStockThreshold(threshold) }
    }
}
