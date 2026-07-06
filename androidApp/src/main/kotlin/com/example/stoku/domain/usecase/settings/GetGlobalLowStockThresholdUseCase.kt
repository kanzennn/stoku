package com.example.stoku.domain.usecase.settings

import com.example.stoku.data.local.AppPreferences
import kotlinx.coroutines.flow.Flow

class GetGlobalLowStockThresholdUseCase(
    private val appPreferences: AppPreferences,
) {
    operator fun invoke(): Flow<Int> = appPreferences.globalLowStockThreshold
}
