package com.ms.womensafetyapp.service

import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.TaskStackBuilder
import com.ms.womensafetyapp.MainActivity
import com.ms.womensafetyapp.R
import com.ms.womensafetyapp.util.NotificationManagerHelper
import com.ms.womensafetyapp.util.Repository
import com.ms.womensafetyapp.util.isServiceRunningInForeground
import com.ms.womensafetyapp.util.showToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class NormalSmsService @Inject constructor() : Service() {

    private val serviceScope: CoroutineScope by lazy { CoroutineScope(SupervisorJob() + Dispatchers.IO) }

    private lateinit var notification: NotificationCompat.Builder

    private lateinit var notificationManager: NotificationManagerCompat

    @Inject
    lateinit var repository: Repository

    override fun onCreate() {
        super.onCreate()
        mInstance = this
        // might register in onStartCommand.
    }

    override fun onBind(intent: Intent?): IBinder? {
        //return binder
        return null
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        // Intent to stop the service (or handle cancellation)
        val cancelIntent = Intent(this, LocationService::class.java).apply {
            action = LocationService.ACTION_STOP
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
            .setContentTitle("Women Safety Application")
            .setContentText("Normal Message Sending.")
            .setContentIntent(resultPendingIntent)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setGroup(NotificationManagerHelper.SERVICE_GROUP).setOngoing(true)
            //.addAction(R.drawable.emergency_icon, "Call", callPendingIntent)
            .addAction(R.drawable.baseline_clear_24, "Stop", cancelPendingIntent)

        notificationManager = NotificationManagerCompat.from(this)

        startForeground(NotificationManagerHelper.NORMAL_SMS_ID_NO, notification.build())

        println(
            ">>>>>>>>>>>>>>>>> IS Foreground Location: ${
                this.isServiceRunningInForeground(
                    NormalSmsService::class.java
                )
            }"
        )

        when (intent?.action) {
            ACTION_START -> start()
            ACTION_STOP -> stop()
        }

        return super.onStartCommand(intent, flags, startId)
    }

    private fun start() {
        serviceScope.launch {
            repository.sendHealthySms(this@NormalSmsService, onError = {
                this@NormalSmsService.showToast(it)
                //stop()
            })
            withContext(Dispatchers.Main) {
                this@NormalSmsService.showToast("Normal sms sending completed.")
            }
            stop()
        }
    }

    private fun stop() {
        println(">>>> Service Stop")
        try {
            //stopSiren()
            stopForeground(true)
            stopSelf()
        } catch (e: Exception) {
            e.printStackTrace()
            println(">>>>>> Error Normal service Stopped onStop():  $e")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mInstance = null
        serviceScope.cancel()
    }

    companion object {

        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP = "ACTION_STOP"

        private var mInstance: NormalSmsService? = null

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