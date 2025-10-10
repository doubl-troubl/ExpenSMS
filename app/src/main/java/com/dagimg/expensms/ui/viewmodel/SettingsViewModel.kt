package com.dagimg.expensms.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dagimg.expensms.data.biometric.BiometricAuthManager
import com.dagimg.expensms.data.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    application: Application,
) : AndroidViewModel(application) {
    private val userPreferencesRepository = UserPreferencesRepository(application)
    private val biometricManager = BiometricAuthManager(application)

    val smsPermissionGranted =
        userPreferencesRepository
            .getSmsPermissionGranted()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val notificationsEnabled =
        userPreferencesRepository
            .getNotificationsEnabled()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val biometricEnabled =
        userPreferencesRepository
            .getBiometricEnabled()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun setSmsPermission(granted: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.setSmsPermission(granted)
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

    fun canUseBiometric(): Boolean = biometricManager.canAuthenticate()
}
