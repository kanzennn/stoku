package com.example.stoku.data.repository

import com.example.stoku.data.local.dao.TransactionDao
import com.example.stoku.data.local.entity.TransactionEntity
import com.example.stoku.domain.model.Transaction
import com.example.stoku.domain.model.TransactionSource
import com.example.stoku.domain.model.TransactionType
import com.example.stoku.domain.repository.TransactionRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TransactionRepositoryImpl @Inject constructor(
    private val transactionDao: TransactionDao,
) : TransactionRepository {
    override fun getAll(): Flow<List<Transaction>> =
        transactionDao.getAll().map { transactions -> transactions.map { it.toDomain() } }

    override fun getBySku(sku: String): Flow<List<Transaction>> =
        transactionDao.getBySku(sku).map { transactions -> transactions.map { it.toDomain() } }

    override fun getBetween(startMillis: Long, endMillis: Long): Flow<List<Transaction>> =
        transactionDao.getBetween(startMillis, endMillis).map { transactions -> transactions.map { it.toDomain() } }

    override suspend fun insert(transaction: Transaction): Long = transactionDao.insert(transaction.toEntity())

    override suspend fun getById(id: Long): Transaction? = transactionDao.getById(id)?.toDomain()
}

private fun TransactionEntity.toDomain(): Transaction = Transaction(
    id = id,
    sku = sku,
    type = TransactionType.fromValue(type),
    source = TransactionSource.fromValue(source),
    quantity = quantity,
    costPriceSnapshot = costPriceSnapshot,
    sellingPriceSnapshot = sellingPriceSnapshot,
    notes = notes,
    userId = userId,
    createdAt = createdAt,
)

private fun Transaction.toEntity(): TransactionEntity = TransactionEntity(
    id = id,
    sku = sku,
    type = type.value,
    source = source.value,
    quantity = quantity,
    costPriceSnapshot = requireNotNull(costPriceSnapshot) {
        "costPriceSnapshot must not be null when persisting a transaction"
    },
    sellingPriceSnapshot = sellingPriceSnapshot,
    notes = notes,
    userId = userId,
    createdAt = createdAt,
)
