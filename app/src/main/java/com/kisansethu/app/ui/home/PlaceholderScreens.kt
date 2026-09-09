package com.kisansethu.app.ui.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@Composable
fun SlotBookingsPlaceholder(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Slot Bookings",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

data class PaymentTransaction(
    val id: String,
    val title: String,
    val date: String,
    val amount: String,
    val status: String = "Credited"
)

@Composable
fun PaymentPlaceholder(
    modifier: Modifier = Modifier,
    totalReceivedAmount: String? = "₹ 48,250",
    transactionsCount: Int? = 3,
    transactions: List<PaymentTransaction> = listOf(
        PaymentTransaction(
            id = "1",
            title = "Medchal Procurement Centre",
            date = "08 Sep 2026",
            amount = "₹ 32,000",
            status = "Credited"
        ),
        PaymentTransaction(
            id = "2",
            title = "Karimnagar Procurement Centre",
            date = "05 Sep 2026",
            amount = "₹ 12,500",
            status = "Credited"
        ),
        PaymentTransaction(
            id = "3",
            title = "Adilabad Procurement Centre",
            date = "01 Sep 2026",
            amount = "₹ 3,750",
            status = "Credited"
        )
    )
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            // 1. Top clearance directly beneath status bar padding
            Spacer(modifier = Modifier.height(12.dp))

            // 2. HEADER SECTION
            Text(
                text = "Payments",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF0F172A)
            )
            Text(
                text = "View your payments and transaction history",
                fontSize = 15.sp,
                fontWeight = FontWeight.Normal,
                color = Color(0xFF64748B)
            )
            Spacer(modifier = Modifier.height(20.dp))

            // 3. HERO BANNER CARD ("Total Received")
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = Color(0xFF1B4D3E),
                shadowElevation = 0.dp,
                tonalElevation = 0.dp
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                ) {
                    // Subtle decorative wave in bottom-right corner
                    Canvas(
                        modifier = Modifier.matchParentSize()
                    ) {
                        val path = Path().apply {
                            moveTo(size.width * 0.55f, size.height)
                            cubicTo(
                                size.width * 0.65f, size.height * 0.72f,
                                size.width * 0.75f, size.height * 0.48f,
                                size.width * 0.84f, size.height * 0.62f
                            )
                            cubicTo(
                                size.width * 0.90f, size.height * 0.72f,
                                size.width * 0.95f, size.height * 0.42f,
                                size.width, size.height * 0.46f
                            )
                            lineTo(size.width, size.height)
                            close()
                        }
                        drawPath(path = path, color = Color(0xFF245B49).copy(alpha = 0.7f))
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        // Top row
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Square Icon Container
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color(0xFF2E6B56)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Payments,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            // Content Column
                            Column {
                                Text(
                                    text = "Total Received",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.White.copy(alpha = 0.85f)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = totalReceivedAmount ?: "₹ 0",
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }

                        // Bottom row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 28.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${transactionsCount ?: transactions.size} transactions",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Normal,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.8f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            // 4. "RECENT PAYMENTS" SECTION HEADER
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Payments",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
                Text(
                    text = "View all",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1B4D3E),
                    modifier = Modifier.clickable { /* View all */ }
                )
            }
            Spacer(modifier = Modifier.height(14.dp))

            // 5. TRANSACTION LIST ITEMS
            if (transactions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 36.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No recent payments found",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF64748B)
                    )
                }
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    transactions.forEach { item ->
                        PaymentTransactionCard(item)
                    }
                }
            }

            // 6. BOTTOM CLEARANCE
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun PaymentTransactionCard(item: PaymentTransaction) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        shadowElevation = 0.dp,
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon Container
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE8F5E9)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.AccountBalance,
                    contentDescription = null,
                    tint = Color(0xFF1B4D3E),
                    modifier = Modifier.size(22.dp)
                )
            }

            // Middle Column
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
            ) {
                Text(
                    text = item.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = item.date,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color(0xFF64748B)
                )
            }

            // Right Column
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = item.amount,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFD1FADF)
                ) {
                    Text(
                        text = item.status,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF15803D),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ProfilePlaceholder(
    farmerName: String = "Sai Santosh",
    farmerId: String = "",
    modifier: Modifier = Modifier,
    onSignOut: (() -> Unit)? = null,
    onClearSession: (suspend () -> Unit)? = null,
    onToggleTheme: (() -> Unit)? = null
) {
    val coroutineScope = rememberCoroutineScope()
    val displayName = if (farmerName.isNotBlank() && farmerName != "Farmer") farmerName else "Sai Santosh"
    val displayInitial = displayName.firstOrNull()?.uppercaseChar()?.toString() ?: "S"
    val displayPhone = "+91 98765 43210"

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            // 1. Top spacer directly below status bar padding
            Spacer(modifier = Modifier.height(12.dp))

            // 2. HEADER SECTION
            Text(
                text = "Profile",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF0F172A)
            )
            Text(
                text = "Manage your account",
                fontSize = 15.sp,
                fontWeight = FontWeight.Normal,
                color = Color(0xFF64748B)
            )
            Spacer(modifier = Modifier.height(20.dp))

            // 3. PROFILE CARD
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = Color.White,
                shadowElevation = 0.dp,
                tonalElevation = 0.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Avatar
                    Surface(
                        modifier = Modifier.size(60.dp),
                        shape = CircleShape,
                        color = Color(0xFF14532D)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = displayInitial,
                                color = Color.White,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Details
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = displayName,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = displayPhone,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Normal,
                            color = Color(0xFF64748B)
                        )
                    }

                    // Edit Button
                    Surface(
                        modifier = Modifier
                            .size(40.dp)
                            .clickable { /* Edit Profile */ },
                        shape = CircleShape,
                        color = Color(0xFFF1F5F9)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Outlined.Edit,
                                contentDescription = "Edit Profile",
                                tint = Color(0xFF334155),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // 4. SPACING
            Spacer(modifier = Modifier.height(16.dp))

            // 5. MENU SETTINGS CARD
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = Color.White,
                shadowElevation = 0.dp,
                tonalElevation = 0.dp
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Row 1: Language
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { /* Language */ }
                            .padding(horizontal = 20.dp, vertical = 18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Language,
                            contentDescription = "Language",
                            tint = Color(0xFF0F172A),
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            text = "Language",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF0F172A),
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 16.dp)
                        )
                        Text(
                            text = "English",
                            fontSize = 14.sp,
                            color = Color(0xFF64748B),
                            modifier = Modifier.padding(end = 6.dp)
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null,
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    HorizontalDivider(
                        thickness = 0.8.dp,
                        color = Color(0xFFF1F5F9),
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )

                    // Row 2: Notifications
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { /* Notifications */ }
                            .padding(horizontal = 20.dp, vertical = 18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Notifications,
                            contentDescription = "Notifications",
                            tint = Color(0xFF0F172A),
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            text = "Notifications",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF0F172A),
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 16.dp)
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null,
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    HorizontalDivider(
                        thickness = 0.8.dp,
                        color = Color(0xFFF1F5F9),
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )

                    // Row 3: Help & Support
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { /* Help & Support */ }
                            .padding(horizontal = 20.dp, vertical = 18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.HelpOutline,
                            contentDescription = "Help & Support",
                            tint = Color(0xFF0F172A),
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            text = "Help & Support",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF0F172A),
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 16.dp)
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null,
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    HorizontalDivider(
                        thickness = 0.8.dp,
                        color = Color(0xFFF1F5F9),
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )

                    // Row 4: About
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { /* About */ }
                            .padding(horizontal = 20.dp, vertical = 18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Info,
                            contentDescription = "About",
                            tint = Color(0xFF0F172A),
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            text = "About",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF0F172A),
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 16.dp)
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null,
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // 6. LOGOUT CARD
            Spacer(modifier = Modifier.height(16.dp))

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        coroutineScope.launch {
                            onClearSession?.invoke()
                            onSignOut?.invoke()
                        }
                    },
                shape = RoundedCornerShape(24.dp),
                color = Color.White,
                shadowElevation = 0.dp,
                tonalElevation = 0.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.Logout,
                        contentDescription = "Logout",
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        text = "Logout",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFEF4444),
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }
            }

            // Bottom spacer so content doesn't get clipped by bottom navigation bar
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
