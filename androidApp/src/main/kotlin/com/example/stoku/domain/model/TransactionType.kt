package com.example.stoku.domain.model

enum class TransactionType(val value: String) {
    IN("IN"),
    OUT("OUT"),
    ;

    companion object {
        fun fromValue(value: String): TransactionType = entries.first { it.value == value }
    }
}
