package com.example.stoku.domain.usecase.transaction

import com.example.stoku.domain.repository.TransactionRepository
import java.util.Calendar
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Just a count, so no role/cost-price masking is needed here. */
class GetTodaysTransactionCountUseCase(
    private val transactionRepository: TransactionRepository,
) {
    operator fun invoke(): Flow<Int> {
        val (startOfDay, endOfDay) = todayBounds()
        return transactionRepository.getBetween(startOfDay, endOfDay).map { it.size }
    }

    private fun todayBounds(): Pair<Long, Long> {
        val start = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val end = (start.clone() as Calendar).apply {
            add(Calendar.DAY_OF_MONTH, 1)
            add(Calendar.MILLISECOND, -1)
        }
        return start.timeInMillis to end.timeInMillis
    }
}
