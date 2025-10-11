package com.dagimg.expensms.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dagimg.expensms.data.model.Transaction
import com.dagimg.expensms.data.repository.TransactionRepository
import com.dagimg.expensms.ui.components.TransactionEditDialog
import com.dagimg.expensms.ui.components.TransactionItem
import com.dagimg.expensms.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreen(
    modifier: Modifier = Modifier,
    repository: TransactionRepository,
    onTransactionClick: (Transaction) -> Unit = {},
    onLinkClick: (String) -> Unit = {},
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Dialog state
    var showEditDialog by remember { mutableStateOf(false) }
    var selectedTransaction by remember { mutableStateOf<Transaction?>(null) }

    val transactions by repository.transactions.collectAsState(initial = emptyList())
    val availableBanks by repository.availableBanks.collectAsState(initial = emptyList())

    // Get custom categories from SettingsViewModel
    val settingsViewModel: com.dagimg.expensms.ui.viewmodel.SettingsViewModel =
        androidx.lifecycle.viewmodel.compose.viewModel {
            com.dagimg.expensms.ui.viewmodel.SettingsViewModel(context.applicationContext as android.app.Application)
        }
    val customCategories by settingsViewModel.customCategories.collectAsState()

    // Define filter options
    val categoryOptions =
        listOf(
            "All Categories",
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

    val timeOptions = listOf("All Time", "Today", "Last 7 Days", "Last 30 Days")

    var searchQuery by remember { mutableStateOf("") }
    var selectedBankFilters by remember { mutableStateOf(setOf("All Banks")) }
    var selectedCategoryFilters by remember { mutableStateOf(setOf("All Categories")) }
    var selectedTimeFilter by remember { mutableStateOf("All Time") }

    val filteredTransactions =
        transactions.filter { transaction ->
            // Search filter
            val matchesSearch =
                searchQuery.isEmpty() ||
                    transaction.merchant.contains(searchQuery, ignoreCase = true) ||
                    transaction.category.contains(searchQuery, ignoreCase = true)

            // Bank filter - OR logic: show if any selected bank matches OR "All Banks" is selected
            val matchesBank =
                selectedBankFilters.contains("All Banks") ||
                    selectedBankFilters.contains(transaction.bankName)

            // Category filter - OR logic: show if any selected category matches OR "All Categories" is selected
            val matchesCategory =
                selectedCategoryFilters.contains("All Categories") ||
                    selectedCategoryFilters.contains(transaction.category)

            // Time filter - single selection
            val matchesTime =
                selectedTimeFilter == "All Time" ||
                    isTransactionInTimeRange(transaction.timestamp, selectedTimeFilter)

            // All filters must match (AND logic between filter types, OR logic within each type)
            matchesSearch && matchesBank && matchesCategory && matchesTime
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
                    .padding(horizontal = Spacing.md)
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

        // Bank Filter Chips
        LazyRow(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.xl)
                    .padding(bottom = Spacing.sm),
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            item {
                FilterChip(
                    label = "All Banks",
                    selected = selectedBankFilters.contains("All Banks"),
                    onClick = {
                        selectedBankFilters = setOf("All Banks")
                    },
                )
            }
            items(availableBanks) { bankName ->
                FilterChip(
                    label = bankName,
                    selected = selectedBankFilters.contains(bankName),
                    onClick = {
                        val newSelection = selectedBankFilters.toMutableSet()
                        if (newSelection.contains(bankName)) {
                            // If this was the only specific bank selected, go back to "All Banks"
                            newSelection.remove(bankName)
                            if (newSelection.isEmpty() || newSelection == setOf("All Banks")) {
                                newSelection.clear()
                                newSelection.add("All Banks")
                            }
                        } else {
                            // Remove "All Banks" and add this specific bank
                            newSelection.remove("All Banks")
                            newSelection.add(bankName)
                        }
                        selectedBankFilters = newSelection
                    },
                )
            }
        }

        // Category Filter Chips
        LazyRow(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.xl)
                    .padding(bottom = Spacing.sm),
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            items(categoryOptions) { category ->
                FilterChip(
                    label = category,
                    selected = selectedCategoryFilters.contains(category),
                    onClick = {
                        if (category == "All Categories") {
                            selectedCategoryFilters = setOf("All Categories")
                        } else {
                            val newSelection = selectedCategoryFilters.toMutableSet()
                            if (newSelection.contains(category)) {
                                // If this was the only specific category selected, go back to "All Categories"
                                newSelection.remove(category)
                                if (newSelection.isEmpty() || newSelection == setOf("All Categories")) {
                                    newSelection.clear()
                                    newSelection.add("All Categories")
                                }
                            } else {
                                // Remove "All Categories" and add this specific category
                                newSelection.remove("All Categories")
                                newSelection.add(category)
                            }
                            selectedCategoryFilters = newSelection
                        }
                    },
                )
            }
        }

        // Time Filter Chips
        LazyRow(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.xl)
                    .padding(bottom = Spacing.md),
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            items(timeOptions) { timeOption ->
                FilterChip(
                    label = timeOption,
                    selected = selectedTimeFilter == timeOption,
                    onClick = {
                        selectedTimeFilter = timeOption
                    },
                )
            }
        }

        // Results count and sum
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.xl, vertical = Spacing.sm),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "${filteredTransactions.size} transactions found",
                style = MaterialTheme.typography.bodySmall,
                color = AppColors.MutedForeground,
            )

            // Calculate and display transaction sum
            val totalSum =
                filteredTransactions.sumOf { transaction ->
                    if (transaction.type == com.dagimg.expensms.data.model.TransactionType.INCOME) {
                        transaction.amount
                    } else {
                        -transaction.amount
                    }
                }

            Text(
                text = formatTransactionSum(totalSum),
                style = MaterialTheme.typography.bodySmall,
                color = if (totalSum >= 0) AppTransactionColors.Income else AppTransactionColors.Expense,
                fontWeight = FontWeight.Medium,
            )
        }

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
                        onTransactionClick = { clickedTransaction ->
                            selectedTransaction = clickedTransaction
                            showEditDialog = true
                        },
                        onLinkClick = onLinkClick,
                    )
                }
            }
        }
    }

    // Transaction Edit Dialog
    if (showEditDialog && selectedTransaction != null) {
        TransactionEditDialog(
            transaction = selectedTransaction!!,
            customCategories = customCategories,
            onAddCustomCategory = { category ->
                settingsViewModel.addCustomCategory(category)
            },
            onDismiss = {
                showEditDialog = false
                selectedTransaction = null
            },
            onSave = { updatedTransaction ->
                scope.launch {
                    try {
                        repository.updateTransaction(updatedTransaction)
                        showEditDialog = false
                        selectedTransaction = null
                    } catch (e: Exception) {
                        // Handle error - could show a snackbar
                        showEditDialog = false
                        selectedTransaction = null
                    }
                }
            },
        )
    }
}

