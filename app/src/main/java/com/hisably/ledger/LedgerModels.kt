package com.hisably.ledger

data class Customer(
    val id: Long,
    val name: String,
    val mobile: String,
    val openingBalance: Double
)

data class LedgerTransaction(
    val id: Long,
    val customerId: Long,
    val type: String,
    val amount: Double,
    val note: String,
    val createdAt: Long
)

data class RecentTransaction(
    val customerName: String,
    val transaction: LedgerTransaction
)

data class DashboardSummary(
    val customerCount: Int,
    val totalCredit: Double,
    val totalDebit: Double,
    val balance: Double
)

data class CustomerSummary(
    val totalCredit: Double,
    val totalDebit: Double,
    val balance: Double
)
