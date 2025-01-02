package com.ms.womensafetyapp.service

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.TaskStackBuilder
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.ms.womensafetyapp.MainActivity
import com.ms.womensafetyapp.MainActivity.Companion.EVENTS_TYPES_WS
import com.ms.womensafetyapp.MainActivity.Companion.EVENTS_WS
import com.ms.womensafetyapp.R
import com.ms.womensafetyapp.database.ContactEntity
import com.ms.womensafetyapp.location.DefaultLocationClient
import com.ms.womensafetyapp.location.NetworkLocationClient
import com.ms.womensafetyapp.preference.UserPreferencesStore
import com.ms.womensafetyapp.preference.UserPreferencesStore.Companion.DEFAULT_DURATION
import com.ms.womensafetyapp.preference.UserPreferencesStore.Companion.DEFAULT_SHOULD_SEND_SMS
import com.ms.womensafetyapp.util.IntentExtraCodes.ERROR_TYPES
import com.ms.womensafetyapp.util.IntentExtraCodes.STATUS_TYPES
import com.ms.womensafetyapp.util.NotificationManagerHelper
import com.ms.womensafetyapp.util.Repository
import com.ms.womensafetyapp.util.ServiceErrorType.ERROR_GPS_DISABLED
import com.ms.womensafetyapp.util.ServiceErrorType.ERROR_NETWORK_DISABLED
import com.ms.womensafetyapp.util.ServiceEventTypes.ERROR_EVENT_WS
import com.ms.womensafetyapp.util.ServiceEventTypes.SERVICE_STATUS_EVENT_WS
import com.ms.womensafetyapp.util.ServiceStatusTypes.LOCATION_SERVICE_NOT_RUNNING
import com.ms.womensafetyapp.util.ServiceStatusTypes.LOCATION_SERVICE_RUNNING
import com.ms.womensafetyapp.util.isServiceRunningInForeground
import com.ms.womensafetyapp.util.minutesToMillis
import com.ms.womensafetyapp.util.showToast
import com.ms.womensafetyapp.util.toMinutes
import com.ms.womensafetyapp.util.toMinutesAndSeconds
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

//@Keep
@AndroidEntryPoint
class LocationService @Inject constructor() : Service() {

    @Inject
    lateinit var repository: Repository

    @Inject
    lateinit var locationClient: DefaultLocationClient

    @Inject
    lateinit var networkLocationClient: NetworkLocationClient

    @Inject
    lateinit var userPreferencesStore: UserPreferencesStore

    private lateinit var locationManager: LocationManager

    private lateinit var notification: NotificationCompat.Builder

    private lateinit var notificationManager: NotificationManagerCompat

    private val serviceScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private var duration: Int = DEFAULT_DURATION
    private var shouldSendSms: Boolean = DEFAULT_SHOULD_SEND_SMS

    private val locationListener = object : LocationListener {

        @Deprecated("Deprecated in Java")
        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
            super.onStatusChanged(provider, status, extras)
        }

        override fun onProviderDisabled(provider: String) {
            super.onProviderDisabled(provider)
            println(">>>>>>>>>> onProviders")

            if (provider == LocationManager.GPS_PROVIDER) {
                println(" >>>>>>>>>> GPS Disabled BroadcastReceiver")
                //if (lifecycleCallbacks.isMainActivityActive()){
                if (MainActivity.isActivityIsVisible()) {
                    val localBroadcastIntent = Intent(EVENTS_WS)
                    localBroadcastIntent.putExtra(EVENTS_TYPES_WS, ERROR_EVENT_WS)
                    localBroadcastIntent.putExtra(ERROR_TYPES, ERROR_GPS_DISABLED)
                    LocalBroadcastManager.getInstance(this@LocationService)
                        .sendBroadcast(localBroadcastIntent)
                } else {
                    applicationContext.showToast("Please enable GPS and Network to perform tracking.")
                }
                stop()
            } else if (provider == LocationManager.NETWORK_PROVIDER) {
                println(" >>>>>>>>>> Network Disabled BroadcastReceiver")

                if (MainActivity.isActivityIsVisible()) {
                    val localBroadcastIntent = Intent(EVENTS_WS)
                    localBroadcastIntent.putExtra(EVENTS_TYPES_WS, ERROR_EVENT_WS)
                    localBroadcastIntent.putExtra(ERROR_TYPES, ERROR_NETWORK_DISABLED)
                    LocalBroadcastManager.getInstance(this@LocationService)
                        .sendBroadcast(localBroadcastIntent)
                } else {
                    applicationContext.showToast("Please enable GPS and Network to perform tracking.")
                }
                stop()
            }
        }

