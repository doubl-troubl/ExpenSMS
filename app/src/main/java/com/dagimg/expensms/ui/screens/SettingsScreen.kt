package com.dagimg.expensms.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Help
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Notifications
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
import com.dagimg.expensms.data.permissions.rememberSmsPermissionLauncher
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
    val initialNotificationsEnabled by remember { mutableStateOf(settingsViewModel.getNotificationsEnabledSync()) }
    val initialBiometricEnabled by remember { mutableStateOf(settingsViewModel.getBiometricEnabledSync()) }

    // Collect state from ViewModel (these will update reactively after initial load)
    val smsPermissionGranted by settingsViewModel.smsPermissionGranted.collectAsState(
        initial = initialSmsPermissionGranted,
    )
    val notificationsEnabled by settingsViewModel.notificationsEnabled.collectAsState(
        initial = initialNotificationsEnabled,
    )
    val biometricEnabled by settingsViewModel.biometricEnabled.collectAsState(initial = initialBiometricEnabled)

    val smsPermissionLauncher =
        rememberSmsPermissionLauncher(
            onGranted = {
                settingsViewModel.setSmsPermission(true)
            },
            onDenied = {
                settingsViewModel.setSmsPermission(false)
            },
        )

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(horizontal = Spacing.lg),
        verticalArrangement = Arrangement.spacedBy(Spacing.lg),
    ) {
        Spacer(modifier = Modifier.height(Spacing.lg))

        // Profile Card
        ProfileCard()

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
                        checked = notificationsEnabled,
                        onCheckedChange = { enabled ->
                            settingsViewModel.setNotifications(enabled)
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

            SettingItem(
                icon = {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = "Privacy",
                        tint = AppColors.Foreground,
                    )
                },
                title = "Privacy Policy",
                description = "Learn about our privacy practices",
                onClick = {
                    // TODO: Open privacy policy
                },
            )

            SettingItem(
                icon = {
                    Icon(
                        imageVector = Icons.Outlined.Help,
                        contentDescription = "Support",
                        tint = AppColors.Foreground,
                    )
                },
                title = "Contact Support",
                description = "Get help with your account",
                onClick = {
                    // TODO: Open support contact
                },
            )
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
}

@Composable
private fun ProfileCard() {
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
                    text = "A",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                )
            }

            Spacer(modifier = Modifier.width(Spacing.md))

            // User Info
            Column {
                Text(
                    text = "Alex Thompson",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = AppColors.Foreground,
                    fontSize = 18.sp,
                )

                Text(
                    text = "alex.thompson@email.com",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppColors.MutedForeground,
                    fontSize = 14.sp,
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
