package com.kisansethu.app.data

import java.security.MessageDigest

object PinHasher {
    private const val SALT = "KisanSethuFarmerPinSalt2025"

    fun hashPin(pin: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val combined = "$SALT:$pin"
        val hashBytes = digest.digest(combined.toByteArray(Charsets.UTF_8))
        return hashBytes.joinToString("") { "%02x".format(it) }
    }
}
