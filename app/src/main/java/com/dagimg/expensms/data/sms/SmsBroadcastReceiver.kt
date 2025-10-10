package com.dagimg.expensms.data.sms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
// import com.dagimg.expensms.data.repository.TransactionRepository // TODO: Implement when database is ready

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

            for (sms in messages) {
                val sender = sms.displayOriginatingAddress ?: continue
                val message = sms.messageBody ?: continue

                // Only process if sender matches supported banks
                if (parserRegistry.getAllSenderIds().any { sender.contains(it, ignoreCase = true) }) {
                    // Try to parse with registered parsers
                    val parsed = parserRegistry.parseTransaction(sender, message)

                    if (parsed != null) {
                        // Save to database in background
                        CoroutineScope(Dispatchers.IO).launch {
                            saveTransaction(context, parsed)
                        }
                    }
                }
            }
        }
    }

    private suspend fun saveTransaction(
        context: Context,
        parsed: ParsedTransaction,
    ) {
        try {
            // TODO: Get TransactionRepository instance and save the transaction
            // For now, just log the parsed transaction
            println("Parsed transaction: $parsed")
        } catch (e: Exception) {
            // Handle error - could show a toast or log
            println("Error saving transaction: ${e.message}")
        }
    }
}
