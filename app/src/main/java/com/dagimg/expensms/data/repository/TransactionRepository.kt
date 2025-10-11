package com.dagimg.expensms.data.repository

import android.content.Context
import com.dagimg.expensms.data.database.AppDatabase
import com.dagimg.expensms.data.database.TransactionEntity
import com.dagimg.expensms.data.model.Transaction
import com.dagimg.expensms.data.model.TransactionType
import com.dagimg.expensms.data.sms.ParsedTransaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Repository for managing transaction data
 * Uses Room database for persistence
 */
class TransactionRepository(
    private val context: Context,
) {
    private val database = AppDatabase.getInstance(context)

    // Flow of all transactions from the database
    val transactions: Flow<List<Transaction>> =
        database
            .transactionDao()
            .getAllTransactions()
            .map { entities -> entities.map { it.toDomainModel() } }

    // Convert entity to domain model
    private fun TransactionEntity.toDomainModel(): Transaction =
        Transaction(
            id = id,
            merchant = merchant,
            amount = amount,
            type = type,
            timestamp = timestamp,
            category = category,
            bankName = bankName,
            balanceAfter = balanceAfter,
            transactionUrl = transactionUrl,
            note = note,
            isEdited = isEdited,
            rawSmsContent = rawSmsContent,
            smsTimestamp = smsTimestamp,
            createdAt = createdAt,
        )

    // Convert domain model to entity
    private fun Transaction.toEntity(): TransactionEntity =
        TransactionEntity(
            id = id,
            merchant = merchant,
            amount = amount,
            type = type,
            timestamp = timestamp,
            category = category,
            bankName = bankName,
            balanceAfter = balanceAfter,
            transactionUrl = transactionUrl,
            note = note,
            isEdited = isEdited,
            rawSmsContent = rawSmsContent,
            smsTimestamp = smsTimestamp,
            createdAt = createdAt,
        )

    // Get recent transactions (last 10) - using DAO flow
    val recentTransactions: Flow<List<Transaction>> =
        database
            .transactionDao()
            .getRecentTransactions()
            .map { entities -> entities.map { it.toDomainModel() } }

    // Get all transactions for a specific bank
    fun getTransactionsForBank(bankName: String): Flow<List<Transaction>> =
        database
            .transactionDao()
            .getTransactionsByBank(bankName)
            .map { entities -> entities.map { it.toDomainModel() } }

    // Get list of available banks (distinct bank names from transactions)
    val availableBanks: Flow<List<String>> =
        database
            .transactionDao()
            .getDistinctBankNames()
            .map { it.sorted() }

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

        val entity = transaction.toEntity()
        val insertedId = database.transactionDao().insertTransaction(entity)

        println("DEBUG: Inserted transaction with ID: $insertedId")

        return insertedId
    }

    // Save transaction from parsed SMS (alias for addTransaction)
    suspend fun saveFromParsedTransaction(parsedTransaction: ParsedTransaction): Long =
        addTransaction(parsedTransaction)

    // Update an existing transaction
    suspend fun updateTransaction(updatedTransaction: Transaction) {
        val entity = updatedTransaction.toEntity()
        database.transactionDao().updateTransaction(entity)
        println("DEBUG: Updated transaction: ${updatedTransaction.id}")
    }

    // Delete a transaction
    suspend fun deleteTransaction(transactionId: Long) {
        database.transactionDao().deleteTransactionById(transactionId)
        println("DEBUG: Deleted transaction: $transactionId")
    }

    // Clear all transaction data
    suspend fun clearAllData() {
        database.transactionDao().deleteAllTransactions()
        println("DEBUG: Cleared all transaction data")
    }

    // Get transaction by ID
    suspend fun getTransactionById(id: Long): Transaction? =
        database.transactionDao().getTransactionById(id)?.toDomainModel()

    // Calculate total balance across all banks
    val totalBalance: Flow<Double> =
        database
            .transactionDao()
            .getAllTransactions()
            .map { entities ->
                val transactions = entities.map { it.toDomainModel() }
                // Group by bank and get latest balance for each
                transactions
                    .groupBy { it.bankName }
                    .mapNotNull { (_, bankTransactions) ->
                        bankTransactions.maxByOrNull { it.timestamp }?.balanceAfter
                    }.sum()
            }

    // Get all unique bank names
    val bankNames: Flow<List<String>> =
        database
            .transactionDao()
            .getAllTransactions()
            .map { entities ->
                entities.map { it.bankName }.distinct()
            }

    // Get balance cards data (for each bank)
    val balanceCards: Flow<List<com.dagimg.expensms.data.model.Bank>> =
        database
            .transactionDao()
            .getAllTransactions()
            .map { entities ->
                val transactions = entities.map { it.toDomainModel() }
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
            "commercial bank of ethiopia" -> "#86198F"
            "abyssinia bank" -> "#F6A701"
            "telebirr" -> "#1A88C5"
            else -> "#6b7280" // Gray for unknown banks
        }

    companion object {
        // Singleton instance - will be initialized with context
        private var instance: TransactionRepository? = null

        fun getInstance(context: Context): TransactionRepository =
            instance ?: TransactionRepository(context.applicationContext).also {
                instance = it
            }
    }
}
