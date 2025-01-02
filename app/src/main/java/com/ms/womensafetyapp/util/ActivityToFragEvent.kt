package com.ms.womensafetyapp.util

import androidx.annotation.Keep

//@Keep
interface ActivityToFragEvent {
    fun eventReceivedServiceInfo(isServiceRunning: Boolean, serviceName: Int)
}