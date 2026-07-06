package com.example.stoku.util

import java.security.MessageDigest

object PasswordHasher {
    fun sha256(password: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
