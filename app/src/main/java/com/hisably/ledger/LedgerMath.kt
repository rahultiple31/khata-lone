package com.hisably.ledger

object LedgerMath {
    fun balance(openingBalance: Double, credit: Double, debit: Double): Double {
        return openingBalance + credit - debit
    }

    fun positiveAmount(value: String): Double? {
        val amount = value.trim().toDoubleOrNull()
        return amount?.takeIf { it > 0.0 }
    }
}
