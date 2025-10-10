package com.dagimg.expensms.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Link
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
import com.dagimg.expensms.data.model.Transaction
import com.dagimg.expensms.data.model.TransactionType
import com.dagimg.expensms.ui.theme.*
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TransactionItem(
    transaction: Transaction,
    modifier: Modifier = Modifier,
    onTransactionClick: (Transaction) -> Unit = {},
    onLinkClick: (String) -> Unit = {},
) {
    Card(
        modifier =
            modifier
                .fillMaxWidth()
                .clickable { onTransactionClick(transaction) },
        shape = Shapes.medium,
        colors =
            CardDefaults.cardColors(
                containerColor = LightColors.Card,
            ),
        elevation =
            CardDefaults.cardElevation(
                defaultElevation = 1.dp,
                pressedElevation = 2.dp,
            ),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(Spacing.lg),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Transaction Type Icon
            TransactionTypeIcon(
                type = transaction.type,
                modifier = Modifier.padding(end = Spacing.md),
            )

            // Transaction Details
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                // Merchant Name
                Text(
                    text = transaction.merchant,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = LightColors.Foreground,
                    fontSize = 16.sp,
                )

                // Metadata: Date • Category • Bank
                Text(
                    text =
                        buildString {
                            append(formatTransactionDate(transaction.timestamp))
                            append(" • ")
                            append(transaction.category)
                            append(" • ")
                            append(transaction.bankName)
                        },
                    style = MaterialTheme.typography.bodySmall,
                    color = LightColors.MutedForeground,
                    fontSize = 12.sp,
                )
            }

            // Link Icon (if transaction has URL)
            if (!transaction.transactionUrl.isNullOrEmpty()) {
                IconButton(
                    onClick = { onLinkClick(transaction.transactionUrl) },
                    modifier = Modifier.size(24.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Link,
                        contentDescription = "View Receipt",
                        tint = LightColors.MutedForeground,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }

            // Amount
            Text(
                text = formatTransactionAmount(transaction.amount, transaction.type),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = getTransactionColor(transaction.type),
                fontSize = 16.sp,
            )
        }
    }
}

@Composable
fun TransactionTypeIcon(
    type: TransactionType,
    modifier: Modifier = Modifier,
) {
    val (backgroundColor, icon, iconColor) =
        when (type) {
            TransactionType.INCOME ->
                Triple(
                    TransactionColors.IncomeBackgroundLight,
                    Icons.Default.ArrowDownward,
                    TransactionColors.IncomeLight,
                )
            TransactionType.EXPENSE ->
                Triple(
                    TransactionColors.ExpenseBackgroundLight,
                    Icons.Default.ArrowUpward,
                    TransactionColors.ExpenseLight,
                )
        }

    Box(
        modifier =
            modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(backgroundColor),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = type.name,
            tint = iconColor,
            modifier = Modifier.size(20.dp),
        )
    }
}

private fun getTransactionColor(type: TransactionType): Color =
    when (type) {
        TransactionType.INCOME -> TransactionColors.IncomeLight
        TransactionType.EXPENSE -> TransactionColors.ExpenseLight
    }

private fun formatTransactionAmount(
    amount: Double,
    type: TransactionType,
): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale.US)
    val formattedAmount = formatter.format(amount)
    return when (type) {
        TransactionType.INCOME -> "+$formattedAmount"
        TransactionType.EXPENSE -> "-$formattedAmount"
    }.replace("$", "").let {
        when (type) {
            TransactionType.INCOME -> "+$$it"
            TransactionType.EXPENSE -> "-$$it"
        }
    }
}

private fun formatTransactionDate(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diffInMillis = now - timestamp
    val diffInHours = diffInMillis / (1000 * 60 * 60)
    val diffInDays = diffInMillis / (1000 * 60 * 60 * 24)

    return when {
        diffInHours < 24 -> "Today"
        diffInDays == 1L -> "Yesterday"
        diffInDays < 7 -> "$diffInDays days ago"
        else -> {
            val dateFormat = SimpleDateFormat("MMM d", Locale.getDefault())
            dateFormat.format(Date(timestamp))
        }
    }
}

@Preview
@Composable
fun TransactionItemPreview() {
    val dummyTransactions =
        listOf(
            Transaction(
                id = 1,
                merchant = "Salary Deposit",
                amount = 3500.00,
                type = TransactionType.INCOME,
                timestamp = System.currentTimeMillis() - 32400000, // 9 hours ago
                category = "Income",
                bankName = "Chase",
                balanceAfter = 27209.68,
            ),
            Transaction(
                id = 2,
                merchant = "Whole Foods Market",
                amount = 156.42,
                type = TransactionType.EXPENSE,
                timestamp = System.currentTimeMillis() - 19800000, // 5.5 hours ago
                category = "Groceries",
                bankName = "Chase",
                balanceAfter = 26053.26,
            ),
        )

    MaterialTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            dummyTransactions.forEach { transaction ->
                TransactionItem(transaction = transaction)
            }
        }
    }
}
