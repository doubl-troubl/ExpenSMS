package com.dagimg.expensms.data.notifications

import android.app.NotificationChannel
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.dagimg.expensms.MainActivity
import com.dagimg.expensms.R
import com.dagimg.expensms.data.model.Transaction
import com.dagimg.expensms.data.model.TransactionType
import com.dagimg.expensms.data.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.util.*
import android.app.NotificationManager as AndroidNotificationManager

/**
 * Notification manager for handling transaction notifications
 */
class NotificationManager(
    private val context: Context,
) {
    private val notificationManager = NotificationManagerCompat.from(context)
    private val androidNotificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as AndroidNotificationManager

    companion object {
        const val CHANNEL_ID = "transaction_notifications"
        const val CHANNEL_NAME = "Transaction Notifications"
        const val CHANNEL_DESCRIPTION = "Notifications for new bank transactions"

        // Notification IDs
        const val TRANSACTION_NOTIFICATION_ID = 1001
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel =
                NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    AndroidNotificationManager.IMPORTANCE_DEFAULT,
                ).apply {
                    description = CHANNEL_DESCRIPTION
                    setShowBadge(true)
                }
            androidNotificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Send a notification for a new transaction
     */
    fun sendTransactionNotification(transaction: Transaction) {
        // Check if notifications are enabled using UserPreferencesRepository
        val userPreferencesRepository = UserPreferencesRepository(context)
        val notificationsEnabled =
            runBlocking {
                userPreferencesRepository.getNotificationsEnabled().first()
            }

        if (!notificationsEnabled) {
            return
        }

        val title = getNotificationTitle(transaction)
        val content = getNotificationContent(transaction)

        val intent =
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }

        val pendingIntent =
            PendingIntent.getActivity(
                context,
                0,
                intent,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    PendingIntent.FLAG_IMMUTABLE
                } else {
                    0
                },
            )

        val notification =
            NotificationCompat
                .Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground) // You'll need to add this icon
                .setContentTitle(title)
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setStyle(NotificationCompat.BigTextStyle().bigText(content))
                .build()

        try {
            notificationManager.notify(TRANSACTION_NOTIFICATION_ID, notification)
        } catch (e: SecurityException) {
            // Handle case where POST_NOTIFICATIONS permission is not granted
            println("ERROR: Cannot send notification - POST_NOTIFICATIONS permission not granted")
        }
    }

    private fun getNotificationTitle(transaction: Transaction): String {
        val action = if (transaction.type == TransactionType.INCOME) "credited" else "debited"
        return "${formatAmount(transaction.amount)} $action from your ${transaction.bankName}"
    }

    private fun getNotificationContent(transaction: Transaction): String {
        val action = if (transaction.type == TransactionType.INCOME) "credited" else "debited"
        return "${formatAmount(transaction.amount)} has been $action from your ${transaction.bankName}. Track it now!"
    }

    private fun formatAmount(amount: Double): String {
        // Format Ethiopian Birr with proper symbol
        val numberFormat = java.text.DecimalFormat("#,##0.00")
        return "ብር${numberFormat.format(amount)}"
    }
}
