package com.example.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class LocationTracker(private val context: Context) {

    private val fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): Location? = suspendCancellableCoroutine { continuation ->
        val hasFineLocation = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val hasCoarseLocation = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasFineLocation && !hasCoarseLocation) {
            if (continuation.isActive) continuation.resume(null)
            return@suspendCancellableCoroutine
        }

        try {
            fusedLocationProviderClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener { location ->
                    if (continuation.isActive) {
                        if (location != null) {
                            continuation.resume(location)
                        } else {
                            // Try last location if current location is null
                            fusedLocationProviderClient.lastLocation
                                .addOnSuccessListener { lastLoc ->
                                    if (continuation.isActive) continuation.resume(lastLoc)
                                }
                                .addOnFailureListener {
                                    if (continuation.isActive) continuation.resume(null)
                                }
                        }
                    }
                }
                .addOnFailureListener {
                    // Try fallback to last location as well
                    fusedLocationProviderClient.lastLocation
                        .addOnSuccessListener { lastLoc ->
                            if (continuation.isActive) continuation.resume(lastLoc)
                        }
                        .addOnFailureListener {
                            if (continuation.isActive) continuation.resume(null)
                        }
                }
        } catch (e: Exception) {
            if (continuation.isActive) continuation.resume(null)
        }
    }
}
