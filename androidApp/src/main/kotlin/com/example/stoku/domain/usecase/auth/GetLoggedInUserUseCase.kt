package com.example.stoku.domain.usecase.auth

import com.example.stoku.data.local.AppPreferences
import com.example.stoku.domain.model.User
import com.example.stoku.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GetLoggedInUserUseCase(
    private val userRepository: UserRepository,
    private val appPreferences: AppPreferences,
) {
    operator fun invoke(): Flow<User?> =
        appPreferences.loggedInUserId.map { id -> id?.let { userRepository.getById(it) } }
}
