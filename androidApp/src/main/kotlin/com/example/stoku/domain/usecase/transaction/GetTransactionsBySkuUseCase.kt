package com.example.stoku.domain.usecase.transaction

import com.example.stoku.domain.model.Transaction
import com.example.stoku.domain.model.UserRole
import com.example.stoku.domain.model.maskFor
import com.example.stoku.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GetTransactionsBySkuUseCase(
    private val transactionRepository: TransactionRepository,
) {
    operator fun invoke(sku: String, role: UserRole): Flow<List<Transaction>> =
        transactionRepository.getBySku(sku).map { transactions -> transactions.map { it.maskFor(role) } }
}
