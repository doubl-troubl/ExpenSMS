package com.dagimg.expensms.data.database

import androidx.room.TypeConverter
import com.dagimg.expensms.data.model.TransactionType

class TransactionTypeConverter {
    @TypeConverter
    fun fromTransactionType(type: TransactionType): String = type.name

    @TypeConverter
    fun toTransactionType(value: String): TransactionType =
        try {
            TransactionType.valueOf(value)
        } catch (e: IllegalArgumentException) {
            TransactionType.EXPENSE // Default fallback
        }
}
