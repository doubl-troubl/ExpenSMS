package com.dagimg.expensms.data.sms

import com.dagimg.expensms.data.model.TransactionType
import java.text.SimpleDateFormat
import java.util.*
import kotlin.text.Regex

/**
 * SMS parser for Telebirr (Ethio Telecom mobile money service)
 * Handles credit and debit transactions using regex patterns
 */
class TelebirrSmsParser : BankSmsParser {
    override val bankName = "Telebirr"
    override val senderIds = listOf("127", "Telebirr", "ETHIOTELECOM")

    // Regex patterns for Telebirr SMS format (case insensitive)
    private val balanceRegex =
        Regex("Your current(?: E-Money Account)?\\s+balance is ETB ([\\d,]+\\.?\\d*)", RegexOption.IGNORE_CASE)
    private val creditAmountRegex = Regex("received\\s+ETB ([\\d,]+\\.?\\d*)", RegexOption.IGNORE_CASE)
    private val debitAmountRegex = Regex("transferred ETB ([\\d,]+\\.?\\d*) to", RegexOption.IGNORE_CASE)

    // Extract merchant/recipient for debits
    private val debitMerchantRegex = Regex("transferred ETB [\\d,]+\\.?\\d* to ([^\\n]+?) on", RegexOption.IGNORE_CASE)

    // Extract sender for credits
    private val creditMerchantRegex =
        Regex("received\\s+ETB [\\d,]+\\.?\\d* .* from ([^\\s]+(?:\\s+[^\\s]+)*?) (?:to|by)", RegexOption.IGNORE_CASE)

    private val dateRegex1 = Regex("on (\\d{2}/\\d{2}/\\d{4} \\d{2}:\\d{2}:\\d{2})", RegexOption.IGNORE_CASE)
    private val dateRegex2 = Regex("on (\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2})", RegexOption.IGNORE_CASE)
    private val urlRegex =
        Regex("(https://transactioninfo\\.ethiotelecom\\.et/receipt/[^\\s]+)", RegexOption.IGNORE_CASE)

    override fun canParse(
        sender: String,
        message: String,
    ): Boolean =
        senderIds.any { sender.contains(it, ignoreCase = true) } &&
            message.contains("Your current", ignoreCase = true) &&
            message.contains("balance is ETB", ignoreCase = true) &&
            (
                message.contains("transferred ETB", ignoreCase = true) ||
                    message.contains("received ETB", ignoreCase = true)
            )

    override fun parse(
        sender: String,
        message: String,
    ): ParsedTransaction? {
        println("DEBUG: TelebirrSmsParser.parse() called with sender='$sender', message='$message'")
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
                    message.contains("transferred", ignoreCase = true) -> TransactionType.EXPENSE
                    message.contains("received", ignoreCase = true) -> TransactionType.INCOME
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
                    TransactionType.EXPENSE -> extractDebitMerchant(message) ?: "Telebirr Transfer"
                    TransactionType.INCOME -> extractCreditMerchant(message) ?: "Telebirr Deposit"
                    else -> "Telebirr Transaction"
                }

            // Extract timestamp
            val timestamp = extractDate(message) ?: System.currentTimeMillis()

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

    private fun extractDebitMerchant(message: String): String? =
        debitMerchantRegex
            .find(message)
            ?.groupValues
            ?.get(1)
            ?.trim()

    private fun extractDate(message: String): Long? {
        // Try first date format (DD/MM/YYYY HH:MM:SS)
        val dateStr1 = dateRegex1.find(message)?.groupValues?.get(1)
        if (dateStr1 != null) {
            return try {
                val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
                sdf.parse(dateStr1)?.time
            } catch (e: Exception) {
                null
            }
        }

        // Try second date format (YYYY-MM-DD HH:MM:SS)
        val dateStr2 = dateRegex2.find(message)?.groupValues?.get(1)
        if (dateStr2 != null) {
            return try {
                val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                sdf.parse(dateStr2)?.time
            } catch (e: Exception) {
                null
            }
        }

        return null
    }

    private fun extractUrl(message: String): String? = urlRegex.find(message)?.groupValues?.get(1)
}
