package com.ms.womensafetyapp.util

import android.Manifest
import android.app.ActivityManager
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.telephony.TelephonyManager
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.ms.womensafetyapp.database.ContactEntity


//@Keep
data class SmsWrap(
    val message: String,
    val contactList: List<ContactEntity>
)

//@Keep
data class LocationInfo(
    val latitude: Double,
    val longitude: Double,
    val time: String
)

//@Keep
data class ServiceAction(
    val shouldStart: Boolean,
    val actionType: String
)

//@Keep
fun Context.isLocationEnabled(): Boolean {
    val locationManager = this.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    val isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    val isLocationEnabled = isGpsEnabled || isNetworkEnabled
    return isLocationEnabled
}

//@Keep
fun Context.hasLocationPermission(): Boolean {
    return ContextCompat.checkSelfPermission(
        this, Manifest.permission.ACCESS_COARSE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
        this, Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
}

//@Keep
fun Context.isInternetAvailable(): Boolean{
    val connectivityManager = this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    if(connectivityManager == null)
        return true
    else{
        val capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        if(capabilities != null){
            if(capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR))
                return true
            else if(capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI))
                return true
            else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET))
                return true
        }
    }
     return false
}

//@Keep
fun Context.isPermissionsRequired(vararg permissions: String): Boolean {
    val permissionsToBeRequested = permissions.filter { permission ->
        ContextCompat.checkSelfPermission(
            this, permission
        ) != PackageManager.PERMISSION_GRANTED
    }

    return permissionsToBeRequested.isNotEmpty()
}

//@Keep
fun Context.getCurrentCountryCode(): String {
    val telephonyManager = this.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
    return telephonyManager.networkCountryIso ?: ""
}

//@Keep
fun LocationManager.getIsGPSEnabled(): Boolean {
    return this.isProviderEnabled(LocationManager.GPS_PROVIDER)
}

//@Keep
fun LocationManager.getIsNetworkEnabled(): Boolean {
    return this.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
}

//@Keep
fun Context.showToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}


// INT to LONG and vice-versa For Timing

//@Keep
fun Int.minutesToMillis(): Long = this * 60 * 1000L

//@Keep
fun Int.secondsToMillis(): Long = this * 1000L


fun Int.minutesAndSecondsToMillis(seconds: Int): Long {
    val minutesMillis = this.minutesToMillis()
    val secondsMillis = seconds.secondsToMillis()
    return minutesMillis + secondsMillis
}

//@Keep
fun Long.toMinutes(): Int = (this / (60 * 1000)).toInt()

//@Keep
fun Long.toSeconds(): Int = ((this % (60 * 1000)) / 1000).toInt()

//@Keep
fun Long.toMinutesAndSeconds(): Pair<Int, Int> {
    val minutes = this.toMinutes()
    val seconds = this.toSeconds()
    return Pair(minutes, seconds)
}

// \\


fun <T> Context.isServiceRunningInForeground(serviceClass: Class<T>): Boolean {
    val activityManager =this.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    for (service in activityManager.getRunningServices(Integer.MAX_VALUE)) {
        if (serviceClass.name == service.service.className) {
            return service.foreground
        }
    }
    return false
}

fun <T> Context.isMyServiceRunning(classObj: Class<T>): Boolean {
    val manager =
        this.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    for (service in manager.getRunningServices(Integer.MAX_VALUE)) {
        if (classObj.name.equals(service.service.className)) {
            return true
        }
    }
    return false
}

fun BottomNavigationView.hide() {
    this.visibility = View.GONE
}


fun BottomNavigationView.show() {
    this.visibility = View.VISIBLE
}



object ActionEvents {
    private var _actionType = ""

    private val _serviceActionType = MutableLiveData<String>()
    val serviceActionType: LiveData<String> = _serviceActionType

    fun setActionType(type: String) {
        _actionType = type
        println(">>>>>>>>> ActionType: $_actionType, $type")
    }

    fun setServiceActionType() {
        println(">>>>>>>>> ActionTypeSet: $_actionType")
        _serviceActionType.setValue(_actionType)
        println(">>>>>>>>>>> setServiceType: ${_serviceActionType.value}")
    }
}
