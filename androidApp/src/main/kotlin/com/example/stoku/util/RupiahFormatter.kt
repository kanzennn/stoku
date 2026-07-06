package com.example.stoku.util

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

/** Formats whole-Rupiah amounts as "Rp 150.000" (thousands separated by '.', no decimals). */
object RupiahFormatter {
    private val symbols = DecimalFormatSymbols(Locale("id", "ID")).apply { groupingSeparator = '.' }
    private val pattern = DecimalFormat("#,###", symbols)

    fun format(amount: Long): String = "Rp ${pattern.format(amount)}"

    fun format(amount: Int): String = format(amount.toLong())
}
