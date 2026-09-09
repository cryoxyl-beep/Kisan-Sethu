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
            repository.getFarmerActiveQueueEntry(farmerId).collect { result ->
                if (result.isSuccess) {
                    val activeQueueEntry = result.getOrNull()

                    if (activeQueueEntry != null) {
                        observeLiveQueue(activeQueueEntry)
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

    private fun observeLiveQueue(myQueueEntry: QueueEntry) {
        liveQueueJob?.cancel()
        liveQueueJob = viewModelScope.launch {
            repository.getLiveQueueRealtime(myQueueEntry.centreId, myQueueEntry.procurementDate).collect { result ->
                if (result.isSuccess) {
                    val queue = result.getOrNull() ?: emptyList()
                    
                    // The queue is already sorted by checkedInAt from the repository
                    val myIndex = queue.indexOfFirst { it.id == myQueueEntry.id }
                    
                    val myPosition = if (myIndex >= 0) myIndex + 1 else 0
                    
                    // People ahead are the ones before me in the queue
                    val farmersAhead = if (myIndex > 0) myIndex else 0
                    
                    // Current serving token: the first one with NOW_SERVING or PROCESSING
                    val currentServing = queue.firstOrNull { 
                        it.status == BookingStatus.NOW_SERVING.name || 
                        it.status == BookingStatus.PROCESSING.name 
                    }?.queueToken ?: "None"

                    _uiState.value = _uiState.value.copy(
                        hasActiveQueue = true,
                        liveQueueData = LiveQueueData(
                            centreName = myQueueEntry.centreName,
                            bookingDate = myQueueEntry.procurementDate,
                            timeSlot = "${myQueueEntry.slotStartTime} - ${myQueueEntry.slotEndTime}",
                            myToken = myQueueEntry.queueToken.ifEmpty { myQueueEntry.id },
                            currentServingToken = currentServing,
                            farmersAhead = farmersAhead,
                            myPosition = myPosition,
                            queueStatus = myQueueEntry.status
                        )
                    )
                }
            }
        }
    }
}
