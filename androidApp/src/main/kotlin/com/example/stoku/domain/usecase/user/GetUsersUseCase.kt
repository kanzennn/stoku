package com.example.stoku.domain.usecase.user

import com.example.stoku.domain.model.User
import com.example.stoku.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow

/** Used by History's user filter dropdown. */
class GetUsersUseCase(
    private val userRepository: UserRepository,
) {
    operator fun invoke(): Flow<List<User>> = userRepository.getAll()
}
