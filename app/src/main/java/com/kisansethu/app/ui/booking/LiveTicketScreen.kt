package com.kisansethu.app.ui.booking

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kisansethu.app.data.Booking
import com.kisansethu.app.data.BookingStatus

private val ForestGreen = Color(0xFF1B3B26)
private val LightEmerald = Color(0xFF2E7D32)
private val SoftMint = Color(0xFFE5F3EA)
private val PageBg = Color(0xFFF7F8F7)
private val Charcoal = Color(0xFF1E1E1E)
private val MutedGray = Color(0xFF6E6E6E)
private val CardBorder = Color(0xFFE8EEEA)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveTicketScreen(
    booking: Booking,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LiveTicketViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    DisposableEffect(booking.trackingId) {
        viewModel.startListening(booking)
        onDispose {
            viewModel.stopListening()
        }
    }

    // Pulsing animation for the Live indicator
    val infiniteTransition = rememberInfiniteTransition(label = "pulseTransition")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    val normStatus = uiState.status
    val isServing = uiState.isMyTurn
    val isProcessing = uiState.isProcessing
    val isCompleted = uiState.isCompleted
    val isWaiting = !isServing && !isProcessing && !isCompleted

    val statusBgColor = when (normStatus) {
        BookingStatus.COMPLETED.name -> SoftMint
        BookingStatus.PROCESSING.name -> Color(0xFFE1F5FE)
        BookingStatus.NOW_SERVING.name -> Color(0xFFE8F5E9)
        BookingStatus.WAITING.name -> Color(0xFFFFF3E0)
        BookingStatus.CHECKED_IN.name -> Color(0xFFE3F2FD)
        else -> Color(0xFFF5F5F5)
    }

    val statusTextColor = when (normStatus) {
        BookingStatus.COMPLETED.name -> Color(0xFF1F6B45)
        BookingStatus.PROCESSING.name -> Color(0xFF0277BD)
        BookingStatus.NOW_SERVING.name -> LightEmerald
        BookingStatus.WAITING.name -> Color(0xFFE65100)
        BookingStatus.CHECKED_IN.name -> Color(0xFF1565C0)
        else -> Color(0xFF757575)
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = PageBg,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Live Procurement Ticket",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Charcoal
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back",
                            tint = Charcoal
                        )
                    }
                },
                actions = {
                    // Subtle Live Chip
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = SoftMint,
                        border = BorderStroke(1.dp, Color(0xFFC8E6C9)),
                        modifier = Modifier.padding(end = 16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(LightEmerald)
                                    .alpha(pulseAlpha)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "LIVE",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = LightEmerald,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PageBg)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Main Digital Ticket Surface
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = Color.White,
                border = BorderStroke(1.dp, CardBorder),
                shadowElevation = 1.dp
            ) {
                Column(
                    modifier = Modifier.padding(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header Brand
                    Text(
                        text = "KISSAAN SYNC",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = ForestGreen,
                        letterSpacing = 2.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "LIVE PROCUREMENT TICKET",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Charcoal,
                        letterSpacing = 0.5.sp
                    )

                    Spacer(modifier = Modifier.height(18.dp))
                    HorizontalDivider(color = CardBorder)
                    Spacer(modifier = Modifier.height(18.dp))

                    // ==========================================
                    // STATE-DEPENDENT MAIN DISPLAY (NOW_SERVING, PROCESSING, COMPLETED, WAITING)
                    // ==========================================
                    when {
                        isServing -> {
                            // Prominent NOW_SERVING Display
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(18.dp),
                                color = Color(0xFFE8F5E9),
                                border = BorderStroke(1.5.dp, LightEmerald.copy(alpha = 0.4f))
                            ) {
                                Column(
                                    modifier = Modifier.padding(18.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        Icons.Rounded.Campaign,
                                        contentDescription = null,
                                        tint = LightEmerald,
                                        modifier = Modifier.size(36.dp)
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "YOUR TURN",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = ForestGreen,
                                        letterSpacing = 1.sp
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = uiState.myToken.ifEmpty { "#---" },
                                        fontSize = 44.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = ForestGreen
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Surface(
                                        shape = RoundedCornerShape(50),
                                        color = LightEmerald
                                    ) {
                                        Text(
                                            text = "NOW SERVING",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = "Please proceed to the procurement counter.",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Charcoal,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }

                        isProcessing -> {
                            // Prominent PROCESSING Display
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(18.dp),
                                color = Color(0xFFE1F5FE),
                                border = BorderStroke(1.5.dp, Color(0xFF0277BD).copy(alpha = 0.3f))
                            ) {
                                Column(
                                    modifier = Modifier.padding(18.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        Icons.Rounded.HourglassTop,
                                        contentDescription = null,
                                        tint = Color(0xFF0277BD),
                                        modifier = Modifier.size(34.dp)
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "PROCUREMENT IN PROGRESS",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFF0277BD),
                                        letterSpacing = 0.5.sp
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = uiState.myToken.ifEmpty { "#---" },
                                        fontSize = 44.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = ForestGreen
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Surface(
                                        shape = RoundedCornerShape(50),
                                        color = Color(0xFF0277BD)
                                    ) {
                                        Text(
                                            text = "Processing",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Your procurement process has started.",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Charcoal,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }

                        isCompleted -> {
                            // Prominent COMPLETED Display
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(18.dp),
                                color = SoftMint,
                                border = BorderStroke(1.5.dp, Color(0xFF1F6B45).copy(alpha = 0.3f))
                            ) {
                                Column(
                                    modifier = Modifier.padding(18.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        Icons.Rounded.CheckCircle,
                                        contentDescription = null,
                                        tint = Color(0xFF1F6B45),
                                        modifier = Modifier.size(36.dp)
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "PROCUREMENT COMPLETED",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFF1F6B45),
                                        letterSpacing = 0.5.sp
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = uiState.myToken.ifEmpty { "#---" },
                                        fontSize = 40.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = ForestGreen
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Surface(
                                        shape = RoundedCornerShape(50),
                                        color = Color(0xFF1F6B45)
                                    ) {
                                        Text(
                                            text = "Completed",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Your procurement visit has been completed.",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Charcoal,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }

                        else -> {
                            // Calm WAITING / CHECKED_IN Display
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "YOUR TOKEN",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MutedGray,
                                    letterSpacing = 1.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = uiState.myToken.ifEmpty { "#---" },
                                    fontSize = 48.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = ForestGreen
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = statusBgColor
                                ) {
                                    Text(
                                        text = uiState.displayStatus.ifEmpty { "WAITING" },
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = statusTextColor,
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = if (normStatus == BookingStatus.CHECKED_IN.name) {
                                        "You are checked in. Waiting for your queue placement."
                                    } else {
                                        "Please wait in the waiting area until your token is called."
                                    },
                                    fontSize = 13.sp,
                                    color = MutedGray,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))
                    HorizontalDivider(color = CardBorder)
                    Spacer(modifier = Modifier.height(18.dp))

                    // ==========================================
                    // CENTRE & APPOINTMENT DETAILS
                    // ==========================================
                    TicketDetailRow(
                        label = "Procurement Centre",
                        value = uiState.centreName.ifEmpty { booking.centreName }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    TicketDetailRow(
                        label = "Date",
                        value = uiState.bookingDate.ifEmpty { booking.bookingDate }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    val slotDisplay = if (uiState.slotTime.isNotEmpty()) {
                        uiState.slotTime
                    } else {
                        "${booking.slotStartTime} - ${booking.slotEndTime}".trim().removePrefix("-").removeSuffix("-").trim()
                    }
                    TicketDetailRow(
                        label = "Time Slot",
                        value = slotDisplay.ifEmpty { "Assigned Slot" }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    val crop = uiState.crop.ifEmpty { booking.crop }
                    val produceDisplay = if (crop.isNotEmpty()) {
                        if (uiState.quantityFormatted.isNotEmpty()) {
                            "$crop • ${uiState.quantityFormatted}"
                        } else {
                            val qty = if (booking.quantity % 1.0 == 0.0) "${booking.quantity.toInt()}.0" else booking.quantity.toString()
                            "$crop • $qty ${booking.quantityUnit.lowercase()}"
                        }
                    } else ""
                    if (produceDisplay.isNotEmpty()) {
                        TicketDetailRow(
                            label = "Produce",
                            value = produceDisplay
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                    TicketDetailRow(
                        label = "Booking ID",
                        value = uiState.trackingId.ifEmpty { booking.trackingId },
                        isHighlight = true
                    )

                    // ==========================================
                    // QUEUE STATUS SECTION (for WAITING / active queue)
                    // ==========================================
                    if (isWaiting) {
                        Spacer(modifier = Modifier.height(20.dp))
                        HorizontalDivider(color = CardBorder)
                        Spacer(modifier = Modifier.height(18.dp))

                        Text(
                            text = "QUEUE STATUS",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MutedGray,
                            letterSpacing = 1.sp,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        // Now Serving Box
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            color = PageBg,
                            border = BorderStroke(1.dp, CardBorder)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Now Serving",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Charcoal
                                )
                                Text(
                                    text = uiState.currentServingToken,
                                    fontSize = if (uiState.currentServingToken.startsWith("#")) 22.sp else 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (uiState.currentServingToken.startsWith("#")) ForestGreen else MutedGray
                                )
                            }
                        }

                        if (isWaiting && uiState.myPosition > 0) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Your Position Box
                                Surface(
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(16.dp),
                                    color = PageBg,
                                    border = BorderStroke(1.dp, CardBorder)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(14.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = "Your Position",
                                            fontSize = 12.sp,
                                            color = MutedGray,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = uiState.myPosition.toString(),
                                            fontSize = 28.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = ForestGreen
                                        )
                                    }
                                }

                                // People Ahead Box
                                Surface(
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(16.dp),
                                    color = PageBg,
                                    border = BorderStroke(1.dp, CardBorder)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(14.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = "People Ahead",
                                            fontSize = 12.sp,
                                            color = MutedGray,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = uiState.peopleAhead.toString(),
                                            fontSize = 28.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Charcoal
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                    HorizontalDivider(color = CardBorder)
                    Spacer(modifier = Modifier.height(16.dp))

                    // Live auto-update footer
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(LightEmerald)
                                .alpha(pulseAlpha)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (uiState.isLiveConnected) "Live updates automatically" else "Updating queue...",
                            fontSize = 12.sp,
                            color = MutedGray,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun TicketDetailRow(
    label: String,
    value: String,
    isHighlight: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            color = MutedGray,
            fontWeight = FontWeight.Normal,
            modifier = Modifier.weight(1.1f)
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = if (isHighlight) FontWeight.Bold else FontWeight.SemiBold,
            color = if (isHighlight) ForestGreen else Charcoal,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1.9f)
        )
    }
}
