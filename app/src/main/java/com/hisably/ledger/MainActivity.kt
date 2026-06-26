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
import java.util.Calendar
import java.util.Date
import java.util.Locale

class MainActivity : Activity() {
    private lateinit var database: LedgerDatabaseHelper
    private var currentScreen: Screen = Screen.SPLASH
    private var currentLedgerCustomerId: Long? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        database = LedgerDatabaseHelper(this)
        database.seedDemoDataIfEmpty()
        showSplash()
    }

    override fun onBackPressed() {
        when (currentScreen) {
            Screen.SPLASH -> Unit
            Screen.DASHBOARD -> super.onBackPressed()
            else -> showDashboard()
        }
    }

    private fun showSplash() {
        currentScreen = Screen.SPLASH
        setContentView(R.layout.activity_splash)
        Handler(Looper.getMainLooper()).postDelayed({
            showDashboard()
        }, 800)
    }

    private fun showLogin() {
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
        currentLedgerCustomerId = null
        setContentView(R.layout.screen_dashboard)

        val summary = database.getDashboardSummary()
        val customers = database.getCustomers()
        val receiveCount = customers.count { database.getCustomerSummary(it.id).balance >= 0.0 }
        val payCount = customers.count { database.getCustomerSummary(it.id).balance < 0.0 }

        find<TextView>(R.id.todayLabelText).text = "Nagpur, Maharashtra"
        find<TextView>(R.id.dashboardGreetingText).text = "Aarav Stores"
        find<TextView>(R.id.dashboardBalanceText).text = formatMoney(summary.balance)
        find<TextView>(R.id.dashboardCustomerCountText).text = "From $receiveCount customers"
        find<TextView>(R.id.dashboardPayCountText).text = "To $payCount customers"
        find<TextView>(R.id.dashboardCreditText).text = formatMoney(summary.totalCredit)
        find<TextView>(R.id.dashboardDebitText).text = formatMoney(summary.totalDebit)
        find<TextView>(R.id.weeklyInsightText).text = "You collected ${formatMoney(summary.totalDebit)} this week"

        val recentCustomers = find<LinearLayout>(R.id.recentCustomersContainer)
        recentCustomers.removeAllViews()
        customers.take(4).forEach { customer ->
            recentCustomers.addView(compactCustomerRow(customer))
        }

        find<Button>(R.id.addCustomerButton).setOnClickListener { showAddCustomer() }
        find<Button>(R.id.customerListButton).setOnClickListener { showCustomerList() }
        find<Button>(R.id.addTransactionButton).setOnClickListener { showAddTransaction() }
        find<Button>(R.id.viewReportsButton).setOnClickListener { showReports() }
        find<Button>(R.id.sendReminderButton).setOnClickListener { showReminders() }
        bindBottomNav(Screen.DASHBOARD)
    }

    private fun showAddCustomer() {
        currentScreen = Screen.ADD_CUSTOMER
        currentLedgerCustomerId = null
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
        currentLedgerCustomerId = null
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
        bindBottomNav(Screen.CUSTOMER_LIST)
    }

    private fun showReports() {
        currentScreen = Screen.REPORTS
        currentLedgerCustomerId = null
        setContentView(R.layout.screen_reports)

        val summary = database.getDashboardSummary()
        find<TextView>(R.id.reportCollectedText).text = "Payments collected\n${formatMoney(summary.totalDebit)}"
        find<TextView>(R.id.reportCreditText).text = "Credit given\n${formatMoney(summary.totalCredit)}"
        find<TextView>(R.id.reportNetText).text = "Net movement\n${formatMoney(summary.totalDebit - summary.totalCredit)}"

        renderReportChart(find(R.id.reportChartContainer))
        renderRecentTransactions(find(R.id.reportTransactionsContainer), limit = 8)
        bindBottomNav(Screen.REPORTS)
    }

    private fun showReminders() {
        currentScreen = Screen.REMINDERS
        currentLedgerCustomerId = null
        setContentView(R.layout.screen_reminders)

        val dueCustomers = database.getCustomers().filter { database.getCustomerSummary(it.id).balance > 0.0 }
        val totalDue = dueCustomers.fold(0.0) { total, customer ->
            total + database.getCustomerSummary(customer.id).balance
        }
        find<TextView>(R.id.reminderSummaryText).text = "${dueCustomers.size} pending reminders\n${formatMoney(totalDue)} due"

        val container = find<LinearLayout>(R.id.reminderListContainer)
        container.removeAllViews()
        if (dueCustomers.isEmpty()) {
            container.addView(text("No pending dues. There are no reminder-ready customers right now.", 15f, R.color.secondary_text))
        } else {
            dueCustomers.forEach { customer ->
                val balance = database.getCustomerSummary(customer.id).balance
                container.addView(reminderRow(customer, balance))
            }
        }

        bindBottomNav(Screen.REMINDERS)
    }

    private fun showSettings() {
        currentScreen = Screen.SETTINGS
        currentLedgerCustomerId = null
        setContentView(R.layout.screen_settings)

        find<TextView>(R.id.settingsSummaryText).text =
            "Settings        >\nSMS Settings        >\nPayment Settings        >\nRecycle Bin        >\nApp Lock        OFF\nLanguage        >\nBackup Information        >\nDelete Khata        >\nApp Update        >"
        bindBottomNav(Screen.SETTINGS)
    }

    private fun showAddTransaction(preselectedCustomerId: Long? = null) {
        currentScreen = Screen.ADD_TRANSACTION
        currentLedgerCustomerId = preselectedCustomerId
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
        return ledgerPartyRow(customer, summary, showSubActions = true)
    }

    private fun compactCustomerRow(customer: Customer): View {
        val summary = database.getCustomerSummary(customer.id)
        return ledgerPartyRow(customer, summary, showSubActions = false)
    }

    private fun ledgerPartyRow(customer: Customer, summary: CustomerSummary, showSubActions: Boolean): View {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = android.view.Gravity.CENTER_VERTICAL
            setBackgroundColor(getColor(R.color.surface))
            setPadding(dp(18), dp(12), dp(14), dp(12))
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        row.addView(TextView(this).apply {
            text = initials(customer.name)
            gravity = android.view.Gravity.CENTER
            setTextColor(getColor(android.R.color.white))
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            background = getDrawable(R.drawable.avatar_background)
            layoutParams = LinearLayout.LayoutParams(dp(46), dp(46))
        })

        row.addView(LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply {
                leftMargin = dp(12)
            }
            addView(text(customer.name, 18f, R.color.primary_text))
            addView(text("5 years ago", 13f, R.color.secondary_text))
        })

        val balance = summary.balance
        row.addView(LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = android.view.Gravity.END or android.view.Gravity.CENTER_VERTICAL
            layoutParams = LinearLayout.LayoutParams(dp(116), ViewGroup.LayoutParams.WRAP_CONTENT)

            addView(TextView(this@MainActivity).apply {
                text = formatMoney(balance)
                setTextColor(getColor(if (balance > 0.0) R.color.magenta else R.color.secondary_text))
                textSize = 19f
                gravity = android.view.Gravity.END
                if (balance > 0.0) {
                    setTypeface(typeface, android.graphics.Typeface.BOLD)
                }
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            })

            if (balance > 0.0) {
                addView(TextView(this@MainActivity).apply {
                    text = "REMIND >"
                    setTextColor(getColor(R.color.primary))
                    textSize = 12f
                    gravity = android.view.Gravity.END
                    setTypeface(typeface, android.graphics.Typeface.BOLD)
                    layoutParams = LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                })
            } else if (showSubActions) {
                addView(TextView(this@MainActivity).apply {
                    text = "Ledger >"
                    setTextColor(getColor(R.color.primary))
                    textSize = 12f
                    gravity = android.view.Gravity.END
                    layoutParams = LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                })
            }
        })

        row.setOnClickListener { showLedger(customer.id) }
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            addView(row)
            addView(View(this@MainActivity).apply {
                setBackgroundColor(getColor(R.color.divider))
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    dp(1)
                ).apply {
                    leftMargin = dp(76)
                }
            })
        }
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

    private fun reminderRow(customer: Customer, amount: Double): View {
        val row = panelLayout()
        row.addView(text(customer.name, 16f, R.color.primary_text, bold = true))
        row.addView(text("+91 ${customer.mobile}", 13f, R.color.secondary_text))
        row.addView(text("${formatMoney(amount)} due", 18f, R.color.success, bold = true))
        row.addView(text("Send reminder via WhatsApp or SMS", 13f, R.color.primary))
        return row
    }

    private fun renderRecentTransactions(container: LinearLayout, limit: Int) {
        container.removeAllViews()
        val recent = database.getRecentTransactions(limit)
        if (recent.isEmpty()) {
            container.addView(text("No transactions yet.", 15f, R.color.secondary_text))
        } else {
            recent.forEach { item ->
                val transaction = item.transaction
                container.addView(panelLayout().apply {
                    addView(text(item.customerName, 15f, R.color.primary_text, bold = true))
                    addView(text("${typeLabel(transaction.type)} ${formatMoney(transaction.amount)}", 18f, if (transaction.type == TYPE_CREDIT) R.color.coral else R.color.success, bold = true))
                    val note = if (transaction.note.isBlank()) "Ledger entry" else transaction.note
                    addView(text("$note - ${formatDate(transaction.createdAt)}", 12f, R.color.secondary_text))
                })
            }
        }
    }

    private fun renderReportChart(container: LinearLayout) {
        container.removeAllViews()
        val values = listOf(40 to 62, 58 to 48, 47 to 76, 75 to 55, 65 to 88, 80 to 71, 48 to 79)
        val days = listOf("M", "T", "W", "T", "F", "S", "S")
        values.forEachIndexed { index, pair ->
            val group = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = android.view.Gravity.BOTTOM or android.view.Gravity.CENTER_HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f)
            }
            val bars = LinearLayout(this).apply {
                gravity = android.view.Gravity.BOTTOM or android.view.Gravity.CENTER_HORIZONTAL
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, 0, 1f)
            }
            bars.addView(bar(pair.first, R.color.coral))
            bars.addView(bar(pair.second, R.color.primary))
            group.addView(bars)
            group.addView(text(days[index], 10f, R.color.secondary_text).apply {
                gravity = android.view.Gravity.CENTER
            })
            container.addView(group)
        }
    }

    private fun bar(percent: Int, colorRes: Int): View {
        return View(this).apply {
            setBackgroundColor(getColor(colorRes))
            layoutParams = LinearLayout.LayoutParams(dp(10), dp((percent * 1.35).toInt())).apply {
                leftMargin = dp(2)
                rightMargin = dp(2)
            }
        }
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
        val prefix = if (amount < 0.0) "-₹" else "₹"
        return prefix + String.format(Locale.US, "%,.0f", kotlin.math.abs(amount))
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

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }

    private fun todayLabel(): String {
        val calendar = Calendar.getInstance()
        return SimpleDateFormat("EEEE, d MMMM", Locale.US).format(calendar.time).uppercase(Locale.US)
    }

    private fun initials(name: String): String {
        val value = name.trim()
            .split(Regex("\\s+"))
            .take(2)
            .mapNotNull { it.firstOrNull()?.toString() }
            .joinToString("")
            .uppercase(Locale.US)
        return if (value.isBlank()) "AS" else value
    }

    private fun bindBottomNav(active: Screen) {
        val activeColor = getColor(R.color.primary)
        val mutedColor = getColor(R.color.secondary_text)
        val buttons = listOf(
            R.id.navHomeButton to Screen.DASHBOARD,
            R.id.navCustomersButton to Screen.CUSTOMER_LIST,
            R.id.navReportsButton to Screen.REPORTS,
            R.id.navSettingsButton to Screen.SETTINGS
        )
        buttons.forEach { (id, screen) ->
            val isActive = screen == active || (id == R.id.navHomeButton && active == Screen.CUSTOMER_LIST)
            find<Button>(id).setTextColor(if (isActive) activeColor else mutedColor)
        }
        find<Button>(R.id.navHomeButton).setOnClickListener { showDashboard() }
        find<Button>(R.id.navCustomersButton).setOnClickListener { showCustomerList() }
        find<Button>(R.id.navEntryButton).setOnClickListener { showAddTransaction() }
        find<Button>(R.id.navReportsButton).setOnClickListener { showReports() }
        find<Button>(R.id.navSettingsButton).setOnClickListener { showSettings() }
    }

    private fun toast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun <T : View> find(id: Int): T = findViewById(id)

    private enum class Screen {
        SPLASH,
        DASHBOARD,
        ADD_CUSTOMER,
        CUSTOMER_LIST,
        ADD_TRANSACTION,
        LEDGER,
        REPORTS,
        REMINDERS,
        SETTINGS
    }

    companion object {
        private const val TYPE_CREDIT = "CREDIT"
        private const val TYPE_DEBIT = "DEBIT"
        private const val KEY_LOGGED_IN = "KEY_LOGGED_IN"
    }
}
