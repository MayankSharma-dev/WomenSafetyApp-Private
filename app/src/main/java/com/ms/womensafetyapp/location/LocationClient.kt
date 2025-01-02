package com.ms.womensafetyapp.location

import android.location.Location
import androidx.annotation.Keep
import kotlinx.coroutines.flow.Flow

//@Keep
interface LocationClient {
    fun getLocationUpdates(timeInterval: Long): Flow<Location>
    class LocationException(message: String): Exception(message)
}