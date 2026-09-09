package com.kisansethu.app.ui.booking

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.kisansethu.app.data.Booking

enum class SlotBookingRoute { LIST, WIZARD, DETAIL, LIVE_TICKET }

@Composable
fun SlotBookingsScreen(
    farmerId: String,
    farmerName: String,
    initialRoute: SlotBookingRoute = SlotBookingRoute.LIST,
    initialSelectedBooking: Booking? = null,
    onResetInitialRoute: (() -> Unit)? = null,
    onNavigateToHome: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var currentRoute by remember(initialRoute) { mutableStateOf(initialRoute) }
    var selectedBooking by remember(initialSelectedBooking) { mutableStateOf(initialSelectedBooking) }
    var previousRoute by remember { mutableStateOf<SlotBookingRoute?>(null) }

    when (currentRoute) {
        SlotBookingRoute.WIZARD -> {
            androidx.activity.compose.BackHandler {
                currentRoute = SlotBookingRoute.LIST
                onResetInitialRoute?.invoke()
            }
            BookingFlowScreen(
                farmerId = farmerId,
                farmerName = farmerName,
                onClose = {
                    currentRoute = SlotBookingRoute.LIST
                    onResetInitialRoute?.invoke()
                },
                modifier = modifier
            )
        }
        SlotBookingRoute.DETAIL -> {
            androidx.activity.compose.BackHandler {
                selectedBooking = null
                currentRoute = SlotBookingRoute.LIST
                onResetInitialRoute?.invoke()
            }
            selectedBooking?.let { booking ->
                BookingDetailScreen(
                    booking = booking,
                    onBack = {
                        selectedBooking = null
                        currentRoute = SlotBookingRoute.LIST
                        onResetInitialRoute?.invoke()
                    },
                    onViewLiveDetails = { b ->
                        previousRoute = SlotBookingRoute.DETAIL
                        selectedBooking = b
                        currentRoute = SlotBookingRoute.LIVE_TICKET
                    },
                    modifier = modifier
                )
            } ?: run {
                currentRoute = SlotBookingRoute.LIST
            }
        }
        SlotBookingRoute.LIVE_TICKET -> {
            androidx.activity.compose.BackHandler {
                if (previousRoute == SlotBookingRoute.DETAIL && selectedBooking != null) {
                    currentRoute = SlotBookingRoute.DETAIL
                } else {
                    currentRoute = SlotBookingRoute.LIST
                    onResetInitialRoute?.invoke()
                    onNavigateToHome?.invoke()
                }
            }
            selectedBooking?.let { booking ->
                LiveTicketScreen(
                    booking = booking,
                    onBack = {
                        if (previousRoute == SlotBookingRoute.DETAIL && selectedBooking != null) {
                            currentRoute = SlotBookingRoute.DETAIL
                        } else {
                            currentRoute = SlotBookingRoute.LIST
                            onResetInitialRoute?.invoke()
                            onNavigateToHome?.invoke()
                        }
                    },
                    modifier = modifier
                )
            } ?: run {
                currentRoute = SlotBookingRoute.LIST
            }
        }
        SlotBookingRoute.LIST -> {
            SlotBookingsListScreen(
                farmerId = farmerId,
                farmerName = farmerName,
                onBookSlotClick = {
                    previousRoute = SlotBookingRoute.LIST
                    currentRoute = SlotBookingRoute.WIZARD
                },
                onViewBookingClick = { booking ->
                    previousRoute = SlotBookingRoute.LIST
                    selectedBooking = booking
                    currentRoute = SlotBookingRoute.DETAIL
                },
                modifier = modifier
            )
        }
    }
}
