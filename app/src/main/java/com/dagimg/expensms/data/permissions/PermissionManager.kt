package com.dagimg.expensms.data.permissions

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

/**
 * Manages SMS and other app permissions
 */
class PermissionManager(
    private val activity: Activity,
) {
    val smsPermissions =
        arrayOf(
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.READ_SMS,
        )

    /**
     * Check if SMS permissions are granted
     */
    fun hasSmsPermissions(): Boolean =
        smsPermissions.all { permission ->
            ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED
        }

    /**
     * Request SMS permissions with rationale
     */
    fun requestSmsPermissions(
        onGranted: () -> Unit,
        onDenied: () -> Unit,
    ) {
        val permissionsToRequest =
            smsPermissions.filter { permission ->
                ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED
            }

        if (permissionsToRequest.isEmpty()) {
            onGranted()
            return
        }

        // Show rationale if needed
        if (shouldShowRationale(permissionsToRequest)) {
            // TODO: Show rationale dialog
            // For now, request directly
        }

        // Request permissions
        ActivityCompat.requestPermissions(
            activity,
            permissionsToRequest.toTypedArray(),
            PERMISSION_REQUEST_CODE,
        )
    }

    private fun shouldShowRationale(permissions: List<String>): Boolean =
        permissions.any { permission ->
            ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)
        }

    /**
     * Handle permission result
     */
    fun handlePermissionResult(
        permissions: Map<String, Boolean>,
        onAllGranted: () -> Unit,
        onSomeDenied: (List<String>) -> Unit,
    ) {
        val deniedPermissions = permissions.filter { !it.value }.keys.toList()

        if (deniedPermissions.isEmpty()) {
            onAllGranted()
        } else {
            onSomeDenied(deniedPermissions)
        }
    }

    companion object {
        private const val PERMISSION_REQUEST_CODE = 1001
    }
}

/**
 * Composable for handling SMS permissions
 */
@Composable
fun rememberSmsPermissionLauncher(
    onGranted: () -> Unit,
    onDenied: () -> Unit,
): (Boolean) -> Unit {
    val smsPermissions =
        arrayOf(
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.READ_SMS,
        )

    val launcher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestMultiplePermissions(),
            onResult = { permissions ->
                val allGranted = permissions.all { it.value }
                if (allGranted) {
                    onGranted()
                } else {
                    onDenied()
                }
            },
        )

    return { shouldRequest ->
        if (shouldRequest) {
            launcher.launch(smsPermissions)
        }
    }
}
