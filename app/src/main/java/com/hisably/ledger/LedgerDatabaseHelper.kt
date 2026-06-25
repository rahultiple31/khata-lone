package com.hisably.ledger

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class LedgerDatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE customers (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                mobile TEXT NOT NULL,
                opening_balance REAL NOT NULL DEFAULT 0,
                created_at INTEGER NOT NULL
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE transactions (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                customer_id INTEGER NOT NULL,
                type TEXT NOT NULL,
                amount REAL NOT NULL,
                note TEXT NOT NULL DEFAULT '',
                created_at INTEGER NOT NULL,
                FOREIGN KEY(customer_id) REFERENCES customers(id) ON DELETE CASCADE
            )
            """.trimIndent()
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS transactions")
        db.execSQL("DROP TABLE IF EXISTS customers")
        onCreate(db)
    }

    fun addCustomer(name: String, mobile: String, openingBalance: Double): Long {
        val values = ContentValues().apply {
            put("name", name)
            put("mobile", mobile)
            put("opening_balance", openingBalance)
            put("created_at", System.currentTimeMillis())
        }
        return writableDatabase.insert("customers", null, values)
    }

    fun getCustomers(): List<Customer> {
        val customers = mutableListOf<Customer>()
        readableDatabase.rawQuery(
            "SELECT id, name, mobile, opening_balance FROM customers ORDER BY name COLLATE NOCASE",
            null
        ).use { cursor ->
            while (cursor.moveToNext()) {
                customers.add(
                    Customer(
                        id = cursor.longValue("id"),
                        name = cursor.stringValue("name"),
                        mobile = cursor.stringValue("mobile"),
                        openingBalance = cursor.doubleValue("opening_balance")
                    )
                )
            }
        }
        return customers
    }

    fun addTransaction(customerId: Long, type: String, amount: Double, note: String): Long {
        val values = ContentValues().apply {
            put("customer_id", customerId)
            put("type", type)
            put("amount", amount)
            put("note", note)
            put("created_at", System.currentTimeMillis())
        }
        return writableDatabase.insert("transactions", null, values)
    }

    fun getTransactions(customerId: Long): List<LedgerTransaction> {
        val transactions = mutableListOf<LedgerTransaction>()
        readableDatabase.rawQuery(
            """
            SELECT id, customer_id, type, amount, note, created_at
            FROM transactions
            WHERE customer_id = ?
            ORDER BY created_at DESC
            """.trimIndent(),
            arrayOf(customerId.toString())
        ).use { cursor ->
            while (cursor.moveToNext()) {
                transactions.add(cursor.toTransaction())
            }
        }
        return transactions
    }

    fun getRecentTransactions(limit: Int): List<RecentTransaction> {
        val recent = mutableListOf<RecentTransaction>()
        readableDatabase.rawQuery(
            """
            SELECT transactions.id, transactions.customer_id, transactions.type, transactions.amount,
                   transactions.note, transactions.created_at, customers.name AS customer_name
            FROM transactions
            INNER JOIN customers ON customers.id = transactions.customer_id
            ORDER BY transactions.created_at DESC
            LIMIT ?
            """.trimIndent(),
            arrayOf(limit.toString())
        ).use { cursor ->
            while (cursor.moveToNext()) {
                recent.add(
                    RecentTransaction(
                        customerName = cursor.stringValue("customer_name"),
                        transaction = cursor.toTransaction()
                    )
                )
            }
        }
        return recent
    }

    fun getDashboardSummary(): DashboardSummary {
        readableDatabase.rawQuery(
            """
            SELECT
                (SELECT COUNT(*) FROM customers) AS customer_count,
                COALESCE((SELECT SUM(opening_balance) FROM customers), 0) AS opening_total,
                COALESCE((SELECT SUM(amount) FROM transactions WHERE type = 'CREDIT'), 0) AS credit_total,
                COALESCE((SELECT SUM(amount) FROM transactions WHERE type = 'DEBIT'), 0) AS debit_total
            """.trimIndent(),
            null
        ).use { cursor ->
            cursor.moveToFirst()
            val credit = cursor.doubleValue("credit_total")
            val debit = cursor.doubleValue("debit_total")
            val opening = cursor.doubleValue("opening_total")
            return DashboardSummary(
                customerCount = cursor.intValue("customer_count"),
                totalCredit = credit + opening,
                totalDebit = debit,
                balance = LedgerMath.balance(opening, credit, debit)
            )
        }
    }

    fun getCustomerSummary(customerId: Long): CustomerSummary {
        readableDatabase.rawQuery(
            """
            SELECT
                customers.opening_balance AS opening_balance,
                COALESCE(SUM(CASE WHEN transactions.type = 'CREDIT' THEN transactions.amount ELSE 0 END), 0) AS credit_total,
                COALESCE(SUM(CASE WHEN transactions.type = 'DEBIT' THEN transactions.amount ELSE 0 END), 0) AS debit_total
            FROM customers
            LEFT JOIN transactions ON transactions.customer_id = customers.id
            WHERE customers.id = ?
            GROUP BY customers.id
            """.trimIndent(),
            arrayOf(customerId.toString())
        ).use { cursor ->
            if (!cursor.moveToFirst()) {
                return CustomerSummary(0.0, 0.0, 0.0)
            }
            val opening = cursor.doubleValue("opening_balance")
            val credit = cursor.doubleValue("credit_total")
            val debit = cursor.doubleValue("debit_total")
            return CustomerSummary(
                totalCredit = credit + opening,
                totalDebit = debit,
                balance = LedgerMath.balance(opening, credit, debit)
            )
        }
    }

    private fun Cursor.toTransaction(): LedgerTransaction {
        return LedgerTransaction(
            id = longValue("id"),
            customerId = longValue("customer_id"),
            type = stringValue("type"),
            amount = doubleValue("amount"),
            note = stringValue("note"),
            createdAt = longValue("created_at")
        )
    }

    private fun Cursor.stringValue(name: String): String = getString(getColumnIndexOrThrow(name))

    private fun Cursor.longValue(name: String): Long = getLong(getColumnIndexOrThrow(name))

    private fun Cursor.intValue(name: String): Int = getInt(getColumnIndexOrThrow(name))

    private fun Cursor.doubleValue(name: String): Double = getDouble(getColumnIndexOrThrow(name))

    companion object {
        private const val DATABASE_NAME = "hisably_ledger.db"
        private const val DATABASE_VERSION = 1
    }
}
