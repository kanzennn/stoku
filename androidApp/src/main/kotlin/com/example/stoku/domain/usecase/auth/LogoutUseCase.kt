package com.example.stoku.domain.usecase.auth

import com.example.stoku.data.local.AppPreferences

class LogoutUseCase(
    private val appPreferences: AppPreferences,
) {
    suspend operator fun invoke() {
        appPreferences.clearLoggedInUser()
    }
}
