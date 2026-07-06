package com.example.stoku.domain.repository

import com.example.stoku.domain.model.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    suspend fun verifyCredentials(username: String, passwordHash: String): User?
    suspend fun getById(id: Long): User?
    fun getAll(): Flow<List<User>>
}
