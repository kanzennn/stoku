package com.example.stoku.domain.usecase.auth

import com.example.stoku.data.local.AppPreferences
import com.example.stoku.domain.model.InvalidCredentialsException
import com.example.stoku.domain.model.User
import com.example.stoku.domain.repository.UserRepository
import com.example.stoku.util.PasswordHasher

class LoginUseCase(
    private val userRepository: UserRepository,
    private val appPreferences: AppPreferences,
) {
    suspend operator fun invoke(username: String, password: String): Result<User> {
        val hashedPassword = PasswordHasher.sha256(password)
        val user = userRepository.verifyCredentials(username, hashedPassword)
            ?: return Result.failure(InvalidCredentialsException("Invalid username or password"))
        appPreferences.setLoggedInUser(user.id, user.role)
        return Result.success(user)
    }
}
