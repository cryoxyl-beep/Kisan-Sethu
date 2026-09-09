package com.kisansethu.app.data

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.security.SecureRandom

class BookingRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val bookingsCollection = firestore.collection("bookings")
    private val queueEntriesCollection = firestore.collection("queueEntries")
    private val queueCountersCollection = firestore.collection("queueCounters")

    private fun generateTrackingId(): String {
        val year = java.time.Year.now().value.toString().takeLast(2)
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        val random = SecureRandom()
        val suffix = (1..6).map { chars[random.nextInt(chars.length)] }.joinToString("")
        return "KS$year$suffix"
    }

    suspend fun createBooking(booking: Booking): Result<Booking> {
        return try {
            var trackingId = generateTrackingId()
            var exists = true
            while (exists) {
                val doc = bookingsCollection.document(trackingId).get().await()
                if (!doc.exists()) {
                    exists = false
                } else {
                    trackingId = generateTrackingId()
                }
            }

            val finalBooking = booking.copy(
                trackingId = trackingId,
                qrCodeData = trackingId
            )

            bookingsCollection.document(trackingId).set(finalBooking).await()
            Result.success(finalBooking)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getFarmerBookingsRealtime(farmerId: String): Flow<Result<List<Booking>>> = callbackFlow {
        val cleanFarmerId = farmerId.trim()
        if (cleanFarmerId.isEmpty()) {
            trySend(Result.success(emptyList()))
            close()
            return@callbackFlow
        }
        var bookingsList: List<Booking>? = null
        var queueEntriesMap: Map<String, QueueEntry> = emptyMap()
        var queueInitialized = false

        fun emitCombined() {
            val rawBookings = bookingsList ?: return
            val merged = rawBookings.map { booking ->
                val tId = booking.trackingId.trim()
                val queueEntry = queueEntriesMap[booking.trackingId]
                    ?: queueEntriesMap[tId]
                    ?: queueEntriesMap[tId.uppercase()]
                    ?: queueEntriesMap[tId.lowercase()]
                    ?: queueEntriesMap[booking.slotId]
                    ?: queueEntriesMap[booking.slotId.trim()]
                mergeBookingWithQueueEntry(booking, queueEntry)
            }
            trySend(Result.success(merged))
        }

        var bookingsListener: ListenerRegistration? = null
        var queueListener: ListenerRegistration? = null

        try {
            bookingsListener = bookingsCollection
                .whereEqualTo("farmerId", cleanFarmerId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e("BookingRepository", "bookings listener error: ${error.message}", error)
                        trySend(Result.failure(error))
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        bookingsList = snapshot.documents.mapNotNull { doc ->
                            try {
                                val b = doc.toObject(Booking::class.java) ?: return@mapNotNull null
                                b.copy(
                                    trackingId = if (b.trackingId.isEmpty()) doc.id else b.trackingId,
                                    qrCodeData = if (b.qrCodeData.isEmpty()) doc.id else b.qrCodeData
                                )
                            } catch (e: Exception) {
                                Log.e("BookingRepository", "Error deserializing booking ${doc.id}", e)
                                null
                            }
                        }
                        if (queueInitialized || (bookingsList?.isEmpty() == true)) {
                            emitCombined()
                        }
                    }
                }

            queueListener = queueEntriesCollection
                .whereEqualTo("farmerId", cleanFarmerId)
                .addSnapshotListener { snapshot, error ->
                    queueInitialized = true
                    if (error != null) {
                        Log.w("BookingRepository", "queueEntries listener error: ${error.message}")
                        emitCombined()
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        val map = mutableMapOf<String, QueueEntry>()
                        for (doc in snapshot.documents) {
                            try {
                                val entry = doc.toObject(QueueEntry::class.java)
                                if (entry != null) {
                                    val resolved = entry.copy(
                                        id = if (entry.id.isEmpty()) doc.id else entry.id,
                                        trackingId = if (entry.trackingId.isEmpty()) (if (entry.bookingId.isNotEmpty()) entry.bookingId else doc.id) else entry.trackingId,
                                        bookingId = if (entry.bookingId.isEmpty()) (if (entry.trackingId.isNotEmpty()) entry.trackingId else doc.id) else entry.bookingId
                                    )
                                    val keys = listOf(
                                        doc.id,
                                        resolved.id,
                                        resolved.trackingId,
                                        resolved.bookingId
                                    ).filter { it.isNotBlank() }

                                    for (k in keys) {
                                        val cleanKey = k.trim()
                                        map[cleanKey] = resolved
                                        map[cleanKey.uppercase()] = resolved
                                        map[cleanKey.lowercase()] = resolved
                                    }
                                }
                            } catch (e: Exception) {
                                Log.e("BookingRepository", "Error deserializing queue entry ${doc.id}", e)
                            }
                        }
                        queueEntriesMap = map
                        emitCombined()
                    }
                }
        } catch (e: Exception) {
            trySend(Result.failure(e))
        }

        awaitClose {
            bookingsListener?.remove()
            queueListener?.remove()
        }
    }

    fun getUpcomingBookingsRealtime(farmerId: String): Flow<Result<List<Booking>>> =
        getFarmerBookingsRealtime(farmerId).map { result ->
            result.map { list ->
                list.filter { isUpcomingStatus(it.status) }
                    .sortedWith(compareBy({ it.bookingDate }, { it.slotStartTime }))
            }
        }

    fun getCompletedBookingsRealtime(farmerId: String): Flow<Result<List<Booking>>> =
        getFarmerBookingsRealtime(farmerId).map { result ->
            result.map { list ->
                list.filter { isCompletedStatus(it.status) }
                    .sortedWith(compareByDescending<Booking> { it.bookingDate }.thenByDescending { it.createdAt })
            }
        }

    fun getActiveBookingsRealtime(farmerId: String): Flow<Result<List<Booking>>> =
        getFarmerBookingsRealtime(farmerId).map { result ->
            result.map { list ->
                list.filter { isUpcomingStatus(it.status) }
                    .sortedWith(
                        compareBy<Booking> { booking ->
                            when (normalizeStatus(booking.status)) {
                                BookingStatus.PROCESSING.name -> 0
                                BookingStatus.NOW_SERVING.name -> 1
                                BookingStatus.WAITING.name -> 2
                                BookingStatus.CHECKED_IN.name -> 3
                                BookingStatus.CONFIRMED.name -> 4
                                BookingStatus.BOOKED.name -> 5
                                else -> 6
                            }
                        }.thenBy { it.bookingDate }.thenBy { it.slotStartTime }
                    )
            }
        }

    fun getActiveBookingRealtime(farmerId: String): Flow<Result<Booking?>> =
        getActiveBookingsRealtime(farmerId).map { result ->
            result.map { list -> list.firstOrNull() }
        }

    fun getFarmerActiveQueueEntry(farmerId: String): Flow<Result<QueueEntry?>> = callbackFlow {
        val cleanFarmerId = farmerId.trim()
        if (cleanFarmerId.isEmpty()) {
            trySend(Result.success(null))
            close()
            return@callbackFlow
        }
        val listener = queueEntriesCollection
            .whereEqualTo("farmerId", cleanFarmerId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Result.failure(error))
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val activeEntries = snapshot.documents.mapNotNull { doc ->
                        try {
                            doc.toObject(QueueEntry::class.java)?.let { entry ->
                                entry.copy(
                                    id = if (entry.id.isEmpty()) doc.id else entry.id,
                                    trackingId = if (entry.trackingId.isEmpty()) (if (entry.bookingId.isNotEmpty()) entry.bookingId else doc.id) else entry.trackingId,
                                    bookingId = if (entry.bookingId.isEmpty()) (if (entry.trackingId.isNotEmpty()) entry.trackingId else doc.id) else entry.bookingId
                                )
                            }
                        } catch (e: Exception) {
                            null
                        }
                    }.filter {
                        normalizeStatus(it.status) in listOf(
                            BookingStatus.CHECKED_IN.name,
                            BookingStatus.WAITING.name,
                            BookingStatus.NOW_SERVING.name,
                            BookingStatus.PROCESSING.name
                        )
                    }
                    trySend(Result.success(activeEntries.firstOrNull()))
                } else {
                    trySend(Result.success(null))
                }
            }
        awaitClose {
            listener.remove()
        }
    }

    fun getLiveQueueRealtime(centreId: String, procurementDate: String): Flow<Result<List<QueueEntry>>> = callbackFlow {
        val cleanCentreId = centreId.trim()
        val cleanDate = procurementDate.trim()
        if (cleanCentreId.isEmpty()) {
            trySend(Result.success(emptyList()))
            close()
            return@callbackFlow
        }
        val listener = queueEntriesCollection
            .whereEqualTo("centreId", cleanCentreId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Result.failure(error))
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val entries = snapshot.documents.mapNotNull { doc ->
                        try {
                            doc.toObject(QueueEntry::class.java)?.let { entry ->
                                entry.copy(
                                    id = if (entry.id.isEmpty()) doc.id else entry.id,
                                    trackingId = if (entry.trackingId.isEmpty()) (if (entry.bookingId.isNotEmpty()) entry.bookingId else doc.id) else entry.trackingId,
                                    bookingId = if (entry.bookingId.isEmpty()) (if (entry.trackingId.isNotEmpty()) entry.trackingId else doc.id) else entry.bookingId
                                )
                            }
                        } catch (e: Exception) {
                            null
                        }
                    }.filter { entry ->
                        val entryDate = entry.getEffectiveDate().trim()
                        val matchesDate = cleanDate.isEmpty() || entryDate == cleanDate
                        matchesDate &&
                        normalizeStatus(entry.status) in listOf(
                            BookingStatus.WAITING.name,
                            BookingStatus.NOW_SERVING.name,
                            BookingStatus.PROCESSING.name,
                            BookingStatus.CHECKED_IN.name
                        )
                    }
                    val sortedEntries = sortQueueEntries(entries)
                    trySend(Result.success(sortedEntries))
                }
            }
        awaitClose {
            listener.remove()
        }
    }

    fun getQueueCounterRealtime(centreId: String, date: String): Flow<Result<QueueCounter?>> = callbackFlow {
        val cleanCentreId = centreId.trim()
        val cleanDate = date.trim()
        if (cleanCentreId.isEmpty() || cleanDate.isEmpty()) {
            trySend(Result.success(null))
            close()
            return@callbackFlow
        }
        val docId = "${cleanCentreId}_$cleanDate"
        val listener = queueCountersCollection.document(docId).addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(Result.failure(error))
                return@addSnapshotListener
            }
            if (snapshot != null && snapshot.exists()) {
                val counter = try {
                    val base = snapshot.toObject(QueueCounter::class.java)
                    base?.copy(
                        centreId = snapshot.getString("centreId") ?: cleanCentreId,
                        date = snapshot.getString("date") ?: cleanDate,
                        lastTokenNumber = snapshot.getLong("lastTokenNumber") ?: base.lastTokenNumber,
                        activeServingId = snapshot.getString("activeServingId") ?: base.activeServingId,
                        activeServingToken = snapshot.getString("activeServingToken")
                            ?: snapshot.getString("servingToken")
                            ?: base.activeServingToken
                    )
                } catch (e: Exception) {
                    null
                }
                trySend(Result.success(counter))
            } else {
                trySend(Result.success(null))
            }
        }
        awaitClose {
            listener.remove()
        }
    }

    fun getAuthoritativeBookingRealtime(trackingId: String): Flow<Result<Booking>> = callbackFlow {
        val cleanTrackingId = trackingId.trim()
        if (cleanTrackingId.isEmpty()) {
            trySend(Result.failure(IllegalArgumentException("Tracking ID cannot be empty")))
            close()
            return@callbackFlow
        }
        var booking: Booking? = null
        var directQueueEntry: QueueEntry? = null
        var queryQueueEntry: QueueEntry? = null

        fun emitCombined() {
            val b = booking ?: return
            val queueEntry = directQueueEntry ?: queryQueueEntry
            val merged = mergeBookingWithQueueEntry(b, queueEntry)
            trySend(Result.success(merged))
        }

        var bookingListener: ListenerRegistration? = null
        var queueDocListener: ListenerRegistration? = null
        var queueQueryListener: ListenerRegistration? = null

        try {
            bookingListener = bookingsCollection.document(cleanTrackingId).addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Result.failure(error))
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    val b = try {
                        snapshot.toObject(Booking::class.java)
                    } catch (e: Exception) {
                        Log.e("BookingRepository", "Error parsing booking $cleanTrackingId", e)
                        null
                    }
                    booking = b?.let {
                        it.copy(
                            trackingId = if (it.trackingId.isEmpty()) snapshot.id else it.trackingId,
                            qrCodeData = if (it.qrCodeData.isEmpty()) snapshot.id else it.qrCodeData
                        )
                    }
                    emitCombined()
                } else if (snapshot != null && !snapshot.exists()) {
                    trySend(Result.failure(Exception("Booking not found")))
                }
            }

            // Direct document listener on queueEntries/{cleanTrackingId}
            queueDocListener = queueEntriesCollection.document(cleanTrackingId).addSnapshotListener { snapshot, error ->
                if (error == null && snapshot != null && snapshot.exists()) {
                    directQueueEntry = try {
                        snapshot.toObject(QueueEntry::class.java)?.let {
                            it.copy(
                                id = if (it.id.isEmpty()) snapshot.id else it.id,
                                trackingId = if (it.trackingId.isEmpty()) snapshot.id else it.trackingId,
                                bookingId = if (it.bookingId.isEmpty()) snapshot.id else it.bookingId
                            )
                        }
                    } catch (e: Exception) {
                        Log.e("BookingRepository", "Error parsing queue entry $cleanTrackingId", e)
                        null
                    }
                } else {
                    directQueueEntry = null
                }
                emitCombined()
            }

            // Query listener on queueEntries where trackingId == cleanTrackingId
            queueQueryListener = queueEntriesCollection.whereEqualTo("trackingId", cleanTrackingId).addSnapshotListener { snapshot, error ->
                if (error == null && snapshot != null && !snapshot.isEmpty) {
                    val doc = snapshot.documents.firstOrNull()
                    queryQueueEntry = try {
                        doc?.toObject(QueueEntry::class.java)?.let {
                            it.copy(
                                id = if (it.id.isEmpty()) doc.id else it.id,
                                trackingId = if (it.trackingId.isEmpty()) doc.id else it.trackingId,
                                bookingId = if (it.bookingId.isEmpty()) doc.id else it.bookingId
                            )
                        }
                    } catch (e: Exception) {
                        null
                    }
                } else {
                    queryQueueEntry = null
                }
                emitCombined()
            }
        } catch (e: Exception) {
            trySend(Result.failure(e))
        }

        awaitClose {
            bookingListener?.remove()
            queueDocListener?.remove()
            queueQueryListener?.remove()
        }
    }

    fun getBookingRealtime(trackingId: String): Flow<Result<Booking>> =
        getAuthoritativeBookingRealtime(trackingId)
}
