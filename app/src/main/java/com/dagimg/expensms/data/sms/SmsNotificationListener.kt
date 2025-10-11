package com.dagimg.expensms.data.sms

import android.app.Notification
import android.content.Context
import android.os.Bundle
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.dagimg.expensms.data.repository.TransactionRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * NotificationListenerService for real-time SMS parsing
 *
 * This service implements the dual-permission approach:
 * 1. Listens for SMS notifications from banking apps (works when app is closed)
 * 2. Parses transactions using existing SmsParserRegistry (CBE, Telebirr, Abyssinia)
 * 3. Saves parsed transactions to Room database
 *
 * Advantages over BroadcastReceiver:
 * - Works when app is completely killed/closed
 * - More reliable on modern Android versions
 * - Can extract full message content from notifications
 *
 * Requires: BIND_NOTIFICATION_LISTENER_SERVICE permission
 * User must manually enable in Settings > Notification Access
 */
class SmsNotificationListener : NotificationListenerService() {
    private val parserRegistry =
        SmsParserRegistry().apply {
            registerParser(CbeSmsParser())
            registerParser(TelebirrSmsParser())
            registerParser(AbyssiniaSmsParser())
        }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)

        sbn?.let { notification ->
            processSmsNotification(notification)
        }
    }

    private fun processSmsNotification(sbn: StatusBarNotification) {
        try {
            // Only process SMS notifications
            if (!isSmsNotification(sbn)) {
                return
            }

            val extras = sbn.notification.extras
            val sender = extractSender(extras) ?: return
            val message = extractMessage(extras) ?: return

            println("DEBUG: SmsNotificationListener received SMS from '$sender': '$message'")

            // Check if sender is from supported banks
            val supportedSenders = parserRegistry.getAllSenderIds()
            val isSupported = supportedSenders.any { sender.contains(it, ignoreCase = true) }

            if (!isSupported) {
                println("DEBUG: Sender '$sender' not supported, ignoring")
                return
            }

            // Try to parse the transaction
            val parsed = parserRegistry.parseTransaction(sender, message)

            if (parsed != null) {
                println("DEBUG: Successfully parsed transaction: $parsed")

                // Save to database in background
                CoroutineScope(Dispatchers.IO).launch {
                    saveTransaction(applicationContext, parsed)
                }
            } else {
                println("DEBUG: Failed to parse SMS from '$sender'")
            }
        } catch (e: Exception) {
            println("ERROR: Exception processing SMS notification: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun isSmsNotification(sbn: StatusBarNotification): Boolean {
        // Check if notification is from SMS app
        val packageName = sbn.packageName
        val smsPackages =
            listOf(
                "com.google.android.apps.messaging", // Google Messages
                "com.samsung.android.messaging", // Samsung Messages
                "com.android.mms", // Default SMS app
                "com.textra", // Textra
                "com.chomp.android.sms", // ChompSMS
            )

        return smsPackages.contains(packageName) ||
            sbn.notification.category == Notification.CATEGORY_MESSAGE
    }

    private fun extractSender(extras: Bundle): String? =
        try {
            // Try different keys used by SMS apps
            // Note: For SMS notifications, the sender is usually in the title
            extras.getString("android.title")
                ?: extras.getCharSequence("android.title")?.toString()
                ?: extras.getString("android.subText")
                ?: extras.getCharSequence("android.subText")?.toString()
        } catch (e: Exception) {
            null
        }

    private fun extractMessage(extras: Bundle): String? =
        try {
            // Try different keys for message content
            // Priority: bigText (full message) > text (truncated message)
            extras.getString("android.bigText")
                ?: extras.getCharSequence("android.bigText")?.toString()
                ?: extras.getString("android.text")
                ?: extras.getCharSequence("android.text")?.toString()
        } catch (e: Exception) {
            null
        }

    private suspend fun saveTransaction(
        context: Context,
        parsed: ParsedTransaction,
    ) {
        try {
            val repository = TransactionRepository.getInstance(context)
            repository.saveFromParsedTransaction(parsed)
            println("DEBUG: Transaction saved to database")
        } catch (e: Exception) {
            println("ERROR: Failed to save transaction: ${e.message}")
            e.printStackTrace()
        }
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        println("DEBUG: SmsNotificationListener connected")
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        println("DEBUG: SmsNotificationListener disconnected")
    }
}
