package com.hisably.ledger

import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : Activity() {
    private lateinit var database: LedgerDatabaseHelper
    private var currentScreen: Screen = Screen.SPLASH
    private var currentLedgerCustomerId: Long? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        database = LedgerDatabaseHelper(this)
        showSplash()
    }

    override fun onBackPressed() {
        when (currentScreen) {
            Screen.SPLASH -> Unit
            Screen.LOGIN -> super.onBackPressed()
            Screen.DASHBOARD -> super.onBackPressed()
            else -> showDashboard()
        }
    }

    private fun showSplash() {
        currentScreen = Screen.SPLASH
        setContentView(R.layout.activity_splash)
        Handler(Looper.getMainLooper()).postDelayed({
            if (isLoggedIn()) {
                showDashboard()
            } else {
                showLogin()
            }
        }, 800)
    }

    private fun showLogin() {
        currentScreen = Screen.LOGIN
        setContentView(R.layout.screen_login)

        find<Button>(R.id.loginButton).setOnClickListener {
            val mobile = find<EditText>(R.id.loginMobileInput).text.toString().trim()
            val password = find<EditText>(R.id.loginPasswordInput).text.toString().trim()

            if (mobile.isBlank() || password.isBlank()) {
                toast("Enter mobile number and password")
                return@setOnClickListener
            }

            getPreferences(MODE_PRIVATE).edit().putBoolean(KEY_LOGGED_IN, true).apply()
            showDashboard()
        }
    }

    private fun showDashboard() {
        currentScreen = Screen.DASHBOARD
        setContentView(R.layout.screen_dashboard)

        val summary = database.getDashboardSummary()
        find<TextView>(R.id.dashboardBalanceText).text = "Balance: ${formatMoney(summary.balance)}"
        find<TextView>(R.id.dashboardCustomerCountText).text = "${summary.customerCount} customers"
        find<TextView>(R.id.dashboardCreditText).text = "Credit\n${formatMoney(summary.totalCredit)}"
        find<TextView>(R.id.dashboardDebitText).text = "Debit\n${formatMoney(summary.totalDebit)}"

        val recent = database.getRecentTransactions(limit = 5)
        find<TextView>(R.id.recentTransactionsText).text = if (recent.isEmpty()) {
            "No transactions yet. Add a customer, then add credit or debit entries."
        } else {
            recent.joinToString(separator = "\n\n") { item ->
                val transaction = item.transaction
                "${item.customerName}\n${typeLabel(transaction.type)} ${formatMoney(transaction.amount)} - ${formatDate(transaction.createdAt)}${noteSuffix(transaction.note)}"
            }
        }

        find<Button>(R.id.addCustomerButton).setOnClickListener { showAddCustomer() }
        find<Button>(R.id.customerListButton).setOnClickListener { showCustomerList() }
        find<Button>(R.id.addTransactionButton).setOnClickListener { showAddTransaction() }
        find<Button>(R.id.viewLedgerButton).setOnClickListener { showLedger() }
        find<Button>(R.id.logoutButton).setOnClickListener {
            getPreferences(MODE_PRIVATE).edit().putBoolean(KEY_LOGGED_IN, false).apply()
            showLogin()
        }
    }

    private fun showAddCustomer() {
        currentScreen = Screen.ADD_CUSTOMER
        setContentView(R.layout.screen_add_customer)

        find<Button>(R.id.saveCustomerButton).setOnClickListener {
            val name = find<EditText>(R.id.customerNameInput).text.toString().trim()
            val mobile = find<EditText>(R.id.customerMobileInput).text.toString().trim()
            val openingBalanceText = find<EditText>(R.id.openingBalanceInput).text.toString().trim()
            val openingBalance = if (openingBalanceText.isBlank()) {
                0.0
            } else {
                openingBalanceText.toDoubleOrNull()
            }

            when {
                name.isBlank() -> toast("Enter customer name")
                mobile.isBlank() -> toast("Enter mobile number")
                openingBalance == null || openingBalance < 0.0 -> toast("Opening balance must be 0 or more")
                else -> {
                    database.addCustomer(name, mobile, openingBalance)
                    toast("Customer saved")
                    showCustomerList()
                }
            }
        }

        find<Button>(R.id.cancelCustomerButton).setOnClickListener { showDashboard() }
    }

    private fun showCustomerList() {
        currentScreen = Screen.CUSTOMER_LIST
        setContentView(R.layout.screen_customer_list)

        val customers = database.getCustomers()
        val emptyText = find<TextView>(R.id.customerListEmptyText)
        val container = find<LinearLayout>(R.id.customerListContainer)
        container.removeAllViews()

        if (customers.isEmpty()) {
            emptyText.visibility = View.VISIBLE
            emptyText.text = "No customers yet. Add your first customer to start the ledger."
        } else {
            emptyText.visibility = View.GONE
            customers.forEach { customer ->
                container.addView(customerRow(customer))
            }
        }

        find<Button>(R.id.addCustomerFromListButton).setOnClickListener { showAddCustomer() }
        find<Button>(R.id.backFromCustomersButton).setOnClickListener { showDashboard() }
    }

    private fun showAddTransaction(preselectedCustomerId: Long? = null) {
        currentScreen = Screen.ADD_TRANSACTION
        val customers = database.getCustomers()
        if (customers.isEmpty()) {
            toast("Add a customer first")
            showAddCustomer()
            return
        }

        setContentView(R.layout.screen_add_transaction)
        val spinner = find<Spinner>(R.id.transactionCustomerSpinner)
        bindCustomerSpinner(spinner, customers)
        selectCustomer(spinner, customers, preselectedCustomerId)

        find<Button>(R.id.saveTransactionButton).setOnClickListener {
            val selectedCustomer = customers.getOrNull(spinner.selectedItemPosition)
            val amount = LedgerMath.positiveAmount(find<EditText>(R.id.transactionAmountInput).text.toString())
            val note = find<EditText>(R.id.transactionNoteInput).text.toString().trim()
            val type = if (find<RadioButton>(R.id.creditRadio).isChecked) TYPE_CREDIT else TYPE_DEBIT

            when {
                selectedCustomer == null -> toast("Select customer")
                amount == null -> toast("Enter valid amount")
                else -> {
                    database.addTransaction(selectedCustomer.id, type, amount, note)
                    toast("${typeLabel(type)} saved")
                    showLedger(selectedCustomer.id)
                }
            }
        }

        find<Button>(R.id.cancelTransactionButton).setOnClickListener {
            currentLedgerCustomerId?.let { customerId -> showLedger(customerId) } ?: showDashboard()
        }
    }

    private fun showLedger(preselectedCustomerId: Long? = null) {
        currentScreen = Screen.LEDGER
        val customers = database.getCustomers()
        if (customers.isEmpty()) {
            toast("Add a customer first")
            showAddCustomer()
            return
        }

        setContentView(R.layout.screen_ledger)
        val spinner = find<Spinner>(R.id.ledgerCustomerSpinner)
        bindCustomerSpinner(spinner, customers)

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val customer = customers.getOrNull(position) ?: return
                currentLedgerCustomerId = customer.id
                renderLedger(customer)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
        }

        selectCustomer(spinner, customers, preselectedCustomerId)

        find<Button>(R.id.addTransactionFromLedgerButton).setOnClickListener {
            showAddTransaction(currentLedgerCustomerId)
        }
        find<Button>(R.id.backFromLedgerButton).setOnClickListener { showDashboard() }
    }

    private fun renderLedger(customer: Customer) {
        val summary = database.getCustomerSummary(customer.id)
        val transactions = database.getTransactions(customer.id)
        val container = find<LinearLayout>(R.id.ledgerTransactionsContainer)
        val emptyText = find<TextView>(R.id.ledgerEmptyText)

        find<TextView>(R.id.ledgerNameText).text = customer.name
        find<TextView>(R.id.ledgerMobileText).text = customer.mobile
        find<TextView>(R.id.ledgerCreditText).text = "Credit\n${formatMoney(summary.totalCredit)}"
        find<TextView>(R.id.ledgerDebitText).text = "Debit\n${formatMoney(summary.totalDebit)}"
        find<TextView>(R.id.ledgerBalanceText).text = "Balance: ${formatMoney(summary.balance)}"

        container.removeAllViews()
        if (transactions.isEmpty()) {
            emptyText.visibility = View.VISIBLE
            emptyText.text = "No transactions for this customer yet."
        } else {
            emptyText.visibility = View.GONE
            transactions.forEach { transaction ->
                container.addView(transactionRow(transaction))
            }
        }
    }

    private fun bindCustomerSpinner(spinner: Spinner, customers: List<Customer>) {
        val labels = customers.map { "${it.name} - ${it.mobile}" }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, labels)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
    }

    private fun selectCustomer(spinner: Spinner, customers: List<Customer>, customerId: Long?) {
        val index = customers.indexOfFirst { it.id == customerId }
        if (index >= 0) {
            spinner.setSelection(index)
        }
    }

    private fun customerRow(customer: Customer): View {
        val summary = database.getCustomerSummary(customer.id)
        val row = panelLayout()

        row.addView(text(customer.name, 18f, R.color.primary_text, bold = true))
        row.addView(text("Mobile: ${customer.mobile}", 14f, R.color.secondary_text))
        row.addView(text("Balance: ${formatMoney(summary.balance)}", 16f, R.color.primary, bold = true))
        row.addView(text("Credit: ${formatMoney(summary.totalCredit)}    Debit: ${formatMoney(summary.totalDebit)}", 14f, R.color.secondary_text))

        val actions = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(12)
            }
        }

        actions.addView(actionButton("Add entry", primary = true).apply {
            setOnClickListener { showAddTransaction(customer.id) }
        })
        actions.addView(actionButton("Ledger", primary = false).apply {
            setOnClickListener { showLedger(customer.id) }
        })
        row.addView(actions)

        return row
    }

    private fun transactionRow(transaction: LedgerTransaction): View {
        val row = panelLayout()
        val color = if (transaction.type == TYPE_CREDIT) R.color.success else R.color.danger

        row.addView(text(typeLabel(transaction.type), 16f, color, bold = true))
        row.addView(text(formatMoney(transaction.amount), 20f, color, bold = true))
        row.addView(text(formatDate(transaction.createdAt), 13f, R.color.secondary_text))
        if (transaction.note.isNotBlank()) {
            row.addView(text(transaction.note, 14f, R.color.primary_text))
        }

        return row
    }

    private fun panelLayout(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = getDrawable(R.drawable.panel_background)
            setPadding(dp(14), dp(14), dp(14), dp(14))
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(10)
            }
        }
    }

    private fun text(value: String, size: Float, colorRes: Int, bold: Boolean = false): TextView {
        return TextView(this).apply {
            text = value
            textSize = size
            setTextColor(getColor(colorRes))
            if (bold) {
                setTypeface(typeface, android.graphics.Typeface.BOLD)
            }
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(4)
            }
        }
    }

    private fun actionButton(label: String, primary: Boolean): Button {
        return Button(this).apply {
            text = label
            setTextColor(getColor(if (primary) android.R.color.white else R.color.primary))
            background = getDrawable(if (primary) R.drawable.primary_button_background else R.drawable.secondary_button_background)
            layoutParams = LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1f
            ).apply {
                rightMargin = if (primary) dp(6) else 0
                leftMargin = if (primary) 0 else dp(6)
            }
        }
    }

    private fun formatMoney(amount: Double): String {
        return String.format(Locale.US, "Rs. %.2f", amount)
    }

    private fun formatDate(timestamp: Long): String {
        return SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.US).format(Date(timestamp))
    }

    private fun typeLabel(type: String): String {
        return if (type == TYPE_CREDIT) "Credit" else "Debit"
    }

    private fun noteSuffix(note: String): String {
        return if (note.isBlank()) "" else "\n$note"
    }

    private fun isLoggedIn(): Boolean {
        return getPreferences(MODE_PRIVATE).getBoolean(KEY_LOGGED_IN, false)
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }

    private fun toast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun <T : View> find(id: Int): T = findViewById(id)

    private enum class Screen {
        SPLASH,
        LOGIN,
        DASHBOARD,
        ADD_CUSTOMER,
        CUSTOMER_LIST,
        ADD_TRANSACTION,
        LEDGER
    }

    companion object {
        private const val KEY_LOGGED_IN = "logged_in"
        private const val TYPE_CREDIT = "CREDIT"
        private const val TYPE_DEBIT = "DEBIT"
    }
}
