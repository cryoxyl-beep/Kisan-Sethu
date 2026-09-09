package com.kisansethu.app.ui.booking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kisansethu.app.data.Booking
import com.kisansethu.app.data.BookingRepository
import com.kisansethu.app.data.BookingStatus
import com.kisansethu.app.data.QueueCounter
import com.kisansethu.app.data.QueueEntry
import com.kisansethu.app.data.formatTokenDisplay
import com.kisansethu.app.data.getDisplayStatus
import com.kisansethu.app.data.normalizeStatus
import com.kisansethu.app.data.sortQueueEntries
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class LiveTicketUiState(
    val isLoading: Boolean = true,
    val booking: Booking? = null,
    val status: String = "",
    val displayStatus: String = "",
    val myToken: String = "",
    val currentServingToken: String = "",
    val myPosition: Int = 0,
    val peopleAhead: Int = 0,
    val centreName: String = "",
    val bookingDate: String = "",
    val slotTime: String = "",
    val crop: String = "",
    val quantityFormatted: String = "",
    val trackingId: String = "",
    val isMyTurn: Boolean = false,
    val isProcessing: Boolean = false,
    val isCompleted: Boolean = false,
    val isLiveConnected: Boolean = true,
    val error: String? = null
)

class LiveTicketViewModel : ViewModel() {
    private val repository = BookingRepository()

    private val _uiState = MutableStateFlow(LiveTicketUiState())
    val uiState: StateFlow<LiveTicketUiState> = _uiState.asStateFlow()

    private var currentTrackingId: String? = null
    private var bookingJob: Job? = null
    private var queueJob: Job? = null
    private var counterJob: Job? = null

    private var currentBooking: Booking? = null
    private var currentQueue: List<QueueEntry> = emptyList()
    private var currentCounter: QueueCounter? = null

    fun startListening(initialBooking: Booking) {
        val trackingId = initialBooking.trackingId.trim()
        if (trackingId.isEmpty()) return
        if (currentTrackingId == trackingId && bookingJob?.isActive == true) return

        stopListening()
        currentTrackingId = trackingId

        // Apply initial booking synchronously so UI renders immediately
        currentBooking = initialBooking
        updateCombinedState(isConnected = true)

        bookingJob = viewModelScope.launch {
            repository.getAuthoritativeBookingRealtime(trackingId).collect { result ->
                if (result.isSuccess) {
                    val b = result.getOrNull()
                    if (b != null) {
                        val prevCentre = currentBooking?.centreId
                        val prevDate = currentBooking?.bookingDate
                        currentBooking = b

                        if (queueJob == null || prevCentre != b.centreId || prevDate != b.bookingDate) {
                            listenToQueue(b.centreId, b.bookingDate)
                        }
                        updateCombinedState(isConnected = true)
                    }
                } else {
                    updateCombinedState(isConnected = false)
                    _uiState.update {
                        it.copy(
                            isLoading = currentBooking == null,
                            isLiveConnected = false,
                            error = result.exceptionOrNull()?.message
                        )
                    }
                }
            }
        }

        if (initialBooking.centreId.isNotEmpty() && initialBooking.bookingDate.isNotEmpty()) {
            listenToQueue(initialBooking.centreId, initialBooking.bookingDate)
        }
    }

    private fun listenToQueue(centreId: String, procurementDate: String) {
        queueJob?.cancel()
        counterJob?.cancel()

        if (centreId.isEmpty() || procurementDate.isEmpty()) return

        queueJob = viewModelScope.launch {
            repository.getLiveQueueRealtime(centreId, procurementDate).collect { result ->
                if (result.isSuccess) {
                    currentQueue = result.getOrNull() ?: emptyList()
                    updateCombinedState(isConnected = true)
                } else {
                    updateCombinedState(isConnected = false)
                }
            }
        }

        counterJob = viewModelScope.launch {
            repository.getQueueCounterRealtime(centreId, procurementDate).collect { result ->
                if (result.isSuccess) {
                    currentCounter = result.getOrNull()
                    updateCombinedState(isConnected = true)
                }
            }
        }
    }