        override fun onLocationChanged(location: Location) {
        }
    }

    @SuppressLint("MissingPermission")
    override fun onCreate() {
        super.onCreate()

        mInstance = this

        try {
            locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER, 0, 0f, locationListener
            )
            locationManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER, 0, 0f, locationListener
            )
        } catch (e: SecurityException) {
            e.printStackTrace()
            stop()
        }

        serviceScope.launch {
            userPreferencesStore.duration.collect {
                duration = it
            }
        }

        serviceScope.launch {
            userPreferencesStore.isSendSMS.collect {
                shouldSendSms = it
            }
        }

        // might register in onStartCommand.
    }

    override fun onBind(intent: Intent?): IBinder? {
        //return binder
        return null
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        println(">>>>>> onStartCommand")

        //(application as LocationApplication).stopPreWarmingLocationServices()

        /*
        /// calling intent
        // val emergencyNumber = runBlocking { repository.favouriteNumber() }
        val callIntent = Intent(
            Intent.ACTION_CALL, Uri.parse("tel:$emergencyNumber")
        ) // Replace with emergency number
        val callPendingIntent =
            PendingIntent.getActivity(this, 0, callIntent, PendingIntent.FLAG_IMMUTABLE)
         */

        val emergencyServiceIntent = Intent(this, EmergencyService::class.java)
        emergencyServiceIntent.action = ACTION_EMERGENCY

        val emergencyPendingIntent = PendingIntent.getService(
            this,
            0,
            emergencyServiceIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_CANCEL_CURRENT
        )

        // Intent to stop the service (or handle cancellation)
        val cancelIntent = Intent(this, LocationService::class.java).apply {
            action = ACTION_STOP
        }

        val cancelPendingIntent = PendingIntent.getService(
            this, 0, cancelIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_CANCEL_CURRENT
        )

        val resultIntent = Intent(this, MainActivity::class.java)
        val resultPendingIntent: PendingIntent? = TaskStackBuilder.create(this).run {
            addNextIntentWithParentStack(resultIntent)
            getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        }

        notification = NotificationCompat.Builder(this, NotificationManagerHelper.CHANNEL_ID)
            .setContentTitle("Women Tracking Location")
            .setContentText("Women Safety App is running.")
            .setContentIntent(resultPendingIntent)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setGroup(NotificationManagerHelper.SERVICE_GROUP).setOngoing(true)
            //.addAction(R.drawable.emergency_icon, "Call", callPendingIntent)
            .addAction(R.drawable.emergency_icon, "Emergency", emergencyPendingIntent)
            .addAction(R.drawable.baseline_clear_24, "Stop", cancelPendingIntent)

        notificationManager = NotificationManagerCompat.from(this)

        startForeground(NotificationManagerHelper.LOCATION_ID_NO, notification.build())

        val localBroadcastIntent = Intent(EVENTS_WS)
        localBroadcastIntent.putExtra(EVENTS_TYPES_WS, SERVICE_STATUS_EVENT_WS)
        localBroadcastIntent.putExtra(STATUS_TYPES, LOCATION_SERVICE_RUNNING)
        LocalBroadcastManager.getInstance(this).sendBroadcast(localBroadcastIntent)

        println(
            ">>>>>>>>>>>>>>>>> IS Foreground Location: ${
                this.isServiceRunningInForeground(
                    LocationService::class.java
                )
            }"
        )

        println(" >>>>>> Interval: ${System.currentTimeMillis().toMinutesAndSeconds()}")

        when (intent?.action) {
            ACTION_START -> start(ACTION_START)
            ACTION_STOP -> stop()
            ACTION_EMERGENCY -> {
                start(ACTION_EMERGENCY)
                //startNetworkLocation()
            }
        }

        return super.onStartCommand(intent, flags, startId)
    }

    @SuppressLint("MissingPermission")
    private fun start(action: String) {
        println(">>>>>> service start()")

        println(">>>>>>>>>>>>>>>>> Should Send SMS: $shouldSendSms")
        println(">>>>>>>>>>>>>>>>> Duration : $duration")

        val interval = when (action) {
            ACTION_START -> duration.minutesToMillis()
            ACTION_EMERGENCY -> {
                4.minutesToMillis()
            }

            else -> {
                4.minutesToMillis()
            }
        }

        println(">>>>>>>>>>>>>>>>>>>> Start Interval: $interval to ${interval.toMinutes()}")

        locationClient.getLocationUpdates(interval).catch { e ->
            println(">>>>>>>>. Error caught from Client: ${e.message}")
            //e.printStackTrace()
            if (MainActivity.isActivityIsVisible()) {
                val intent = Intent(EVENTS_WS)
                intent.putExtra(EVENTS_TYPES_WS, ERROR_EVENT_WS)
                intent.putExtra(ERROR_TYPES, e.message)
                LocalBroadcastManager.getInstance(this@LocationService).sendBroadcast(intent)
            } else {
                MainScope().launch {
                    applicationContext.showToast(e.message ?: "Error.")
                }
            }
            stop()
        }.onEach { location ->
            println(">>>>>>>>>Locations service:  ${location.latitude}, ${location.longitude}")

            val updatedNotification =
                notification.setContentText("Location: (${location.latitude}, ${location.longitude})")

            notificationManager.notify(
                NotificationManagerHelper.LOCATION_ID_NO, updatedNotification.build()
            )

            when (action) {

                ACTION_EMERGENCY -> {
                    println(">>>>>>>>> Start Location Emergency Action.")

                    repository.sendEmergencySms(this, location, onError = {
                        //stop() // This Error Handling needs to be reviewed and changed Imp.
                    })
                    //repository.sendEmergencySms(this.applicationContext)
                }

                ACTION_START -> {
                    println(">>>>>>>> Start Action")

                    println(
                        " >>>>>> Interval Start: ${
                            System.currentTimeMillis().toMinutesAndSeconds()
                        }"
                    )

                    repository.sendTrackingSms(this.applicationContext, location, onError = {
                        /*
                        if (MainActivity.isActivityIsVisible()) {
                            val intent = Intent(ERROR_EVENT_WS)
                            intent.putExtra("message", "Empty Contacts.")
                            LocalBroadcastManager.getInstance(this@LocationService)
                                .sendBroadcast(intent)
                        } else {
                            MainScope().launch {
                                applicationContext.showToast("Empty Contacts.")
                            }
                        }*/
                        //stop() // This Error Handling needs to be reviewed and changed Imp.
                    })

                }
            }

            // saving locations in database
            repository.saveLocations(location)

        }.launchIn(serviceScope)
    }


    private fun cancelChildrenScope() {
        println(">>>>>>>>>> Canceled Children Scope.")
        serviceScope.coroutineContext.cancelChildren()
    }

    private fun stop() {
        println(">>>> Service Stop")
        try {
            //stopSiren()
            stopForeground(true)
            stopSelf()
        } catch (e: Exception) {
            e.printStackTrace()
            println(">>>>>> Error service Stopped onStop():  $e")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        //unregisterReceiver(gpsNetworkReceiver)

        mInstance = null
        val localBroadcastIntent = Intent(EVENTS_WS)
        localBroadcastIntent.putExtra(EVENTS_TYPES_WS, SERVICE_STATUS_EVENT_WS)
        localBroadcastIntent.putExtra(STATUS_TYPES, LOCATION_SERVICE_NOT_RUNNING)
        LocalBroadcastManager.getInstance(this).sendBroadcast(localBroadcastIntent)

        locationManager.removeUpdates(locationListener)
        //application.unregisterActivityLifecycleCallbacks(lifecycleCallbacks)
        serviceScope.cancel()
        println(">>>>>>>>>>> onDestroy called.")
    }

    companion object {

        const val ACTION_START = "ACTION_START"
        const val ACTION_TESTING = "ACTION_TESTING"
        const val ACTION_STOP = "ACTION_STOP"
        const val ACTION_EMERGENCY = "ACTION_EMERGENCY"

        private var mInstance: LocationService? = null


        val DEFAULT_CONTACT_ENTITY = listOf(
            ContactEntity(
                contactName = "Abhinv", contactNumber = "7017417217", isFavourite = false
            )
        )

        fun isServiceCreated(): Boolean {
            try {
                return mInstance != null && mInstance!!.ping()
            } catch (e: Exception) {
                println(">>>>>>>>>>>> ExceptionType in service: $e")
                return false
            }
        }

    }

    private fun ping() = true

}


