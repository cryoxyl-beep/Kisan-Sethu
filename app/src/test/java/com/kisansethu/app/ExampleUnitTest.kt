package com.kisansethu.app

import com.kisansethu.app.data.Booking
import com.kisansethu.app.data.BookingStatus
import com.kisansethu.app.data.QueueEntry
import com.kisansethu.app.data.extractNumericToken
import com.kisansethu.app.data.formatCurrencyAmount
import com.kisansethu.app.data.formatQuantityDisplay
import com.kisansethu.app.data.formatRateDisplay
import com.kisansethu.app.data.formatTokenDisplay
import com.kisansethu.app.data.getAuthoritativeStatus
import com.kisansethu.app.data.getDisplayStatus
import com.kisansethu.app.data.isCompletedStatus
import com.kisansethu.app.data.isUpcomingStatus
import com.kisansethu.app.data.mergeBookingWithQueueEntry
import com.kisansethu.app.data.normalizeStatus
import com.kisansethu.app.data.sortQueueEntries
import org.junit.Test
import org.junit.Assert.*
import java.time.LocalDate
import java.util.Date
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest

class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun testNormalizeStatusCasingAndDelimiters() {
        assertEquals("BOOKED", normalizeStatus("booked"))
        assertEquals("BOOKED", normalizeStatus("BOOKED"))
        assertEquals("BOOKED", normalizeStatus(" Booked "))

        assertEquals("CONFIRMED", normalizeStatus("confirmed"))
        assertEquals("CONFIRMED", normalizeStatus("CONFIRMED"))

        assertEquals("CHECKED_IN", normalizeStatus("checked_in"))
        assertEquals("CHECKED_IN", normalizeStatus("CHECKED_IN"))
        assertEquals("CHECKED_IN", normalizeStatus("CHECKED IN"))
        assertEquals("CHECKED_IN", normalizeStatus("checked in"))
        assertEquals("CHECKED_IN", normalizeStatus("checked-in"))
        assertEquals("CHECKED_IN", normalizeStatus("checkedin"))

        assertEquals("WAITING", normalizeStatus("waiting"))
        assertEquals("WAITING", normalizeStatus("WAITING"))
        assertEquals("WAITING", normalizeStatus("WAITING_IN_QUEUE"))
        assertEquals("WAITING", normalizeStatus("waiting in queue"))
        assertEquals("WAITING", normalizeStatus("IN_QUEUE"))

        assertEquals("NOW_SERVING", normalizeStatus("now_serving"))
        assertEquals("NOW_SERVING", normalizeStatus("NOW_SERVING"))
        assertEquals("NOW_SERVING", normalizeStatus("now serving"))
        assertEquals("NOW_SERVING", normalizeStatus("NOW SERVING"))
        assertEquals("NOW_SERVING", normalizeStatus("serving"))

        assertEquals("PROCESSING", normalizeStatus("processing"))
        assertEquals("PROCESSING", normalizeStatus("PROCESSING"))
        assertEquals("PROCESSING", normalizeStatus("in_progress"))
        assertEquals("PROCESSING", normalizeStatus("IN PROGRESS"))

        assertEquals("COMPLETED", normalizeStatus("completed"))
        assertEquals("COMPLETED", normalizeStatus("COMPLETED"))
        assertEquals("COMPLETED", normalizeStatus("Completed"))
        assertEquals("COMPLETED", normalizeStatus("complete"))
        assertEquals("COMPLETED", normalizeStatus("DONE"))

        assertEquals("CANCELLED", normalizeStatus("cancelled"))
        assertEquals("CANCELLED", normalizeStatus("canceled"))
        assertEquals("CANCELLED", normalizeStatus("CANCELLED"))

        assertEquals("MISSED", normalizeStatus("missed"))

