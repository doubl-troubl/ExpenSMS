package com.dagimg.expensms.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dagimg.expensms.data.biometric.BiometricAuthManager
import com.dagimg.expensms.data.repository.TransactionRepository
import com.dagimg.expensms.data.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class SettingsViewModel(
    application: Application,
) : AndroidViewModel(application) {
    private val userPreferencesRepository = UserPreferencesRepository(application)
    private val biometricManager = BiometricAuthManager(application)
    private val transactionRepository = TransactionRepository.getInstance(application)

    val smsPermissionGranted =
        userPreferencesRepository
            .getSmsPermissionGranted()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val notificationAccessGranted =
        userPreferencesRepository
            .getNotificationAccessGranted()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val notificationsEnabled =
        userPreferencesRepository
            .getNotificationsEnabled()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val biometricEnabled =
        userPreferencesRepository
            .getBiometricEnabled()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val userName =
        userPreferencesRepository
            .getUserName()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "User")

    val theme =
        userPreferencesRepository
            .getTheme()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "light")

    fun setSmsPermission(granted: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.setSmsPermission(granted)
        }
    }

    fun setNotificationAccess(granted: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.setNotificationAccess(granted)
        }
    }

    fun setNotifications(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.setNotifications(enabled)
        }
    }

    fun setBiometric(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.setBiometric(enabled)
        }
    }

    fun setTheme(theme: String) {
        viewModelScope.launch {
            userPreferencesRepository.setTheme(theme)
        }
    }

    fun canUseBiometric(): Boolean = biometricManager.canAuthenticate()

    // Synchronous method to get biometric preference (blocks until loaded)
    fun getBiometricEnabledSync(): Boolean =
        runBlocking {
            userPreferencesRepository.getBiometricEnabled().first()
        }

    // Synchronous method to get theme preference (blocks until loaded)
    fun getThemeSync(): String =
        runBlocking {
            userPreferencesRepository.getTheme().first()
        }

    // Synchronous methods for all settings (blocks until loaded)
    fun getSmsPermissionGrantedSync(): Boolean =
        runBlocking {
            userPreferencesRepository.getSmsPermissionGranted().first()
        }

    fun getNotificationAccessGrantedSync(): Boolean =
        runBlocking {
            userPreferencesRepository.getNotificationAccessGranted().first()
        }

    fun getNotificationsEnabledSync(): Boolean =
        runBlocking {
            userPreferencesRepository.getNotificationsEnabled().first()
        }

    fun getHistoricalParsingDoneSync(): Boolean =
        runBlocking {
            userPreferencesRepository.getHistoricalParsingDone().first()
        }

    fun setHistoricalParsingDone(done: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.setHistoricalParsingDone(done)
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            try {
                // Clear all transactions from database
                transactionRepository.clearAllData()

                // Reset historical parsing flag so it can be run again
                userPreferencesRepository.setHistoricalParsingDone(false)

                println("DEBUG: All data cleared successfully")
            } catch (e: Exception) {
                println("ERROR: Failed to clear data: ${e.message}")
                e.printStackTrace()
            }
        }
    }
}
