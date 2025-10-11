package com.dagimg.expensms.data.util

import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern

/**
 * Robust date resolver for bank SMS transactions
 *
 * Handles multiple date formats and sources with smart fallbacks:
 * 1. Parse from SMS message body (highest priority)
 * 2. Use notification timestamp (from StatusBarNotification.postTime)
 * 3. Use SMS provider date (from content://sms/inbox cursor)
 * 4. System current time (last fallback)
 *
 * Supports various formats:
 * - "on 11 Oct 2025 at 12:45 PM"
 * - "on 10 Sep 2025"
 * - "11/10/2025"
 * - "2025-10-11"
 * - Ethiopian calendar formats (future)
 */
object TransactionDateResolver {
    /**
     * Main entry point for date resolution
     */
    fun resolveDate(
        messageBody: String? = null,
        notificationPostTime: Long? = null,
        smsProviderDate: Long? = null,
        bankName: String? = null,
    ): Long {
        // 1. Try parsing date from message body (highest priority)
        messageBody?.let { body ->
            parseDateFromBody(body, bankName)?.let { return it }
        }

        // 2. Fallback to notification timestamp
        notificationPostTime?.let { return it }

        // 3. Fallback to SMS provider date
        smsProviderDate?.let { return it }

        // 4. Final fallback - current system time
        return System.currentTimeMillis()
    }

    /**
     * Parse date from SMS message body with multiple regex patterns
     */
    private fun parseDateFromBody(
        body: String,
        bankName: String? = null,
    ): Long? {
        if (body.isBlank()) return null

        // Try bank-specific patterns first (for better accuracy)
        bankName?.let { bank ->
            getBankSpecificPatterns(bank).forEach { pattern ->
                parseWithPattern(body, pattern)?.let { return it }
            }
        }

        // Try general patterns
        getGeneralPatterns().forEach { pattern ->
            parseWithPattern(body, pattern)?.let { return it }
        }

        return null
    }

    /**
     * Parse date using a specific regex pattern
     */
    private fun parseWithPattern(
        text: String,
        pattern: DatePattern,
    ): Long? =
        try {
            val matcher = pattern.regex.matcher(text)
            if (matcher.find()) {
                val dateString = matcher.group(pattern.dateGroup)
                dateString?.let { pattern.parser.parse(it)?.time }
            } else {
                null
            }
        } catch (e: Exception) {
            // Log parsing error but continue with other patterns
            println("DEBUG: Failed to parse date with pattern ${pattern.name}: ${e.message}")
            null
        }

    /**
     * Bank-specific date patterns for better accuracy
     */
    private fun getBankSpecificPatterns(bankName: String): List<DatePattern> {
        val bankKey = bankName.lowercase().replace(" ", "")

        return when (bankKey) {
            "commercialbankofethiopia", "cbe" -> cbePatterns
            "dashenbank" -> dashenPatterns
            "abyssinia", "bankofabyssinia" -> abyssiniaPatterns
            "telebirr" -> telebirrPatterns
            else -> emptyList()
        }
    }

    /**
     * General date patterns that work across multiple banks
     */
    private fun getGeneralPatterns(): List<DatePattern> = generalPatterns

    /**
     * Data class for date parsing patterns
     */
    private data class DatePattern(
        val name: String,
        val regex: Pattern,
        val dateGroup: Int,
        val parser: SimpleDateFormat,
    )

