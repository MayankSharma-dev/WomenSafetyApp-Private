package com.ms.womensafetyapp.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Looper
import com.ms.womensafetyapp.util.ServiceErrorType.ERROR_GPS_DISABLED
import com.ms.womensafetyapp.util.ServiceErrorType.ERROR_LOC_PERMISSION
import com.ms.womensafetyapp.util.ServiceErrorType.ERROR_NETWORK_DISABLED
import com.ms.womensafetyapp.util.getIsNetworkEnabled
import com.ms.womensafetyapp.util.hasLocationPermission
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch

class NetworkLocationClient(private val context: Context) : LocationClient {

    @SuppressLint("MissingPermission")
    override fun getLocationUpdates(timeInterval: Long): Flow<Location> {
        return callbackFlow {

            if (!context.hasLocationPermission()) {
                println(" >>>>>>>>>> Has Location permission Disabled")
                throw LocationClient.LocationException(ERROR_LOC_PERMISSION)
            }

            val locationManager =
                context.getSystemService(Context.LOCATION_SERVICE) as LocationManager


//            if (!locationManager.getIsNetworkEnabled()) {
//                println(" >>>>>>>>>> Network Disabled Network Client")
//                throw LocationClient.LocationException(ERROR_NETWORK_DISABLED)
//            }

            val locationListener = object : LocationListener {
                override fun onLocationChanged(location: Location) {
                    launch {
                        send(location)
                    }
                }

                override fun onProviderDisabled(provider: String) {
                    super.onProviderDisabled(provider)
                    println(">>>>>>>>>>>>> Provider disabled Network Client.")
                    throw LocationClient.LocationException(ERROR_GPS_DISABLED)
                }
            }

            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                timeInterval, 0f, locationListener, Looper.getMainLooper()
            )


            awaitClose {
                locationManager.removeUpdates(locationListener)
            }
        }
    }
}