package com.dagimg.expensms.data.permissions

import android.Manifest
import android.app.Activity
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.dagimg.expensms.data.sms.SmsNotificationListener

/**
 * Manages SMS and other app permissions
 */
class PermissionManager(
    private val activity: Activity,
) {
    val smsPermissions =
        arrayOf(
            Manifest.permission.READ_SMS,
        )

    val notificationPermissions =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            emptyArray()
        }

    /**
     * Check if SMS permissions are granted
     */
    fun hasSmsPermissions(): Boolean =
        smsPermissions.all { permission ->
            ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED
        }

    /**
     * Check if notification permissions are granted
     */
    fun hasNotificationPermissions(): Boolean =
        notificationPermissions.all { permission ->
            ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED
        }

    /**
     * Request notification permissions
     */
    fun requestNotificationPermissions(
        onGranted: () -> Unit,
        onDenied: () -> Unit,
    ) {
        val permissionsToRequest =
            notificationPermissions.filter { permission ->
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
            NOTIFICATION_PERMISSION_REQUEST_CODE,
        )
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

    /**
     * Check if notification access is granted
     */
    fun hasNotificationAccess(): Boolean {
        val enabledNotificationListeners =
            Settings.Secure.getString(
                activity.contentResolver,
                "enabled_notification_listeners",
            )
        val packageName = activity.packageName
        return enabledNotificationListeners?.contains(packageName) == true
    }

    /**
     * Request notification access by opening settings
     */
    fun requestNotificationAccess() {
        val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
        activity.startActivity(intent)
    }

    /**
     * Check if notification listener service is enabled for our app
     */
    fun isNotificationListenerEnabled(): Boolean {
        val cn = ComponentName(activity, SmsNotificationListener::class.java)
        val flat =
            Settings.Secure.getString(
                activity.contentResolver,
                "enabled_notification_listeners",
            )
        return flat?.contains(cn.flattenToString()) == true
    }

    companion object {
        private const val PERMISSION_REQUEST_CODE = 1001
        private const val NOTIFICATION_PERMISSION_REQUEST_CODE = 1002
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

/**
 * Composable for handling notification permissions
 */
@Composable
fun rememberNotificationPermissionLauncher(
    onGranted: () -> Unit,
    onDenied: () -> Unit,
): (Boolean) -> Unit {
    val notificationPermissions =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            emptyArray()
        }

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
            launcher.launch(notificationPermissions)
        }
    }
}
