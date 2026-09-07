package com.kisansethu.app.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ExitToApp
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.kisansethu.app.ui.home.DashboardScreen
import com.kisansethu.app.ui.home.PaymentPlaceholder
import com.kisansethu.app.ui.home.ProfilePlaceholder
import com.kisansethu.app.ui.home.SlotBookingsPlaceholder
import com.kisansethu.app.ui.theme.KisanSethuTheme
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.hazeChild

sealed class BottomNavItem(val route: String, val title: String, val icon: ImageVector) {
    object Home : BottomNavItem("home_dashboard", "Home", Icons.Rounded.Home)
    object Bookings : BottomNavItem("home_bookings", "Slot Bookings", Icons.Rounded.CalendarToday)
    object Payment : BottomNavItem("home_payment", "Payment", Icons.Rounded.AccountBalanceWallet)
    object Profile : BottomNavItem("home_profile", "Profile", Icons.Rounded.Person)
}

@Composable
fun HomeScreen(
    farmerName: String,
    farmerId: String,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean = false,
    onToggleTheme: (() -> Unit)? = null
) {
    val bottomNavController = rememberNavController()
    val hazeState = remember { HazeState() }
    
    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.Bookings,
        BottomNavItem.Payment,
        BottomNavItem.Profile
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            if (onToggleTheme != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(top = 12.dp, bottom = 8.dp, start = 16.dp, end = 16.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onSignOut) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ExitToApp,
                            contentDescription = "Sign Out",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(onClick = onToggleTheme) {
                        Icon(
                            imageVector = if (isDarkTheme) Icons.Rounded.LightMode else Icons.Rounded.DarkMode,
                            contentDescription = "Toggle Theme",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            // Background Content with Haze applied
            NavHost(
                navController = bottomNavController,
                startDestination = BottomNavItem.Home.route,
                // Only applying top padding so the content scrolls UNDER the floating bottom nav
                modifier = Modifier
                    .haze(state = hazeState)
                    .fillMaxSize()
                    .padding(top = innerPadding.calculateTopPadding()),
                enterTransition = {
                    fadeIn(animationSpec = tween(350, easing = FastOutSlowInEasing)) +
                            scaleIn(initialScale = 0.93f, animationSpec = tween(350, easing = FastOutSlowInEasing))
                },
                exitTransition = {
                    fadeOut(animationSpec = tween(280, easing = FastOutSlowInEasing)) +
                            scaleOut(targetScale = 1.04f, animationSpec = tween(280, easing = FastOutSlowInEasing))
                },
                popEnterTransition = {
                    fadeIn(animationSpec = tween(350, easing = FastOutSlowInEasing)) +
                            scaleIn(initialScale = 1.04f, animationSpec = tween(350, easing = FastOutSlowInEasing))
                },
                popExitTransition = {
                    fadeOut(animationSpec = tween(280, easing = FastOutSlowInEasing)) +
                            scaleOut(targetScale = 0.93f, animationSpec = tween(280, easing = FastOutSlowInEasing))
                }
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
                    SlotBookingsPlaceholder()
                }
                composable(BottomNavItem.Payment.route) {
                    PaymentPlaceholder()
                }
                composable(BottomNavItem.Profile.route) {
                    ProfilePlaceholder()
                }
            }

            // Liquid Glass Navigation Bar Overlay
            val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route

            LiquidGlassNavBar(
                items = items,
                currentRoute = currentRoute,
                hazeState = hazeState,
                onItemSelected = { item ->
                    bottomNavController.navigate(item.route) {
                        popUpTo(bottomNavController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

@Composable
private fun LiquidGlassNavBar(
    items: List<BottomNavItem>,
    currentRoute: String?,
    hazeState: HazeState,
    onItemSelected: (BottomNavItem) -> Unit,
    modifier: Modifier = Modifier
) {
    var swipeOffset by remember { mutableStateOf(0f) }
    val currentIndex = items.indexOfFirst { it.route == currentRoute }.takeIf { it >= 0 } ?: 0

    Row(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 20.dp)
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragEnd = { swipeOffset = 0f },
                    onDragCancel = { swipeOffset = 0f }
                ) { change, dragAmount ->
                    change.consume()
                    swipeOffset += dragAmount
                    if (swipeOffset > 80f) {
                        if (currentIndex > 0) onItemSelected(items[currentIndex - 1])
                        swipeOffset = 0f
                    } else if (swipeOffset < -80f) {
                        if (currentIndex < items.size - 1) onItemSelected(items[currentIndex + 1])
                        swipeOffset = 0f
                    }
                }
            },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        // Main Pill (First 3 items)
        val mainItems = items.take(3)
        Box(
            modifier = Modifier
                .weight(1f)
                .height(72.dp)
                .clip(CircleShape)
                .hazeChild(state = hazeState)
                .background(
                    color = Color.White.copy(alpha = 0.15f),
                    shape = CircleShape
                )
                .border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.4f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                mainItems.forEach { item ->
                    LiquidGlassNavItem(
                        item = item,
                        selected = (currentRoute == item.route),
                        onClick = { onItemSelected(item) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Profile Pill (Last item)
        val profileItem = items.last()
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .hazeChild(state = hazeState)
                .background(
                    color = Color.White.copy(alpha = 0.15f),
                    shape = CircleShape
                )
                .border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.4f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            LiquidGlassNavItem(
                item = profileItem,
                selected = (currentRoute == profileItem.route),
                onClick = { onItemSelected(profileItem) }
            )
        }
    }
}

@Composable
fun LiquidGlassNavItem(
    item: BottomNavItem,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val yOffset by animateDpAsState(
        targetValue = if (selected) (-16).dp else 0.dp,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 300f),
        label = "yOffset"
    )
    
    val badgeScale by animateFloatAsState(
        targetValue = if (selected) 1f else 0f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 400f),
        label = "badgeScale"
    )
    
    val iconColor by animateColorAsState(
        targetValue = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
        animationSpec = tween(200),
        label = "iconColor"
    )

    Box(
        modifier = modifier
            .height(72.dp)
            .width(64.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        // Label at bottom
        AnimatedVisibility(
            visible = selected,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 8.dp)
        ) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1
            )
        }
        
        // Icon and popping badge
        Box(
            modifier = Modifier
                .offset(y = yOffset)
                .size(48.dp),
            contentAlignment = Alignment.Center
        ) {
            // Green badge
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .scale(badgeScale)
                    .background(MaterialTheme.colorScheme.primary, CircleShape)
            )
            
            Icon(
                imageVector = item.icon,
                contentDescription = item.title,
                tint = iconColor,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    KisanSethuTheme(dynamicColor = false) {
        HomeScreen(
            farmerName = "Ramesh Kumar",
            farmerId = "A7K29P",
            onSignOut = {}
        )
    }
}
