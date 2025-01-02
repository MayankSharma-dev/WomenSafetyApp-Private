package com.ms.womensafetyapp.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Looper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import com.ms.womensafetyapp.util.ServiceErrorType.ERROR_GPS_DISABLED
import com.ms.womensafetyapp.util.ServiceErrorType.ERROR_INTERNET_DISABLED
import com.ms.womensafetyapp.util.ServiceErrorType.ERROR_LOC_PERMISSION
import com.ms.womensafetyapp.util.ServiceErrorType.ERROR_NETWORK_DISABLED
import com.ms.womensafetyapp.util.getIsGPSEnabled
import com.ms.womensafetyapp.util.getIsNetworkEnabled
import com.ms.womensafetyapp.util.hasLocationPermission
import com.ms.womensafetyapp.util.isInternetAvailable
import com.ms.womensafetyapp.util.toMinutes
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch

//@Keep
class DefaultLocationClient(
    private val context: Context,
    private val client: FusedLocationProviderClient
) : LocationClient {

    @SuppressLint("MissingPermission")
    override fun getLocationUpdates(timeInterval: Long): Flow<Location> {

        return callbackFlow {

            if (!context.hasLocationPermission()) {
                println(" >>>>>>>>>> Has Location permission Disabled")
                throw LocationClient.LocationException(ERROR_LOC_PERMISSION)
            }

            val locationManager =
                context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

            //if (timeInterval.toMinutes() >= 4)
            if (!locationManager.getIsGPSEnabled()) {
                println(" >>>>>>>>>> GPS Disabled")
                throw LocationClient.LocationException(ERROR_GPS_DISABLED)
            }

            if (!locationManager.getIsNetworkEnabled()) {
                println(" >>>>>>>>>> Network Disabled")
                throw LocationClient.LocationException(ERROR_NETWORK_DISABLED)
            }

            var priority: Int = Priority.PRIORITY_HIGH_ACCURACY

            if(timeInterval.toMinutes() >= 5){
                if(!context.isInternetAvailable())
                    throw LocationClient.LocationException(ERROR_INTERNET_DISABLED)
            }else{
                if(!context.isInternetAvailable())
                    priority = Priority.PRIORITY_BALANCED_POWER_ACCURACY
            }

            val request = LocationRequest.Builder(timeInterval).apply {
                setPriority(priority)
                setIntervalMillis(timeInterval)
                setMinUpdateIntervalMillis(timeInterval) // fastest updates
            }.build()

            val locationCallback = object : LocationCallback() {
                override fun onLocationResult(result: LocationResult) {
                    super.onLocationResult(result)
                    println(">>>>>>>>>> LocationCallback....")
                    result.locations.lastOrNull()?.let { location ->
                        launch { send(location) }
                    }
                }
            }

            client.requestLocationUpdates(
                request,
                locationCallback,
                Looper.getMainLooper()
            )

            awaitClose {
                client.removeLocationUpdates(locationCallback)
            }

        }
    }

}