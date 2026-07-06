package com.example.stoku.domain.usecase.transaction

import com.example.stoku.domain.model.Transaction
import com.example.stoku.domain.model.UserRole
import com.example.stoku.domain.model.maskFor
import com.example.stoku.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GetTransactionHistoryUseCase(
    private val transactionRepository: TransactionRepository,
) {
    operator fun invoke(role: UserRole): Flow<List<Transaction>> =
        transactionRepository.getAll().map { transactions -> transactions.map { it.maskFor(role) } }
}
