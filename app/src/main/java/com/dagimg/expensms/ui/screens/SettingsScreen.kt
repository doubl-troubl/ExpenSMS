package com.dagimg.expensms.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dagimg.expensms.data.biometric.BiometricAuthManager
import com.dagimg.expensms.data.permissions.PermissionManager
import com.dagimg.expensms.data.permissions.rememberNotificationPermissionLauncher
import com.dagimg.expensms.data.permissions.rememberSmsPermissionLauncher
import com.dagimg.expensms.data.sms.HistoricalSmsParser
import com.dagimg.expensms.ui.components.SettingItem
import com.dagimg.expensms.ui.theme.*
import com.dagimg.expensms.ui.viewmodel.SettingsViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val settingsViewModel: SettingsViewModel =
        viewModel {
            SettingsViewModel(context.applicationContext as android.app.Application)
        }
    val permissionManager = remember { PermissionManager(context as androidx.fragment.app.FragmentActivity) }
    val biometricManager = remember { BiometricAuthManager(context) }
    val activity = context as? FragmentActivity
    val coroutineScope = rememberCoroutineScope()

    // Get initial values synchronously to prevent toggle animation from defaults
    val initialSmsPermissionGranted by remember { mutableStateOf(settingsViewModel.getSmsPermissionGrantedSync()) }
    val initialNotificationAccessGranted by remember {
        mutableStateOf(settingsViewModel.getNotificationAccessGrantedSync())
    }
    val initialNotificationsEnabled by remember { mutableStateOf(settingsViewModel.getNotificationsEnabledSync()) }
    val initialBiometricEnabled by remember { mutableStateOf(settingsViewModel.getBiometricEnabledSync()) }
    val initialNotificationPermissionGranted by remember {
        mutableStateOf(permissionManager.hasNotificationPermissions())
    }

    // Collect state from ViewModel (these will update reactively after initial load)
    val smsPermissionGranted by settingsViewModel.smsPermissionGranted.collectAsState(
        initial = initialSmsPermissionGranted,
    )
    val notificationAccessGranted by settingsViewModel.notificationAccessGranted.collectAsState(
        initial = initialNotificationAccessGranted,
    )
    val notificationsEnabled by settingsViewModel.notificationsEnabled.collectAsState(
        initial = initialNotificationsEnabled,
    )
    val biometricEnabled by settingsViewModel.biometricEnabled.collectAsState(initial = initialBiometricEnabled)
    val userName by settingsViewModel.userName.collectAsState(initial = "User")

    // State for notification permissions (checked when screen is shown)
    var notificationPermissionGranted by remember { mutableStateOf(initialNotificationPermissionGranted) }

    // State for clear data confirmation dialog
    var showClearDataDialog by remember { mutableStateOf(false) }

    // Function to trigger historical parsing
    fun triggerHistoricalParsing(forceRun: Boolean = false) {
        coroutineScope.launch {
            try {
                // Check if parsing has already been done (unless forced)
                if (!forceRun && settingsViewModel.getHistoricalParsingDoneSync()) {
                    println("DEBUG: Historical parsing already done, skipping")
                    return@launch
                }

                val parser = HistoricalSmsParser.create(context)
                val count = parser.parseHistoricalSms()
                println("DEBUG: Parsed $count historical transactions")

                // Mark as done
                settingsViewModel.setHistoricalParsingDone(true)

                // TODO: Show success message to user
            } catch (e: Exception) {
                println("ERROR: Failed to parse historical SMS: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    val smsPermissionLauncher =
        rememberSmsPermissionLauncher(
            onGranted = {
                settingsViewModel.setSmsPermission(true)
                // Trigger historical SMS parsing when SMS permission is first granted
                triggerHistoricalParsing()
            },
            onDenied = {
                settingsViewModel.setSmsPermission(false)
            },
        )

    val notificationPermissionLauncher =
        rememberNotificationPermissionLauncher(
            onGranted = {
                notificationPermissionGranted = true
                settingsViewModel.setNotifications(true)
            },
            onDenied = {
                notificationPermissionGranted = false
                settingsViewModel.setNotifications(false)
            },
        )

    // Check notification access and permissions status when screen is shown
    LaunchedEffect(Unit) {
        val hasAccess = permissionManager.hasNotificationAccess()
        settingsViewModel.setNotificationAccess(hasAccess)

        val hasNotificationPermission = permissionManager.hasNotificationPermissions()
        notificationPermissionGranted = hasNotificationPermission
    }

    // Auto-trigger historical parsing when SMS permission is first granted (one-time)
    LaunchedEffect(smsPermissionGranted) {
        if (smsPermissionGranted && permissionManager.hasSmsPermissions()) {
            // Only trigger if this hasn't been done before
            if (!settingsViewModel.getHistoricalParsingDoneSync()) {
                triggerHistoricalParsing()
            }
        }
    }

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(horizontal = Spacing.lg),
        verticalArrangement = Arrangement.spacedBy(Spacing.lg),
    ) {
        Spacer(modifier = Modifier.height(Spacing.lg))

        // Profile Card
        ProfileCard(userName = userName)

        // Permissions & Security Section
        SettingsSection(title = "Permissions & Security") {
            // SMS Access
            SettingItem(
                icon = {
                    Icon(
                        imageVector = Icons.Outlined.Email,
                        contentDescription = "SMS",
                        tint = AppColors.Foreground,
                    )
                },
                title = "SMS Access",
                description = "Required to read bank transaction messages",
                action = {
                    Switch(
                        checked = smsPermissionGranted,
                        onCheckedChange = { checked ->
                            if (checked) {
                                smsPermissionLauncher(true)
                            } else {
                                // Revoke SMS permission
                                settingsViewModel.setSmsPermission(false)
                            }
                        },
                        colors =
                            SwitchDefaults.colors(
                                checkedThumbColor = AppColors.Primary,
                                checkedTrackColor = AppColors.Primary.copy(alpha = 0.3f),
                            ),
                    )
                },
            )

            // Notification Access
            SettingItem(
                icon = {
                    Icon(
                        imageVector = Icons.Outlined.NotificationsActive,
                        contentDescription = "Notification Access",
                        tint = AppColors.Foreground,
                    )
                },
                title = "Notification Access",
                description = "Required for real-time SMS parsing when app is closed",
                action = {
                    Switch(
                        checked = notificationAccessGranted,
                        onCheckedChange = { checked ->
                            if (checked) {
                                // Open notification listener settings
                                permissionManager.requestNotificationAccess()
                            } else {
                                // User wants to disable - update preference
                                settingsViewModel.setNotificationAccess(false)
                            }
                        },
                        colors =
                            SwitchDefaults.colors(
                                checkedThumbColor = AppColors.Primary,
                                checkedTrackColor = AppColors.Primary.copy(alpha = 0.3f),
                            ),
                    )
                },
            )

            // Notifications
            SettingItem(
                icon = {
                    Icon(
                        imageVector = Icons.Outlined.Notifications,
                        contentDescription = "Notifications",
                        tint = AppColors.Foreground,
                    )
                },
                title = "Notifications",
                description = "Get notified about new transactions",
                action = {
                    Switch(
                        checked = notificationsEnabled && notificationPermissionGranted,
                        onCheckedChange = { enabled ->
                            if (enabled) {
                                if (notificationPermissionGranted) {
                                    settingsViewModel.setNotifications(true)
                                } else {
                                    notificationPermissionLauncher(true)
                                }
                            } else {
                                settingsViewModel.setNotifications(false)
                            }
                        },
                        colors =
                            SwitchDefaults.colors(
                                checkedThumbColor = AppColors.Primary,
                                checkedTrackColor = AppColors.Primary.copy(alpha = 0.3f),
                            ),
                    )
                },
            )

            // Biometric Lock
            SettingItem(
                icon = {
                    Icon(
                        imageVector = Icons.Outlined.Lock,
                        contentDescription = "Biometric",
                        tint = AppColors.Foreground,
                    )
                },
                title = "Biometric Lock",
                description = "Use fingerprint or face ID to unlock",
                action = {
                    Switch(
                        checked = biometricEnabled,
                        onCheckedChange = { enabled ->
                            if (enabled) {
                                // When enabling biometric, check if authentication is available and prompt
                                if (biometricManager.canAuthenticate() && activity != null) {
                                    coroutineScope.launch {
                                        val authenticated = biometricManager.authenticate(activity)
                                        settingsViewModel.setBiometric(authenticated)
                                    }
                                } else {
                                    // Biometric not available, don't enable
                                    settingsViewModel.setBiometric(false)
                                }
                            } else {
                                // When disabling, just set to false
                                settingsViewModel.setBiometric(false)
                            }
                        },
                        colors =
                            SwitchDefaults.colors(
                                checkedThumbColor = AppColors.Primary,
                                checkedTrackColor = AppColors.Primary.copy(alpha = 0.3f),
                            ),
                    )
                },
            )
        }

        // Data & Support Section
        SettingsSection(title = "Data & Support") {
            // Parse Historical SMS
            SettingItem(
                icon = {
                    Icon(
                        imageVector = Icons.Outlined.Email,
                        contentDescription = "Parse SMS",
                        tint = AppColors.Foreground,
                    )
                },
                title = "Parse Historical SMS",
                description = "Scan past 30 days of SMS for transactions",
                onClick = {
                    if (permissionManager.hasSmsPermissions()) {
                        triggerHistoricalParsing(forceRun = true)
                    } else {
                        // Show message that SMS permission is needed
                        println("DEBUG: SMS permission required for historical parsing")
                    }
                },
            )

            SettingItem(
                icon = {
                    Icon(
                        imageVector = Icons.Outlined.Save,
                        contentDescription = "Export",
                        tint = AppColors.Foreground,
                    )
                },
                title = "Export Data",
                description = "Export your transaction data",
                onClick = {
                    // TODO: Implement data export
                },
            )

            // Clear Data
            SettingItem(
                icon = {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = "Clear Data",
                        tint = AppColors.Foreground,
                    )
                },
                title = "Clear Data",
                description = "Delete all transactions and reset app data",
                onClick = {
                    // Show confirmation dialog
                    showClearDataDialog = true
                },
            )

            // SettingItem(
            //     icon = {
            //         Icon(
            //             imageVector = Icons.Outlined.Info,
            //             contentDescription = "Privacy",
            //             tint = AppColors.Foreground,
            //         )
            //     },
            //     title = "Privacy Policy",
            //     description = "Learn about our privacy practices",
            //     onClick = {
            //         // TODO: Open privacy policy
            //     },
            // )
        }

        // App Info
        Spacer(modifier = Modifier.weight(1f))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors =
                CardDefaults.cardColors(
                    containerColor = AppColors.Card,
                ),
        ) {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(Spacing.lg),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "ExpenSMS v1.0.0",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppColors.MutedForeground,
                    fontSize = 14.sp,
                )

                Spacer(modifier = Modifier.height(Spacing.xs))

                Text(
                    text = "Made with care for modern banking",
                    style = MaterialTheme.typography.bodySmall,
                    color = AppColors.MutedForeground.copy(alpha = 0.7f),
                    fontSize = 12.sp,
                )
            }
        }

        Spacer(modifier = Modifier.height(Spacing.lg))
    }

    // Clear Data Confirmation Dialog
    if (showClearDataDialog) {
        AlertDialog(
            onDismissRequest = { showClearDataDialog = false },
            title = {
                Text(
                    text = "Clear All Data",
                    style = MaterialTheme.typography.headlineSmall,
                    color = AppColors.Foreground,
                )
            },
            text = {
                Text(
                    text =
                        "This will permanently delete all transactions and reset the app. " +
                            "This action cannot be undone. Are you sure you want to continue?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppColors.MutedForeground,
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        settingsViewModel.clearAllData()
                        showClearDataDialog = false
                    },
                ) {
                    Text(
                        text = "Clear Data",
                        color = AppColors.Primary,
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showClearDataDialog = false },
                ) {
                    Text(
                        text = "Cancel",
                        color = AppColors.MutedForeground,
                    )
                }
            },
            containerColor = AppColors.Card,
        )
    }
}

@Composable
private fun ProfileCard(userName: String) {
    val displayName = if (userName.isBlank() || userName == "User") "User" else userName
    val initial = displayName.firstOrNull()?.uppercaseChar() ?: 'U'

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors =
            CardDefaults.cardColors(
                containerColor = AppColors.Card,
            ),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(Spacing.lg),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Avatar
            Box(
                modifier =
                    Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            brush =
                                Brush.linearGradient(
                                    colors =
                                        listOf(
                                            Color(0xFF3B82F6), // Blue
                                            Color(0xFF8B5CF6), // Purple
                                        ),
                                ),
                        ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = initial.toString(),
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                )
            }

            Spacer(modifier = Modifier.width(Spacing.md))

            // User Info
            Column {
                Text(
                    text = displayName,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = AppColors.Foreground,
                    fontSize = 18.sp,
                )
            }
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable () -> Unit,
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Medium,
            color = AppColors.Foreground,
            fontSize = 16.sp,
            modifier = Modifier.padding(bottom = Spacing.sm),
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors =
                CardDefaults.cardColors(
                    containerColor = AppColors.Card,
                ),
        ) {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = Spacing.sm),
            ) {
                content()
            }
        }
    }
}
