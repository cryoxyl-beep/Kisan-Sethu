package com.kisansethu.app.data

import com.google.firebase.firestore.IgnoreExtraProperties
import java.time.LocalDate

enum class BookingStatus {
    BOOKED,
    CONFIRMED,
    CHECKED_IN,
    WAITING,
    NOW_SERVING,
    PROCESSING,
    COMPLETED,
    MISSED,
    CANCELLED
}

enum class QuantityUnit {
    KG,
    QUINTAL
}

data class ProcurementCentre(
    val id: String,
    val name: String,
    val location: String,
    val availableSlots: Int,
    val status: String
)

data class TimeSlot(
    val id: String,
    val startTime: String,
    val endTime: String,
    val capacity: Int,
    val booked: Int
) {
    val remaining: Int get() = capacity - booked
    val isAvailable: Boolean get() = remaining > 0
    
    fun getDisplayString(): String {
        return "$startTime - $endTime"
    }
}

@IgnoreExtraProperties
data class Booking(
    val trackingId: String = "",
    val farmerId: String = "",
    val farmerName: String = "",
    val centreId: String = "",
    val centreName: String = "",
    val bookingDate: String = "", // ISO format YYYY-MM-DD
    val slotId: String = "",
    val slotStartTime: String = "",
    val slotEndTime: String = "",
    val crop: String = "",
    val quantity: Double = 0.0,
    val quantityUnit: String = QuantityUnit.QUINTAL.name,
    val quantityKg: Double = 0.0,
    val status: String = BookingStatus.BOOKED.name,
    val qrCodeData: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val queueToken: String = "",
    val checkedInAt: java.util.Date? = null
)

@IgnoreExtraProperties
data class QueueEntry(
    val id: String = "",
    val bookingId: String = "",
    val trackingId: String = "",
    val farmerId: String = "",
    val farmerName: String = "",
    val centreId: String = "",
    val centreName: String = "",
    val queueDate: String = "",
    val procurementDate: String = "",
    val tokenLabel: String = "",
    val queueToken: String = "",
    val tokenNumber: Long = 0L,
    val status: String = "",
    val checkInTime: java.util.Date? = null,
    val checkedInAt: java.util.Date? = null,
    val queueJoinedAt: java.util.Date? = null,
    val startedServingAt: java.util.Date? = null,
    val processingStartedAt: java.util.Date? = null,
    val completedAt: java.util.Date? = null,
    val updatedAt: java.util.Date? = null,
    val slotStartTime: String = "",
    val slotEndTime: String = ""
) {
    fun getEffectiveToken(): String = tokenLabel.ifEmpty { queueToken }
    fun getEffectiveDate(): String = queueDate.ifEmpty { procurementDate }
    fun getEffectiveCheckInTime(): Long = (checkInTime ?: checkedInAt ?: queueJoinedAt)?.time ?: 0L
}

fun normalizeStatus(status: String?): String {
    if (status.isNullOrBlank()) return BookingStatus.BOOKED.name
    val clean = status.trim().uppercase().replace(' ', '_').replace('-', '_')
    return when (clean) {
        "BOOKED", "SCHEDULED", "SCHEDULED_BOOKING" -> BookingStatus.BOOKED.name
        "CONFIRMED" -> BookingStatus.CONFIRMED.name
        "CHECKED_IN", "CHECKEDIN" -> BookingStatus.CHECKED_IN.name
        "WAITING", "WAITING_IN_QUEUE", "IN_QUEUE", "QUEUED" -> BookingStatus.WAITING.name
        "NOW_SERVING", "SERVING" -> BookingStatus.NOW_SERVING.name
        "PROCESSING", "IN_PROGRESS", "IN_PROCUREMENT" -> BookingStatus.PROCESSING.name
        "COMPLETED", "COMPLETE", "DONE", "FINISHED", "PROCURED" -> BookingStatus.COMPLETED.name
        "CANCELLED", "CANCELED", "REJECTED" -> BookingStatus.CANCELLED.name
        "MISSED" -> BookingStatus.MISSED.name
        else -> clean
    }
}

