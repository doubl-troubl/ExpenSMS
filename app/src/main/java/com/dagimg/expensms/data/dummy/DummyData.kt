package com.dagimg.expensms.data.dummy

import com.dagimg.expensms.data.model.Bank
import com.dagimg.expensms.data.model.Transaction
import com.dagimg.expensms.data.model.TransactionType

object DummyData {
    // Dummy Banks
    val banks =
        listOf(
            Bank(
                id = 1,
                name = "cbe",
                displayName = "Commercial Bank of Ethiopia",
                colorHex = "#3B82F6", // Blue
                currentBalance = 18070.82,
                lastUpdated = System.currentTimeMillis(),
                smsSenderIds = listOf("CBE", "CBE-ET"),
            ),
            Bank(
                id = 2,
                name = "chase",
                displayName = "Chase",
                colorHex = "#F59E0B", // Orange
                currentBalance = 27209.68,
                lastUpdated = System.currentTimeMillis() - 86400000, // 1 day ago
                smsSenderIds = listOf("CHASE"),
            ),
        )

    // Dummy Transactions based on the screenshot
    val transactions =
        listOf(
            Transaction(
                id = 1,
                merchant = "Salary Deposit",
                amount = 3500.00,
                type = TransactionType.INCOME,
                timestamp = System.currentTimeMillis() - 32400000, // 9 hours ago (9:00 AM)
                category = "Income",
                bankName = "Chase",
                balanceAfter = 27209.68,
                note = "Monthly salary",
            ),
            Transaction(
                id = 2,
                merchant = "Whole Foods Market",
                amount = 156.42,
                type = TransactionType.EXPENSE,
                timestamp = System.currentTimeMillis() - 19800000, // 5.5 hours ago (2:30 PM)
                category = "Groceries",
                bankName = "Chase",
                balanceAfter = 26053.26,
            ),
            Transaction(
                id = 3,
                merchant = "Netflix Subscription",
                amount = 15.99,
                type = TransactionType.EXPENSE,
                timestamp = System.currentTimeMillis() - 86400000, // Yesterday
                category = "Entertainment",
                bankName = "Chase",
                balanceAfter = 26037.27,
            ),
            Transaction(
                id = 4,
                merchant = "Uber Ride",
                amount = 23.50,
                type = TransactionType.EXPENSE,
                timestamp = System.currentTimeMillis() - 86400000, // Yesterday
                category = "Transport",
                bankName = "Chase",
                balanceAfter = 26013.77,
            ),
            Transaction(
                id = 5,
                merchant = "Starbucks",
                amount = 8.75,
                type = TransactionType.EXPENSE,
                timestamp = System.currentTimeMillis() - 172800000, // 2 days ago
                category = "Food & Drink",
                bankName = "Chase",
                balanceAfter = 26005.02,
            ),
            Transaction(
                id = 6,
                merchant = "CBE Transfer",
                amount = 2011.00,
                type = TransactionType.EXPENSE,
                timestamp = System.currentTimeMillis() - 259200000, // 3 days ago
                category = "Other",
                bankName = "Commercial Bank of Ethiopia",
                balanceAfter = 18070.82,
                transactionUrl = "https://apps.cbe.com.et:100/?id=FT25283144TD96543037",
            ),
        )

    // Calculate total balance
    val totalBalance = banks.sumOf { it.currentBalance }

    // Get recent transactions (last 5)
    val recentTransactions = transactions.sortedByDescending { it.timestamp }.take(5)
}
