package com.kisansethu.app.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.kisansethu.app.ui.booking.SlotBookingsScreen
import com.kisansethu.app.ui.home.DashboardScreen
import com.kisansethu.app.ui.home.PaymentPlaceholder
import com.kisansethu.app.ui.home.ProfilePlaceholder

sealed class BottomNavItem(val route: String, val icon: ImageVector, val label: String) {
    object Home : BottomNavItem("home_dashboard", Icons.Outlined.Home, "Home")
    object Bookings : BottomNavItem("home_bookings", Icons.Outlined.CalendarToday, "Bookings")
    object Payments : BottomNavItem("home_payments", Icons.AutoMirrored.Outlined.ReceiptLong, "Payments")
    object Profile : BottomNavItem("home_profile", Icons.Rounded.Person, "Profile")
}

@Composable
fun HomeScreen(
    farmerName: String,
    farmerId: String,
    onSignOut: () -> Unit,
    onClearSession: (suspend () -> Unit)? = null,
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean = false,
    onToggleTheme: (() -> Unit)? = null
) {
    val bottomNavController = rememberNavController()

    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.Bookings,
        BottomNavItem.Payments,
        BottomNavItem.Profile
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color(0xFFF9FAFB),
        bottomBar = {
            val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route

            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 6.dp
            ) {
                items.forEach { item ->
                    val isSelected = currentRoute == item.route
                    NavigationBarItem(
                        icon = { 
                            Icon(
                                item.icon, 
                                contentDescription = item.label,
                                modifier = Modifier.size(24.dp)
                            ) 
                        },
                        label = { 
                            Text(
                                item.label,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 12.sp
                            ) 
                        },
                        selected = isSelected,
                        onClick = {
                            bottomNavController.navigate(item.route) {
                                popUpTo(bottomNavController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF134E35),
                            selectedTextColor = Color(0xFF134E35),
                            indicatorColor = Color(0xFFDDEFE3),
                            unselectedIconColor = Color(0xFF6B7280),
                            unselectedTextColor = Color(0xFF6B7280)
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            NavHost(
                navController = bottomNavController,
                startDestination = BottomNavItem.Home.route
            ) {
                composable(BottomNavItem.Home.route) { 
                    DashboardScreen(
                        farmerName = farmerName,
                        farmerId = farmerId,
                        onBookSlot = {
                            bottomNavController.navigate(BottomNavItem.Bookings.route) {
                                popUpTo(bottomNavController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    ) 
                }
                composable(BottomNavItem.Bookings.route) {
                    SlotBookingsScreen(
                        farmerId = farmerId,
                        farmerName = farmerName
                    )
                }
                composable(BottomNavItem.Payments.route) { PaymentPlaceholder() }
                composable(BottomNavItem.Profile.route) { 
                    ProfilePlaceholder(
                        farmerName = farmerName,
                        farmerId = farmerId,
                        onSignOut = onSignOut,
                        onClearSession = onClearSession,
                        onToggleTheme = onToggleTheme
                    ) 
                }
            }
        }
    }
}
