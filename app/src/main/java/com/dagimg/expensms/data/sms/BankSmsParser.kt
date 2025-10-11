package com.dagimg.expensms.data.sms

import com.dagimg.expensms.data.model.TransactionType

/**
 * Core interface for bank SMS parsers using Strategy Pattern
 * Each Ethiopian bank gets its own implementation
 */
interface BankSmsParser {
    val bankName: String
    val senderIds: List<String>

    /**
     * Check if this parser can handle the given SMS
     */
    fun canParse(
        sender: String,
        message: String,
    ): Boolean

    /**
     * Parse SMS and return structured transaction data
     * Returns null if parsing fails or message is not a transaction SMS
     */
    fun parse(
        sender: String,
        message: String,
    ): ParsedTransaction?
}

/**
 * Result of SMS parsing containing all extracted transaction data
 */
data class ParsedTransaction(
    val merchant: String,
    val amount: Double,
    val type: TransactionType,
    val timestamp: Long,
    val balanceAfter: Double,
    val transactionUrl: String?,
    val rawSms: String,
    val bankName: String,
    val userName: String? = null, // Extracted user name from SMS (e.g., "Dear John")
)
