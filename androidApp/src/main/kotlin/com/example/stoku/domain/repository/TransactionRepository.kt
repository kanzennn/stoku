package com.example.stoku.domain.repository

import com.example.stoku.domain.model.Transaction
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {
    fun getAll(): Flow<List<Transaction>>
    fun getBySku(sku: String): Flow<List<Transaction>>
    fun getBetween(startMillis: Long, endMillis: Long): Flow<List<Transaction>>
    suspend fun insert(transaction: Transaction): Long
    suspend fun getById(id: Long): Transaction?
}