    // Commercial Bank of Ethiopia patterns
    private val cbePatterns =
        listOf(
            // "on 11 Oct 2025 at 12:45 PM"
            DatePattern(
                "CBE Full DateTime",
                Pattern.compile("on (\\d{1,2} \\w{3,9} \\d{4}) at (\\d{1,2}:\\d{2} \\w{2})"),
                1,
                SimpleDateFormat("d MMM yyyy", Locale.ENGLISH),
            ),
            // "on 11/10/2025"
            DatePattern(
                "CBE Short Date",
                Pattern.compile("on (\\d{1,2}/\\d{1,2}/\\d{4})"),
                1,
                SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH),
            ),
        )

    // Dashen Bank patterns
    private val dashenPatterns =
        listOf(
            // Add specific Dashen patterns when available
            DatePattern(
                "Dashen Date",
                Pattern.compile("(\\d{1,2}/\\d{1,2}/\\d{4})"),
                1,
                SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH),
            ),
        )

    // Abyssinia Bank patterns
    private val abyssiniaPatterns =
        listOf(
            // Add specific Abyssinia patterns when available
            DatePattern(
                "Abyssinia Date",
                Pattern.compile("(\\d{1,2}-\\d{1,2}-\\d{4})"),
                1,
                SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH),
            ),
        )

    // Telebirr patterns
    private val telebirrPatterns =
        listOf(
            // Add specific Telebirr patterns when available
            DatePattern(
                "Telebirr Date",
                Pattern.compile("(\\d{1,2}/\\d{1,2}/\\d{4})"),
                1,
                SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH),
            ),
        )

    // General patterns that work across banks
    private val generalPatterns =
        listOf(
            // "on 11 Oct 2025 at 12:45 PM" - Full datetime
            DatePattern(
                "Full DateTime with Time",
                Pattern.compile("on (\\d{1,2} \\w{3,9} \\d{4}) at (\\d{1,2}:\\d{2} \\w{2})"),
                1,
                SimpleDateFormat("d MMM yyyy", Locale.ENGLISH),
            ),
            // "on 11 Oct 2025" - Date only
            DatePattern(
                "Full Date Only",
                Pattern.compile("on (\\d{1,2} \\w{3,9} \\d{4})"),
                1,
                SimpleDateFormat("d MMM yyyy", Locale.ENGLISH),
            ),
            // "11/10/2025" - DD/MM/YYYY
            DatePattern(
                "DD/MM/YYYY",
                Pattern.compile("(\\d{1,2}/\\d{1,2}/\\d{4})"),
                1,
                SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH),
            ),
            // "11-10-2025" - DD-MM-YYYY
            DatePattern(
                "DD-MM-YYYY",
                Pattern.compile("(\\d{1,2}-\\d{1,2}-\\d{4})"),
                1,
                SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH),
            ),
            // "2025/10/11" - YYYY/MM/DD
            DatePattern(
                "YYYY/MM/DD",
                Pattern.compile("(\\d{4}/\\d{1,2}/\\d{1,2})"),
                1,
                SimpleDateFormat("yyyy/MM/dd", Locale.ENGLISH),
            ),
            // "2025-10-11" - YYYY-MM-DD
            DatePattern(
                "YYYY-MM-DD",
                Pattern.compile("(\\d{4}-\\d{1,2}-\\d{1,2})"),
                1,
                SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH),
            ),
            // "11 Oct 2025" - without "on"
            DatePattern(
                "Date without On",
                Pattern.compile("(\\d{1,2} \\w{3,9} \\d{4})"),
                1,
                SimpleDateFormat("d MMM yyyy", Locale.ENGLISH),
            ),
            // "Oct 11, 2025" - US format
            DatePattern(
                "US Date Format",
                Pattern.compile("(\\w{3,9} \\d{1,2}, \\d{4})"),
                1,
                SimpleDateFormat("MMM d, yyyy", Locale.ENGLISH),
            ),
        )

    /**
     * Utility method to validate if a parsed date is reasonable
     * (not too far in the past or future)
     */
    fun isReasonableDate(timestamp: Long): Boolean {
        val now = System.currentTimeMillis()
        val oneYearAgo = now - (365L * 24 * 60 * 60 * 1000) // 1 year ago
        val oneYearFromNow = now + (365L * 24 * 60 * 60 * 1000) // 1 year from now

        return timestamp in oneYearAgo..oneYearFromNow
    }

    /**
     * Debug method to show what date was resolved and from which source
     */
    fun resolveDateWithDebug(
        messageBody: String? = null,
        notificationPostTime: Long? = null,
        smsProviderDate: Long? = null,
        bankName: String? = null,
    ): DateResolutionResult {
        val parsedFromBody = messageBody?.let { parseDateFromBody(it, bankName) }
        val finalDate = resolveDate(messageBody, notificationPostTime, smsProviderDate, bankName)

        val source =
            when {
                parsedFromBody != null -> "SMS_BODY"
                notificationPostTime != null -> "NOTIFICATION_TIMESTAMP"
                smsProviderDate != null -> "SMS_PROVIDER"
                else -> "SYSTEM_TIME"
            }

        return DateResolutionResult(finalDate, source, messageBody, bankName, parsedFromBody)
    }

    /**
     * Result class for debug information
     */
    data class DateResolutionResult(
        val timestamp: Long,
        val source: String,
        val originalMessage: String? = null,
        val bankName: String? = null,
        val parsedFromBody: Long? = null,
    ) {
        val formattedDate: String
            get() =
                SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                    .format(Date(timestamp))

        val debugInfo: String
            get() =
                """
                Date Resolution Result:
                - Final Timestamp: $formattedDate
                - Source: $source
                - Bank: ${bankName ?: "Unknown"}
                - Parsed from body: ${parsedFromBody?.let {
                    SimpleDateFormat(
                        "yyyy-MM-dd HH:mm:ss",
                        Locale.getDefault(),
                    ).format(Date(it))
                } ?: "No date found in message"}
                - Original Message: ${originalMessage?.take(
                    100,
                )}${if (originalMessage != null && originalMessage.length > 100) "..." else ""}
                """.trimIndent()
    }
}
