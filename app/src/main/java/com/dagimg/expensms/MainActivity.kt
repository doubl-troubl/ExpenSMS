package com.dagimg.expensms

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dagimg.expensms.data.biometric.BiometricAuthManager
import com.dagimg.expensms.ui.viewmodel.SettingsViewModel
import kotlinx.coroutines.launch
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.dagimg.expensms.ui.navigation.NavigationItem
import com.dagimg.expensms.ui.navigation.navigationItems
import com.dagimg.expensms.ui.screens.HomeScreen
import com.dagimg.expensms.ui.screens.SettingsScreen
import com.dagimg.expensms.ui.screens.TransactionsScreen
import com.dagimg.expensms.ui.theme.AppTypography
import com.dagimg.expensms.ui.theme.LightColors

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            ExpenSMSApp(
                onOpenUrl = { url -> openUrl(url) },
            )
        }
    }

    private fun openUrl(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        } catch (e: Exception) {
            // Handle error - could show a toast or log
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FragmentActivity.ExpenSMSApp(onOpenUrl: (String) -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val settingsViewModel: SettingsViewModel = viewModel {
        SettingsViewModel(context.applicationContext as android.app.Application)
    }
    val biometricManager = remember { BiometricAuthManager(context) }
    val coroutineScope = rememberCoroutineScope()

    // Check if biometric authentication is required
    val biometricEnabled by settingsViewModel.biometricEnabled.collectAsState()
    var isAuthenticated by remember { mutableStateOf(false) }
    var authenticationChecked by remember { mutableStateOf(false) }

    // Check authentication on app start and when biometric setting changes
    LaunchedEffect(biometricEnabled) {
        if (biometricEnabled && biometricManager.canAuthenticate()) {
            val authenticated = biometricManager.authenticate(this@ExpenSMSApp)
            isAuthenticated = authenticated
            if (!authenticated) {
                // Authentication failed or was cancelled, close the app
                finish()
                return@LaunchedEffect
            }
        } else {
            isAuthenticated = true // No biometric required
        }
        authenticationChecked = true
    }

    // Show loading screen while checking authentication
    if (!authenticationChecked) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CircularProgressIndicator()
                Text("Checking authentication...")
            }
        }
        return
    }

    // If not authenticated and biometric is enabled, this shouldn't happen
    // because we finish() the activity above, but just in case
    if (biometricEnabled && !isAuthenticated) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Authentication required")
        }
        return
    }

    val navController = rememberNavController()

    MaterialTheme(
        typography = AppTypography,
        colorScheme =
            lightColorScheme(
                primary = LightColors.Primary,
                surface = LightColors.Card,
                background = LightColors.Background,
            ),
    ) {
        Scaffold(
            bottomBar = {
                NavigationBar(
                    containerColor = LightColors.Card,
                    contentColor = LightColors.Foreground,
                ) {
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentDestination = navBackStackEntry?.destination

                    navigationItems.forEach { item ->
                        val isSelected =
                            currentDestination?.hierarchy?.any {
                                it.route == item.route
                            } == true

                        NavigationBarItem(
                            icon = {
                                Icon(
                                    imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                    contentDescription = item.title,
                                )
                            },
                            label = {
                                Text(
                                    text = item.title,
                                    style = MaterialTheme.typography.bodySmall,
                                )
                            },
                            selected = isSelected,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            colors =
                                NavigationBarItemDefaults.colors(
                                    selectedIconColor = LightColors.Primary,
                                    selectedTextColor = LightColors.Primary,
                                    unselectedIconColor = LightColors.MutedForeground,
                                    unselectedTextColor = LightColors.MutedForeground,
                                    indicatorColor = LightColors.Primary.copy(alpha = 0.1f),
                                ),
                        )
                    }
                }
            },
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = NavigationItem.Home.route,
                modifier = Modifier.padding(innerPadding),
            ) {
                composable(NavigationItem.Home.route) {
                    HomeScreen(
                        onTransactionClick = { transaction ->
                            // TODO: Open transaction edit dialog
                        },
                        onSeeAllClick = {
                            navController.navigate(NavigationItem.Transactions.route)
                        },
                        onLinkClick = onOpenUrl,
                    )
                }

                composable(NavigationItem.Transactions.route) {
                    TransactionsScreen()
                }

                composable(NavigationItem.Settings.route) {
                    SettingsScreen()
                }
            }
        }
    }
}
