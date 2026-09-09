package com.kisansethu.app.ui.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kisansethu.app.data.Booking
import com.kisansethu.app.data.BookingRepository
import com.kisansethu.app.data.BookingStatus
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
        
        observeMyQueueStatus(id)
    }

    private fun observeMyQueueStatus(farmerId: String) {
        myBookingsJob?.cancel()
        myBookingsJob = viewModelScope.launch {
            repository.getUpcomingBookingsRealtime(farmerId).collect { result ->
                if (result.isSuccess) {
                    val bookings = result.getOrNull() ?: emptyList()
                    val activeQueueBooking = bookings.firstOrNull { 
                        it.status == BookingStatus.WAITING.name || 
                        it.status == BookingStatus.NOW_SERVING.name || 
                        it.status == BookingStatus.PROCESSING.name 
                    }

                    if (activeQueueBooking != null) {
                        observeLiveQueue(activeQueueBooking)
                    } else {
                        liveQueueJob?.cancel()
                        _uiState.value = _uiState.value.copy(
                            hasActiveQueue = false,
                            liveQueueData = null
                        )
                    }
                }
            }
        }
    }

    private fun observeLiveQueue(myBooking: Booking) {
        liveQueueJob?.cancel()
        liveQueueJob = viewModelScope.launch {
            repository.getLiveQueueRealtime(myBooking.centreId, myBooking.bookingDate).collect { result ->
                if (result.isSuccess) {
                    val queue = result.getOrNull() ?: emptyList()
                    
                    // The queue is already sorted by checkedInAt from the repository
                    val myIndex = queue.indexOfFirst { it.trackingId == myBooking.trackingId }
                    
                    val myPosition = if (myIndex >= 0) myIndex + 1 else 0
                    
                    // People ahead are the ones before me in the queue who are WAITING or NOW_SERVING
                    // Wait, people ahead in queue = myIndex
                    val farmersAhead = if (myIndex > 0) myIndex else 0
                    
                    // Current serving token: the first one with NOW_SERVING or PROCESSING
                    val currentServing = queue.firstOrNull { 
                        it.status == BookingStatus.NOW_SERVING.name || 
                        it.status == BookingStatus.PROCESSING.name 
                    }?.queueToken ?: "None"

                    _uiState.value = _uiState.value.copy(
                        hasActiveQueue = true,
                        liveQueueData = LiveQueueData(
                            centreName = myBooking.centreName,
                            bookingDate = myBooking.bookingDate,
                            timeSlot = "${myBooking.slotStartTime} - ${myBooking.slotEndTime}",
                            myToken = myBooking.queueToken.ifEmpty { myBooking.trackingId },
                            currentServingToken = currentServing,
                            farmersAhead = farmersAhead,
                            myPosition = myPosition,
                            queueStatus = myBooking.status
                        )
                    )
                }
            }
        }
    }
}
