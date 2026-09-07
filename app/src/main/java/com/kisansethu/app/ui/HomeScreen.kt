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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
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
import dev.chrisbanes.haze.HazeStyle
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
            // ── Haze source: this content is blurred through the glass pill ──
            NavHost(
                navController = bottomNavController,
                startDestination = BottomNavItem.Home.route,
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
                composable(BottomNavItem.Bookings.route) { SlotBookingsPlaceholder() }
                composable(BottomNavItem.Payment.route) { PaymentPlaceholder() }
                composable(BottomNavItem.Profile.route) { ProfilePlaceholder() }
            }

            // ── Liquid Glass Nav Bar overlay ──
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
    // Use mutableStateOf so the swipe lambda always reads the latest index
    var swipeAccum by remember { mutableFloatStateOf(0f) }

    // Frosted glass style: bright white-tinted blur for light theme
    val glassStyle = HazeStyle(
        backgroundColor = Color.White,
        tint = dev.chrisbanes.haze.HazeTint(Color.White.copy(alpha = 0.55f)),
        blurRadius = 24.dp,
        noiseFactor = 0.08f
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 18.dp)
            // Swipe gesture — currentRoute is read via mutableState; captured by reference correctly
            .pointerInput(currentRoute) {
                detectHorizontalDragGestures(
                    onDragEnd = { swipeAccum = 0f },
                    onDragCancel = { swipeAccum = 0f }
                ) { change, dragAmount ->
                    change.consume()
                    swipeAccum += dragAmount
                    val activeIndex = items.indexOfFirst { it.route == currentRoute }
                        .coerceAtLeast(0)
                    if (swipeAccum > 80f) {
                        if (activeIndex > 0) onItemSelected(items[activeIndex - 1])
                        swipeAccum = 0f
                    } else if (swipeAccum < -80f) {
                        if (activeIndex < items.size - 1) onItemSelected(items[activeIndex + 1])
                        swipeAccum = 0f
                    }
                }
            },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        // ── Main pill: Home, Bookings, Payment ──
        val mainItems = items.take(3)
        Box(
            modifier = Modifier
                .weight(1f)
                // Extra height so the 16dp pop-out badge + label both fit without clipping
                .height(80.dp)
                .clip(CircleShape)
                .hazeChild(
                    state = hazeState,
                    style = glassStyle
                )
                // Top-edge glass sheen highlight
                .border(
                    width = 1.dp,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.85f),
                            Color.White.copy(alpha = 0.20f)
                        )
                    ),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                // Bottom-align so items that pop up go upward and the label stays at base
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

        Spacer(modifier = Modifier.width(14.dp))

        // ── Profile circle pill ──
        val profileItem = items.last()
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .hazeChild(
                    state = hazeState,
                    style = glassStyle
                )
                .border(
                    width = 1.dp,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.85f),
                            Color.White.copy(alpha = 0.20f)
                        )
                    ),
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
    // Icon pops upward when selected — ensure enough container height to see the overflow
    val yOffset by animateDpAsState(
        targetValue = if (selected) (-14).dp else 0.dp,
        animationSpec = spring(dampingRatio = 0.55f, stiffness = 280f),
        label = "iconYOffset"
    )

    val badgeScale by animateFloatAsState(
        targetValue = if (selected) 1f else 0f,
        animationSpec = spring(dampingRatio = 0.65f, stiffness = 380f),
        label = "badgeScale"
    )

    val iconColor by animateColorAsState(
        targetValue = if (selected) Color.White else Color(0xFF555555),
        animationSpec = tween(200),
        label = "iconColor"
    )

    // Primary green for badge — use explicit color to guarantee light-theme consistency
    val primaryGreen = MaterialTheme.colorScheme.primary

    // Minimum 48dp touch target
    Box(
        modifier = modifier
            .height(80.dp)
            .width(68.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        // Label — sits at the bottom of the 80dp container (fully visible)
        AnimatedVisibility(
            visible = selected,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 10.dp)
        ) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.5.sp),
                color = Color(0xFF333333),
                fontWeight = FontWeight.SemiBold,
                maxLines = 1
            )
        }

        // Icon + badge — positioned in the middle, then offset upward when selected
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = if (selected) (-6).dp else 0.dp) // mild upward shift within container
                .size(44.dp),
            contentAlignment = Alignment.Center
        ) {
            // Glassmorphic badge: radial gradient green bubble with top highlight
            if (selected) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .scale(badgeScale)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    primaryGreen.copy(alpha = 0.95f),
                                    primaryGreen
                                )
                            ),
                            shape = CircleShape
                        )
                )
                // Top-edge highlight to add depth / glass refraction feel
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .size(width = 22.dp, height = 10.dp)
                        .offset(y = 5.dp)
                        .scale(badgeScale)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.45f),
                                    Color.White.copy(alpha = 0f)
                                )
                            ),
                            shape = CircleShape
                        )
                )
            }

            Icon(
                imageVector = item.icon,
                contentDescription = item.title,
                tint = iconColor,
                modifier = Modifier.size(22.dp)
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
