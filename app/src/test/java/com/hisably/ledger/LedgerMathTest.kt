package com.hisably.ledger

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LedgerMathTest {
    @Test
    fun balanceAddsCreditAndSubtractsDebit() {
        val balance = LedgerMath.balance(openingBalance = 100.0, credit = 250.0, debit = 75.0)

        assertEquals(275.0, balance, 0.0)
    }

    @Test
    fun positiveAmountRejectsEmptyZeroAndNegativeValues() {
        assertNull(LedgerMath.positiveAmount(""))
        assertNull(LedgerMath.positiveAmount("0"))
        assertNull(LedgerMath.positiveAmount("-10"))
    }

    @Test
    fun positiveAmountAcceptsValidDecimal() {
        assertEquals(99.5, LedgerMath.positiveAmount("99.50") ?: 0.0, 0.0)
    }
}
