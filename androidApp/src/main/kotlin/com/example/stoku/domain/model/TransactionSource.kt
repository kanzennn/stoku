package com.example.stoku.domain.model

enum class TransactionSource(val value: String) {
    SCAN("SCAN"),
    MANUAL("MANUAL"),
    ;

    companion object {
        fun fromValue(value: String): TransactionSource = entries.first { it.value == value }
    }
}
