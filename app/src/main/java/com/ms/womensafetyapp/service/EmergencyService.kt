package com.ms.womensafetyapp.service

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.SensorManager
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import androidx.core.app.NotificationCompat
import androidx.core.app.TaskStackBuilder
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.ms.womensafetyapp.MainActivity
import com.ms.womensafetyapp.MainActivity.Companion.EVENTS_TYPES_WS
import com.ms.womensafetyapp.MainActivity.Companion.EVENTS_WS
import com.ms.womensafetyapp.R
import com.ms.womensafetyapp.service.LocationService.Companion.ACTION_EMERGENCY
import com.ms.womensafetyapp.service.LocationService.Companion.ACTION_STOP
import com.ms.womensafetyapp.util.IntentExtraCodes.STATUS_TYPES
import com.ms.womensafetyapp.util.NotificationManagerHelper.CHANNEL_ID
import com.ms.womensafetyapp.util.NotificationManagerHelper.SERVICE_GROUP
import com.ms.womensafetyapp.util.NotificationManagerHelper.SHAKE_ID_NO
import com.ms.womensafetyapp.util.Repository
import com.ms.womensafetyapp.util.ServiceEventTypes.SERVICE_STATUS_EVENT_WS
import com.ms.womensafetyapp.util.ServiceStatusTypes.EMERGENCY_SERVICE_NOT_RUNNING
import com.ms.womensafetyapp.util.ServiceStatusTypes.EMERGENCY_SERVICE_RUNNING
import com.ms.womensafetyapp.util.ShakeDetectorSeismic
import com.ms.womensafetyapp.util.getCountryEmergencyCode
import com.ms.womensafetyapp.util.getCurrentCountryCode
import com.ms.womensafetyapp.util.isLocationEnabled
import com.ms.womensafetyapp.util.isPermissionsRequired
import com.ms.womensafetyapp.util.isServiceRunningInForeground
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

//@Keep
@AndroidEntryPoint
class EmergencyService : Service(), ShakeDetectorSeismic.Listener {

    @Inject
    lateinit var repository: Repository

//    private val shakeDetector: ShakeDetectorSeismic by lazy {
//        ShakeDetectorSeismic(this)
//    }

    private lateinit var shakeDetector: ShakeDetectorSeismic

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

//    private val localBroadcastManager: LocalBroadcastManager by lazy {
//        LocalBroadcastManager.getInstance(this)
//    }

    //private lateinit var localBroadcastManager: LocalBroadcastManager

    //private val telephonyManager by lazy { getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager }
    private lateinit var telephonyManager: TelephonyManager

    private var isPhoneCallerAvailable = false

    private val phoneStateListener = object : PhoneStateListener() {
        @Deprecated("Deprecated in Java")
        override fun onCallStateChanged(state: Int, phoneNumber: String?) {
            super.onCallStateChanged(state, phoneNumber)
            when (state) {
                TelephonyManager.CALL_STATE_RINGING -> {
                    isPhoneCallerAvailable = false
                }

                TelephonyManager.CALL_STATE_OFFHOOK -> {
                    isPhoneCallerAvailable = false
                }

                TelephonyManager.CALL_STATE_IDLE -> {
                    isPhoneCallerAvailable = true
                }
            }
        }
    }


