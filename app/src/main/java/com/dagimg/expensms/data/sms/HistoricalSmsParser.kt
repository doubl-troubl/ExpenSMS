package com.dagimg.expensms.data.sms

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import androidx.core.content.ContextCompat
import com.dagimg.expensms.data.repository.TransactionRepository
import com.dagimg.expensms.data.repository.UserPreferencesRepository
import com.dagimg.expensms.data.util.TransactionDateResolver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

/**
 * Service for parsing historical SMS messages from the past 30 days
 * Uses ContentResolver to query SMS database and extract bank transactions
 */
class HistoricalSmsParser(
    private val context: Context,
) {
    private val parserRegistry =
        SmsParserRegistry().apply {
            registerParser(CbeSmsParser())
            registerParser(TelebirrSmsParser())
            registerParser(AbyssiniaSmsParser())
        }

    private val repository = TransactionRepository.getInstance(context)

    // SMS content provider URI
    private val smsUri = Uri.parse("content://sms/inbox")

    // Supported sender IDs from all parsers
    private val supportedSenderIds =
        listOf(
            "CBE",
            "CBE-ET",
            "CBEBIRR",
            "+251936744962", // CBE
            "127",
            "Telebirr",
            "ETHIOTELECOM", // Telebirr
            "BOA",
            "ABYSSINIA",
            "BANKOFABYSSINIA", // Bank of Abyssinia
        )

    /**
     * Parse historical SMS messages from the past 30 days
     * @return Number of transactions successfully parsed and saved
     */
    suspend fun parseHistoricalSms(): Int =
        withContext(Dispatchers.IO) {
            if (!hasReadSmsPermission()) {
                println("ERROR: READ_SMS permission not granted")
                return@withContext 0
            }

            try {
                val thirtyDaysAgo = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(30)
                val smsMessages = queryHistoricalSms(thirtyDaysAgo)

                println("DEBUG: Found ${smsMessages.size} SMS messages from past 30 days")

                var successCount = 0
                val processedMessages = mutableSetOf<String>() // To avoid processing the same SMS multiple times
                val processedTransactions = mutableListOf<ParsedTransaction>()
                // Track transactions saved in this session

                for (smsData in smsMessages) {
                    try {
                        // Create unique key to avoid processing duplicates
                        val messageKey = "${smsData.sender}_${smsData.timestamp}_${smsData.body.hashCode()}"

                        if (processedMessages.contains(messageKey)) {
                            continue
                        }

                        // Check if sender is supported
                        val isSupported =
                            supportedSenderIds.any {
                                smsData.sender.contains(it, ignoreCase = true)
                            }

                        if (!isSupported) {
                            continue
                        }

                        // Try to parse the transaction
                        val parsed = parserRegistry.parseTransaction(smsData.sender, smsData.body)

                        if (parsed != null) {
                            // Resolve the transaction date using our robust date resolver
                            val resolvedDate =
                                TransactionDateResolver.resolveDate(
                                    messageBody = smsData.body,
                                    smsProviderDate = smsData.timestamp,
                                    bankName = parsed.bankName,
                                )

                            println("DEBUG: Resolved date for historical SMS: ${java.util.Date(resolvedDate)}")

                            // Create updated parsed transaction with resolved date
                            val parsedWithResolvedDate = parsed.copy(timestamp = resolvedDate)

                            // Check for duplicates in this session and existing database
                            if (!isDuplicateInSession(parsedWithResolvedDate, processedTransactions) &&
                                !isDuplicateInDatabase(parsedWithResolvedDate)
                            ) {
                                repository.saveFromParsedTransaction(parsedWithResolvedDate)
                                processedTransactions.add(parsedWithResolvedDate)
                                successCount++
                                println("DEBUG: Saved historical transaction from ${smsData.sender}")

                                // Save user name if extracted and not already saved
                                if (!parsedWithResolvedDate.userName.isNullOrBlank()) {
                                    saveUserNameIfNeeded(parsedWithResolvedDate.userName)
                                }
                            } else {
                                println("DEBUG: Duplicate transaction detected, skipping")
                            }
                        }

                        processedMessages.add(messageKey)
                    } catch (e: Exception) {
                        println("ERROR: Failed to process SMS: ${e.message}")
                        e.printStackTrace()
                    }
                }

                println("DEBUG: Successfully parsed $successCount historical transactions")
                return@withContext successCount
            } catch (e: Exception) {
                println("ERROR: Failed to parse historical SMS: ${e.message}")
                e.printStackTrace()
                return@withContext 0
            }
        }

    private fun hasReadSmsPermission(): Boolean =
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_SMS,
        ) == PackageManager.PERMISSION_GRANTED

    private fun queryHistoricalSms(sinceTimestamp: Long): List<SmsData> {
        val smsMessages = mutableListOf<SmsData>()

        val projection =
            arrayOf(
                "address", // Sender
                "body", // Message content
                "date", // Timestamp
                "type", // Message type (1 = received, 2 = sent)
            )

        val selection = "date >= ? AND type = ?"
        val selectionArgs =
            arrayOf(
                sinceTimestamp.toString(),
                "1", // Only received messages
            )

        val sortOrder = "date DESC"

        val cursor: Cursor? =
            context.contentResolver.query(
                smsUri,
                projection,
                selection,
                selectionArgs,
                sortOrder,
            )

        cursor?.use {
            val addressIndex = it.getColumnIndex("address")
            val bodyIndex = it.getColumnIndex("body")
            val dateIndex = it.getColumnIndex("date")

            while (it.moveToNext()) {
                val sender = it.getString(addressIndex) ?: ""
                val body = it.getString(bodyIndex) ?: ""
                val timestamp = it.getLong(dateIndex)

                if (sender.isNotBlank() && body.isNotBlank()) {
                    smsMessages.add(SmsData(sender, body, timestamp))
                }
            }
        }

        return smsMessages
    }

    private fun isDuplicateInSession(
        parsed: ParsedTransaction,
        processedTransactions: List<ParsedTransaction>,
    ): Boolean {
        // Check for duplicates within the current processing session
        val duplicate =
            processedTransactions.any { existing ->
                // Same amount (exact match)
                existing.amount == parsed.amount &&
                    // Same transaction type
                    existing.type == parsed.type &&
                    // Same bank
                    existing.bankName == parsed.bankName &&
                    // Timestamp within 2 minutes (very close timing)
                    kotlin.math.abs(existing.timestamp - parsed.timestamp) < (2 * 60 * 1000L) &&
                    // 2 minutes in milliseconds
                    // Similar merchant name (case-insensitive)
                    existing.merchant.equals(parsed.merchant, ignoreCase = true)
            }

        if (duplicate) {
            println("DEBUG: Session duplicate detected: ${parsed.merchant} - ${parsed.amount} - ${parsed.bankName}")
        }

        return duplicate
    }

    private suspend fun isDuplicateInDatabase(parsed: ParsedTransaction): Boolean =
        try {
            // Get all existing transactions for this bank
            val existingTransactions = repository.getTransactionsForBank(parsed.bankName).first()

            // Check for duplicates based on multiple criteria
            val duplicate =
                existingTransactions.any { existing ->
                    // Same amount (exact match)
                    existing.amount == parsed.amount &&
                        // Same transaction type
                        existing.type == parsed.type &&
                        // Same bank
                        existing.bankName == parsed.bankName &&
                        // Timestamp within 5 minutes (to account for slight timing differences)
                        kotlin.math.abs(existing.timestamp - parsed.timestamp) < (5 * 60 * 1000L) &&
                        // 5 minutes in milliseconds
                        // Similar merchant name (case-insensitive, allowing for minor variations)
                        existing.merchant.equals(parsed.merchant, ignoreCase = true)
                }

            if (duplicate) {
                println(
                    "DEBUG: Database duplicate detected: ${parsed.merchant} - ${parsed.amount} - ${parsed.bankName}",
                )
            }

            duplicate
        } catch (e: Exception) {
            println("WARNING: Could not check for database duplicates: ${e.message}")
            // If we can't check for duplicates, err on the side of caution and allow the transaction
            false
        }

    private suspend fun saveUserNameIfNeeded(userName: String) {
        try {
            val userPrefs = UserPreferencesRepository(context)
            val alreadyExtracted = userPrefs.getUserNameExtracted().first()

            if (!alreadyExtracted) {
                userPrefs.setUserName(userName)
                println("DEBUG: User name extracted from historical SMS and saved: $userName")
            }
        } catch (e: Exception) {
            println("ERROR: Failed to save user name from historical SMS: ${e.message}")
        }
    }

    /**
     * Data class for SMS information
     */
    private data class SmsData(
        val sender: String,
        val body: String,
        val timestamp: Long,
    )

    companion object {
        /**
         * Factory method to create HistoricalSmsParser instance
         */
        fun create(context: Context): HistoricalSmsParser = HistoricalSmsParser(context)
    }
}
