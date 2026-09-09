package com.kisansethu.app.ui.home

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
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
    val forestGreen = Color(0xFF1B3B26)
    val mutedGray = Color(0xFF6E6E6E)
    val pageBackground = Color(0xFFF4F6F5)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(pageBackground)
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Payments",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E1E1E),
                letterSpacing = (-0.3).sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "View your payments and transaction history",
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                color = mutedGray
            )
            Spacer(modifier = Modifier.height(22.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFF1C4A38), Color(0xFF163A2D))
                        )
                    )
            ) {
                Canvas(modifier = Modifier.matchParentSize()) {
                    val w = size.width
                    val h = size.height
                    val fill = Path().apply {
                        moveTo(w * 0.46f, h)
                        cubicTo(w * 0.54f, h * 0.82f, w * 0.58f, h * 0.52f, w * 0.70f, h * 0.50f)
                        cubicTo(w * 0.80f, h * 0.58f, w * 0.88f, h * 0.22f, w * 1.05f, h * 0.16f)
                        lineTo(w, h)
                        close()
                    }
                    drawPath(path = fill, color = Color(0xFF2A6350).copy(alpha = 0.55f))

                    val stroke = Path().apply {
                        moveTo(w * 0.46f, h * 0.86f)
                        cubicTo(w * 0.54f, h * 0.82f, w * 0.58f, h * 0.52f, w * 0.70f, h * 0.50f)
                        cubicTo(w * 0.80f, h * 0.58f, w * 0.88f, h * 0.22f, w, h * 0.18f)
                    }
                    drawPath(
                        path = stroke,
                        color = Color(0xFF8FD4B0).copy(alpha = 0.45f),
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 22.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFD7F0DF).copy(alpha = 0.28f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.ReceiptLong,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            Text(
                                text = "Total Received",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.White.copy(alpha = 0.88f)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = totalReceivedAmount ?: "₹ 0",
                                fontSize = 30.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                letterSpacing = (-0.3).sp
                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 26.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${transactionsCount ?: transactions.size} transactions",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Normal,
                            color = Color.White.copy(alpha = 0.82f)
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.9f),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(26.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Payments",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E1E1E)
                )
                Text(
                    text = "View all",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF2E5E41),
                    modifier = Modifier.clickable { }
                )
            }
            Spacer(modifier = Modifier.height(14.dp))

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
                        color = mutedGray
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    transactions.forEach { item ->
                        PaymentTransactionCard(item)
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun PaymentTransactionCard(item: PaymentTransaction) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 1.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = Color(0x14000000),
                spotColor = Color(0x14000000)
            ),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFE8EEEA)),
        shadowElevation = 0.dp,
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE5F3EA)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.AccountBalance,
                    contentDescription = null,
                    tint = Color(0xFF2E5E41),
                    modifier = Modifier.size(22.dp)
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
            ) {
                Text(
                    text = item.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E1E1E),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = item.date,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color(0xFF7A847E)
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = item.amount,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E1E1E)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    shape = RoundedCornerShape(50),
                    color = Color(0xFFDCEFE3)
                ) {
                    Text(
                        text = item.status,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1F6B45),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
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
            Spacer(modifier = Modifier.height(16.dp))

            // 2. HEADER SECTION
            Text(
                text = "Profile",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E1E1E)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Manage your account",
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                color = Color(0xFF6E6E6E)
            )
            Spacer(modifier = Modifier.height(20.dp))

            // 3. PROFILE CARD
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                shadowElevation = 0.dp,
                tonalElevation = 0.dp,
                border = BorderStroke(1.dp, Color(0xFFE8EEEA))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Surface(
                        modifier = Modifier.size(56.dp),
                        shape = CircleShape,
                        color = Color(0xFF1B3B26)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = displayInitial,
                                color = Color.White,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = displayName,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E1E1E),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = displayPhone,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Normal,
                            color = Color(0xFF6E6E6E)
                        )
                    }

                    Icon(
                        imageVector = Icons.Outlined.Edit,
                        contentDescription = "Edit Profile",
                        tint = Color(0xFF334155),
                        modifier = Modifier
                            .size(22.dp)
                            .clickable { /* Edit Profile */ }
                    )
                }
            }

            // 4. SPACING
            Spacer(modifier = Modifier.height(16.dp))

            // 5. MENU SETTINGS CARD
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                shadowElevation = 0.dp,
                tonalElevation = 0.dp,
                border = BorderStroke(1.dp, Color(0xFFE8EEEA))
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
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                shadowElevation = 0.dp,
                tonalElevation = 0.dp,
                border = BorderStroke(1.dp, Color(0xFFE8EEEA))
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
