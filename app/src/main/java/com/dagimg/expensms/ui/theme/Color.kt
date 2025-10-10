package com.dagimg.expensms.ui.theme

import androidx.compose.ui.graphics.Color

// Light Theme Colors
object LightColors {
    val Background = Color(0xFFFFFFFF) // rgb(255, 255, 255)
    val Foreground = Color(0xFF1A1A1A) // rgb(26, 26, 26)
    val Card = Color(0xFFFFFFFF) // rgb(255, 255, 255)
    val Muted = Color(0xFFECECF0) // rgb(236, 236, 240)
    val MutedForeground = Color(0xFF717182) // rgb(113, 113, 130)
    val Accent = Color(0xFFE9EBEF) // rgb(233, 235, 239)
    val Border = Color(0xFFE5E5E5) // rgb(229, 229, 229)
    val Primary = Color(0xFF030213) // rgb(3, 2, 19)
}

// Dark Theme Colors
object DarkColors {
    val Background = Color(0xFF1A1A1A) // rgb(26, 26, 26)
    val Foreground = Color(0xFFFAFAFA) // rgb(250, 250, 250)
    val Card = Color(0xFF1A1A1A) // rgb(26, 26, 26)
    val Muted = Color(0xFF3A3A3A) // rgb(58, 58, 58)
    val MutedForeground = Color(0xFFB4B4B4) // rgb(180, 180, 180)
    val Accent = Color(0xFF3A3A3A) // rgb(58, 58, 58)
    val Border = Color(0xFF3A3A3A) // rgb(58, 58, 58)
    val Primary = Color(0xFFFAFAFA) // rgb(250, 250, 250)
}

// Bank Accent Colors
object BankColors {
    val TotalBalance = Color(0xFF10B981) // rgb(16, 185, 129) - Green
    val Bank1 = Color(0xFF3B82F6) // rgb(59, 130, 246) - Blue
    val Bank2 = Color(0xFFF59E0B) // rgb(245, 158, 11) - Orange
    val Bank3 = Color(0xFF8B5CF6) // rgb(139, 92, 246) - Purple
    val Bank4 = Color(0xFFEC4899) // rgb(236, 72, 153) - Pink
}

// Transaction Colors
object TransactionColors {
    // Light Theme
    val IncomeLight = Color(0xFF10B981) // rgb(16, 185, 129)
    val ExpenseLight = Color(0xFFDC2626) // rgb(220, 38, 38)
    val IncomeBackgroundLight = Color(0xFFDCFCE7) // rgb(220, 252, 231)
    val ExpenseBackgroundLight = Color(0xFFFEE2E2) // rgb(254, 226, 226)

    // Dark Theme
    val IncomeDark = Color(0xFF4ADE80) // rgb(74, 222, 128)
    val ExpenseDark = Color(0xFFF87171) // rgb(248, 113, 113)
    val IncomeBackgroundDark = Color(0xFF14532D) // rgb(20, 83, 45)
    val ExpenseBackgroundDark = Color(0xFF450A0A) // rgb(69, 10, 10)
}