    override fun onCreate() {
        super.onCreate()
        mInstance = this
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        /*
        val cancelIntent = Intent(this, EmergencyService::class.java).apply {
            action = ACTION_STOP
        }

        val cancelPendingIntent = PendingIntent.getService(
            this, 1, cancelIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_CANCEL_CURRENT
        )
        */

        // To start the application from Notification
        val resultIntent = Intent(this, MainActivity::class.java)
        val resultPendingIntent: PendingIntent? = TaskStackBuilder.create(this).run {
            addNextIntentWithParentStack(resultIntent)
            getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        }

        //val notificationChannel = NotificationManagerHelper.getNotificationChannel(SHAKE_ID)

        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Emergency Service Started")
            .setContentText("Shake to Send SOS.")
            .setContentIntent(resultPendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            //.addAction(R.drawable.baseline_clear_24, "Stop", cancelPendingIntent)
            .setGroup(SERVICE_GROUP)
            .build()

        startForeground(SHAKE_ID_NO, notificationBuilder)


        val sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        shakeDetector = ShakeDetectorSeismic(this)
        shakeDetector.start(sensorManager)
        telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE)
        //localBroadcastManager = LocalBroadcastManager.getInstance(this)

        val localBroadcastIntent = Intent(EVENTS_WS)
        localBroadcastIntent.putExtra(EVENTS_TYPES_WS, SERVICE_STATUS_EVENT_WS)
        localBroadcastIntent.putExtra(STATUS_TYPES, EMERGENCY_SERVICE_RUNNING)
        LocalBroadcastManager.getInstance(this).sendBroadcast(localBroadcastIntent)

        println(">>>>>>>>>>>>>>>>> IS Foreground Emergency: ${this.isServiceRunningInForeground(EmergencyService::class.java)}")

        serviceScope.launch {
            when (intent?.action) {
                ACTION_EMERGENCY -> {
                    sos()
                }
                ACTION_STOP -> stop()
            }
        }

        return super.onStartCommand(intent, flags, startId)
    }

    override fun hearShake() {
        println(">>>>>>>>>>>>>> Shake Service Shake detected.")
        serviceScope.launch {
            sos()
        }
    }

    @SuppressLint("MissingPermission")
    private suspend fun sos() {

        val countryCode = this.getCurrentCountryCode()
        println(">>>>>>>>>>>>>>>>>>>>>. Country Code: $countryCode")

        val number = repository.favouriteNumber() ?: getCountryEmergencyCode(countryCode)
        println(">>>>>>> number fetched from db: $number")

        if (isPhoneCallerAvailable) {
            val callingIntent = Intent(Intent.ACTION_CALL, Uri.parse("tel:$number"))
            callingIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(callingIntent)
        }

        repository.sendEmergencySms(this@EmergencyService)

//        val telecomManager = this@EmergencyService.getSystemService(Context.TELECOM_SERVICE) as TelecomManager
//        val emergencyNumber1 = telecomManager.getDefaultDialerPackage()
//        val emergencyNumber = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//           telephonyManager.emergencyNumberList[0]
//        } else {
//            emptyList()
//        }

        if(this@EmergencyService.isLocationEnabled()){
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
                if(!this@EmergencyService.isPermissionsRequired(Manifest.permission.ACCESS_BACKGROUND_LOCATION)){
                    withContext(Dispatchers.Main) {
                        val intent = Intent(this@EmergencyService, LocationService::class.java)
                        intent.action = ACTION_EMERGENCY
                        this@EmergencyService.startForegroundService(intent)
                    }
                }
            }else{
                withContext(Dispatchers.Main) {
                    val intent = Intent(this@EmergencyService, LocationService::class.java)
                    intent.action = ACTION_EMERGENCY
                    this@EmergencyService.startForegroundService(intent)
                }
            }
        }
    }

    private fun stop() {
        try {
            stopForeground(true)
            stopSelf()
        } catch (e: Exception) {
            e.printStackTrace()
            println(">>>>>> Error while stopping the service onStop():  $e")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        println(">>>>>>>>>>>>. Shake Service destroyed.")

        val localBroadcastIntent = Intent(EVENTS_WS)
        localBroadcastIntent.putExtra(EVENTS_TYPES_WS, SERVICE_STATUS_EVENT_WS)
        localBroadcastIntent.putExtra(STATUS_TYPES, EMERGENCY_SERVICE_NOT_RUNNING)
        LocalBroadcastManager.getInstance(this@EmergencyService).sendBroadcast(localBroadcastIntent)

        serviceScope.cancel()
        telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE)
        shakeDetector.stop()
        mInstance = null
        println(">>>>>>>>>> Emergency Service onDestroy.")
    }

    companion object {
        private var mInstance: EmergencyService? = null

        fun isServiceCreated(): Boolean {
            try {
                return mInstance != null && mInstance!!.ping()
            } catch (e: Exception) {
                println(">>>>>>>>>>>> ExceptionType in Emergency service: $e")
                return false
            }
        }
    }

    private fun ping() = true

}