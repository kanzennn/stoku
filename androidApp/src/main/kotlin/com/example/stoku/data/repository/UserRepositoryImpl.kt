package com.example.stoku.data.repository

import com.example.stoku.data.local.dao.UserDao
import com.example.stoku.data.local.entity.UserEntity
import com.example.stoku.domain.model.User
import com.example.stoku.domain.model.UserRole
import com.example.stoku.domain.repository.UserRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UserRepositoryImpl @Inject constructor(
    private val userDao: UserDao,
) : UserRepository {
    override suspend fun verifyCredentials(username: String, passwordHash: String): User? {
        val entity = userDao.getByUsername(username) ?: return null
        return entity.takeIf { it.passwordHash == passwordHash }?.toDomain()
    }

    override suspend fun getById(id: Long): User? = userDao.getById(id)?.toDomain()

    override fun getAll(): Flow<List<User>> = userDao.getAll().map { users -> users.map { it.toDomain() } }
}

private fun UserEntity.toDomain(): User = User(
    id = id,
    username = username,
    role = UserRole.fromValue(role),
)
