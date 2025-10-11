package com.dagimg.expensms.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dagimg.expensms.R
import com.dagimg.expensms.data.model.Bank
import com.dagimg.expensms.ui.theme.*
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BalanceCard(
    bank: Bank,
    balanceVisible: Boolean,
    onToggleBalanceVisibility: () -> Unit,
    isTotal: Boolean = false,
    modifier: Modifier = Modifier,
    _onClick: () -> Unit = {},
) {
    val accentColor =
        if (isTotal) {
            BankColors.TotalBalance
        } else {
            Color(android.graphics.Color.parseColor(bank.colorHex))
        }

    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .height(180.dp)
                .padding(horizontal = 24.dp)
                .shadow(
                    elevation = 8.dp,
                    shape = RoundedCornerShape(24.dp),
                    spotColor = Color.Black.copy(alpha = 0.15f),
                    ambientColor = Color.Black.copy(alpha = 0.1f),
                ).clip(RoundedCornerShape(24.dp))
                .background(
                    brush =
                        Brush.linearGradient(
                            colors =
                                listOf(
                                    accentColor.copy(alpha = 0.9f),
                                    accentColor.copy(alpha = 1f),
                                ),
                        ),
                ),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = if (isTotal) "Total Balance" else bank.displayName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                )

                Box(
                    modifier =
                        Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.9f)),
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            BirrAmountText(
                amount = bank.currentBalance,
                style =
                    MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 36.sp,
                        letterSpacing = (-0.5).sp,
                    ),
                color = Color.White,
                balanceVisible = balanceVisible,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Text(
                        text = if (isTotal) "All Accounts" else "Account Balance",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 12.sp,
                    )

                    if (!isTotal) {
                        Text(
                            text = "****${bank.id}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 11.sp,
                            letterSpacing = 1.sp,
                        )
                    }
                }

                IconButton(
                    onClick = onToggleBalanceVisibility,
                    modifier = Modifier.size(32.dp),
                ) {
                    Icon(
                        imageVector = if (balanceVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                        contentDescription = if (balanceVisible) "Hide balance" else "Show balance",
                        tint = Color.White.copy(alpha = 0.9f),
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        }
    }
}

@Composable
fun TotalBalanceCard(
    totalBalance: Double,
    balanceVisible: Boolean,
    onToggleBalanceVisibility: () -> Unit,
    modifier: Modifier = Modifier,
    _onClick: () -> Unit = {},
) {
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .height(180.dp)
                .padding(horizontal = 24.dp)
                .shadow(
                    elevation = 8.dp,
                    shape = RoundedCornerShape(24.dp),
                    spotColor = Color.Black.copy(alpha = 0.15f),
                    ambientColor = Color.Black.copy(alpha = 0.1f),
                ).clip(RoundedCornerShape(24.dp))
                .background(
                    brush =
                        Brush.linearGradient(
                            colors =
                                listOf(
                                    BankColors.TotalBalance.copy(alpha = 0.9f),
                                    BankColors.TotalBalance.copy(alpha = 1f),
                                ),
                        ),
                ),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Total Balance",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                )

                Box(
                    modifier =
                        Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.9f)),
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            BirrAmountText(
                amount = totalBalance,
                style =
                    MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 36.sp,
                        letterSpacing = (-0.5).sp,
                    ),
                color = Color.White,
                balanceVisible = balanceVisible,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "All Accounts",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 12.sp,
                )

                IconButton(
                    onClick = onToggleBalanceVisibility,
                    modifier = Modifier.size(32.dp),
                ) {
                    Icon(
                        imageVector = if (balanceVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                        contentDescription = if (balanceVisible) "Hide balance" else "Show balance",
                        tint = Color.White.copy(alpha = 0.9f),
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        }
    }
}

@Composable
fun BirrAmountText(
    amount: Double,
    modifier: Modifier = Modifier,
    style: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.headlineLarge,
    color: Color = Color.White,
    balanceVisible: Boolean = true,
) {
    if (balanceVisible) {
        Row(
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Birr symbol
            Icon(
                painter = painterResource(id = R.drawable.ic_birr),
                contentDescription = "Birr symbol",
                tint = color,
                modifier = Modifier.size(24.dp),
            )

            Spacer(modifier = Modifier.width(4.dp))

            // Formatted amount
            Text(
                text = formatBirrAmount(amount),
                style = style,
                color = color,
            )
        }
    } else {
        Text(
            text = "••••••",
            style = style,
            color = color,
            modifier = modifier,
        )
    }
}

private fun formatBirrAmount(amount: Double): String {
    // Create Ethiopian Birr formatter
    val symbols =
        DecimalFormatSymbols(Locale.US).apply {
            // Note: We can't easily change the currency symbol in Java's NumberFormat
            // So we'll manually format and add the Birr symbol
        }

    // Format the number part (without currency symbol)
    val numberFormat = DecimalFormat("#,##0.00", symbols)
    return numberFormat.format(amount)
}

private fun formatCurrency(amount: Double): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale.US)
    return formatter.format(amount)
}
