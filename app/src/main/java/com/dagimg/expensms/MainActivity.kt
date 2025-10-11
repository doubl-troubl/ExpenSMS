package com.dagimg.expensms

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.dagimg.expensms.data.biometric.BiometricAuthManager
import com.dagimg.expensms.data.repository.TransactionRepository
import com.dagimg.expensms.ui.navigation.NavigationItem
import com.dagimg.expensms.ui.navigation.navigationItems
import com.dagimg.expensms.ui.screens.AuthScreen
import com.dagimg.expensms.ui.screens.HomeScreen
import com.dagimg.expensms.ui.screens.SettingsScreen
import com.dagimg.expensms.ui.screens.SplashScreen
import com.dagimg.expensms.ui.screens.TransactionsScreen
import com.dagimg.expensms.ui.theme.AppColors
import com.dagimg.expensms.ui.theme.AppTheme
import com.dagimg.expensms.ui.theme.AppTypography
import com.dagimg.expensms.ui.theme.LocalAppTheme
import com.dagimg.expensms.ui.viewmodel.SettingsViewModel
import com.dagimg.expensms.ui.viewmodel.SplashViewModel
import androidx.core.net.toUri

class MainActivity : FragmentActivity() {
    private fun updateSystemBarsTheme(isDarkTheme: Boolean) {
        val window = window
        val insetsController = WindowCompat.getInsetsController(window, window.decorView)

        // Set status bar icons to be dark when app theme is light (for visibility)
        // Set status bar icons to be light when app theme is dark
        insetsController.isAppearanceLightStatusBars = !isDarkTheme

        // Set navigation bar icons to be dark when app theme is light (for visibility)
        // Set navigation bar icons to be light when app theme is dark
        insetsController.isAppearanceLightNavigationBars = !isDarkTheme
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            ExpenSMSApp(
                onOpenUrl = { url -> openUrl(url) },
                onUpdateSystemBars = { isDark -> updateSystemBarsTheme(isDark) },
            )
        }
    }

    private fun openUrl(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, url.toUri())
            startActivity(intent)
        } catch (e: Exception) {
            // Handle error - could show a toast or log
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FragmentActivity.ExpenSMSApp(
    onOpenUrl: (String) -> Unit,
    onUpdateSystemBars: (Boolean) -> Unit,
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val settingsViewModel: SettingsViewModel =
        viewModel {
            SettingsViewModel(context.applicationContext as android.app.Application)
        }
    val splashViewModel: SplashViewModel =
        viewModel {
            SplashViewModel(context.applicationContext as android.app.Application)
        }
    val biometricManager = remember { BiometricAuthManager(context) }
    val transactionRepository = remember { TransactionRepository.getInstance(context) }

    val navController = rememberNavController()

    // Get current theme reactively
    val currentTheme by settingsViewModel.theme.collectAsState()
    val appTheme = if (currentTheme == "dark") AppTheme.DARK else AppTheme.LIGHT

    // Get splash state
    val splashState by splashViewModel.state.collectAsState()

    val startDestination = NavigationItem.Splash.route

    // Update system bars when theme changes
    LaunchedEffect(appTheme) {
        onUpdateSystemBars(appTheme == AppTheme.DARK)
    }

    CompositionLocalProvider(LocalAppTheme provides appTheme) {
        MaterialTheme(
            typography = AppTypography,
            colorScheme =
                if (appTheme == AppTheme.DARK) {
                    darkColorScheme(
                        primary = androidx.compose.ui.graphics.Color.White,
                        surface =
                            androidx.compose.ui.graphics
                                .Color(0xFF1A1A1A),
                        background =
                            androidx.compose.ui.graphics
                                .Color(0xFF1A1A1A),
                        onSurface = androidx.compose.ui.graphics.Color.White,
                        onBackground = androidx.compose.ui.graphics.Color.White,
                    )
                } else {
                    lightColorScheme(
                        primary =
                            androidx.compose.ui.graphics
                                .Color(0xFF030213),
                        surface = androidx.compose.ui.graphics.Color.White,
                        background = androidx.compose.ui.graphics.Color.White,
                        onSurface =
                            androidx.compose.ui.graphics
                                .Color(0xFF1A1A1A),
                        onBackground =
                            androidx.compose.ui.graphics
                                .Color(0xFF1A1A1A),
                    )
                },
        ) {
            Scaffold(
                bottomBar = {
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentDestination = navBackStackEntry?.destination
                    val currentRoute = currentDestination?.route

                    // Only show bottom navigation on main app screens (not on Splash or Auth)
                    if (currentRoute != NavigationItem.Splash.route && currentRoute != NavigationItem.Auth.route) {
                        NavigationBar(
                            containerColor = AppColors.Card,
                            contentColor = AppColors.Foreground,
                        ) {
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
                                            selectedIconColor = AppColors.Primary,
                                            selectedTextColor = AppColors.Primary,
                                            unselectedIconColor = AppColors.MutedForeground,
                                            unselectedTextColor = AppColors.MutedForeground,
                                            indicatorColor = AppColors.Primary.copy(alpha = 0.1f),
                                        ),
                                )
                            }
                        }
                    }
                },
            ) { innerPadding ->
                NavHost(
                    navController = navController,
                    startDestination = startDestination,
                    modifier = Modifier,
                ) {
                    composable(NavigationItem.Splash.route) {
                        // Splash screen takes full screen (no padding)
                        SplashScreen(
                            isLoading = splashState.isLoading,
                            loadingMessage = splashState.loadingMessage,
                            onAnimationComplete = {
                                if (splashState.isComplete) {
                                    val destination =
                                        if (splashState.shouldShowAuth) {
                                            NavigationItem.Auth.route
                                        } else {
                                            NavigationItem.Home.route
                                        }
                                    navController.navigate(destination) {
                                        popUpTo(NavigationItem.Splash.route) { inclusive = true }
                                    }
                                }
                            },
                        )

                        // Auto-navigate when splash is complete
                        LaunchedEffect(splashState.isComplete) {
                            if (splashState.isComplete) {
                                val destination =
                                    if (splashState.shouldShowAuth) {
                                        NavigationItem.Auth.route
                                    } else {
                                        NavigationItem.Home.route
                                    }
                                navController.navigate(destination) {
                                    popUpTo(NavigationItem.Splash.route) { inclusive = true }
                                }
                            }
                        }
                    }

                    composable(NavigationItem.Auth.route) {
                        // Auth screen takes full screen (no padding)
                        AuthScreen(
                            modifier = Modifier.fillMaxSize(),
                            onAuthenticationSuccess = {
                                // Authentication successful, navigate to home
                                navController.navigate(NavigationItem.Home.route) {
                                    popUpTo(NavigationItem.Auth.route) { inclusive = true }
                                }
                            },
                            onAuthenticationFailure = {
                                // Authentication failed, close the app
                                finish()
                            },
                        )

                        // Trigger authentication when Auth screen is shown
                        LaunchedEffect(Unit) {
                            val authenticated = biometricManager.authenticate(this@ExpenSMSApp)
                            if (authenticated) {
                                navController.navigate(NavigationItem.Home.route) {
                                    popUpTo(NavigationItem.Auth.route) { inclusive = true }
                                }
                            } else {
                                // Authentication failed or was cancelled, close the app
                                finish()
                            }
                        }
                    }

                    composable(NavigationItem.Home.route) {
                        Box(modifier = Modifier.padding(innerPadding)) {
                            HomeScreen(
                                onTransactionClick = { _transaction ->
                                    // TODO: Open transaction edit dialog
                                },
                                onSeeAllClick = {
                                    navController.navigate(NavigationItem.Transactions.route)
                                },
                                onLinkClick = onOpenUrl,
                                onThemeChange = { newTheme ->
                                    settingsViewModel.setTheme(newTheme)
                                },
                            )
                        }
                    }

                    composable(NavigationItem.Transactions.route) {
                        Box(modifier = Modifier.padding(innerPadding)) {
                            TransactionsScreen(
                                repository = transactionRepository,
                                onTransactionClick = {
                                    // TODO: Open transaction edit dialog
                                },
                                onLinkClick = onOpenUrl,
                            )
                        }
                    }

                    composable(NavigationItem.Settings.route) {
                        Box(modifier = Modifier.padding(innerPadding)) {
                            SettingsScreen()
                        }
                    }
                }
            }
        }
    }
}
