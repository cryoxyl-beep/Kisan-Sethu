package com.kisansethu.app.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.hazeChild

sealed class BottomNavItem(val route: String, val icon: ImageVector, val contentDesc: String) {
    object Home : BottomNavItem("home_dashboard", Icons.Rounded.Home, "Home")
    object Bookings : BottomNavItem("home_bookings", Icons.Rounded.CalendarToday, "Slot Bookings")
    object Payment : BottomNavItem("home_payment", Icons.Rounded.AccountBalanceWallet, "Payment")
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
    val hazeState = remember { HazeState() }
    val coroutineScope = rememberCoroutineScope()

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
                    IconButton(onClick = {
                        coroutineScope.launch {
                            onClearSession?.invoke()
                            onSignOut()
                        }
                    }) {
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
            // ── Haze blur source ──
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

            // ── Nav bar overlay ──
            val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route

            SimpleGlassNavBar(
                items = items,
                currentRoute = currentRoute,
                hazeState = hazeState,
                isDark = isDarkTheme,
                onItemSelected = { item ->
                    bottomNavController.navigate(item.route) {
                        popUpTo(bottomNavController.graph.findStartDestination().id) { saveState = true }
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
private fun SimpleGlassNavBar(
    items: List<BottomNavItem>,
    currentRoute: String?,
    hazeState: HazeState,
    isDark: Boolean,
    onItemSelected: (BottomNavItem) -> Unit,
    modifier: Modifier = Modifier
) {
    var swipeAccum by remember { mutableFloatStateOf(0f) }

    // ── Haze styles: bright for light, dim for dark ──
    val lightStyle = HazeStyle(
        backgroundColor = Color(0xFFEFEFEF),
        tint = HazeTint(Color.White.copy(alpha = 0.45f)),
        blurRadius = 20.dp,
        noiseFactor = 0.05f
    )
    val darkStyle = HazeStyle(
        backgroundColor = Color(0xFF1C1C1E),
        tint = HazeTint(Color(0xFF2A2A2C).copy(alpha = 0.60f)),
        blurRadius = 20.dp,
        noiseFactor = 0.05f
    )
    val activeStyle = if (isDark) darkStyle else lightStyle

    // ── Border: subtle dark in light mode, subtle light in dark mode ──
    val borderColor = if (isDark)
        Color.White.copy(alpha = 0.18f)
    else
        Color.Black.copy(alpha = 0.10f)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .pointerInput(currentRoute) {
                detectHorizontalDragGestures(
                    onDragEnd = { swipeAccum = 0f },
                    onDragCancel = { swipeAccum = 0f }
                ) { change, dragAmount ->
                    change.consume()
                    swipeAccum += dragAmount
                    val activeIndex = items.indexOfFirst { it.route == currentRoute }.coerceAtLeast(0)
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
        verticalAlignment = Alignment.CenterVertically
    ) {
        // ── Main pill: Home, Bookings, Payment ──
        val pillShape = CircleShape
        Box(
            modifier = Modifier
                .weight(1f)
                .height(64.dp)
                .shadow(
                    elevation = if (isDark) 6.dp else 8.dp,
                    shape = pillShape,
                    ambientColor = Color.Black.copy(alpha = 0.25f),
                    spotColor = Color.Black.copy(alpha = 0.18f)
                )
                .clip(pillShape)
                .hazeChild(state = hazeState, style = activeStyle)
                .border(width = 1.dp, color = borderColor, shape = pillShape),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.take(3).forEach { item ->
                    GlassNavIcon(
                        item = item,
                        selected = (currentRoute == item.route),
                        isDark = isDark,
                        onClick = { onItemSelected(item) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // ── Profile circle ──
        val profileItem = items.last()
        Box(
            modifier = Modifier
                .size(64.dp)
                .shadow(
                    elevation = if (isDark) 6.dp else 8.dp,
                    shape = CircleShape,
                    ambientColor = Color.Black.copy(alpha = 0.25f),
                    spotColor = Color.Black.copy(alpha = 0.18f)
                )
                .clip(CircleShape)
                .hazeChild(state = hazeState, style = activeStyle)
                .border(width = 1.dp, color = borderColor, shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            GlassNavIcon(
                item = profileItem,
                selected = (currentRoute == profileItem.route),
                isDark = isDark,
                onClick = { onItemSelected(profileItem) }
            )
        }
    }
}

@Composable
private fun GlassNavIcon(
    item: BottomNavItem,
    selected: Boolean,
    isDark: Boolean,
    onClick: () -> Unit
) {
    // Selected highlight pill: light gray, slightly lighter than the blurred surface
    val highlightColor = if (isDark)
        Color.White.copy(alpha = 0.16f)   // softly lighter than dark glass
    else
        Color.Black.copy(alpha = 0.08f)   // softly darker than light glass

    // Icon tint: bright/on-surface when selected, muted when not
    val iconTint by animateColorAsState(
        targetValue = if (selected) {
            if (isDark) Color(0xFFFFFFFF) else Color(0xFF1A1A1A)
        } else {
            if (isDark) Color(0xFF9E9E9E) else Color(0xFF6E6E6E)
        },
        animationSpec = tween(durationMillis = 180),
        label = "iconTint"
    )

    val highlightAlpha by animateFloatAsState(
        targetValue = if (selected) 1f else 0f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 350f),
        label = "highlightAlpha"
    )

    // Minimum 48dp touch target
    Box(
        modifier = Modifier
            .size(48.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        // Small pill highlight — rounded rectangle tightly wrapping the icon
        Box(
            modifier = Modifier
                .size(width = 40.dp, height = 32.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(highlightColor.copy(alpha = highlightColor.alpha * highlightAlpha))
        )

        Icon(
            imageVector = item.icon,
            contentDescription = item.contentDesc,
            tint = iconTint,
            modifier = Modifier.size(22.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreviewLight() {
    KisanSethuTheme(dynamicColor = false, darkTheme = false) {
        HomeScreen(farmerName = "Ramesh Kumar", farmerId = "A7K29P", onSignOut = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreviewDark() {
    KisanSethuTheme(dynamicColor = false, darkTheme = true) {
        HomeScreen(farmerName = "Ramesh Kumar", farmerId = "A7K29P", onSignOut = {}, isDarkTheme = true)
    }
}
