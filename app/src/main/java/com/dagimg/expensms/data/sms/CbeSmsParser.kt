package com.dagimg.expensms.data.sms

import com.dagimg.expensms.data.model.TransactionType
import java.text.SimpleDateFormat
import java.util.*
import kotlin.text.Regex

/**
 * SMS parser for Commercial Bank of Ethiopia (CBE)
 * Handles credit, debit, and transfer transactions
 */
class CbeSmsParser : BankSmsParser {
    override val bankName = "Commercial Bank of Ethiopia"
    override val senderIds = listOf("CBE", "CBE-ET", "CBEBIRR", "Jeremiah I")

    // Regex patterns for CBE SMS format (case insensitive)
    private val balanceRegex = Regex("Your Current Balance is ETB ([\\d,]+\\.?\\d*)", RegexOption.IGNORE_CASE)
    private val creditRegex = Regex("credited with ETB ([\\d,]+\\.?\\d*)", RegexOption.IGNORE_CASE)
    private val totalAmountRegex = Regex("total of ETB([\\d,]+\\.?\\d*)", RegexOption.IGNORE_CASE)
    private val debitRegex = Regex("debited with ETB([\\d,]+\\.?\\d*)", RegexOption.IGNORE_CASE)

    private val merchantRegex = Regex("transfered ETB [\\d,]+\\.?\\d* to ([^\\n]+?) on", RegexOption.IGNORE_CASE)
    private val dateRegex = Regex("on (\\d{2}/\\d{2}/\\d{4})", RegexOption.IGNORE_CASE)
    private val urlRegex = Regex("(https://[^\\s]+)", RegexOption.IGNORE_CASE)

    override fun canParse(
        sender: String,
        message: String,
    ): Boolean =
        senderIds.any { sender.contains(it, ignoreCase = true) } &&
            message.contains("Your Current Balance is ETB", ignoreCase = true)

    override fun parse(
        sender: String,
        message: String,
    ): ParsedTransaction? {
        println("DEBUG: CbeSmsParser.parse() called with sender='$sender', message='$message'")
        try {
            // Extract balance (required field)
            val balance = extractBalance(message)
            println("DEBUG: Extracted balance: $balance")
            if (balance == null) {
                println("DEBUG: Failed to extract balance, returning null")
                return null
            }

            // Determine transaction type
            val type =
                when {
                    message.contains("credited", ignoreCase = true) -> {
                        println("DEBUG: Detected INCOME transaction")
                        TransactionType.INCOME
                    }
                    message.contains(
                        "debited",
                        ignoreCase = true,
                    ) ||
                        message.contains("transfered", ignoreCase = true) -> {
                        println("DEBUG: Detected EXPENSE transaction")
                        TransactionType.EXPENSE
                    }
                    else -> {
                        println("DEBUG: Could not determine transaction type, returning null")
                        return null
                    }
                }

            // Extract amount based on type
            val amount =
                when (type) {
                    TransactionType.INCOME -> extractCreditAmount(message)
                    TransactionType.EXPENSE -> extractDebitAmount(message)
                }
            println("DEBUG: Extracted amount: $amount")
            if (amount == null) {
                println("DEBUG: Failed to extract amount, returning null")
                return null
            }

            // Extract optional fields
            val merchant = extractMerchant(message) ?: "Unknown"
            val timestamp = extractDate(message) ?: System.currentTimeMillis()
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
            creditRegex
                .find(message)
                ?.groupValues
                ?.get(1)
                ?.replace(",", "")
                ?.toDoubleOrNull()
        println("DEBUG: extractCreditAmount('$message') = $result")
        return result
    }

    private fun extractDebitAmount(message: String): Double? {
        // Prioritize "total" amount (includes fees)
        val totalResult =
            totalAmountRegex
                .find(message)
                ?.groupValues
                ?.get(1)
                ?.replace(",", "")
                ?.toDoubleOrNull()

        if (totalResult != null) {
            println("DEBUG: extractDebitAmount('$message') - using total amount: $totalResult")
            return totalResult
        }

        // Fallback to initial debit amount
        val debitResult =
            debitRegex
                .find(message)
                ?.groupValues
                ?.get(1)
                ?.replace(",", "")
                ?.toDoubleOrNull()

        println("DEBUG: extractDebitAmount('$message') - using debit amount: $debitResult")
        return debitResult
    }

    private fun extractMerchant(message: String): String? =
        merchantRegex
            .find(message)
            ?.groupValues
            ?.get(1)
            ?.trim()

    private fun extractDate(message: String): Long? {
        val dateStr = dateRegex.find(message)?.groupValues?.get(1) ?: return null
        return try {
            // Parse Ethiopian date format (DD/MM/YYYY)
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            sdf.parse(dateStr)?.time
        } catch (e: Exception) {
            null
        }
    }

    private fun extractUrl(message: String): String? = urlRegex.find(message)?.groupValues?.get(1)
}
