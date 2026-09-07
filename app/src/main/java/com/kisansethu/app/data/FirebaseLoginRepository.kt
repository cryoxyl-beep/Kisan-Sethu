package com.kisansethu.app.data

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

data class FarmerLoginResult(
    val farmerId: String,
    val fullName: String,
    val phoneNumber: String
)

class FirebaseLoginRepository {

    private val database: FirebaseDatabase by lazy {
        try {
            FirebaseDatabase.getInstance("https://kissaan-sync-default-rtdb.asia-southeast1.firebasedatabase.app")
        } catch (e: Exception) {
            try {
                FirebaseDatabase.getInstance("https://kissaan-sync-default-rtdb.firebaseio.com")
            } catch (e2: Exception) {
                FirebaseDatabase.getInstance()
            }
        }
    }

    suspend fun login(
        identifier: String,
        pin: String
    ): Result<FarmerLoginResult> {
        val trimmedIdentifier = identifier.trim()
        val trimmedPin = pin.trim()

        if (trimmedIdentifier.isEmpty()) {
            return Result.failure(Exception("Please enter your Mobile Number or Farmer ID."))
        }

        if (trimmedPin.isEmpty() || trimmedPin.length != 6) {
            return Result.failure(Exception("Please enter your 6-digit PIN."))
        }

        // Determine if identifier is Phone Number (10 digits) or Farmer ID (6 chars)
        val resolvedFarmerId: String = if (trimmedIdentifier.all { it.isDigit() } && trimmedIdentifier.length == 10) {
            val farmerIdFromPhone = lookupFarmerIdByPhone(trimmedIdentifier)
                ?: return Result.failure(Exception("No account found with this mobile number."))
            farmerIdFromPhone
        } else if (trimmedIdentifier.length == 6) {
            val farmerIdUpper = trimmedIdentifier.uppercase()
            val farmerExists = checkFarmerExists(farmerIdUpper)
            if (!farmerExists) {
                return Result.failure(Exception("No account found with Farmer ID $farmerIdUpper."))
            }
            farmerIdUpper
        } else {
            return Result.failure(Exception("Invalid input. Enter a 10-digit mobile number or a 6-character Farmer ID."))
        }

        // Verify PIN
        val storedPinHash = getPinHash(resolvedFarmerId)
            ?: return Result.failure(Exception("Authentication credentials not found."))

        val enteredPinHash = PinHasher.hashPin(trimmedPin)
        if (storedPinHash != enteredPinHash) {
            return Result.failure(Exception("Incorrect PIN. Please try again."))
        }

        // Fetch Farmer details
        val farmerProfile = getFarmerProfile(resolvedFarmerId)
            ?: return Result.failure(Exception("Unable to fetch farmer profile."))

        return Result.success(farmerProfile)
    }

    private suspend fun lookupFarmerIdByPhone(phoneNumber: String): String? =
        suspendCancellableCoroutine { continuation ->
            database.getReference("phoneNumbers").child(phoneNumber)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (continuation.isActive) {
                            continuation.resume(snapshot.getValue(String::class.java))
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        if (continuation.isActive) {
                            continuation.resume(null)
                        }
                    }
                })
        }

    private suspend fun checkFarmerExists(farmerId: String): Boolean =
        suspendCancellableCoroutine { continuation ->
            database.getReference("farmers").child(farmerId)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (continuation.isActive) {
                            continuation.resume(snapshot.exists())
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        if (continuation.isActive) {
                            continuation.resume(false)
                        }
                    }
                })
        }

    private suspend fun getPinHash(farmerId: String): String? =
        suspendCancellableCoroutine { continuation ->
            database.getReference("authCredentials").child(farmerId).child("pinHash")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (continuation.isActive) {
                            continuation.resume(snapshot.getValue(String::class.java))
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        if (continuation.isActive) {
                            continuation.resume(null)
                        }
                    }
                })
        }

    private suspend fun getFarmerProfile(farmerId: String): FarmerLoginResult? =
        suspendCancellableCoroutine { continuation ->
            database.getReference("farmers").child(farmerId)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (continuation.isActive) {
                            val fullName = snapshot.child("fullName").getValue(String::class.java) ?: "Farmer"
                            val phone = snapshot.child("phoneNumber").getValue(String::class.java) ?: ""
                            continuation.resume(
                                FarmerLoginResult(
                                    farmerId = farmerId,
                                    fullName = fullName,
                                    phoneNumber = phone
                                )
                            )
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        if (continuation.isActive) {
                            continuation.resume(null)
                        }
                    }
                })
        }
}
