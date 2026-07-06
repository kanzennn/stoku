package com.example.stoku.domain.usecase.transaction

import com.example.stoku.domain.model.Transaction
import com.example.stoku.domain.model.UserRole
import com.example.stoku.domain.model.maskFor
import com.example.stoku.domain.repository.TransactionRepository

class GetTransactionByIdUseCase(
    private val transactionRepository: TransactionRepository,
) {
    suspend operator fun invoke(id: Long, role: UserRole): Transaction? =
        transactionRepository.getById(id)?.maskFor(role)
}
