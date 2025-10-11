package com.dagimg.expensms.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Receipt
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class NavigationItem(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
) {
    object Splash : NavigationItem(
        route = "splash",
        title = "Splash",
        selectedIcon = Icons.Filled.Lock, // This won't be used in bottom nav
        unselectedIcon = Icons.Filled.Lock, // This won't be used in bottom nav
    )

    object Auth : NavigationItem(
        route = "auth",
        title = "Authentication",
        selectedIcon = Icons.Filled.Lock, // This won't be used in bottom nav
        unselectedIcon = Icons.Filled.Lock, // This won't be used in bottom nav
    )

    object Home : NavigationItem(
        route = "home",
        title = "Home",
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home,
    )

    object Transactions : NavigationItem(
        route = "transactions",
        title = "Transactions",
        selectedIcon = Icons.Filled.Receipt,
        unselectedIcon = Icons.Outlined.Receipt,
    )

    object Settings : NavigationItem(
        route = "settings",
        title = "Settings",
        selectedIcon = Icons.Filled.Settings,
        unselectedIcon = Icons.Outlined.Settings,
    )
}

val navigationItems =
    listOf(
        NavigationItem.Home,
        NavigationItem.Transactions,
        NavigationItem.Settings,
    )
