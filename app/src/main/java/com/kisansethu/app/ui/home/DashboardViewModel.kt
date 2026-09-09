package com.kisansethu.app.ui.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kisansethu.app.data.BookingRepository
import com.kisansethu.app.data.BookingStatus
import com.kisansethu.app.data.QueueEntry
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
    val activeBooking: com.kisansethu.app.data.Booking? = null,
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
        if (_uiState.value.farmerId == id) return
        
        _uiState.value = _uiState.value.copy(
            farmerName = name,
            farmerId = id
        )
        
        observeMyActiveBooking(id)
    }

    private fun observeMyActiveBooking(farmerId: String) {
        myBookingsJob?.cancel()
        myBookingsJob = viewModelScope.launch {
            repository.getActiveBookingRealtime(farmerId).collect { result ->
                if (result.isSuccess) {
                    val activeBooking = result.getOrNull()

                    if (activeBooking != null) {
                        _uiState.value = _uiState.value.copy(
                            hasActiveBooking = true,
                            activeBooking = activeBooking
                        )
                        // If it's in a queue state, observe the live queue
                        val isQueueState = activeBooking.status in listOf(
                            BookingStatus.WAITING.name,
                            BookingStatus.NOW_SERVING.name,
                            BookingStatus.PROCESSING.name,
                            BookingStatus.CHECKED_IN.name // Checked in might not have queue data yet, but we can try
                        )
                        if (isQueueState) {
                            observeLiveQueue(activeBooking)
                        } else {
                            liveQueueJob?.cancel()
                            _uiState.value = _uiState.value.copy(
                                hasActiveQueue = false,
                                liveQueueData = null
                            )
                        }
                    } else {
                        liveQueueJob?.cancel()
                        _uiState.value = _uiState.value.copy(
                            hasActiveBooking = false,
                            activeBooking = null,
                            hasActiveQueue = false,
                            liveQueueData = null
                        )
                    }
                }
            }
        }
    }

    private fun observeLiveQueue(activeBooking: com.kisansethu.app.data.Booking) {
        liveQueueJob?.cancel()
        liveQueueJob = viewModelScope.launch {
            repository.getLiveQueueRealtime(activeBooking.centreId, activeBooking.bookingDate).collect { result ->
                if (result.isSuccess) {
                    val queue = result.getOrNull() ?: emptyList()
                    
                    val myIndex = queue.indexOfFirst { it.farmerId == activeBooking.farmerId }
                    
                    val myPosition = if (myIndex >= 0) myIndex + 1 else 0
                    
                    val farmersAhead = if (myIndex > 0) myIndex else 0
                    
                    val currentServing = queue.firstOrNull { 
                        it.status == BookingStatus.NOW_SERVING.name || 
                        it.status == BookingStatus.PROCESSING.name 
                    }?.queueToken ?: "None"

                    _uiState.value = _uiState.value.copy(
                        hasActiveQueue = true,
                        liveQueueData = LiveQueueData(
                            centreName = activeBooking.centreName,
                            bookingDate = activeBooking.bookingDate,
                            timeSlot = "${activeBooking.slotStartTime} - ${activeBooking.slotEndTime}",
                            myToken = activeBooking.queueToken.ifEmpty { activeBooking.trackingId },
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
}
