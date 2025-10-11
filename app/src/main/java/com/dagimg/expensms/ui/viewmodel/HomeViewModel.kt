package com.dagimg.expensms.ui.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dagimg.expensms.data.repository.TransactionRepository
import com.dagimg.expensms.data.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HomeViewModel(
    application: Application,
) : ViewModel() {
    private val transactionRepository = TransactionRepository.getInstance(application)
    private val userPreferencesRepository = UserPreferencesRepository(application)

    // State for balance cards - derived from transaction data
    val balanceCardsState =
        transactionRepository.balanceCards
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // State for recent transactions
    val recentTransactionsState =
        transactionRepository.recentTransactions
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // State for total balance across all banks
    val totalBalance =
        transactionRepository.totalBalance
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // State for user name (from extracted SMS data or default)
    val userName: StateFlow<String> =
        userPreferencesRepository
            .getUserName()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "User")

    // State for balance visibility (eye icon toggle)
    val balanceVisible: StateFlow<Boolean> =
        userPreferencesRepository
            .getBalanceVisible()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    // State for current date
    private val _currentDate = MutableStateFlow("")
    val currentDate: StateFlow<String> = _currentDate.asStateFlow()

    init {
        println("DEBUG: HomeViewModel initialized")
        updateCurrentDate()
    }

    private fun updateCurrentDate() {
        val dateFormat = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault())
        _currentDate.value = dateFormat.format(Date())
    }

    fun toggleBalanceVisibility() {
        viewModelScope.launch {
            val currentVisible = balanceVisible.value
            userPreferencesRepository.setBalanceVisible(!currentVisible)
            println("DEBUG: Balance visibility toggled to: ${!currentVisible}")
        }
    }

    fun refreshData() {
        println("DEBUG: HomeViewModel.refreshData() called")
        updateCurrentDate()
        // Transaction data is automatically updated through Flow

        // Debug current state
        println("DEBUG: Current state check:")
        println("DEBUG: - balanceCardsState: ${balanceCardsState.value.size} cards")
        println("DEBUG: - recentTransactionsState: ${recentTransactionsState.value.size} transactions")
        println("DEBUG: - totalBalance: ${totalBalance.value}")
        println("DEBUG: - balanceVisible: ${balanceVisible.value}")
    }
}
