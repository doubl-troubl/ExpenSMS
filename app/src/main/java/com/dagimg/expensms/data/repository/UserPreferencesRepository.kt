package com.dagimg.expensms.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Repository for user preferences using DataStore
 */
class UserPreferencesRepository(
    private val context: Context,
) {
    private val dataStore: DataStore<Preferences> = context.dataStore

    companion object {
        private val SMS_PERMISSION_KEY = booleanPreferencesKey("sms_permission_granted")
        private val NOTIFICATION_ACCESS_KEY = booleanPreferencesKey("notification_access_granted")
        private val NOTIFICATIONS_KEY = booleanPreferencesKey("notifications_enabled")
        private val BIOMETRIC_KEY = booleanPreferencesKey("biometric_enabled")
        private val HISTORICAL_PARSING_DONE_KEY = booleanPreferencesKey("historical_parsing_done")
        private val USER_NAME_KEY = stringPreferencesKey("user_name")
        private val USER_EMAIL_KEY = stringPreferencesKey("user_email")
        private val THEME_KEY = stringPreferencesKey("theme")
        private val USER_NAME_EXTRACTED_KEY = booleanPreferencesKey("user_name_extracted")

        // Track if we've extracted a user name
        private val BALANCE_VISIBLE_KEY = booleanPreferencesKey("balance_visible")
        // Track if balance amounts are visible
        private val CUSTOM_CATEGORIES_KEY = stringPreferencesKey("custom_categories")
        // JSON string of custom categories
    }

    fun getSmsPermissionGranted(): Flow<Boolean> =
        dataStore.data
            .map { preferences ->
                preferences[SMS_PERMISSION_KEY] ?: false
            }

    fun getNotificationAccessGranted(): Flow<Boolean> =
        dataStore.data
            .map { preferences ->
                preferences[NOTIFICATION_ACCESS_KEY] ?: false
            }

    fun getNotificationsEnabled(): Flow<Boolean> =
        dataStore.data
            .map { preferences ->
                preferences[NOTIFICATIONS_KEY] ?: true
            }

    fun getBiometricEnabled(): Flow<Boolean> =
        dataStore.data
            .map { preferences ->
                preferences[BIOMETRIC_KEY] ?: false
            }

    fun getUserName(): Flow<String> =
        dataStore.data
            .map { preferences ->
                preferences[USER_NAME_KEY] ?: "User"
            }

    fun getUserEmail(): Flow<String> =
        dataStore.data
            .map { preferences ->
                preferences[USER_EMAIL_KEY] ?: ""
            }

    fun getTheme(): Flow<String> =
        dataStore.data
            .map { preferences ->
                preferences[THEME_KEY] ?: "light"
            }

    fun getHistoricalParsingDone(): Flow<Boolean> =
        dataStore.data
            .map { preferences ->
                preferences[HISTORICAL_PARSING_DONE_KEY] ?: false
            }

    fun getUserNameExtracted(): Flow<Boolean> =
        dataStore.data
            .map { preferences ->
                preferences[USER_NAME_EXTRACTED_KEY] ?: false
            }

    fun getBalanceVisible(): Flow<Boolean> =
        dataStore.data
            .map { preferences ->
                preferences[BALANCE_VISIBLE_KEY] ?: true // Default to visible
            }

    suspend fun setSmsPermission(granted: Boolean) {
        dataStore.edit { preferences ->
            preferences[SMS_PERMISSION_KEY] = granted
        }
    }

    suspend fun setNotificationAccess(granted: Boolean) {
        dataStore.edit { preferences ->
            preferences[NOTIFICATION_ACCESS_KEY] = granted
        }
    }

    suspend fun setNotifications(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[NOTIFICATIONS_KEY] = enabled
        }
    }

    suspend fun setBiometric(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[BIOMETRIC_KEY] = enabled
        }
    }

    suspend fun setUserName(name: String) {
        dataStore.edit { preferences ->
            preferences[USER_NAME_KEY] = name
            preferences[USER_NAME_EXTRACTED_KEY] = true
        }
    }

    suspend fun setUserEmail(email: String) {
        dataStore.edit { preferences ->
            preferences[USER_EMAIL_KEY] = email
        }
    }

    suspend fun setTheme(theme: String) {
        dataStore.edit { preferences ->
            preferences[THEME_KEY] = theme
        }
    }

    suspend fun setHistoricalParsingDone(done: Boolean) {
        dataStore.edit { preferences ->
            preferences[HISTORICAL_PARSING_DONE_KEY] = done
        }
    }

    suspend fun setBalanceVisible(visible: Boolean) {
        dataStore.edit { preferences ->
            preferences[BALANCE_VISIBLE_KEY] = visible
        }
    }

    fun getCustomCategories(): Flow<List<String>> =
        dataStore.data
            .map { preferences ->
                val categoriesJson = preferences[CUSTOM_CATEGORIES_KEY] ?: "[]"
                try {
                    Json.decodeFromString<List<String>>(categoriesJson)
                } catch (e: Exception) {
                    emptyList()
                }
            }

    suspend fun setCustomCategories(categories: List<String>) {
        val categoriesJson = Json.encodeToString(categories)
        dataStore.edit { preferences ->
            preferences[CUSTOM_CATEGORIES_KEY] = categoriesJson
        }
    }

    suspend fun addCustomCategory(category: String) {
        val currentCategories = getCustomCategories().first()
        val updatedCategories = (currentCategories + category).distinct() // Avoid duplicates
        setCustomCategories(updatedCategories)
    }
}

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")
