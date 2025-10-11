package com.dagimg.expensms.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.dagimg.expensms.data.model.Transaction
import com.dagimg.expensms.data.model.TransactionType
import com.dagimg.expensms.ui.theme.*
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionEditDialog(
    transaction: Transaction,
    onDismiss: () -> Unit,
    onSave: (Transaction) -> Unit,
) {
    var merchantName by remember { mutableStateOf(transaction.merchant) }
    var selectedCategory by remember { mutableStateOf(transaction.category) }
    var note by remember { mutableStateOf(transaction.note) }
    var isMerchantNameValid by remember { mutableStateOf(true) }

    val categoryOptions =
        listOf(
            "Income",
            "Groceries",
            "Food & Drink",
            "Transport",
            "Shopping",
            "Entertainment",
            "Bills & Utilities",
            "Healthcare",
            "Education",
            "Travel",
            "Other",
        )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Card(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(Spacing.lg),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = AppColors.Card),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        ) {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(Spacing.lg),
                verticalArrangement = Arrangement.spacedBy(Spacing.lg),
            ) {
                // Header
                Column(
                    verticalArrangement = Arrangement.spacedBy(Spacing.xs),
                ) {
                    Text(
                        text = "Edit Transaction",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = AppColors.Foreground,
                        fontSize = 20.sp,
                    )
                    Text(
                        text = "Update the merchant name, category and add notes",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppColors.MutedForeground,
                        fontSize = 14.sp,
                    )
                }

                // Transaction Summary Card (Reference Only)
                TransactionSummaryCard(transaction = transaction)

                // Edit Form
                Column(
                    verticalArrangement = Arrangement.spacedBy(Spacing.md),
                ) {
                    // Merchant Name Field
                    OutlinedTextField(
                        value = merchantName,
                        onValueChange = {
                            merchantName = it
                            isMerchantNameValid = it.isNotBlank()
                        },
                        label = {
                            Text(
                                "Merchant Name",
                                color = AppColors.Foreground,
                            )
                        },
                        placeholder = {
                            Text(
                                "Enter merchant name...",
                                color = AppColors.MutedForeground,
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors =
                            OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AppColors.Primary,
                                unfocusedBorderColor = if (isMerchantNameValid) AppColors.Border else Color.Red,
                                focusedContainerColor = AppColors.Card,
                                unfocusedContainerColor = AppColors.Card,
                                cursorColor = AppColors.Primary,
                            ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        isError = !isMerchantNameValid,
                        supportingText =
                            if (!isMerchantNameValid) {
                                { Text("Merchant name is required", color = Color.Red) }
                            } else {
                                null
                            },
                        singleLine = true,
                    )

                    // Category Dropdown
                    var expanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = it },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        OutlinedTextField(
                            value = selectedCategory,
                            onValueChange = { },
                            readOnly = true,
                            label = {
                                Text(
                                    "Category",
                                    color = AppColors.Foreground,
                                )
                            },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                            },
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(type = MenuAnchorType.PrimaryEditable, enabled = true),
                            colors =
                                OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = AppColors.Primary,
                                    unfocusedBorderColor = AppColors.Border,
                                    focusedContainerColor = AppColors.Card,
                                    unfocusedContainerColor = AppColors.Card,
                                ),
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                        ) {
                            categoryOptions.forEach { category ->
                                DropdownMenuItem(
                                    text = { Text(category) },
                                    onClick = {
                                        selectedCategory = category
                                        expanded = false
                                    },
                                )
                            }
                        }
                    }

                    // Note Field
                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
                        label = {
                            Text(
                                "Note (Optional)",
                                color = AppColors.Foreground,
                            )
                        },
                        placeholder = {
                            Text(
                                "Add a note about this transaction...",
                                color = AppColors.MutedForeground,
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors =
                            OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AppColors.Primary,
                                unfocusedBorderColor = AppColors.Border,
                                focusedContainerColor = AppColors.Card,
                                unfocusedContainerColor = AppColors.Card,
                                cursorColor = AppColors.Primary,
                            ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        minLines = 3,
                        maxLines = 3,
                        supportingText = {
                            Text(
                                "${note.length}/200 characters",
                                color = AppColors.MutedForeground,
                                fontSize = 12.sp,
                            )
                        },
                    )
                }

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                ) {
                    // Cancel Button
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors =
                            ButtonDefaults.outlinedButtonColors(
                                contentColor = AppColors.Foreground,
                            ),
                    ) {
                        Text("Cancel")
                    }

                    // Save Button
                    Button(
                        onClick = {
                            if (merchantName.isNotBlank()) {
                                val updatedTransaction =
                                    transaction.copy(
                                        merchant = merchantName,
                                        category = selectedCategory,
                                        note = note,
                                        isEdited = true,
                                        createdAt = System.currentTimeMillis(),
                                    )
                                onSave(updatedTransaction)
                            } else {
                                isMerchantNameValid = false
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor = AppColors.Primary,
                                contentColor = AppColors.Card,
                            ),
                        enabled = merchantName.isNotBlank(),
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }
}

@Composable
private fun TransactionSummaryCard(transaction: Transaction) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors =
            CardDefaults.cardColors(
                containerColor = AppColors.Muted.copy(alpha = 0.5f),
            ),
        border = BorderStroke(1.dp, AppColors.Border.copy(alpha = 0.3f)),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(Spacing.md),
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
                verticalArrangement = Arrangement.spacedBy(Spacing.xs),
            ) {
                // Amount (colored)
                Text(
                    text = formatTransactionAmount(transaction.amount, transaction.type),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = getTransactionColor(transaction.type),
                    fontSize = 16.sp,
                )

                // Date and Bank
                Text(
                    text = "${formatTransactionDate(transaction.timestamp)} • ${transaction.bankName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = AppColors.MutedForeground,
                    fontSize = 12.sp,
                )
            }

            // Link Icon (if transaction has URL)
            if (!transaction.transactionUrl.isNullOrEmpty()) {
                IconButton(
                    onClick = { },
                    modifier = Modifier.size(24.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Link,
                        contentDescription = "View Receipt",
                        tint = AppColors.MutedForeground,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun getTransactionColor(type: TransactionType): Color =
    when (type) {
        TransactionType.INCOME -> AppTransactionColors.Income
        TransactionType.EXPENSE -> AppTransactionColors.Expense
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
