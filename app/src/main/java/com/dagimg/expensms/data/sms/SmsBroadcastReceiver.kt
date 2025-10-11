package com.dagimg.expensms.data.sms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import com.dagimg.expensms.data.repository.TransactionRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Broadcast receiver for incoming SMS messages
 * Automatically parses banking SMS and stores transactions
 */
class SmsBroadcastReceiver : BroadcastReceiver() {
    private val parserRegistry =
        SmsParserRegistry().apply {
            registerParser(CbeSmsParser())
            // Add more parsers here as they are implemented
        }

    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        if (intent.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
            println("DEBUG: Received ${messages.size} SMS messages")

            // Combine multi-part messages from the same sender
            val combinedMessages = mutableMapOf<String, StringBuilder>()

            for (sms in messages) {
                val sender = sms.displayOriginatingAddress ?: continue
                val message = sms.messageBody ?: continue

                println("DEBUG: SMS from '$sender': '$message'")

                combinedMessages.getOrPut(sender) { StringBuilder() }.append(message)
            }

            // Process combined messages
            for ((sender, messageBuilder) in combinedMessages) {
                val fullMessage = messageBuilder.toString()
                println("DEBUG: Combined message from '$sender': '$fullMessage'")

                // Only process if sender matches supported banks
                val supportedSenders = parserRegistry.getAllSenderIds()
                println("DEBUG: Supported senders: $supportedSenders")

                val isSupported = supportedSenders.any { sender.contains(it, ignoreCase = true) }
                println("DEBUG: Is sender '$sender' supported? $isSupported")

                if (isSupported) {
                    // Try to parse with registered parsers
                    val parsed = parserRegistry.parseTransaction(sender, fullMessage)
                    println("DEBUG: Parsed transaction: $parsed")

                    if (parsed != null) {
                        // Save to database in background
                        CoroutineScope(Dispatchers.IO).launch {
                            saveTransaction(context, parsed)
                        }
                    } else {
                        println("DEBUG: Failed to parse SMS from '$sender'")
                    }
                }
            }
        } else {
            println("DEBUG: Received intent with action: ${intent.action}")
        }
    }

    private suspend fun saveTransaction(
        context: Context,
        parsed: ParsedTransaction,
    ) {
        try {
            println("DEBUG: Saving transaction: $parsed")
            val repository = TransactionRepository.getInstance(context)
            val transactionId = repository.addTransaction(parsed)
            println("DEBUG: Saved transaction with ID: $transactionId from ${parsed.bankName}")

            // Print current repository state
            val currentTransactions = repository.transactions.first()
            println("DEBUG: Total transactions in repository: ${currentTransactions.size}")
        } catch (e: Exception) {
            // Handle error - could show a toast or log
            println("DEBUG: Error saving transaction: ${e.message}")
            e.printStackTrace()
        }
    }
}
