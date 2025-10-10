package com.dagimg.expensms.data.biometric

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.fragment.app.FragmentActivity
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class BiometricAuthManager(
    private val context: Context,
) {
    private val biometricManager = BiometricManager.from(context)

    fun canAuthenticate(): Boolean =
        when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_SUCCESS -> true
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> false
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> false
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> false
            else -> false
        }

    suspend fun authenticate(activity: FragmentActivity): Boolean =
        suspendCancellableCoroutine { continuation ->
            val executor = { runnable: Runnable -> runnable.run() }

            val biometricPrompt =
                BiometricPrompt(
                    activity,
                    executor,
                    object : BiometricPrompt.AuthenticationCallback() {
                        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                            continuation.resume(true)
                        }

                        override fun onAuthenticationFailed() {
                            // User failed authentication, but can try again
                            // Don't resume here, let them try again
                        }

                        override fun onAuthenticationError(
                            errorCode: Int,
                            errString: CharSequence,
                        ) {
                            continuation.resume(false)
                        }
                    },
                )

            val promptInfo =
                BiometricPrompt.PromptInfo
                    .Builder()
                    .setTitle("Biometric Authentication")
                    .setSubtitle("Confirm your identity")
                    .setNegativeButtonText("Cancel")
                    .build()

            biometricPrompt.authenticate(promptInfo)
        }
}
