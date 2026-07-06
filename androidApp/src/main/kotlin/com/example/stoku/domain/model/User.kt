package com.example.stoku.domain.model

/** Never carries the password hash — that detail stays in the data layer. */
data class User(
    val id: Long,
    val username: String,
    val role: UserRole,
)
