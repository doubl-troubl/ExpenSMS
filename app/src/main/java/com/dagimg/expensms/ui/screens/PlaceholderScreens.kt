package com.dagimg.expensms.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.dagimg.expensms.ui.theme.LightColors
import com.dagimg.expensms.ui.theme.Spacing

@Composable
fun TransactionsScreen(modifier: Modifier = Modifier) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(Spacing.lg),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Transactions",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.SemiBold,
            color = LightColors.Foreground,
            fontSize = 24.sp,
        )

        Spacer(modifier = Modifier.height(Spacing.md))

        Text(
            text = "Coming Soon...",
            style = MaterialTheme.typography.bodyLarge,
            color = LightColors.MutedForeground,
            fontSize = 16.sp,
        )
    }
}

@Composable
fun SettingsScreen(modifier: Modifier = Modifier) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(Spacing.lg),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.SemiBold,
            color = LightColors.Foreground,
            fontSize = 24.sp,
        )

        Spacer(modifier = Modifier.height(Spacing.md))

        Text(
            text = "Coming Soon...",
            style = MaterialTheme.typography.bodyLarge,
            color = LightColors.MutedForeground,
            fontSize = 16.sp,
        )
    }
}
