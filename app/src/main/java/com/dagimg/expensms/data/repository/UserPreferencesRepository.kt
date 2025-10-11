package com.dagimg.expensms.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Repository for user preferences using DataStore
 */
class UserPreferencesRepository(
    private val context: Context,
) {
    private val dataStore: DataStore<Preferences> = context.dataStore

    companion object {
        private val SMS_PERMISSION_KEY = booleanPreferencesKey("sms_permission_granted")
        private val NOTIFICATIONS_KEY = booleanPreferencesKey("notifications_enabled")
        private val BIOMETRIC_KEY = booleanPreferencesKey("biometric_enabled")
        private val USER_NAME_KEY = stringPreferencesKey("user_name")
        private val USER_EMAIL_KEY = stringPreferencesKey("user_email")
        private val THEME_KEY = stringPreferencesKey("theme")
    }

    fun getSmsPermissionGranted(): Flow<Boolean> =
        dataStore.data
            .map { preferences ->
                preferences[SMS_PERMISSION_KEY] ?: false
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

    suspend fun setSmsPermission(granted: Boolean) {
        dataStore.edit { preferences ->
            preferences[SMS_PERMISSION_KEY] = granted
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
}

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")
