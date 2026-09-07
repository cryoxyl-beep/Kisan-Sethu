package com.kisansethu.app.ui.home

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class BookingMockData(
    val centerName: String = "Medchal Procurement Centre",
    val time: String = "10:30 AM",
    val token: String = "#43",
    val status: String = "Confirmed",
    val currentToken: String = "#40",
    val farmersAhead: Int = 2,
    val estimatedWaitMin: Int = 15,
    val queueStatus: String = "Waiting"
)

data class DashboardUiState(
    val farmerName: String = "",
    val farmerId: String = "",
    val hasActiveBooking: Boolean = false,
    val bookingData: BookingMockData? = null
)

class DashboardViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    fun initialize(name: String, id: String) {
        _uiState.value = _uiState.value.copy(
            farmerName = name,
            farmerId = id,
            hasActiveBooking = false, // Set to true to test active booking state
            bookingData = if (false) BookingMockData() else null
        )
    }

    // Call this to toggle for testing if needed
    fun toggleMockBooking() {
        val hasBooking = !_uiState.value.hasActiveBooking
        _uiState.value = _uiState.value.copy(
            hasActiveBooking = hasBooking,
            bookingData = if (hasBooking) BookingMockData() else null
        )
    }
}
