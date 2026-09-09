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

    private var currentObservedCentre: String? = null
    private var currentObservedDate: String? = null

    private fun observeLiveQueue(activeBooking: Booking) {
        val effectiveDate = activeBooking.bookingDate.trim()
        val centreId = activeBooking.centreId.trim()
        if (centreId == currentObservedCentre && effectiveDate == currentObservedDate && liveQueueJob?.isActive == true) {
            return
        }
        liveQueueJob?.cancel()
        currentObservedCentre = centreId
        currentObservedDate = effectiveDate

        liveQueueJob = viewModelScope.launch {
            repository.getLiveQueueRealtime(centreId, effectiveDate).collect { result ->
                if (result.isSuccess) {
                    val queue = result.getOrNull() ?: emptyList()
                    val normStatus = normalizeStatus(activeBooking.status)

                    val myIndex = queue.indexOfFirst { entry ->
                        entry.trackingId.equals(activeBooking.trackingId, ignoreCase = true) ||
                        entry.bookingId.equals(activeBooking.trackingId, ignoreCase = true) ||
                        entry.id.equals(activeBooking.trackingId, ignoreCase = true)
                    }.let { idx ->
                        if (idx < 0) {
                            queue.indexOfFirst { entry ->
                                entry.farmerId.isNotEmpty() && entry.farmerId.equals(activeBooking.farmerId, ignoreCase = true)
                            }
                        } else idx
                    }

                    val (myPosition, farmersAhead) = when (normStatus) {
                        BookingStatus.NOW_SERVING.name, BookingStatus.PROCESSING.name -> 1 to 0
                        else -> {
                            if (myIndex >= 0) {
                                (myIndex + 1) to (if (myIndex > 0) myIndex else 0)
                            } else {
                                0 to 0
                            }
                        }
                    }

                    val servingEntry = queue.firstOrNull {
                        val s = normalizeStatus(it.status)
                        s == BookingStatus.NOW_SERVING.name
                    }
                    val currentServing = if (servingEntry != null) {
                        com.kisansethu.app.data.formatTokenDisplay(servingEntry.getEffectiveToken(), servingEntry.tokenNumber)
                    } else {
                        "No farmer currently being served"
                    }

                    val myEntry = if (myIndex >= 0) queue[myIndex] else null
                    val rawMyToken = myEntry?.getEffectiveToken()?.ifEmpty { activeBooking.queueToken } ?: activeBooking.queueToken
                    val tokenNum = if ((myEntry?.tokenNumber ?: 0L) > 0L) myEntry!!.tokenNumber else activeBooking.tokenNumber
                    val formattedMyToken = com.kisansethu.app.data.formatTokenDisplay(rawMyToken, tokenNum)

                    _uiState.value = _uiState.value.copy(
                        hasActiveQueue = true,
                        liveQueueData = LiveQueueData(
                            centreName = activeBooking.centreName,
                            bookingDate = activeBooking.bookingDate,
                            timeSlot = "${activeBooking.slotStartTime} - ${activeBooking.slotEndTime}",
                            myToken = formattedMyToken,
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
        currentObservedCentre = null
        currentObservedDate = null
        _uiState.value = DashboardUiState()
    }

    override fun onCleared() {
        super.onCleared()
        myBookingsJob?.cancel()
        liveQueueJob?.cancel()
        currentObservedCentre = null
        currentObservedDate = null
    }
}
