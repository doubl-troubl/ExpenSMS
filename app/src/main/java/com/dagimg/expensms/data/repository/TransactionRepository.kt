package com.dagimg.expensms.data.repository

import com.dagimg.expensms.data.model.Transaction
import com.dagimg.expensms.data.model.TransactionType
import com.dagimg.expensms.data.sms.ParsedTransaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

/**
 * Repository for managing transaction data
 * Currently uses in-memory storage, can be replaced with database later
 */
class TransactionRepository {
    // In-memory storage for transactions
    private val _transactions = MutableStateFlow<List<Transaction>>(emptyList())
    val transactions: Flow<List<Transaction>> = _transactions.asStateFlow()

    // Get recent transactions (last 10)
    val recentTransactions: Flow<List<Transaction>> =
        transactions.map { transactions ->
            transactions.sortedByDescending { it.timestamp }.take(10)
        }

    // Get all transactions for a specific bank
    fun getTransactionsForBank(bankName: String): Flow<List<Transaction>> =
        transactions.map { transactions ->
            transactions
                .filter { it.bankName == bankName }
                .sortedByDescending { it.timestamp }
        }

    // Add a new transaction from parsed SMS
    suspend fun addTransaction(parsedTransaction: ParsedTransaction): Long {
        println("DEBUG: TransactionRepository.addTransaction called with: $parsedTransaction")

        val transaction =
            Transaction(
                id = generateId(),
                merchant = parsedTransaction.merchant,
                amount = parsedTransaction.amount,
                type = parsedTransaction.type,
                timestamp = parsedTransaction.timestamp,
                category = categorizeTransaction(parsedTransaction.merchant, parsedTransaction.type),
                bankName = parsedTransaction.bankName,
                balanceAfter = parsedTransaction.balanceAfter,
                transactionUrl = parsedTransaction.transactionUrl,
                rawSmsContent = parsedTransaction.rawSms,
                smsTimestamp = System.currentTimeMillis(),
            )

        println("DEBUG: Created transaction object: $transaction")

        val currentList = _transactions.value
        println("DEBUG: Current transactions count: ${currentList.size}")

        _transactions.value = currentList + transaction

        println("DEBUG: Added transaction, new count: ${currentList.size + 1}")

        return transaction.id
    }

    // Update an existing transaction
    suspend fun updateTransaction(updatedTransaction: Transaction) {
        val currentList = _transactions.value
        _transactions.value =
            currentList.map { transaction ->
                if (transaction.id == updatedTransaction.id) updatedTransaction else transaction
            }
    }

    // Delete a transaction
    suspend fun deleteTransaction(transactionId: Long) {
        val currentList = _transactions.value
        _transactions.value = currentList.filter { it.id != transactionId }
    }

    // Get transaction by ID
    fun getTransactionById(id: Long): Transaction? = _transactions.value.find { it.id == id }

    // Calculate total balance across all banks
    val totalBalance: Flow<Double> =
        transactions.map { transactions ->
            // Group by bank and get latest balance for each
            transactions
                .groupBy { it.bankName }
                .mapNotNull { (_, bankTransactions) ->
                    bankTransactions.maxByOrNull { it.timestamp }?.balanceAfter
                }.sum()
        }

    // Get all unique bank names
    val bankNames: Flow<List<String>> =
        transactions.map { transactions ->
            transactions.map { it.bankName }.distinct()
        }

    // Get balance cards data (for each bank)
    val balanceCards: Flow<List<com.dagimg.expensms.data.model.Bank>> =
        transactions.map { transactions ->
            // Group transactions by bank
            transactions
                .groupBy { it.bankName }
                .map { (bankName, bankTransactions) ->
                    val latestTransaction = bankTransactions.maxByOrNull { it.timestamp }
                    com.dagimg.expensms.data.model.Bank(
                        id = bankName.hashCode().toLong(), // Simple ID generation
                        name = bankName,
                        displayName = bankName,
                        colorHex = getBankColor(bankName),
                        currentBalance = latestTransaction?.balanceAfter ?: 0.0,
                        lastUpdated = latestTransaction?.timestamp ?: 0L,
                    )
                }
        }

    private fun generateId(): Long = System.currentTimeMillis()

    private fun categorizeTransaction(
        merchant: String,
        type: TransactionType,
    ): String {
        // Simple categorization logic
        val merchantLower = merchant.lowercase()

        return when {
            // Food & Dining
            merchantLower.contains("restaurant") ||
                merchantLower.contains("cafe") ||
                merchantLower.contains("food") ||
                merchantLower.contains("hotel") ||
                merchantLower.contains("bar") -> "Food & Dining"

            // Shopping
            merchantLower.contains("shop") ||
                merchantLower.contains("store") ||
                merchantLower.contains("mart") ||
                merchantLower.contains("supermarket") -> "Shopping"

            // Transportation
            merchantLower.contains("taxi") ||
                merchantLower.contains("uber") ||
                merchantLower.contains("bus") ||
                merchantLower.contains("fuel") ||
                merchantLower.contains("petrol") -> "Transportation"

            // Utilities
            merchantLower.contains("electric") ||
                merchantLower.contains("water") ||
                merchantLower.contains("internet") ||
                merchantLower.contains("phone") ||
                merchantLower.contains("telecom") -> "Utilities"

            // Healthcare
            merchantLower.contains("hospital") ||
                merchantLower.contains("clinic") ||
                merchantLower.contains("pharmacy") ||
                merchantLower.contains("medical") -> "Healthcare"

            // Entertainment
            merchantLower.contains("cinema") ||
                merchantLower.contains("movie") ||
                merchantLower.contains("game") ||
                merchantLower.contains("entertainment") -> "Entertainment"

            // Transfer/Income
            type == TransactionType.INCOME -> "Income"
            merchantLower.contains("transfer") ||
                merchantLower.contains("salary") -> "Transfer"

            // Default
            else -> "Other"
        }
    }

    private fun getBankColor(bankName: String): String =
        when (bankName.lowercase()) {
            "commercial bank of ethiopia" -> "#1e40af" // Blue
            "dashen bank" -> "#dc2626" // Red
            "awash international bank" -> "#059669" // Green
            "bank of abyssinia" -> "#7c3aed" // Purple
            "bunna international bank" -> "#ea580c" // Orange
            "deutsche geschaftsbank" -> "#0891b2" // Cyan
            "lion international bank" -> "#c2410c" // Brown
            "zemen bank" -> "#be185d" // Pink
            "cooperative bank" -> "#365314" // Dark Green
            else -> "#6b7280" // Gray for unknown banks
        }

    companion object {
        // Singleton instance for in-memory storage
        val instance by lazy { TransactionRepository() }
    }
}
