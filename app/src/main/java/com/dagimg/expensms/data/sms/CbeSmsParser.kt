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
    override val senderIds = listOf("CBE", "CBE-ET", "CBEBIRR")

    // Regex patterns for CBE SMS format
    private val balanceRegex = Regex("Your Current Balance is ETB ([\\d,]+\\.?\\d*)")
    private val creditRegex = Regex("credited with ETB ([\\d,]+\\.?\\d*)")
    private val totalAmountRegex = Regex("total of ETB([\\d,]+\\.?\\d*)")
    private val debitRegex = Regex("debited with ETB([\\d,]+\\.?\\d*)")
    private val merchantRegex = Regex("transfered ETB [\\d,]+\\.?\\d* to ([^\\n]+?) on")
    private val dateRegex = Regex("on (\\d{2}/\\d{2}/\\d{4})")
    private val urlRegex = Regex("(https://[^\\s]+)")

    override fun canParse(
        sender: String,
        message: String,
    ): Boolean =
        senderIds.any { sender.contains(it, ignoreCase = true) } &&
            message.contains("Your Current Balance is ETB")

    override fun parse(
        sender: String,
        message: String,
    ): ParsedTransaction? {
        try {
            // Extract balance (required field)
            val balance = extractBalance(message) ?: return null

            // Determine transaction type
            val type =
                when {
                    message.contains("credited") -> TransactionType.INCOME
                    message.contains("debited") || message.contains("transfered") -> TransactionType.EXPENSE
                    else -> return null
                }

            // Extract amount based on type
            val amount =
                when (type) {
                    TransactionType.INCOME -> extractCreditAmount(message)
                    TransactionType.EXPENSE -> extractDebitAmount(message)
                } ?: return null

            // Extract optional fields
            val merchant = extractMerchant(message) ?: "Unknown"
            val timestamp = extractDate(message) ?: System.currentTimeMillis()
            val url = extractUrl(message)

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
            return null
        }
    }

    private fun extractBalance(message: String): Double? =
        balanceRegex
            .find(message)
            ?.groupValues
            ?.get(1)
            ?.replace(",", "")
            ?.toDoubleOrNull()

    private fun extractCreditAmount(message: String): Double? =
        creditRegex
            .find(message)
            ?.groupValues
            ?.get(1)
            ?.replace(",", "")
            ?.toDoubleOrNull()

    private fun extractDebitAmount(message: String): Double? {
        // Prioritize "total" amount (includes fees)
        totalAmountRegex
            .find(message)
            ?.groupValues
            ?.get(1)
            ?.replace(",", "")
            ?.toDoubleOrNull()
            ?.let { return it }

        // Fallback to initial debit amount
        return debitRegex
            .find(message)
            ?.groupValues
            ?.get(1)
            ?.replace(",", "")
            ?.toDoubleOrNull()
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
