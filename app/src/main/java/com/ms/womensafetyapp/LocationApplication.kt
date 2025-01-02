package com.ms.womensafetyapp

import android.annotation.SuppressLint
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Looper
import androidx.annotation.Keep
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.ms.womensafetyapp.util.NotificationManagerHelper
import com.ms.womensafetyapp.util.hasLocationPermission
import com.ms.womensafetyapp.util.minutesToMillis
import dagger.hilt.android.HiltAndroidApp

//@Keep
@HiltAndroidApp
class LocationApplication: Application() {

    private val fusedLocationClient by lazy { LocationServices.getFusedLocationProviderClient(this) }
    private val locationCallback = object : LocationCallback() {}

    override fun onCreate() {
        super.onCreate()
        println(">>>>>>>>>>>>>>>>>>>>> LocationAPP")
        //preWarmLocationServices()

            val name = "Emergency Service"
            val descriptionText = "Shake To SOS running."
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(NotificationManagerHelper.CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system.
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)

        // for pre-warming the location updates even before starting the location service.
    }

    @SuppressLint("MissingPermission")
    private fun preWarmLocationServices() {
        if(this.hasLocationPermission()){
            //val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
            println(">>>>>>>>>> PreWarming Started.")

            val interval = 60 // 30 minutes

            val request = LocationRequest.Builder(10000).apply {
                setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                setIntervalMillis(interval.minutesToMillis())
                setMinUpdateIntervalMillis(interval.minutesToMillis())
            }.build()

//            val locationRequest = LocationRequest.create().apply {
//                priority = Priority.PRIORITY_HIGH_ACCURACY
//                interval = 10000 // 10 seconds
//                fastestInterval = 5000 // 5 seconds
//            }

            // Request location updates with a callback that does nothing
            // This is just to warm up the location provider
            fusedLocationClient.requestLocationUpdates(
                request,
                locationCallback,
                Looper.getMainLooper()
            )
        }
    }

    fun stopPreWarmingLocationServices() {
        println(">>>>>>>>> PreWarming Stopped.")
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }
}