        assertEquals("BOOKED", normalizeStatus(null))
        assertEquals("BOOKED", normalizeStatus(""))
        assertEquals("BOOKED", normalizeStatus("   "))
    }

    @Test
    fun testAuthoritativeStatusLifecycleProgression() {
        // Step 1: CONFIRMED
        assertEquals("CONFIRMED", getAuthoritativeStatus("CONFIRMED", null))

        // Step 2: CHECKED_IN
        assertEquals("CHECKED_IN", getAuthoritativeStatus("CONFIRMED", "CHECKED_IN"))
        assertEquals("CHECKED_IN", getAuthoritativeStatus("CHECKED_IN", null))

        // Step 3: WAITING
        assertEquals("WAITING", getAuthoritativeStatus("CHECKED_IN", "WAITING"))

        // Step 4: NOW_SERVING
        assertEquals("NOW_SERVING", getAuthoritativeStatus("CHECKED_IN", "NOW_SERVING"))
        assertEquals("NOW_SERVING", getAuthoritativeStatus("WAITING", "NOW_SERVING"))

        // Step 5: PROCESSING
        assertEquals("PROCESSING", getAuthoritativeStatus("CHECKED_IN", "PROCESSING"))
        assertEquals("PROCESSING", getAuthoritativeStatus("NOW_SERVING", "PROCESSING"))

        // Step 6: COMPLETED (the live bug scenario: booking is CHECKED_IN, queue is COMPLETED)
        assertEquals("COMPLETED", getAuthoritativeStatus("CHECKED_IN", "COMPLETED"))
        assertEquals("COMPLETED", getAuthoritativeStatus("PROCESSING", "COMPLETED"))
        assertEquals("COMPLETED", getAuthoritativeStatus("COMPLETED", "PROCESSING")) // completed booking takes precedence over stale queue
    }

    @Test
    fun testTerminalStatusesPreserved() {
        assertEquals("CANCELLED", getAuthoritativeStatus("CANCELLED", "PROCESSING"))
        assertEquals("MISSED", getAuthoritativeStatus("MISSED", "WAITING"))
        // Completed booking is terminal and cannot be reverted by active or stale queue entry
        assertEquals("COMPLETED", getAuthoritativeStatus("COMPLETED", "PROCESSING"))
        assertEquals("COMPLETED", getAuthoritativeStatus("COMPLETED", "WAITING"))
        assertEquals("COMPLETED", getAuthoritativeStatus("COMPLETED", "CANCELLED"))
    }

    @Test
    fun testNormalizeStatusScheduledAndQueueSynonyms() {
        // SCHEDULED represents a BOOKED procurement not occurring today
        assertEquals("BOOKED", normalizeStatus("SCHEDULED"))
        assertEquals("BOOKED", normalizeStatus("scheduled"))
        assertEquals("BOOKED", normalizeStatus("SCHEDULED_BOOKING"))
        assertTrue(isUpcomingStatus("SCHEDULED"))
        assertFalse(isCompletedStatus("SCHEDULED"))

        assertEquals("WAITING", normalizeStatus("QUEUED"))
        assertEquals("WAITING", normalizeStatus("queued"))

        assertEquals("COMPLETED", normalizeStatus("FINISHED"))
        assertEquals("COMPLETED", normalizeStatus("finished"))
        assertEquals("COMPLETED", normalizeStatus("PROCURED"))
        assertEquals("COMPLETED", normalizeStatus("procured"))

        assertEquals("CANCELLED", normalizeStatus("REJECTED"))
        assertEquals("CANCELLED", normalizeStatus("rejected"))
    }

    @Test
    fun testUpcomingVsCompletedClassification() {
        val upcomingStatuses = listOf(
            BookingStatus.BOOKED.name,
            BookingStatus.CONFIRMED.name,
            BookingStatus.CHECKED_IN.name,
            BookingStatus.WAITING.name,
            BookingStatus.NOW_SERVING.name,
            BookingStatus.PROCESSING.name,
            "checked in",
            "now serving",
            "in progress"
        )

        for (status in upcomingStatuses) {
            assertTrue("Expected $status to be upcoming", isUpcomingStatus(status))
            assertFalse("Expected $status not to be completed", isCompletedStatus(status))
        }

        val completedStatuses = listOf(
            BookingStatus.COMPLETED.name,
            "completed",
            "Completed",
            "done",
            BookingStatus.CANCELLED.name,
            BookingStatus.MISSED.name
        )

        for (status in completedStatuses) {
            assertTrue("Expected $status to be completed", isCompletedStatus(status))
            assertFalse("Expected $status not to be upcoming", isUpcomingStatus(status))
        }
    }

    @Test
    fun testScheduledBookingLogic() {
        val today = LocalDate.now().toString()
        val futureDate = LocalDate.now().plusDays(3).toString()
        val pastDate = LocalDate.now().minusDays(1).toString()

        // BOOKED today -> "BOOKED"
        val bookedToday = Booking(status = "BOOKED", bookingDate = today)
        assertEquals("BOOKED", getDisplayStatus(bookedToday))

        // BOOKED not today -> "SCHEDULED"
        val bookedFuture = Booking(status = "BOOKED", bookingDate = futureDate)
        assertEquals("SCHEDULED", getDisplayStatus(bookedFuture))

        val bookedPast = Booking(status = "BOOKED", bookingDate = pastDate)
        assertEquals("SCHEDULED", getDisplayStatus(bookedPast))

        // Status takes priority over date: CONFIRMED/CHECKED_IN/PROCESSING in future should NOT be "SCHEDULED"
        val confirmedFuture = Booking(status = "CONFIRMED", bookingDate = futureDate)
        assertEquals("CONFIRMED", getDisplayStatus(confirmedFuture))

        val checkedInFuture = Booking(status = "CHECKED_IN", bookingDate = futureDate)
        assertEquals("CHECKED IN", getDisplayStatus(checkedInFuture))

        val waitingFuture = Booking(status = "WAITING", bookingDate = futureDate)
        assertEquals("WAITING", getDisplayStatus(waitingFuture))

        val nowServingFuture = Booking(status = "NOW_SERVING", bookingDate = futureDate)
        assertEquals("NOW SERVING", getDisplayStatus(nowServingFuture))

        val processingFuture = Booking(status = "PROCESSING", bookingDate = futureDate)
        assertEquals("PROCESSING", getDisplayStatus(processingFuture))

        val completedFuture = Booking(status = "COMPLETED", bookingDate = futureDate)
        assertEquals("COMPLETED", getDisplayStatus(completedFuture))
    }

    @Test
    fun testMergeBookingWithQueueEntry() {
        val checkInDate = Date(1788944590000L)
        val booking = Booking(
            trackingId = "KS26LV7WI9",
            farmerId = "ZVMXMZ",
            farmerName = "Sai Santosh",
            centreId = "C1",
            centreName = "Medchal Procurement Centre",
            bookingDate = "2026-09-09",
            crop = "Maize",
            quantity = 555.0,
            status = "CHECKED_IN"
        )

        val queueEntry = QueueEntry(
            id = "KS26LV7WI9",
            bookingId = "KS26LV7WI9",
            trackingId = "KS26LV7WI9",
            farmerId = "ZVMXMZ",
            tokenLabel = "MP-002",
            tokenNumber = 2L,
            status = "COMPLETED",
            checkInTime = checkInDate
        )

        val merged = mergeBookingWithQueueEntry(booking, queueEntry)

        assertEquals("COMPLETED", merged.status)
        assertEquals("MP-002", merged.queueToken)
        assertEquals(checkInDate, merged.checkedInAt)
        assertEquals("KS26LV7WI9", merged.trackingId)
        assertEquals("Medchal Procurement Centre", merged.centreName)
        assertEquals(555.0, merged.quantity, 0.001)

        // Verify it categorizes into Completed section
        assertTrue(isCompletedStatus(merged.status))
        assertFalse(isUpcomingStatus(merged.status))
    }

    @Test
    fun testFullLifecycleRequirement15Sequence() {
        // Step 1: Farmer has CONFIRMED booking
        var booking = Booking(
            trackingId = "KS26TEST01",
            farmerId = "FARMER1",
            status = "CONFIRMED"
        )
        assertEquals("CONFIRMED", getAuthoritativeStatus(booking.status, null))
        assertTrue(isUpcomingStatus(booking.status))
        assertFalse(isCompletedStatus(booking.status))

        // Step 2: Admin changes it to CHECKED_IN
        var queue = QueueEntry(bookingId = "KS26TEST01", trackingId = "KS26TEST01", status = "CHECKED_IN", tokenLabel = "TK-101")
        booking = mergeBookingWithQueueEntry(booking, queue)
        assertEquals("CHECKED_IN", booking.status)
        assertEquals("CHECKED IN", getDisplayStatus(booking))
        assertTrue(isUpcomingStatus(booking.status))
        assertFalse(isCompletedStatus(booking.status))

        // Step 3: Admin moves it to WAITING
        queue = queue.copy(status = "WAITING")
        booking = mergeBookingWithQueueEntry(booking, queue)
        assertEquals("WAITING", booking.status)
        assertEquals("WAITING", getDisplayStatus(booking))
        assertEquals("TK-101", booking.queueToken)
        assertTrue(isUpcomingStatus(booking.status))
        assertFalse(isCompletedStatus(booking.status))

        // Step 4: Admin moves it to NOW_SERVING
        queue = queue.copy(status = "NOW_SERVING")
        booking = mergeBookingWithQueueEntry(booking, queue)
        assertEquals("NOW_SERVING", booking.status)
        assertEquals("NOW SERVING", getDisplayStatus(booking))
        assertTrue(isUpcomingStatus(booking.status))
        assertFalse(isCompletedStatus(booking.status))

        // Step 5: Admin moves it to PROCESSING
        queue = queue.copy(status = "PROCESSING")
        booking = mergeBookingWithQueueEntry(booking, queue)
        assertEquals("PROCESSING", booking.status)
        assertEquals("PROCESSING", getDisplayStatus(booking))
        assertTrue(isUpcomingStatus(booking.status))
        assertFalse(isCompletedStatus(booking.status))

        // Step 6: Admin completes the procurement
        queue = queue.copy(status = "COMPLETED")
        booking = mergeBookingWithQueueEntry(booking, queue)
        assertEquals("COMPLETED", booking.status)
        assertEquals("COMPLETED", getDisplayStatus(booking))
        assertFalse(isUpcomingStatus(booking.status))
        assertTrue(isCompletedStatus(booking.status))
    }

    @Test
    fun testTokenDisplayFormatting() {
        assertEquals("#014", formatTokenDisplay("14"))
        assertEquals("#011", formatTokenDisplay("11"))
        assertEquals("#014", formatTokenDisplay("014"))
        assertEquals("#014", formatTokenDisplay("#014"))
        assertEquals("#MP-002", formatTokenDisplay("MP-002"))
        assertEquals("#TK-101", formatTokenDisplay("TK-101"))
        assertEquals("#KA-001", formatTokenDisplay("KA-001", 1L))
        assertEquals("#MP-002", formatTokenDisplay("MP-002", 2L))
        assertEquals("#TK-101", formatTokenDisplay("TK-101", 101L))
        assertEquals("#014", formatTokenDisplay(null, 14L))
        assertEquals("#014", formatTokenDisplay("", 14L))
        assertEquals("-", formatTokenDisplay(null, 0L))
        assertEquals("-", formatTokenDisplay("", 0L))
    }

    @Test
    fun testPhaseA54AcceptanceQueueProgression() {
        // Initial setup matching task specification:
        // Farmer A: #011 NOW_SERVING
        // Farmer B: #012 WAITING
        // Farmer C: #013 WAITING
        // Current farmer: #014 WAITING
        val farmerA = QueueEntry(
            id = "B011",
            trackingId = "KS26A011",
            farmerId = "FARMER_A",
            centreId = "CENTRE_XYZ",
            queueDate = "2026-09-09",
            tokenNumber = 11L,
            queueToken = "11",
            status = "NOW_SERVING"
        )
        val farmerB = QueueEntry(
            id = "B012",
            trackingId = "KS26B012",
            farmerId = "FARMER_B",
            centreId = "CENTRE_XYZ",
            queueDate = "2026-09-09",
            tokenNumber = 12L,
            queueToken = "12",
            status = "WAITING"
        )
        val farmerC = QueueEntry(
            id = "B013",
            trackingId = "KS26C013",
            farmerId = "FARMER_C",
            centreId = "CENTRE_XYZ",
            queueDate = "2026-09-09",
            tokenNumber = 13L,
            queueToken = "13",
            status = "WAITING"
        )
        var currentFarmerQueue = QueueEntry(
            id = "B014",
            trackingId = "KS26D014",
            farmerId = "CURRENT_FARMER",
            centreId = "CENTRE_XYZ",
            queueDate = "2026-09-09",
            tokenNumber = 14L,
            queueToken = "14",
            status = "WAITING"
        )

        var currentFarmerBooking = Booking(
            trackingId = "KS26D014",
            farmerId = "CURRENT_FARMER",
            centreId = "CENTRE_XYZ",
            centreName = "XYZ Procurement Centre",
            bookingDate = "2026-09-09",
            slotStartTime = "10:00 AM",
            slotEndTime = "11:00 AM",
            status = "CHECKED_IN",
            queueToken = "14",
            tokenNumber = 14L
        )

        // Function helper mimicking ViewModel queue computation
        fun computeQueueState(
            activeQueue: List<QueueEntry>,
            farmerBooking: Booking
        ): Triple<String, Int, Int> {
            val sorted = sortQueueEntries(activeQueue)
            val servingEntry = sorted.firstOrNull { normalizeStatus(it.status) == BookingStatus.NOW_SERVING.name }
            val servingToken = if (servingEntry != null) {
                formatTokenDisplay(servingEntry.getEffectiveToken(), servingEntry.tokenNumber)
            } else {
                "No farmer currently being served"
            }

            val normStatus = normalizeStatus(farmerBooking.status)
            val (pos, ahead) = when (normStatus) {
                BookingStatus.COMPLETED.name -> 0 to 0
                BookingStatus.NOW_SERVING.name, BookingStatus.PROCESSING.name -> 1 to 0
                else -> {
                    val idx = sorted.indexOfFirst { it.trackingId == farmerBooking.trackingId }
                    if (idx >= 0) (idx + 1) to (if (idx > 0) idx else 0) else 0 to 0
                }
            }
            return Triple(servingToken, pos, ahead)
        }

        // 1. Initial State
        var activeQueue = listOf(farmerA, farmerB, farmerC, currentFarmerQueue)
        var (nowServing, pos, ahead) = computeQueueState(activeQueue, currentFarmerBooking)

        assertEquals("#014", formatTokenDisplay(currentFarmerBooking.queueToken, currentFarmerBooking.tokenNumber))
        assertEquals("WAITING", normalizeStatus(currentFarmerQueue.status))
        assertEquals("#011", nowServing)
        assertEquals(4, pos)
        assertEquals(3, ahead)

        // 2. Admin advances Farmer B (Farmer B removed / processed from active queue)
        activeQueue = listOf(farmerA, farmerC, currentFarmerQueue)
        val step2 = computeQueueState(activeQueue, currentFarmerBooking)
        assertEquals("#011", step2.first)
        assertEquals(3, step2.second)
        assertEquals(2, step2.third)

        // 3. Admin advances Farmer C (Farmer C removed / processed from active queue)
        activeQueue = listOf(farmerA, currentFarmerQueue)
        val step3 = computeQueueState(activeQueue, currentFarmerBooking)
        assertEquals("#011", step3.first)
        assertEquals(2, step3.second)
        assertEquals(1, step3.third)

        // 4. Admin makes current farmer NOW_SERVING
        currentFarmerQueue = currentFarmerQueue.copy(status = "NOW_SERVING")
        currentFarmerBooking = mergeBookingWithQueueEntry(currentFarmerBooking, currentFarmerQueue)
        activeQueue = listOf(currentFarmerQueue)
        val step4 = computeQueueState(activeQueue, currentFarmerBooking)
        assertEquals("NOW_SERVING", normalizeStatus(currentFarmerBooking.status))
        assertEquals("#014", step4.first)
        assertEquals(1, step4.second)
        assertEquals(0, step4.third)

        // 5. Admin changes current farmer to PROCESSING
        currentFarmerQueue = currentFarmerQueue.copy(status = "PROCESSING")
        currentFarmerBooking = mergeBookingWithQueueEntry(currentFarmerBooking, currentFarmerQueue)
        val step5 = computeQueueState(activeQueue, currentFarmerBooking)
        assertEquals("PROCESSING", normalizeStatus(currentFarmerBooking.status))
        assertEquals(1, step5.second)
        assertEquals(0, step5.third)

        // 6. Admin marks current farmer COMPLETED
        currentFarmerQueue = currentFarmerQueue.copy(status = "COMPLETED")
        currentFarmerBooking = mergeBookingWithQueueEntry(currentFarmerBooking, currentFarmerQueue)
        // Completed bookings leave active queue
        activeQueue = emptyList()
        val step6 = computeQueueState(activeQueue, currentFarmerBooking)
        assertEquals("COMPLETED", normalizeStatus(currentFarmerBooking.status))
        assertEquals("No farmer currently being served", step6.first)
        assertEquals(0, step6.second)
        assertEquals(0, step6.third)
        assertTrue(isCompletedStatus(currentFarmerBooking.status))
        assertFalse(isUpcomingStatus(currentFarmerBooking.status))
    }

    @Test
    fun testCentreAndDateIsolation() {
        val today = "2026-09-09"
        val tomorrow = "2026-09-10"

        val entryCentreA = QueueEntry(
            id = "E1", centreId = "CENTRE_A", queueDate = today, tokenNumber = 1L, status = "WAITING"
        )
        val entryCentreB = QueueEntry(
            id = "E2", centreId = "CENTRE_B", queueDate = today, tokenNumber = 1L, status = "WAITING"
        )
        val entryTomorrow = QueueEntry(
            id = "E3", centreId = "CENTRE_A", queueDate = tomorrow, tokenNumber = 2L, status = "WAITING"
        )

        val allEntries = listOf(entryCentreA, entryCentreB, entryTomorrow)

        // Filter for CENTRE_A and today
        val filtered = allEntries.filter { entry ->
            entry.centreId == "CENTRE_A" && entry.getEffectiveDate() == today
        }

        assertEquals(1, filtered.size)
        assertEquals("E1", filtered[0].id)
        assertFalse(filtered.any { it.centreId == "CENTRE_B" })
        assertFalse(filtered.any { it.getEffectiveDate() == tomorrow })
    }

    @Test
    fun testViewLiveDetailsButtonVisibilityRules() {
        fun shouldShowLiveDetailsButton(status: String): Boolean {
            val norm = normalizeStatus(status)
            return norm in listOf(
                BookingStatus.CHECKED_IN.name,
                BookingStatus.WAITING.name,
                BookingStatus.NOW_SERVING.name,
                BookingStatus.PROCESSING.name,
                BookingStatus.COMPLETED.name // Accessible from completed history
            )
        }

        assertFalse("BOOKED should not show Live Details", shouldShowLiveDetailsButton("BOOKED"))
        assertFalse("CONFIRMED should not show Live Details", shouldShowLiveDetailsButton("CONFIRMED"))
        assertTrue("CHECKED_IN should show Live Details", shouldShowLiveDetailsButton("CHECKED_IN"))
        assertTrue("WAITING should show Live Details", shouldShowLiveDetailsButton("WAITING"))
        assertTrue("NOW_SERVING should show Live Details", shouldShowLiveDetailsButton("NOW_SERVING"))
        assertTrue("PROCESSING should show Live Details", shouldShowLiveDetailsButton("PROCESSING"))
        assertTrue("COMPLETED should be accessible from history", shouldShowLiveDetailsButton("COMPLETED"))
    }

    @Test
    fun testQueueEntryDateFieldFallbacks() {
        val entryWithQueueDate = QueueEntry(queueDate = "2026-09-09")
        assertEquals("2026-09-09", entryWithQueueDate.getEffectiveDate())

        val entryWithProcurementDate = QueueEntry(procurementDate = "2026-09-09")
        assertEquals("2026-09-09", entryWithProcurementDate.getEffectiveDate())

        val entryWithDate = QueueEntry(date = "2026-09-09")
        assertEquals("2026-09-09", entryWithDate.getEffectiveDate())

        val entryWithBookingDate = QueueEntry(bookingDate = "2026-09-09")
        assertEquals("2026-09-09", entryWithBookingDate.getEffectiveDate())
    }

    @Test
    fun testSortQueueEntriesDeterministicTieBreaking() {
        val nowServing = QueueEntry(id = "E1", tokenNumber = 10L, status = "NOW_SERVING")
        val processing = QueueEntry(id = "E2", tokenNumber = 9L, status = "PROCESSING")
        val waiting1 = QueueEntry(id = "E3", tokenNumber = 11L, status = "WAITING")
        val waiting2 = QueueEntry(id = "E4", tokenNumber = 12L, status = "WAITING")

        val sorted = sortQueueEntries(listOf(waiting2, waiting1, processing, nowServing))
        assertEquals("E1", sorted[0].id) // NOW_SERVING
        assertEquals("E2", sorted[1].id) // PROCESSING
        assertEquals("E3", sorted[2].id) // WAITING token 11
        assertEquals("E4", sorted[3].id) // WAITING token 12
    }

    @Test
    fun testActiveServingFallbackToCounter() {
        // When no entry is NOW_SERVING, should show "No farmer currently being served"
        val waitingOnly = listOf(QueueEntry(id = "E1", tokenNumber = 1L, status = "WAITING"))
        val sorted = sortQueueEntries(waitingOnly)
        val servingEntry = sorted.firstOrNull { normalizeStatus(it.status) == BookingStatus.NOW_SERVING.name }
        assertNull(servingEntry)

        val counterWithToken = com.kisansethu.app.data.QueueCounter(activeServingToken = "15")
        val displayWithCounter = formatTokenDisplay(counterWithToken.activeServingToken)
        assertEquals("#015", displayWithCounter)
    }

    @Test
    fun testPhaseA61CompletedProcurementDisplayFields() {
        // Acceptance criteria: The completed item must display:
        // - Crop
        // - Final quantity
        // - Unit
        // - Rate
        // - Final payable amount
        // - Procurement centre
        // - Procurement date
        // - Tracking/Booking ID
        // - Procurement status
        //
        // Example:
        // Rice, 245 kg, Rate: ₹28 / kg, Final Amount: ₹6,860, Status: Completed
        val completedBooking = Booking(
            trackingId = "KS26RICE01",
            farmerId = "FARMER_123",
            farmerName = "Ramesh Kumar",
            centreId = "CENTRE_01",
            centreName = "Medchal Procurement Centre",
            bookingDate = "2026-09-09",
            crop = "Paddy",
            quantity = 250.0,
            quantityUnit = "kg",
            status = "COMPLETED",
            finalCrop = "Rice",
            finalQuantity = 245.0,
            finalUnit = "kg",
            finalRate = 28.0,
            deductions = 0.0,
            finalPayableAmount = 6860.0,
            completedBy = "Admin Officer"
        )

        // 1. Crop
        assertEquals("Rice", completedBooking.getEffectiveCrop())
        // 2. Final quantity
        assertEquals(245.0, completedBooking.getEffectiveQuantity(), 0.001)
        // 3. Unit
        assertEquals("kg", completedBooking.getEffectiveUnit())
        // Quantity with unit formatted
        assertEquals("245 kg", formatQuantityDisplay(completedBooking.getEffectiveQuantity(), completedBooking.getEffectiveUnit()))
        // 4. Rate
        assertEquals(28.0, completedBooking.getEffectiveRate()!!, 0.001)
        assertEquals("₹28 / kg", formatRateDisplay(completedBooking.finalRate, completedBooking.getEffectiveUnit()))
        // 5. Final payable amount
        assertEquals(6860.0, completedBooking.getEffectivePayableAmount()!!, 0.001)
        assertEquals("₹6,860", formatCurrencyAmount(completedBooking.finalPayableAmount))
        // 6. Procurement centre
        assertEquals("Medchal Procurement Centre", completedBooking.centreName)
        // 7. Procurement date
        assertEquals("2026-09-09", completedBooking.bookingDate)
        // 8. Tracking/Booking ID
        assertEquals("KS26RICE01", completedBooking.trackingId)
        // 9. Procurement status
        assertEquals("COMPLETED", normalizeStatus(completedBooking.status))
        assertEquals("COMPLETED", getDisplayStatus(completedBooking))

        // Ensure it is categorized as completed
        assertTrue(isCompletedStatus(completedBooking.status))
        assertFalse(isUpcomingStatus(completedBooking.status))
    }

    @Test
    fun testFormatCurrencyAndRateDisplay() {
        // Whole amounts with Indian numbering system
        assertEquals("₹6,860", formatCurrencyAmount(6860.0))
        assertEquals("₹1,25,000", formatCurrencyAmount(125000.0))
        assertEquals("₹28", formatCurrencyAmount(28.0))
        assertEquals("₹0", formatCurrencyAmount(0.0))
        assertEquals("-", formatCurrencyAmount(null))

        // Decimal amounts
        assertEquals("₹6,860.50", formatCurrencyAmount(6860.50))
        assertEquals("₹1,25,000.75", formatCurrencyAmount(125000.75))

        // Rates
        assertEquals("₹28 / kg", formatRateDisplay(28.0, "kg"))
        assertEquals("₹28.50 / kg", formatRateDisplay(28.5, "kg"))
        assertEquals("₹2,400 / quintal", formatRateDisplay(2400.0, "quintal"))
        assertEquals("₹2,400 / quintal", formatRateDisplay(2400.0, "QUINTAL"))
        assertEquals("-", formatRateDisplay(null, "kg"))

        // Quantities
        assertEquals("245 kg", formatQuantityDisplay(245.0, "kg"))
        assertEquals("245.5 kg", formatQuantityDisplay(245.5, "kg"))
        assertEquals("50 quintal", formatQuantityDisplay(50.0, "quintal"))
        assertEquals("-", formatQuantityDisplay(null, "kg"))
    }

    @Test
    fun testAdminDashboardSourceOfTruthNoRecalculation() {
        // Critical requirement:
        // "Read the finalized values written by the Admin Dashboard.
        // Do NOT calculate a different amount on the farmer device.
        // The Admin Dashboard is the source of truth."
        val bookingWithAdminMath = Booking(
            trackingId = "KS26SPECIAL",
            crop = "Wheat",
            finalCrop = "Wheat Grade A",
            finalQuantity = 100.0,
            finalRate = 30.0,
            deductions = 50.0,
            finalPayableAmount = 2950.0, // Admin-entered source of truth
            status = "COMPLETED"
        )

        // Farmer app must display Admin's exact amount, never recalculating locally
        assertEquals(2950.0, bookingWithAdminMath.finalPayableAmount!!, 0.001)
        assertEquals("₹2,950", formatCurrencyAmount(bookingWithAdminMath.finalPayableAmount))
    }

    @Test
    fun testRealtimeStatusFlowProcessingToCompleted() {
        // Acceptance test sequence:
        // 1. Farmer has a PROCESSING booking
        var booking = Booking(
            trackingId = "KS26LIVE01",
            farmerId = "FARMER_1",
            crop = "Rice",
            quantity = 250.0,
            quantityUnit = "kg",
            status = "PROCESSING"
        )
        // 2. Farmer sees PROCESSING
        assertEquals("PROCESSING", normalizeStatus(booking.status))
        assertTrue(isUpcomingStatus(booking.status))
        assertFalse(isCompletedStatus(booking.status))

        // 3-9. Admin finalizes procurement in Firestore:
        // Admin enters final crop ("Rice"), final quantity (245.0), unit ("kg"), rate (28.0),
        // calculates final payable amount (6860.0), marks procurement COMPLETED.
        val adminQueueUpdate = QueueEntry(
            id = "KS26LIVE01",
            bookingId = "KS26LIVE01",
            trackingId = "KS26LIVE01",
            farmerId = "FARMER_1",
            status = "COMPLETED",
            finalCrop = "Rice",
            finalQuantity = 245.0,
            finalUnit = "kg",
            finalRate = 28.0,
            finalPayableAmount = 6860.0,
            completedBy = "Admin Officer"
        )

        // 10. Farmer app detects COMPLETED automatically via merge
        booking = mergeBookingWithQueueEntry(booking, adminQueueUpdate)

        // 11. Processing UI disappears: isUpcomingStatus is false
        assertEquals("COMPLETED", normalizeStatus(booking.status))
        assertFalse(isUpcomingStatus(booking.status))

        // 12. Procurement appears in Completed section: isCompletedStatus is true
        assertTrue(isCompletedStatus(booking.status))

        // 13. Final quantity is correct
        assertEquals(245.0, booking.getEffectiveQuantity(), 0.001)
        assertEquals("245 kg", formatQuantityDisplay(booking.getEffectiveQuantity(), booking.getEffectiveUnit()))

        // 14. Rate is correct
        assertEquals(28.0, booking.finalRate!!, 0.001)
        assertEquals("₹28 / kg", formatRateDisplay(booking.finalRate, booking.getEffectiveUnit()))

        // 15. Final amount exactly matches Admin data
        assertEquals(6860.0, booking.finalPayableAmount!!, 0.001)
        assertEquals("₹6,860", formatCurrencyAmount(booking.finalPayableAmount))
    }

    @Test
    fun testFallbackWhenFinalCropOrUnitNull() {
        val bookingWithoutOverrides = Booking(
            trackingId = "KS26FALLBACK",
            crop = "Cotton",
            quantity = 150.0,
            quantityUnit = "kg",
            status = "COMPLETED",
            finalCrop = null,
            finalQuantity = null,
            finalUnit = null,
            finalRate = 60.0,
            finalPayableAmount = 9000.0
        )

        assertEquals("Cotton", bookingWithoutOverrides.getEffectiveCrop())
        assertEquals(150.0, bookingWithoutOverrides.getEffectiveQuantity(), 0.001)
        assertEquals("kg", bookingWithoutOverrides.getEffectiveUnit())
        assertEquals("150 kg", formatQuantityDisplay(bookingWithoutOverrides.getEffectiveQuantity(), bookingWithoutOverrides.getEffectiveUnit()))
    }

    @Test
    fun testRealtimeSnapshotListenerUpdatingUI() = runTest {
        // Simulating the Firestore snapshot listener pipeline that feeds the UI
        val snapshotFlow = MutableSharedFlow<Pair<Booking, QueueEntry?>>(replay = 1)

        val uiStateFlow = MutableStateFlow<Booking?>(null)
        val isUpcomingFlow = MutableStateFlow(false)
        val isCompletedFlow = MutableStateFlow(false)

        val job = launch {
            snapshotFlow.collect { (b, q) ->
                val merged = mergeBookingWithQueueEntry(b, q)
                uiStateFlow.value = merged
                isUpcomingFlow.value = isUpcomingStatus(merged.status)
                isCompletedFlow.value = isCompletedStatus(merged.status)
            }
        }
        testScheduler.runCurrent()

        // 1. Initial Firestore snapshot: Booking is in PROCESSING state
        val initialBooking = Booking(
            trackingId = "KS26SNAP01",
            farmerId = "F1",
            centreName = "Mandya APMC Centre",
            bookingDate = "2026-03-12",
            crop = "Rice",
            quantity = 250.0,
            quantityUnit = "kg",
            status = "PROCESSING"
        )
        snapshotFlow.emit(initialBooking to null)
        testScheduler.runCurrent()

        assertEquals("PROCESSING", uiStateFlow.value?.status)
        assertTrue(isUpcomingFlow.value)
        assertFalse(isCompletedFlow.value)

        // 2. Admin Dashboard finalizes procurement in Firestore:
        // Admin writes finalCrop="Rice", finalQuantity=245.0, finalUnit="kg", finalRate=28.0,
        // finalPayableAmount=6860.0, status="COMPLETED"
        val adminFinalizedQueue = QueueEntry(
            id = "KS26SNAP01",
            bookingId = "KS26SNAP01",
            trackingId = "KS26SNAP01",
            status = "COMPLETED",
            finalCrop = "Rice",
            finalQuantity = 245.0,
            finalUnit = "kg",
            finalRate = 28.0,
            finalPayableAmount = 6860.0,
            completedBy = "Admin APMC Officer"
        )
        // Real-time snapshot listener receives update immediately
        snapshotFlow.emit(initialBooking to adminFinalizedQueue)
        testScheduler.runCurrent()

        // 3. UI reflects update immediately without polling or manual refresh
        val updatedBooking = uiStateFlow.value!!
        assertEquals("COMPLETED", normalizeStatus(updatedBooking.status))

        // Active procurement / processing UI disappears
        assertFalse("Active UI must disappear when COMPLETED", isUpcomingFlow.value)

        // Completed section gains the booking
        assertTrue("Completed UI must appear when COMPLETED", isCompletedFlow.value)

        // Display fields are exact
        assertEquals("Rice", updatedBooking.getEffectiveCrop())
        assertEquals(245.0, updatedBooking.getEffectiveQuantity(), 0.001)
        assertEquals("kg", updatedBooking.getEffectiveUnit())
        assertEquals("245 kg", formatQuantityDisplay(updatedBooking.getEffectiveQuantity(), updatedBooking.getEffectiveUnit()))
        assertEquals(28.0, updatedBooking.getEffectiveRate()!!, 0.001)
        assertEquals("₹28 / kg", formatRateDisplay(updatedBooking.finalRate, updatedBooking.getEffectiveUnit()))
        assertEquals(6860.0, updatedBooking.getEffectivePayableAmount()!!, 0.001)
        assertEquals("₹6,860", formatCurrencyAmount(updatedBooking.finalPayableAmount))
        assertEquals("Admin APMC Officer", updatedBooking.completedBy)

        // Listener cleanup
        job.cancel()
    }
}