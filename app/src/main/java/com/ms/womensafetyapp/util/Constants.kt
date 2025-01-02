package com.ms.womensafetyapp.util

import androidx.annotation.Keep

//@Keep
object ServiceEventTypes {
    const val ERROR_EVENT_WS = 0x30
    const val SERVICE_STATUS_EVENT_WS = 0x31
}

//@Keep
object ServiceErrorType {
    const val ERROR_LOC_PERMISSION = "Please enable Location Permission(Precise Location)."
    const val ERROR_NETWORK_DISABLED = "Please enable Network."
    const val ERROR_GPS_DISABLED = "Please enable GPS."
    const val ERROR_INTERNET_DISABLED = "Please enable internet."
}

//@Keep
object ServiceStatusTypes {
    const val LOCATION_SERVICE_RUNNING = 96
    const val LOCATION_SERVICE_NOT_RUNNING = 97
    const val EMERGENCY_SERVICE_NOT_RUNNING = 99
    const val EMERGENCY_SERVICE_RUNNING = 98
}

//@Keep
object ServiceTypes {
    const val EMERGENCY_SERVICE = 0x11
    const val LOCATIONS_SERVICE = 0x22
}

//@Keep
object IntentExtraCodes{
    const val ERROR_TYPES = "ERROR_TYPES"
    const val STATUS_TYPES = "STATUS_TYPES"
}