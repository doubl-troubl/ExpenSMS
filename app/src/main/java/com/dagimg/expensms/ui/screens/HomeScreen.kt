package com.dagimg.expensms.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dagimg.expensms.data.model.Bank
import com.dagimg.expensms.data.model.Transaction
import com.dagimg.expensms.data.repository.TransactionRepository
import com.dagimg.expensms.ui.components.BalanceCard
import com.dagimg.expensms.ui.components.ThemeToggleButton
import com.dagimg.expensms.ui.components.TotalBalanceCard
import com.dagimg.expensms.ui.components.TransactionEditDialog
import com.dagimg.expensms.ui.components.TransactionItem
import com.dagimg.expensms.ui.theme.*
import com.dagimg.expensms.ui.viewmodel.HomeViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    onTransactionClick: (Transaction) -> Unit = {},
    onSeeAllClick: () -> Unit = {},
    onLinkClick: (String) -> Unit = {},
    onThemeChange: (String) -> Unit = {},
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val homeViewModel: HomeViewModel =
        viewModel {
            HomeViewModel(context.applicationContext as android.app.Application)
        }
    val transactionRepository = remember { TransactionRepository.getInstance(context) }

    // Dialog state
    var showEditDialog by remember { mutableStateOf(false) }
    var selectedTransaction by remember { mutableStateOf<Transaction?>(null) }

    // Collect state from ViewModel
    val userName by homeViewModel.userName.collectAsState()
    val currentDate by homeViewModel.currentDate.collectAsState()
    val totalBalance by homeViewModel.totalBalance.collectAsState()
    val banks by homeViewModel.balanceCardsState.collectAsState()
    val recentTransactions by homeViewModel.recentTransactionsState.collectAsState()

    // Create list of cards (Total + Individual banks)
    val allCards =
        remember(totalBalance, banks) {
            listOf(
                // Create a dummy bank object for total balance
                Bank(
                    id = -1,
                    name = "total",
                    displayName = "Total Balance",
                    colorHex = "#10B981",
                    currentBalance = totalBalance,
                    lastUpdated = System.currentTimeMillis(),
                ),
            ) + banks
        }

    val pagerState = rememberPagerState(pageCount = { allCards.size })

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(Spacing.lg),
    ) {
        item {
            Spacer(modifier = Modifier.height(Spacing.lg))
        }

        // Header Section
        item {
            HomeHeader(
                userName = userName,
                currentDate = currentDate,
                onThemeChange = onThemeChange,
                modifier = Modifier.padding(horizontal = Spacing.lg),
            )
        }

        // Balance Cards Section (no horizontal padding - cards handle it)
        item {
            BalanceCardsSection(
                cards = allCards,
                pagerState = pagerState,
            )
        }

        // Recent Transactions Section
        item {
            RecentTransactionsHeader(
                onSeeAllClick = onSeeAllClick,
                modifier = Modifier.padding(horizontal = Spacing.lg),
            )
        }

        // Transactions List
        items(recentTransactions) { transaction ->
            TransactionItem(
                transaction = transaction,
                onTransactionClick = { clickedTransaction ->
                    selectedTransaction = clickedTransaction
                    showEditDialog = true
                },
                onLinkClick = onLinkClick,
                modifier = Modifier.padding(horizontal = Spacing.lg),
            )
        }

        item {
            Spacer(modifier = Modifier.height(Spacing.xxl))
        }
    }

    // Transaction Edit Dialog
    if (showEditDialog && selectedTransaction != null) {
        TransactionEditDialog(
            transaction = selectedTransaction!!,
            onDismiss = {
                showEditDialog = false
                selectedTransaction = null
            },
            onSave = { updatedTransaction ->
                scope.launch {
                    try {
                        transactionRepository.updateTransaction(updatedTransaction)
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

@Composable
private fun HomeHeader(
    userName: String,
    currentDate: String,
    onThemeChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Theme Toggle Button on the left
        ThemeToggleButton(
            onThemeChange = onThemeChange,
        )

        // Greeting and Date on the right
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(Spacing.xs),
        ) {
            Text(
                text = "Hi, $userName",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.SemiBold,
                color = AppColors.Foreground,
                fontSize = 24.sp,
            )
            Text(
                text = currentDate,
                style = MaterialTheme.typography.bodyMedium,
                color = AppColors.MutedForeground,
                fontSize = 14.sp,
            )
        }
    }
}

@Composable
private fun BalanceCardsSection(
    cards: List<Bank>,
    pagerState: PagerState,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(Spacing.md),
    ) {
        // Horizontal Pager for cards
        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(horizontal = 0.dp),
            pageSpacing = Spacing.lg,
        ) { page ->
            val card = cards[page]
            if (page == 0) {
                // Total Balance Card
                TotalBalanceCard(totalBalance = card.currentBalance)
            } else {
                // Individual Bank Card
                BalanceCard(bank = card)
            }
        }

        // Page Indicators
        if (cards.size > 1) {
            PagerIndicators(
                pageCount = cards.size,
                currentPage = pagerState.currentPage,
            )
        }
    }
}

@Composable
private fun PagerIndicators(
    pageCount: Int,
    currentPage: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(pageCount) { page ->
            val isActive = page == currentPage
            Box(
                modifier =
                    Modifier
                        .padding(horizontal = 4.dp)
                        .width(if (isActive) 24.dp else 8.dp)
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            if (isActive) {
                                AppColors.Foreground
                            } else {
                                AppColors.MutedForeground.copy(alpha = 0.25f)
                            },
                        ),
            )
        }
    }
}

@Composable
private fun RecentTransactionsHeader(
    onSeeAllClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "Recent Transactions",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Medium,
            color = AppColors.Foreground,
            fontSize = 20.sp,
        )

        TextButton(
            onClick = onSeeAllClick,
        ) {
            Text(
                text = "See All",
                style = MaterialTheme.typography.bodyMedium,
                color = AppColors.Primary,
                fontSize = 14.sp,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    MaterialTheme {
        HomeScreen()
    }
}