    private fun updateCombinedState(isConnected: Boolean = true) {
        val b = currentBooking ?: return
        val normStatus = normalizeStatus(b.status)
        val sortedQueue = sortQueueEntries(currentQueue)

        val isMyTurn = normStatus == BookingStatus.NOW_SERVING.name
        val isProcessing = normStatus == BookingStatus.PROCESSING.name
        val isCompleted = normStatus == BookingStatus.COMPLETED.name

        // Find currently serving token
        val servingEntry = sortedQueue.firstOrNull {
            normalizeStatus(it.status) == BookingStatus.NOW_SERVING.name
        }
        val servingFromCounter = if (servingEntry == null && currentCounter != null && currentCounter!!.activeServingId.isNotEmpty()) {
            sortedQueue.firstOrNull {
                it.trackingId.equals(currentCounter!!.activeServingId, ignoreCase = true) ||
                it.bookingId.equals(currentCounter!!.activeServingId, ignoreCase = true) ||
                it.id.equals(currentCounter!!.activeServingId, ignoreCase = true)
            }
        } else null

        val currentServingToken = when {
            servingEntry != null -> formatTokenDisplay(servingEntry.getEffectiveToken(), servingEntry.tokenNumber)
            servingFromCounter != null -> formatTokenDisplay(servingFromCounter.getEffectiveToken(), servingFromCounter.tokenNumber)
            currentCounter != null && currentCounter!!.activeServingToken.isNotEmpty() -> formatTokenDisplay(currentCounter!!.activeServingToken)
            else -> "No farmer currently being served"
        }

        // Determine farmer's token
        val myQueueEntry = sortedQueue.firstOrNull {
            it.trackingId.equals(b.trackingId, ignoreCase = true) ||
            it.bookingId.equals(b.trackingId, ignoreCase = true) ||
            it.id.equals(b.trackingId, ignoreCase = true)
        } ?: sortedQueue.firstOrNull {
            it.farmerId.isNotEmpty() && it.farmerId.equals(b.farmerId, ignoreCase = true)
        }
        val rawToken = myQueueEntry?.getEffectiveToken()?.ifEmpty { b.queueToken } ?: b.queueToken
        val tokenNum = if ((myQueueEntry?.tokenNumber ?: 0L) > 0L) myQueueEntry!!.tokenNumber else b.tokenNumber
        val formattedMyToken = formatTokenDisplay(rawToken, tokenNum)

        // Determine queue position and people ahead
        val (myPosition, peopleAhead) = when {
            isCompleted -> 0 to 0
            isMyTurn -> 1 to 0
            isProcessing -> 1 to 0
            else -> {
                val myIndex = sortedQueue.indexOfFirst {
                    it.trackingId.equals(b.trackingId, ignoreCase = true) ||
                    it.bookingId.equals(b.trackingId, ignoreCase = true) ||
                    it.id.equals(b.trackingId, ignoreCase = true)
                }.let { idx ->
                    if (idx < 0) {
                        sortedQueue.indexOfFirst { it.farmerId.isNotEmpty() && it.farmerId.equals(b.farmerId, ignoreCase = true) }
                    } else idx
                }
                if (myIndex >= 0) {
                    (myIndex + 1) to (if (myIndex > 0) myIndex else 0)
                } else {
                    0 to 0
                }
            }
        }

        val formattedDate = try {
            val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy")
            LocalDate.parse(b.bookingDate).format(formatter)
        } catch (e: Exception) {
            b.bookingDate
        }

        val formattedQty = if (b.quantity % 1.0 == 0.0) {
            "${b.quantity.toInt()}.0"
        } else {
            b.quantity.toString()
        }

        _uiState.update {
            it.copy(
                isLoading = false,
                booking = b,
                status = normStatus,
                displayStatus = getDisplayStatus(b),
                myToken = formattedMyToken,
                currentServingToken = currentServingToken,
                myPosition = myPosition,
                peopleAhead = peopleAhead,
                centreName = b.centreName,
                bookingDate = formattedDate,
                slotTime = "${b.slotStartTime} - ${b.slotEndTime}".trim().removePrefix("-").removeSuffix("-").trim(),
                crop = b.crop,
                quantityFormatted = "$formattedQty ${b.quantityUnit.lowercase()}",
                trackingId = b.trackingId,
                isMyTurn = isMyTurn,
                isProcessing = isProcessing,
                isCompleted = isCompleted,
                isLiveConnected = isConnected,
                error = null
            )
        }
    }

    fun stopListening() {
        bookingJob?.cancel()
        bookingJob = null
        queueJob?.cancel()
        queueJob = null
        counterJob?.cancel()
        counterJob = null
        currentTrackingId = null
    }

    override fun onCleared() {
        super.onCleared()
        stopListening()
    }
}
