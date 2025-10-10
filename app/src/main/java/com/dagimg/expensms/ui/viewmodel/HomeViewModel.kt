package com.dagimg.expensms.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.dagimg.expensms.data.dummy.DummyData
import com.dagimg.expensms.data.model.Bank
import com.dagimg.expensms.data.model.Transaction
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HomeViewModel : ViewModel() {
    // State for balance cards
    private val _balanceCardsState = MutableStateFlow<List<Bank>>(emptyList())
    val balanceCardsState: StateFlow<List<Bank>> = _balanceCardsState.asStateFlow()

    // State for recent transactions
    private val _recentTransactionsState = MutableStateFlow<List<Transaction>>(emptyList())
    val recentTransactionsState: StateFlow<List<Transaction>> = _recentTransactionsState.asStateFlow()

    // State for total balance
    private val _totalBalance = MutableStateFlow(0.0)
    val totalBalance: StateFlow<Double> = _totalBalance.asStateFlow()

    // State for user name
    private val _userName = MutableStateFlow("Alex")
    val userName: StateFlow<String> = _userName.asStateFlow()

    // State for current date
    private val _currentDate = MutableStateFlow("")
    val currentDate: StateFlow<String> = _currentDate.asStateFlow()

    init {
        loadData()
        updateCurrentDate()
    }

    private fun loadData() {
        // Load dummy data
        _balanceCardsState.value = DummyData.banks
        _recentTransactionsState.value = DummyData.recentTransactions
        _totalBalance.value = DummyData.totalBalance
    }

    private fun updateCurrentDate() {
        val dateFormat = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault())
        _currentDate.value = dateFormat.format(Date())
    }

    fun refreshData() {
        loadData()
        updateCurrentDate()
    }
}
