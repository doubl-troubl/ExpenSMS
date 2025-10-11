package com.dagimg.expensms.data.sms

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import androidx.core.content.ContextCompat
import com.dagimg.expensms.data.repository.TransactionRepository
import com.dagimg.expensms.data.util.TransactionDateResolver
import kotlinx.coroutines.Dispatchers
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
                val processedMessages = mutableSetOf<String>() // To avoid duplicates

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

                            // Check if transaction already exists (avoid duplicates)
                            if (!isDuplicateTransaction(parsedWithResolvedDate)) {
                                repository.saveFromParsedTransaction(parsedWithResolvedDate)
                                successCount++
                                println("DEBUG: Saved historical transaction from ${smsData.sender}")
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

    private suspend fun isDuplicateTransaction(parsed: ParsedTransaction): Boolean =
        try {
            // Simple duplicate check based on amount, bank, and timestamp (within 1 minute)
            val existingTransactions = repository.transactions
            // Note: In a real implementation, you'd want to query the database directly
            // for efficiency rather than collecting the entire flow
            false // For now, assume no duplicates
        } catch (e: Exception) {
            println("WARNING: Could not check for duplicates: ${e.message}")
            false
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
