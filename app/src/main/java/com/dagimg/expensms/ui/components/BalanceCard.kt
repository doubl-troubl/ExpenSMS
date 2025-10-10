package com.dagimg.expensms.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dagimg.expensms.data.model.Bank
import com.dagimg.expensms.ui.theme.*
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BalanceCard(
    bank: Bank,
    isTotal: Boolean = false,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    Card(
        onClick = onClick,
        modifier =
            modifier
                .fillMaxWidth()
                .height(140.dp),
        shape = Shapes.extraLarge,
        colors =
            CardDefaults.cardColors(
                containerColor =
                    if (isTotal) {
                        Color(0xFF10B981).copy(alpha = 0.1f) // Light green background for total
                    } else {
                        LightColors.Card
                    },
            ),
        elevation =
            CardDefaults.cardElevation(
                defaultElevation = 2.dp,
                pressedElevation = 4.dp,
            ),
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(Spacing.lg),
        ) {
            // Color accent indicator (top right)
            Box(
                modifier =
                    Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(
                            if (isTotal) {
                                BankColors.TotalBalance
                            } else {
                                Color(android.graphics.Color.parseColor(bank.colorHex))
                            },
                        ).align(Alignment.TopEnd),
            )

            // Content
            Column(
                verticalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxSize(),
            ) {
                // Label
                Text(
                    text = if (isTotal) "Total Balance" else bank.displayName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = LightColors.MutedForeground,
                    fontSize = 14.sp,
                )

                // Amount
                Text(
                    text = formatCurrency(bank.currentBalance),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = LightColors.Foreground,
                    fontSize = 24.sp,
                )

                // Bank name (only for individual bank cards)
                if (!isTotal) {
                    Text(
                        text = bank.displayName,
                        style = MaterialTheme.typography.bodySmall,
                        color = LightColors.MutedForeground,
                        fontSize = 12.sp,
                    )
                }
            }
        }
    }
}

@Composable
fun TotalBalanceCard(
    totalBalance: Double,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    Card(
        onClick = onClick,
        modifier =
            modifier
                .fillMaxWidth()
                .height(140.dp),
        shape = Shapes.extraLarge,
        colors =
            CardDefaults.cardColors(
                containerColor = BankColors.TotalBalance.copy(alpha = 0.1f),
            ),
        elevation =
            CardDefaults.cardElevation(
                defaultElevation = 2.dp,
                pressedElevation = 4.dp,
            ),
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(Spacing.lg),
        ) {
            // Green accent indicator (top right)
            Box(
                modifier =
                    Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(BankColors.TotalBalance)
                        .align(Alignment.TopEnd),
            )

            // Content
            Column(
                verticalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxSize(),
            ) {
                // Label
                Text(
                    text = "Total Balance",
                    style = MaterialTheme.typography.bodyMedium,
                    color = LightColors.MutedForeground,
                    fontSize = 14.sp,
                )

                // Amount
                Text(
                    text = formatCurrency(totalBalance),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = LightColors.Foreground,
                    fontSize = 28.sp,
                )

                // Subtitle
                Text(
                    text = "Total Balance",
                    style = MaterialTheme.typography.bodySmall,
                    color = LightColors.MutedForeground,
                    fontSize = 12.sp,
                )
            }
        }
    }
}

private fun formatCurrency(amount: Double): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale.US)
    return formatter.format(amount)
}

@Preview
@Composable
fun BalanceCardPreview() {
    val dummyBank =
        Bank(
            id = 1,
            name = "chase",
            displayName = "Chase",
            colorHex = "#3B82F6",
            currentBalance = 27209.68,
            lastUpdated = System.currentTimeMillis(),
        )

    MaterialTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            TotalBalanceCard(totalBalance = 45280.50)
            BalanceCard(bank = dummyBank)
        }
    }
}
