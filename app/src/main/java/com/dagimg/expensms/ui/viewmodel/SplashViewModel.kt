package com.dagimg.expensms.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dagimg.expensms.data.biometric.BiometricAuthManager
import com.dagimg.expensms.data.repository.UserPreferencesRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class SplashState(
    val isLoading: Boolean = true,
    val shouldShowAuth: Boolean = false,
    val isComplete: Boolean = false,
    val loadingMessage: String = "Initializing...",
)

class SplashViewModel(
    application: Application,
) : AndroidViewModel(application) {
    private val userPreferencesRepository = UserPreferencesRepository(application)
    private val biometricManager = BiometricAuthManager(application)

    private val _state = MutableStateFlow(SplashState())
    val state: StateFlow<SplashState> = _state.asStateFlow()

    init {
        initializeApp()
    }

    private fun initializeApp() {
        viewModelScope.launch {
            try {
                // Show loading for at least 2 seconds for the beautiful animation
                val startTime = System.currentTimeMillis()

                // Step 1: Load user preferences
                updateLoadingMessage("Loading preferences...")
                delay(500)

                val biometricEnabled = userPreferencesRepository.getBiometricEnabled().first()
                val theme = userPreferencesRepository.getTheme().first()
                val smsPermissionGranted = userPreferencesRepository.getSmsPermissionGranted().first()

                // Step 2: Check biometric availability
                updateLoadingMessage("Checking security settings...")
                delay(500)

                val canUseBiometric = biometricManager.canAuthenticate()
                val shouldShowAuth = biometricEnabled && canUseBiometric

                // Step 3: Prepare app data
                updateLoadingMessage("Preparing your financial data...")
                delay(500)

                // Step 4: Final setup
                updateLoadingMessage("Almost ready...")
                delay(300)

                // Ensure minimum splash time for UX
                val elapsedTime = System.currentTimeMillis() - startTime
                val remainingTime = 2500 - elapsedTime
                if (remainingTime > 0) {
                    delay(remainingTime)
                }

                // Complete initialization
                _state.value =
                    SplashState(
                        isLoading = false,
                        shouldShowAuth = shouldShowAuth,
                        isComplete = true,
                        loadingMessage = "Ready!",
                    )
            } catch (e: Exception) {
                // Handle initialization error
                _state.value =
                    SplashState(
                        isLoading = false,
                        shouldShowAuth = false,
                        isComplete = true,
                        loadingMessage = "Error occurred",
                    )
            }
        }
    }

    private fun updateLoadingMessage(message: String) {
        _state.value = _state.value.copy(loadingMessage = message)
    }

    fun onSplashComplete() {
        _state.value = _state.value.copy(isComplete = true)
    }
}