fun statusRank(status: String): Int {
    return when (normalizeStatus(status)) {
        BookingStatus.BOOKED.name -> 1
        BookingStatus.CONFIRMED.name -> 2
        BookingStatus.CHECKED_IN.name -> 3
        BookingStatus.WAITING.name -> 4
        BookingStatus.NOW_SERVING.name -> 5
        BookingStatus.PROCESSING.name -> 6
        BookingStatus.COMPLETED.name -> 7
        BookingStatus.CANCELLED.name -> 8
        BookingStatus.MISSED.name -> 8
        else -> 0
    }
}

fun getAuthoritativeStatus(bookingStatus: String, queueEntryStatus: String?): String {
    val bNorm = normalizeStatus(bookingStatus)
    // Terminal statuses already reached on booking must be preserved
    if (bNorm in listOf(BookingStatus.COMPLETED.name, BookingStatus.CANCELLED.name, BookingStatus.MISSED.name)) {
        return bNorm
    }
    if (queueEntryStatus.isNullOrBlank()) {
        return bNorm
    }
    val qNorm = normalizeStatus(queueEntryStatus)
    return if (statusRank(qNorm) >= statusRank(bNorm)) qNorm else bNorm
}

fun mergeBookingWithQueueEntry(booking: Booking, queueEntry: QueueEntry?): Booking {
    if (queueEntry == null) {
        return booking.copy(status = normalizeStatus(booking.status))
    }
    val authStatus = getAuthoritativeStatus(booking.status, queueEntry.status)
    val token = queueEntry.getEffectiveToken().ifEmpty { booking.queueToken }
    val checkIn = queueEntry.checkInTime ?: queueEntry.checkedInAt ?: booking.checkedInAt
    return booking.copy(
        status = authStatus,
        queueToken = token,
        checkedInAt = checkIn
    )
}

fun isCompletedStatus(status: String?): Boolean {
    val norm = normalizeStatus(status)
    return norm in listOf(BookingStatus.COMPLETED.name, BookingStatus.CANCELLED.name, BookingStatus.MISSED.name)
}

fun isUpcomingStatus(status: String?): Boolean {
    val norm = normalizeStatus(status)
    return norm in listOf(
        BookingStatus.BOOKED.name,
        BookingStatus.CONFIRMED.name,
        BookingStatus.CHECKED_IN.name,
        BookingStatus.WAITING.name,
        BookingStatus.NOW_SERVING.name,
        BookingStatus.PROCESSING.name
    )
}

fun isBookingToday(dateStr: String?): Boolean {
    if (dateStr.isNullOrBlank()) return false
    val clean = dateStr.trim()
    val today = LocalDate.now().toString()
    if (clean == today) return true
    return try {
        val parsed = LocalDate.parse(clean.take(10))
        parsed == LocalDate.now()
    } catch (e: Exception) {
        false
    }
}

fun getDisplayStatus(booking: Booking): String {
    val norm = normalizeStatus(booking.status)
    return when (norm) {
        BookingStatus.BOOKED.name -> {
            if (isBookingToday(booking.bookingDate)) "BOOKED" else "SCHEDULED"
        }
        BookingStatus.CONFIRMED.name -> "CONFIRMED"
        BookingStatus.CHECKED_IN.name -> "CHECKED IN"
        BookingStatus.WAITING.name -> "WAITING"
        BookingStatus.NOW_SERVING.name -> "NOW SERVING"
        BookingStatus.PROCESSING.name -> "PROCESSING"
        BookingStatus.COMPLETED.name -> "COMPLETED"
        BookingStatus.CANCELLED.name -> "CANCELLED"
        BookingStatus.MISSED.name -> "MISSED"
        else -> norm.replace('_', ' ')
    }
}
