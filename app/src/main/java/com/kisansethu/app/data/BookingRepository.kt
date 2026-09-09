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

    private fun safeGetDouble(doc: com.google.firebase.firestore.DocumentSnapshot, vararg fieldNames: String): Double? {
        for (name in fieldNames) {
            val v = doc.get(name) ?: continue
            when (v) {
                is Number -> return v.toDouble()
                is String -> {
                    val parsed = v.trim().toDoubleOrNull()
                    if (parsed != null) return parsed
                }
            }
        }
        return null
    }

    private fun safeGetLong(doc: com.google.firebase.firestore.DocumentSnapshot, vararg fieldNames: String): Long? {
        for (name in fieldNames) {
            val v = doc.get(name) ?: continue
            when (v) {
                is Number -> return v.toLong()
                is String -> {
                    val parsed = v.trim().toLongOrNull()
                    if (parsed != null) return parsed
                }
            }
        }
        return null
    }

    private fun safeGetString(doc: com.google.firebase.firestore.DocumentSnapshot, vararg fieldNames: String): String? {
        for (name in fieldNames) {
            val v = doc.get(name) ?: continue
            val str = v.toString().trim()
            if (str.isNotEmpty()) return str
        }
        return null
    }

    private fun safeGetDate(doc: com.google.firebase.firestore.DocumentSnapshot, vararg fieldNames: String): java.util.Date? {
        for (name in fieldNames) {
            val v = doc.get(name) ?: continue
            when (v) {
                is com.google.firebase.Timestamp -> return v.toDate()
                is java.util.Date -> return v
                is Number -> return java.util.Date(v.toLong())
                is String -> {
                    val s = v.trim()
                    val epoch = s.toLongOrNull()
                    if (epoch != null) return java.util.Date(epoch)
                    try {
                        val instant = java.time.Instant.parse(s)
                        return java.util.Date.from(instant)
                    } catch (e: Exception) {
                        try {
                            val localDate = java.time.LocalDate.parse(s.take(10))
                            return java.util.Date.from(localDate.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant())
                        } catch (e2: Exception) {
                            // ignore
                        }
                    }
                }
            }
        }
        return null
    }

    private fun extractBookingFromDoc(doc: com.google.firebase.firestore.DocumentSnapshot): Booking? {
        return try {
            val b = try {
                doc.toObject(Booking::class.java)
            } catch (e: Exception) {
                Log.w("BookingRepository", "toObject(Booking) failed for ${doc.id}, falling back to resilient extraction", e)
                null
            }

            val trackingIdVal = safeGetString(doc, "trackingId")?.takeIf { it.isNotBlank() }
                ?: (b?.trackingId?.takeIf { it.isNotBlank() } ?: doc.id)
            val farmerIdVal = safeGetString(doc, "farmerId") ?: (b?.farmerId ?: "")
            val farmerNameVal = safeGetString(doc, "farmerName") ?: (b?.farmerName ?: "")
            val centreIdVal = safeGetString(doc, "centreId") ?: (b?.centreId ?: "")
            val centreNameVal = safeGetString(doc, "centreName") ?: (b?.centreName ?: "")
            val bookingDateVal = safeGetString(doc, "bookingDate", "procurementDate", "date") ?: (b?.bookingDate ?: "")
            val slotIdVal = safeGetString(doc, "slotId") ?: (b?.slotId ?: "")
            val slotStartTimeVal = safeGetString(doc, "slotStartTime") ?: (b?.slotStartTime ?: "")
            val slotEndTimeVal = safeGetString(doc, "slotEndTime") ?: (b?.slotEndTime ?: "")
            val cropVal = safeGetString(doc, "crop") ?: (b?.crop ?: "")
            val qtyVal = safeGetDouble(doc, "quantity") ?: (b?.quantity ?: 0.0)
            val qtyUnitVal = safeGetString(doc, "quantityUnit", "unit") ?: (b?.quantityUnit ?: QuantityUnit.QUINTAL.name)
            val qtyKgVal = safeGetDouble(doc, "quantityKg") ?: (b?.quantityKg ?: 0.0)
            val statusVal = safeGetString(doc, "status") ?: (b?.status ?: BookingStatus.BOOKED.name)
            val qrCodeDataVal = safeGetString(doc, "qrCodeData")?.takeIf { it.isNotBlank() }
                ?: (b?.qrCodeData?.takeIf { it.isNotBlank() } ?: trackingIdVal)
            val createdAtVal = safeGetLong(doc, "createdAt") ?: (b?.createdAt ?: System.currentTimeMillis())
            val queueTokenVal = safeGetString(doc, "queueToken", "tokenLabel", "token") ?: (b?.queueToken ?: "")
            val checkedInAtVal = safeGetDate(doc, "checkedInAt", "checkInTime") ?: b?.checkedInAt
            val tokenNumVal = safeGetLong(doc, "tokenNumber") ?: (b?.tokenNumber ?: 0L)

            val finalCropVal = safeGetString(doc, "finalCrop")?.takeIf { it.isNotBlank() } ?: b?.finalCrop
            val finalUnitVal = safeGetString(doc, "finalUnit", "unit")?.takeIf { it.isNotBlank() } ?: b?.finalUnit
            val finalQtyVal = safeGetDouble(doc, "finalQuantity") ?: b?.finalQuantity
            val finalRateVal = safeGetDouble(doc, "finalRate", "rate") ?: b?.finalRate
            val deductionsVal = safeGetDouble(doc, "deductions") ?: b?.deductions
            val finalPayableVal = safeGetDouble(doc, "finalPayableAmount", "finalAmount", "payableAmount") ?: b?.finalPayableAmount
            val completedByVal = safeGetString(doc, "completedBy")?.takeIf { it.isNotBlank() } ?: b?.completedBy
            val completedAtVal = safeGetDate(doc, "completedAt") ?: b?.completedAt

            Booking(
                trackingId = trackingIdVal,
                farmerId = farmerIdVal,
                farmerName = farmerNameVal,
                centreId = centreIdVal,
                centreName = centreNameVal,
                bookingDate = bookingDateVal,
                slotId = slotIdVal,
                slotStartTime = slotStartTimeVal,
                slotEndTime = slotEndTimeVal,
                crop = cropVal,
                quantity = qtyVal,
                quantityUnit = qtyUnitVal,
                quantityKg = qtyKgVal,
                status = statusVal,
                qrCodeData = qrCodeDataVal,
                createdAt = createdAtVal,
                queueToken = queueTokenVal,
                checkedInAt = checkedInAtVal,
                tokenNumber = tokenNumVal,
                finalCrop = finalCropVal,
                finalQuantity = finalQtyVal,
                finalUnit = finalUnitVal,
                finalRate = finalRateVal,
                deductions = deductionsVal,
                finalPayableAmount = finalPayableVal,
                completedAt = completedAtVal,
                completedBy = completedByVal
            )
        } catch (e: Exception) {
            Log.e("BookingRepository", "Error deserializing booking ${doc.id}", e)
            null
        }
    }

    private fun extractQueueEntryFromDoc(doc: com.google.firebase.firestore.DocumentSnapshot): QueueEntry? {
        return try {
            val entry = try {
                doc.toObject(QueueEntry::class.java)
            } catch (e: Exception) {
                Log.w("BookingRepository", "toObject(QueueEntry) failed for ${doc.id}, falling back to resilient extraction", e)
                null
            }

            val idVal = safeGetString(doc, "id")?.takeIf { it.isNotBlank() }
                ?: (entry?.id?.takeIf { it.isNotBlank() } ?: doc.id)
            val bookingIdVal = safeGetString(doc, "bookingId")?.takeIf { it.isNotBlank() }
                ?: (entry?.bookingId?.takeIf { it.isNotBlank() } ?: idVal)
            val trackingIdVal = safeGetString(doc, "trackingId")?.takeIf { it.isNotBlank() }
                ?: (entry?.trackingId?.takeIf { it.isNotBlank() } ?: bookingIdVal)
            val farmerIdVal = safeGetString(doc, "farmerId") ?: (entry?.farmerId ?: "")
            val farmerNameVal = safeGetString(doc, "farmerName") ?: (entry?.farmerName ?: "")
            val centreIdVal = safeGetString(doc, "centreId") ?: (entry?.centreId ?: "")
            val tokenLabelVal = safeGetString(doc, "tokenLabel") ?: (entry?.tokenLabel ?: "")
            val queueTokenVal = safeGetString(doc, "queueToken") ?: (entry?.queueToken ?: tokenLabelVal)
            val tokenNumVal = safeGetLong(doc, "tokenNumber") ?: (entry?.tokenNumber ?: 0L)
            val statusVal = safeGetString(doc, "status") ?: (entry?.status ?: BookingStatus.BOOKED.name)
            val dateVal = safeGetString(doc, "queueDate", "procurementDate", "date", "bookingDate") ?: (entry?.date ?: "")
            val checkInTimeVal = safeGetDate(doc, "checkInTime", "checkedInAt") ?: entry?.checkInTime
            val queueJoinedAtVal = safeGetDate(doc, "calledTime", "queueJoinedAt") ?: entry?.queueJoinedAt
            val startedServingAtVal = safeGetDate(doc, "servingStartTime", "startedServingAt") ?: entry?.startedServingAt
            val processingStartedAtVal = safeGetDate(doc, "processingStartTime", "processingStartedAt") ?: entry?.processingStartedAt
            val completedAtVal = safeGetDate(doc, "completedAt") ?: entry?.completedAt
            val updatedAtVal = safeGetDate(doc, "updatedAt") ?: entry?.updatedAt
            val slotStartTimeVal = safeGetString(doc, "slotStartTime") ?: (entry?.slotStartTime ?: "")
            val slotEndTimeVal = safeGetString(doc, "slotEndTime") ?: (entry?.slotEndTime ?: "")

            val finalCropVal = safeGetString(doc, "finalCrop")?.takeIf { it.isNotBlank() } ?: entry?.finalCrop
            val finalUnitVal = safeGetString(doc, "finalUnit", "unit")?.takeIf { it.isNotBlank() } ?: entry?.finalUnit
            val finalQtyVal = safeGetDouble(doc, "finalQuantity") ?: entry?.finalQuantity
            val finalRateVal = safeGetDouble(doc, "finalRate", "rate") ?: entry?.finalRate
            val deductionsVal = safeGetDouble(doc, "deductions") ?: entry?.deductions
            val finalPayableVal = safeGetDouble(doc, "finalPayableAmount", "finalAmount", "payableAmount") ?: entry?.finalPayableAmount
            val completedByVal = safeGetString(doc, "completedBy")?.takeIf { it.isNotBlank() } ?: entry?.completedBy

            QueueEntry(
                id = idVal,
                bookingId = bookingIdVal,
                trackingId = trackingIdVal,
                farmerId = farmerIdVal,
                farmerName = farmerNameVal,
                centreId = centreIdVal,
                tokenLabel = tokenLabelVal,
                queueToken = queueTokenVal,
                tokenNumber = tokenNumVal,
                status = statusVal,
                queueDate = dateVal,
                procurementDate = dateVal,
                date = dateVal,
                bookingDate = dateVal,
                checkInTime = checkInTimeVal,
                checkedInAt = checkInTimeVal,
                queueJoinedAt = queueJoinedAtVal,
                startedServingAt = startedServingAtVal,
                processingStartedAt = processingStartedAtVal,
                completedAt = completedAtVal,
                updatedAt = updatedAtVal,
                slotStartTime = slotStartTimeVal,
                slotEndTime = slotEndTimeVal,
                finalCrop = finalCropVal,
                finalQuantity = finalQtyVal,
                finalUnit = finalUnitVal,
                finalRate = finalRateVal,
                deductions = deductionsVal,
                finalPayableAmount = finalPayableVal,
                completedBy = completedByVal
            )
        } catch (e: Exception) {
            Log.e("BookingRepository", "Error deserializing queue entry ${doc.id}", e)
            null
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
                            extractBookingFromDoc(doc)
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
                            val entry = extractQueueEntryFromDoc(doc)
                            if (entry != null) {
                                val resolved = entry
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
                        extractQueueEntryFromDoc(doc)
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
                        extractQueueEntryFromDoc(doc)
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
                    booking = extractBookingFromDoc(snapshot)
                    emitCombined()
                } else if (snapshot != null && !snapshot.exists()) {
                    trySend(Result.failure(Exception("Booking not found")))
                }
            }

            // Direct document listener on queueEntries/{cleanTrackingId}
            queueDocListener = queueEntriesCollection.document(cleanTrackingId).addSnapshotListener { snapshot, error ->
                if (error == null && snapshot != null && snapshot.exists()) {
                    directQueueEntry = extractQueueEntryFromDoc(snapshot)
                } else {
                    directQueueEntry = null
                }
                emitCombined()
            }

            // Query listener on queueEntries where trackingId == cleanTrackingId
            queueQueryListener = queueEntriesCollection.whereEqualTo("trackingId", cleanTrackingId).addSnapshotListener { snapshot, error ->
                if (error == null && snapshot != null && !snapshot.isEmpty) {
                    val doc = snapshot.documents.firstOrNull()
                    queryQueueEntry = doc?.let { extractQueueEntryFromDoc(it) }
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
