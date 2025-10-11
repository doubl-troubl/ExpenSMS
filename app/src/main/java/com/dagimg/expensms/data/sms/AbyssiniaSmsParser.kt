package com.dagimg.expensms.data.sms

import com.dagimg.expensms.data.model.TransactionType
import kotlin.text.Regex

/**
 * SMS parser for Abyssinia Bank (Bank of Abyssinia)
 * Handles credit and debit transactions using regex patterns
 */
class AbyssiniaSmsParser : BankSmsParser {
    override val bankName = "Abyssinia Bank"
    override val senderIds = listOf("BOA", "ABYSSINIA", "BANKOFABYSSINIA")

    // Regex patterns for Abyssinia Bank SMS format (case insensitive)
    private val balanceRegex = Regex("Available Balance:\\s*ETB ([\\d,]+\\.?\\d*)", RegexOption.IGNORE_CASE)
    private val creditAmountRegex = Regex("credited with ETB ([\\d,]+\\.?\\d*)", RegexOption.IGNORE_CASE)
    private val debitAmountRegex = Regex("debited with ETB ([\\d,]+\\.?\\d*)", RegexOption.IGNORE_CASE)

    // Extract merchant/sender for credits
    private val creditMerchantRegex =
        Regex(
            "credited with ETB [\\d,]+\\.?\\d* by ([^\\s]+(?:\\s+[^\\s]+)*?)\\. Available Balance:",
            RegexOption.IGNORE_CASE,
        )

    // Extract URL
    private val urlRegex = Regex("(https://cs\\.bankofabyssinia\\.com/slip/\\?trx=[^\\s]+)", RegexOption.IGNORE_CASE)

    override fun canParse(
        sender: String,
        message: String,
    ): Boolean {
        // Check sender if provided, or check message content for bank-specific patterns
        val senderMatch = sender.isBlank() || senderIds.any { sender.contains(it, ignoreCase = true) }
        val messageMatch =
            message.contains("Available Balance:", ignoreCase = true) &&
                (
                    message.contains("credited with ETB", ignoreCase = true) ||
                        message.contains("debited with ETB", ignoreCase = true)
                ) &&
                message.contains("bankofabyssinia.com", ignoreCase = true)

        return senderMatch && messageMatch
    }

    override fun parse(
        sender: String,
        message: String,
    ): ParsedTransaction? {
        println("DEBUG: AbyssiniaSmsParser.parse() called with sender='$sender', message='$message'")
        try {
            // Extract balance (required field)
            val balance = extractBalance(message)
            println("DEBUG: Extracted balance: $balance")
            if (balance == null) {
                println("DEBUG: Failed to extract balance, returning null")
                return null
            }

            // Determine transaction type and extract amount
            val type =
                when {
                    message.contains("credited", ignoreCase = true) -> TransactionType.INCOME
                    message.contains("debited", ignoreCase = true) -> TransactionType.EXPENSE
                    else -> {
                        println("DEBUG: Could not determine transaction type, returning null")
                        return null
                    }
                }

            val amount =
                when (type) {
                    TransactionType.EXPENSE -> extractDebitAmount(message)
                    TransactionType.INCOME -> extractCreditAmount(message)
                    else -> null
                }

            println("DEBUG: Extracted amount: $amount")
            if (amount == null) {
                println("DEBUG: Failed to extract amount, returning null")
                return null
            }

            // Extract merchant based on type
            val merchant =
                when (type) {
                    TransactionType.EXPENSE ->
                        "Abyssinia Bank Transfer" // For debits, merchant info might not be available
                    TransactionType.INCOME ->
                        extractCreditMerchant(message) ?: "Abyssinia Bank Deposit"
                    else -> "Abyssinia Bank Transaction"
                }

            // Extract timestamp (use current time if not available in SMS)
            val timestamp = System.currentTimeMillis()

            // Extract URL
            val url = extractUrl(message)

            println(
                "DEBUG: Parsed transaction - merchant: '$merchant', amount: $amount, type: $type, balance: $balance",
            )

            return ParsedTransaction(
                merchant = merchant,
                amount = amount,
                type = type,
                timestamp = timestamp,
                balanceAfter = balance,
                transactionUrl = url,
                rawSms = message,
                bankName = bankName,
            )
        } catch (e: Exception) {
            // Log error and return null
            println("DEBUG: Exception during parsing: ${e.message}")
            e.printStackTrace()
            return null
        }
    }

    private fun extractBalance(message: String): Double? {
        val result =
            balanceRegex
                .find(message)
                ?.groupValues
                ?.get(1)
                ?.replace(",", "")
                ?.toDoubleOrNull()
        println("DEBUG: extractBalance('$message') = $result")
        return result
    }

    private fun extractCreditAmount(message: String): Double? {
        val result =
            creditAmountRegex
                .find(message)
                ?.groupValues
                ?.get(1)
                ?.replace(",", "")
                ?.toDoubleOrNull()
        println("DEBUG: extractCreditAmount('$message') = $result")
        return result
    }

    private fun extractDebitAmount(message: String): Double? {
        val result =
            debitAmountRegex
                .find(message)
                ?.groupValues
                ?.get(1)
                ?.replace(",", "")
                ?.toDoubleOrNull()
        println("DEBUG: extractDebitAmount('$message') = $result")
        return result
    }

    private fun extractCreditMerchant(message: String): String? =
        creditMerchantRegex
            .find(message)
            ?.groupValues
            ?.get(1)
            ?.trim()

    private fun extractUrl(message: String): String? = urlRegex.find(message)?.groupValues?.get(1)
}
