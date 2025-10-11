package com.dagimg.expensms.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dagimg.expensms.ui.theme.AppColors

@Composable
fun AuthScreen(
    modifier: Modifier = Modifier,
    onAuthenticationSuccess: () -> Unit = {},
    onAuthenticationFailure: () -> Unit = {},
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        // Lock Icon
        Icon(
            imageVector = Icons.Filled.Lock,
            contentDescription = "Biometric Authentication",
            modifier = Modifier.size(120.dp),
            tint = AppColors.Primary,
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Authentication needed text
        Text(
            text = "Authentication needed",
            style = MaterialTheme.typography.headlineMedium,
            color = AppColors.Foreground,
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Subtitle
        Text(
            text = "Please authenticate to continue",
            style = MaterialTheme.typography.bodyLarge,
            color = AppColors.MutedForeground,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AuthScreenPreview() {
    AuthScreen()
}
