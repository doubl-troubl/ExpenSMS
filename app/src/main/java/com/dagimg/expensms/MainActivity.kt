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
import androidx.fragment.app.FragmentActivity
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
