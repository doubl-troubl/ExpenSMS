package com.dagimg.expensms.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dagimg.expensms.data.model.Bank
import com.dagimg.expensms.data.model.Transaction
import com.dagimg.expensms.ui.components.BalanceCard
import com.dagimg.expensms.ui.components.TotalBalanceCard
import com.dagimg.expensms.ui.components.TransactionItem
import com.dagimg.expensms.ui.theme.*
import com.dagimg.expensms.ui.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    homeViewModel: HomeViewModel = viewModel(),
    onTransactionClick: (Transaction) -> Unit = {},
    onSeeAllClick: () -> Unit = {},
    onLinkClick: (String) -> Unit = {},
) {
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
        modifier =
            modifier
                .fillMaxSize()
                .padding(horizontal = Spacing.lg),
        verticalArrangement = Arrangement.spacedBy(Spacing.xl),
    ) {
        item {
            Spacer(modifier = Modifier.height(Spacing.lg))
        }

        // Header Section
        item {
            HomeHeader(
                userName = userName,
                currentDate = currentDate,
            )
        }

        // Balance Cards Section
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
            )
        }

        // Transactions List
        items(recentTransactions) { transaction ->
            TransactionItem(
                transaction = transaction,
                onTransactionClick = onTransactionClick,
                onLinkClick = onLinkClick,
            )
        }

        item {
            Spacer(modifier = Modifier.height(Spacing.xxl))
        }
    }
}

@Composable
private fun HomeHeader(
    userName: String,
    currentDate: String,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(Spacing.xs),
    ) {
        Text(
            text = "Hi, $userName",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.SemiBold,
            color = LightColors.Foreground,
            fontSize = 24.sp,
        )
        Text(
            text = currentDate,
            style = MaterialTheme.typography.bodyMedium,
            color = LightColors.MutedForeground,
            fontSize = 14.sp,
        )
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
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(pageCount) { page ->
            Box(
                modifier =
                    Modifier
                        .size(if (page == currentPage) 8.dp else 6.dp)
                        .padding(horizontal = 2.dp),
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    shape = MaterialTheme.shapes.small,
                    color =
                        if (page == currentPage) {
                            LightColors.Foreground
                        } else {
                            LightColors.MutedForeground.copy(alpha = 0.3f)
                        },
                ) {}
            }
        }
    }
}

@Composable
private fun RecentTransactionsHeader(onSeeAllClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "Recent Transactions",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Medium,
            color = LightColors.Foreground,
            fontSize = 20.sp,
        )

        TextButton(
            onClick = onSeeAllClick,
        ) {
            Text(
                text = "See All",
                style = MaterialTheme.typography.bodyMedium,
                color = LightColors.Primary,
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
