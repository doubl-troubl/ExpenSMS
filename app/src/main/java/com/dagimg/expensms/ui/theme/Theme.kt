package com.dagimg.expensms.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Typography Scale (from PRD)
val AppTypography =
    Typography(
        // h1: 24px / 1.5rem (Home greeting)
        headlineLarge =
            TextStyle(
                fontFamily = FontFamily.Default,
                fontWeight = FontWeight.SemiBold,
                fontSize = 24.sp,
                lineHeight = 36.sp,
            ),
        // h2: 20px / 1.25rem (Section headers)
        headlineMedium =
            TextStyle(
                fontFamily = FontFamily.Default,
                fontWeight = FontWeight.Medium,
                fontSize = 20.sp,
                lineHeight = 30.sp,
            ),
        // h3: 18px / 1.125rem (Settings section headers)
        headlineSmall =
            TextStyle(
                fontFamily = FontFamily.Default,
                fontWeight = FontWeight.Medium,
                fontSize = 18.sp,
                lineHeight = 27.sp,
            ),
        // p/body: 16px / 1rem (Default)
        bodyLarge =
            TextStyle(
                fontFamily = FontFamily.Default,
                fontWeight = FontWeight.Normal,
                fontSize = 16.sp,
                lineHeight = 24.sp,
            ),
        bodyMedium =
            TextStyle(
                fontFamily = FontFamily.Default,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                lineHeight = 21.sp,
            ),
        // small/caption: 12px / 0.75rem (Metadata)
        bodySmall =
            TextStyle(
                fontFamily = FontFamily.Default,
                fontWeight = FontWeight.Normal,
                fontSize = 12.sp,
                lineHeight = 18.sp,
            ),
        // Amount text with semibold weight
        titleLarge =
            TextStyle(
                fontFamily = FontFamily.Default,
                fontWeight = FontWeight.SemiBold,
                fontSize = 20.sp,
                lineHeight = 30.sp,
            ),
    )

// Spacing Scale (from PRD)
object Spacing {
    val xs = 4.dp // 0.25rem (Minimal gaps)
    val sm = 8.dp // 0.5rem (Icon spacing)
    val md = 12.dp // 0.75rem (Card inner padding)
    val lg = 16.dp // 1rem (Standard margin/padding)
    val xl = 24.dp // 1.5rem (Section spacing)
    val xxl = 32.dp // 2rem (Large gaps)
}

// Border Radius (from PRD)
object Shapes {
    val small = RoundedCornerShape(8.dp) // Inputs, buttons
    val medium = RoundedCornerShape(10.dp) // Transaction items
    val large = RoundedCornerShape(12.dp) // Cards, dialogs
    val extraLarge = RoundedCornerShape(16.dp) // Balance cards
    val full = RoundedCornerShape(50) // Circular elements
}
