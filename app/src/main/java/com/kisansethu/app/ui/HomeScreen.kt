package com.kisansethu.app.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
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
import com.kisansethu.app.data.Booking
import com.kisansethu.app.ui.booking.SlotBookingRoute
import com.kisansethu.app.ui.booking.SlotBookingsScreen
import com.kisansethu.app.ui.home.DashboardScreen
import com.kisansethu.app.ui.home.PaymentPlaceholder
import com.kisansethu.app.ui.home.ProfilePlaceholder

sealed class BottomNavItem(
    val route: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val label: String
) {
    object Home : BottomNavItem(
        "home_dashboard",
        Icons.Filled.Home,
        Icons.Outlined.Home,
        "Home"
    )
    object Bookings : BottomNavItem(
        "home_bookings",
        Icons.Outlined.ShoppingBag,
        Icons.Outlined.ShoppingBag,
        "Bookings"
    )
    object Payments : BottomNavItem(
        "home_payments",
        Icons.AutoMirrored.Outlined.ReceiptLong,
        Icons.AutoMirrored.Outlined.ReceiptLong,
        "Payments"
    )
    object Profile : BottomNavItem(
        "home_profile",
        Icons.Filled.Person,
        Icons.Outlined.Person,
        "Profile"
    )
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

    var bookingInitialRoute by remember { mutableStateOf(SlotBookingRoute.LIST) }
    var bookingInitialSelected by remember { mutableStateOf<Booking?>(null) }

    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.Bookings,
        BottomNavItem.Payments,
        BottomNavItem.Profile
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color(0xFFF7F8F7),
        bottomBar = {
            val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route

            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 0.dp,
                contentColor = Color(0xFF6B7280)
            ) {
                items.forEach { item ->
                    val isSelected = currentRoute == item.route
                    NavigationBarItem(
                        icon = {
                            Icon(
                                imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                contentDescription = item.label,
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        label = {
                            Text(
                                item.label,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                fontSize = 12.sp
                            )
                        },
                        selected = isSelected,
                        onClick = {
                            if (item.route == BottomNavItem.Bookings.route && currentRoute != BottomNavItem.Bookings.route) {
                                bookingInitialRoute = SlotBookingRoute.LIST
                                bookingInitialSelected = null
                            }
                            bottomNavController.navigate(item.route) {
                                popUpTo(bottomNavController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF1B3B26),
                            selectedTextColor = Color(0xFF1B3B26),
                            indicatorColor = Color(0xFFDCEFE3),
                            unselectedIconColor = Color(0xFF8A938C),
                            unselectedTextColor = Color(0xFF8A938C)
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
                            bookingInitialRoute = SlotBookingRoute.WIZARD
                            bookingInitialSelected = null
                            bottomNavController.navigate(BottomNavItem.Bookings.route) {
                                popUpTo(bottomNavController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = false
                            }
                        },
                        onViewAll = {
                            bookingInitialRoute = SlotBookingRoute.LIST
                            bookingInitialSelected = null
                            bottomNavController.navigate(BottomNavItem.Bookings.route) {
                                popUpTo(bottomNavController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        onViewBooking = { booking ->
                            bookingInitialRoute = SlotBookingRoute.DETAIL
                            bookingInitialSelected = booking
                            bottomNavController.navigate(BottomNavItem.Bookings.route) {
                                popUpTo(bottomNavController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = false
                            }
                        }
                    )
                }
                composable(BottomNavItem.Bookings.route) {
                    SlotBookingsScreen(
                        farmerId = farmerId,
                        farmerName = farmerName,
                        initialRoute = bookingInitialRoute,
                        initialSelectedBooking = bookingInitialSelected,
                        onResetInitialRoute = {
                            bookingInitialRoute = SlotBookingRoute.LIST
                            bookingInitialSelected = null
                        }
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
