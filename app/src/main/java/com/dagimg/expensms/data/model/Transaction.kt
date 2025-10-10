package com.dagimg.expensms.data.model

data class Transaction(
    val id: Long = 0,
    val merchant: String,
    val amount: Double, // Transaction amount (total including fees)
    val type: TransactionType, // INCOME or EXPENSE
    val timestamp: Long, // Unix timestamp from SMS
    val category: String, // Auto-categorized or user-edited
    val bankName: String, // Which bank this transaction belongs to
    val balanceAfter: Double, // Remaining balance from SMS (key field!)
    val transactionUrl: String? = null, // Transaction link from SMS (clickable in app)
    val note: String = "", // Optional user note
    val rawSmsContent: String = "", // Original SMS for debugging/reference
    val smsTimestamp: Long = 0L, // When SMS was received
    val isEdited: Boolean = false, // Track if user modified transaction
    val createdAt: Long = System.currentTimeMillis(),
)
