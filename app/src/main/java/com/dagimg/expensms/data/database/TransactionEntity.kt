package com.dagimg.expensms.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.dagimg.expensms.data.model.TransactionType

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey
    val id: Long,
    val merchant: String,
    val amount: Double,
    val type: TransactionType,
    val timestamp: Long,
    val category: String,
    val bankName: String,
    val balanceAfter: Double,
    val transactionUrl: String?,
    val rawSmsContent: String,
    val smsTimestamp: Long,
    val note: String,
    val isEdited: Boolean,
    val createdAt: Long,
)
