package com.ms.womensafetyapp.util

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.annotation.Keep
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationManagerCompat

//@Keep
object NotificationManagerHelper {

    const val CHANNEL_ID = "safetyLocation"
    const val SHAKE_ID = "SHAKE_ID"
    const val LOCATION_ID = "LOCATION_ID"
    const val NORMAL_SMS_ID = "NORMAL_SMS_ID"
    const val SHAKE_ID_NO = 1
    const val LOCATION_ID_NO = 2
    const val NORMAL_SMS_ID_NO = 3
    const val SERVICE_GROUP = "WOMEN_SERVICE_GROUP"

    private var notificationManager: NotificationManager? = null
    @SuppressLint("StaticFieldLeak")
    private var notificationManagerCompact: NotificationManagerCompat? = null

    private fun createNotificationManager(context: Context){
       notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    private fun createNotificationManagerCompact(context: Context){
        notificationManagerCompact = NotificationManagerCompat.from(context)
    }

    fun getNotificationManager(context: Context): NotificationManagerCompat {

//        if(notificationManager == null){
//            createNotificationManager(context)
//        }

        if(notificationManagerCompact == null){
            createNotificationManagerCompact(context)
        }
        //return notificationManager as NotificationManager
        return notificationManagerCompact!!
    }



    @RequiresApi(Build.VERSION_CODES.O)
    fun getNotificationChannel(channelName: String): NotificationChannel {
        var name: String = ""
        var importance: Int = 0

        when(channelName){
            SHAKE_ID -> {
                name = "Shake Detector Running."
                importance = NotificationManager.IMPORTANCE_DEFAULT
            }
            LOCATION_ID -> {
                name = "Location Tracking Running."
                importance = NotificationManager.IMPORTANCE_LOW
            }

            NORMAL_SMS_ID -> {
                name = "Normal Message Running."
                importance = NotificationManager.IMPORTANCE_LOW
            }
        }
        val channel = NotificationChannel(CHANNEL_ID,name, importance)
        return  channel
    }

}