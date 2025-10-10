package com.dagimg.expensms.data.model

data class Bank(
    val id: Long = 0,
    val name: String, // Internal name (e.g., "cbe", "dashen")
    val displayName: String, // Display name (e.g., "Commercial Bank of Ethiopia")
    val colorHex: String, // Accent color for UI
    val currentBalance: Double, // Updated from SMS "remaining balance" field
    val lastUpdated: Long, // Timestamp of last transaction SMS
    val isActive: Boolean = true,
    val smsSenderIds: List<String> = emptyList(), // Sender IDs to filter SMS (e.g., ["CBE", "CBE-ET"])
)
