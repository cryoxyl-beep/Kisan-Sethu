package com.kisansethu.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kisansethu.app.data.Booking
import com.kisansethu.app.data.BookingRepository
import com.kisansethu.app.data.BookingStatus
import com.kisansethu.app.data.normalizeStatus
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LiveQueueData(
    val centreName: String = "",
    val bookingDate: String = "",
    val timeSlot: String = "",
    val myToken: String = "",
    val currentServingToken: String = "",
    val farmersAhead: Int = 0,
    val myPosition: Int = 0,
    val queueStatus: String = ""
)

data class DashboardUiState(
    val farmerName: String = "",
    val farmerId: String = "",
    val hasActiveBooking: Boolean = false,
    val activeBooking: Booking? = null,
    val activeBookings: List<Booking> = emptyList(),
    val hasActiveQueue: Boolean = false,
    val liveQueueData: LiveQueueData? = null
)

class DashboardViewModel : ViewModel() {
    private val repository = BookingRepository()
    
    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private var myBookingsJob: Job? = null
    private var liveQueueJob: Job? = null

    fun initialize(name: String, id: String) {
        if (_uiState.value.farmerId == id && myBookingsJob?.isActive == true) return
        
        _uiState.value = _uiState.value.copy(
            farmerName = name,
            farmerId = id
        )
        
        observeMyActiveBookings(id)
    }

    private fun observeMyActiveBookings(farmerId: String) {
        myBookingsJob?.cancel()
        myBookingsJob = viewModelScope.launch {
            repository.getActiveBookingsRealtime(farmerId).collect { result ->
                if (result.isSuccess) {
                    val activeBookingsList = result.getOrNull() ?: emptyList()
                    val primaryActiveBooking = activeBookingsList.firstOrNull()

                    if (primaryActiveBooking != null) {
                        _uiState.value = _uiState.value.copy(
                            hasActiveBooking = true,
                            activeBooking = primaryActiveBooking,
                            activeBookings = activeBookingsList
                        )

                        val normStatus = normalizeStatus(primaryActiveBooking.status)
                        val isQueueState = normStatus in listOf(
                            BookingStatus.WAITING.name,
                            BookingStatus.NOW_SERVING.name,
                            BookingStatus.PROCESSING.name,
                            BookingStatus.CHECKED_IN.name
                        )

                        if (isQueueState) {
                            observeLiveQueue(primaryActiveBooking)
                        } else {
                            liveQueueJob?.cancel()
                            liveQueueJob = null
                            _uiState.value = _uiState.value.copy(
                                hasActiveQueue = false,
                                liveQueueData = null
                            )
                        }
                    } else {
                        liveQueueJob?.cancel()
                        liveQueueJob = null
                        _uiState.value = _uiState.value.copy(
                            hasActiveBooking = false,
                            activeBooking = null,
                            activeBookings = emptyList(),
                            hasActiveQueue = false,
                            liveQueueData = null
                        )
                    }
                }
            }
        }
    }

    private fun observeLiveQueue(activeBooking: Booking) {
        liveQueueJob?.cancel()
        val effectiveDate = activeBooking.bookingDate
        liveQueueJob = viewModelScope.launch {
            repository.getLiveQueueRealtime(activeBooking.centreId, effectiveDate).collect { result ->
                if (result.isSuccess) {
                    val queue = result.getOrNull() ?: emptyList()
                    
                    val myIndex = queue.indexOfFirst { entry ->
                        entry.farmerId == activeBooking.farmerId ||
                        entry.trackingId == activeBooking.trackingId ||
                        entry.bookingId == activeBooking.trackingId ||
                        entry.id == activeBooking.trackingId
                    }
                    
                    val myPosition = if (myIndex >= 0) myIndex + 1 else 0
                    val farmersAhead = if (myIndex > 0) myIndex else 0
                    
                    val servingEntry = queue.firstOrNull { 
                        val s = normalizeStatus(it.status)
                        s == BookingStatus.NOW_SERVING.name || s == BookingStatus.PROCESSING.name 
                    }
                    val currentServing = servingEntry?.getEffectiveToken()?.ifEmpty { "None" } ?: "None"

                    val myToken = activeBooking.queueToken.ifEmpty { 
                        if (myIndex >= 0) queue[myIndex].getEffectiveToken() else ""
                    }.ifEmpty { activeBooking.trackingId }

                    _uiState.value = _uiState.value.copy(
                        hasActiveQueue = true,
                        liveQueueData = LiveQueueData(
                            centreName = activeBooking.centreName,
                            bookingDate = activeBooking.bookingDate,
                            timeSlot = "${activeBooking.slotStartTime} - ${activeBooking.slotEndTime}",
                            myToken = myToken,
                            currentServingToken = currentServing,
                            farmersAhead = farmersAhead,
                            myPosition = myPosition,
                            queueStatus = activeBooking.status
                        )
                    )
                }
            }
        }
    }

    fun reset() {
        myBookingsJob?.cancel()
        myBookingsJob = null
        liveQueueJob?.cancel()
        liveQueueJob = null
        _uiState.value = DashboardUiState()
    }

    override fun onCleared() {
        super.onCleared()
        myBookingsJob?.cancel()
        liveQueueJob?.cancel()
    }
}
