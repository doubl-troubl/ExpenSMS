package com.dagimg.expensms.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dagimg.expensms.data.model.Transaction
import com.dagimg.expensms.data.repository.TransactionRepository
import com.dagimg.expensms.ui.components.TransactionItem
import com.dagimg.expensms.ui.theme.AppColors
import com.dagimg.expensms.ui.theme.Spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreen(
    modifier: Modifier = Modifier,
    repository: TransactionRepository,
    onTransactionClick: (Transaction) -> Unit = {},
    onLinkClick: (String) -> Unit = {},
) {
    val transactions by repository.transactions.collectAsState(initial = emptyList())
    var searchQuery by remember { mutableStateOf("") }
    var selectedBankFilter by remember { mutableStateOf("All Banks") }
    var selectedCategoryFilter by remember { mutableStateOf("All Categories") }
    var selectedTimeFilter by remember { mutableStateOf("All Time") }

    val filteredTransactions =
        transactions.filter { transaction ->
            // Search filter
            searchQuery.isEmpty() ||
                transaction.merchant.contains(searchQuery, ignoreCase = true) ||
                transaction.category.contains(searchQuery, ignoreCase = true)

            // Bank filter (for now, just check if bank name matches)
            selectedBankFilter == "All Banks" || transaction.bankName == selectedBankFilter

            // Category filter (for now, just check if category matches)
            selectedCategoryFilter == "All Categories" || transaction.category == selectedCategoryFilter

            // Time filter (for now, just return all - can be enhanced later)
            selectedTimeFilter == "All Time" || true
        }

    Column(modifier = modifier.fillMaxSize()) {
        // Header
        Text(
            text = "Transactions",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.SemiBold,
            color = AppColors.Foreground,
            modifier = Modifier.padding(horizontal = Spacing.lg, vertical = Spacing.md),
        )

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = {
                Text(
                    "Search transactions...",
                    color = AppColors.MutedForeground,
                )
            },
            leadingIcon = {
                Icon(
                    Icons.Filled.Search,
                    contentDescription = "Search",
                    tint = AppColors.MutedForeground,
                )
            },
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.xl)
                    .padding(bottom = Spacing.md)
                    .clip(RoundedCornerShape(12.dp)),
            colors =
                OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AppColors.Primary,
                    unfocusedBorderColor = AppColors.Border,
                    focusedContainerColor = AppColors.Card,
                    unfocusedContainerColor = AppColors.Card,
                ),
            singleLine = true,
        )

        // Filter Chips
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.xl)
                    .padding(bottom = Spacing.md),
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            FilterChip(
                label = "All Banks",
                selected = selectedBankFilter == "All Banks",
                onClick = { selectedBankFilter = "All Banks" },
            )

            FilterChip(
                label = "Chase",
                selected = selectedBankFilter == "Chase",
                onClick = { selectedBankFilter = "Chase" },
            )

            FilterChip(
                label = "CBE",
                selected = selectedBankFilter == "Commercial Bank of Ethiopia",
                onClick = { selectedBankFilter = "Commercial Bank of Ethiopia" },
            )
        }

        // Results count
        Text(
            text = "${filteredTransactions.size} transactions found",
            style = MaterialTheme.typography.bodySmall,
            color = AppColors.MutedForeground,
            modifier = Modifier.padding(horizontal = Spacing.xl, vertical = Spacing.sm),
        )

        // Transactions List
        if (filteredTransactions.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "No transactions found",
                    style = MaterialTheme.typography.bodyLarge,
                    color = AppColors.MutedForeground,
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(Spacing.sm),
                contentPadding = PaddingValues(horizontal = Spacing.xl, vertical = Spacing.sm),
            ) {
                items(filteredTransactions) { transaction ->
                    TransactionItem(
                        transaction = transaction,
                        onTransactionClick = onTransactionClick,
                        onLinkClick = onLinkClick,
                    )
                }
            }
        }
    }
}

@Composable
private fun FilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        colors =
            FilterChipDefaults.filterChipColors(
                selectedContainerColor = AppColors.Primary,
                selectedLabelColor = AppColors.Card,
            ),
    )
}

// SettingsScreen moved to SettingsScreen.kt