// Helper function to check if transaction is within time range
private fun isTransactionInTimeRange(
    timestamp: Long,
    timeFilter: String,
): Boolean {
    val now = System.currentTimeMillis()
    val transactionTime = timestamp

    return when (timeFilter) {
        "Today" -> {
            // Get start of today (midnight)
            val calendar = java.util.Calendar.getInstance()
            calendar.timeInMillis = now
            calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
            calendar.set(java.util.Calendar.MINUTE, 0)
            calendar.set(java.util.Calendar.SECOND, 0)
            calendar.set(java.util.Calendar.MILLISECOND, 0)
            val todayStart = calendar.timeInMillis
            transactionTime >= todayStart
        }
        "Last 7 Days" -> {
            val sevenDaysAgo = now - (7 * 24 * 60 * 60 * 1000)
            transactionTime >= sevenDaysAgo
        }
        "Last 30 Days" -> {
            val thirtyDaysAgo = now - (30L * 24 * 60 * 60 * 1000)
            transactionTime >= thirtyDaysAgo
        }
        else -> true
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

// Format transaction sum for display
private fun formatTransactionSum(amount: Double): String {
    val numberFormat = java.text.DecimalFormat("#,##0.00")
    val formattedAmount = numberFormat.format(kotlin.math.abs(amount))

    return if (amount >= 0) {
        "+$formattedAmount"
    } else {
        "-$formattedAmount"
    }
}