/*
private fun startSiren() {
println(">>>>>> siren start()")
//        val resId = resources.getIdentifier(R.raw.sossignal.toString(),"raw",packageName)
//        val mediaPlayer = MediaPlayer.create(this,resId)
mediaPlayer.setLooping(true)
mediaPlayer.start()
}

private fun stopSiren() {
println(">>>>>> siren stop()")
mediaPlayer.stop()
mediaPlayer.pause()
//mediaPlayer.release()
}

*/

/*
private var isGpsStateChange = false

private val gpsNetworkReceiver = object : BroadcastReceiver() {
override fun onReceive(context: Context?, intent: Intent?) {

    if (intent?.action == LocationManager.PROVIDERS_CHANGED_ACTION) {
        val locationManager =
            context?.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
        locationManager?.let {
            val isGPSEnabled = it.isProviderEnabled(LocationManager.GPS_PROVIDER)
            val isNetworkEnabled = it.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

            println(">>>>>>>>>> gps Broadcast...")

            val changeReason = intent.getStringExtra("change_reason")
            println(">>>>>>>> reason: $changeReason")

            if(!isGPSEnabled){
                println(" >>>>>>>>>> GPS Disabled BroadcastReceiver")
                isGpsStateChange = true
                val localBroadcastIntent = Intent("ERROR_SNACKBAR")
                localBroadcastIntent.putExtra("message", ERROR_GPS_DISABLED)
                LocalBroadcastManager.getInstance(this@LocationService)
                    .sendBroadcast(localBroadcastIntent)
            }
            if(!isNetworkEnabled){
                println(" >>>>>>>>>> Network Disabled BroadcastReceiver")
                //throw LocationClient.LocationException(ERROR_NETWORK_DISABLED)
//                            val localBroadcastIntent = Intent("ERROR_SNACKBAR")
//                            localBroadcastIntent.putExtra("message", ERROR_NETWORK_DISABLED)
//                            LocalBroadcastManager.getInstance(this@LocationService)
//                                .sendBroadcast(localBroadcastIntent)
            }
        }
    }
}
}*/

/*
private val mediaPlayer by lazy {
    val resId = resources.getIdentifier(R.raw.sossignal.toString(), "raw", packageName)
    MediaPlayer.create(this, resId)
}*/
