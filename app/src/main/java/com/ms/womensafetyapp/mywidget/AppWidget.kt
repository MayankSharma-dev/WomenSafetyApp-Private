package com.ms.womensafetyapp.mywidget

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.Keep
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.glance.Button
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.actionStartService
import androidx.glance.appwidget.components.Scaffold
import androidx.glance.appwidget.components.TitleBar
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.updateAll
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import com.ms.womensafetyapp.MainActivity
import com.ms.womensafetyapp.R
import com.ms.womensafetyapp.service.EmergencyService
import com.ms.womensafetyapp.service.LocationService.Companion.ACTION_EMERGENCY
import com.ms.womensafetyapp.service.LocationService.Companion.ACTION_STOP
import com.ms.womensafetyapp.util.isPermissionsRequired
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

//@Keep
class EmergencyWidget : GlanceAppWidget() {
    companion object {
        private val SMALL_SQUARE = DpSize(100.dp, 100.dp)
        private val HORIZONTAL_RECTANGLE = DpSize(250.dp, 100.dp)
        private val BIG_SQUARE = DpSize(250.dp, 250.dp)
    }

//    override val sizeMode: SizeMode
//        get() = SizeMode.Responsive(
//            setOf(
//                SMALL_SQUARE,
//                HORIZONTAL_RECTANGLE,
//                BIG_SQUARE
//            )
//        )


    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override suspend fun provideGlance(context: Context, id: GlanceId) {

        val sosIntent = Intent(context, EmergencyService::class.java).apply {
            action = ACTION_EMERGENCY
        }

        val stopIntent = Intent(context, EmergencyService::class.java)
        stopIntent.action = ACTION_STOP

        val isPermissionRequired = context.isPermissionsRequired(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.POST_NOTIFICATIONS,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.SEND_SMS,
            Manifest.permission.CALL_PHONE,
            Manifest.permission.READ_CONTACTS
        )


//        val prefs = context.getSharedPreferences("widget_prefs", Context.MODE_PRIVATE)
//        val isRequired = prefs.getBoolean("isRequired", true)

        provideContent {
            GlanceTheme {
                AppWidget(
                    sosIntent = sosIntent,
                    stopIntent = stopIntent,
                    isPermissionRequired = isPermissionRequired
                )
            }
        }

    }
}

//@Keep
@Composable
private fun AppWidget(sosIntent: Intent, stopIntent: Intent, isPermissionRequired: Boolean) {

    Scaffold(
        titleBar = {
            TitleBar(
                startIcon = ImageProvider(R.drawable.ic_launcher_foreground),
                title = "Women Safety",
                modifier = GlanceModifier.clickable(actionStartActivity(MainActivity::class.java))
            )
        },
        backgroundColor = GlanceTheme.colors.widgetBackground,
        modifier = GlanceModifier.fillMaxSize()
    ) {
        Column(
            modifier = GlanceModifier.padding(bottom = 10.dp).fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            val modifier = GlanceModifier.fillMaxWidth().defaultWeight()

            Button(
                modifier = modifier,
                enabled = !isPermissionRequired,
                text = "SOS",
                onClick = actionStartService(sosIntent, true)
            )

            //actionStartService<EmergencyService>(true)

            Spacer(modifier = GlanceModifier.fillMaxWidth().height(5.dp))

            Button(
                modifier = modifier,
                enabled = !isPermissionRequired,
                text = "STOP", onClick = actionStartService(stopIntent, true)
            )
        }
    }
}

//@Keep
class EmergencyWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget
        get() = EmergencyWidget()

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            CoroutineScope(Dispatchers.IO).launch {
                println(">>>>>>>>>>>>>>>> Action Boot Completed")
                val widgetManager = GlanceAppWidgetManager(context)
                val glanceIds = widgetManager.getGlanceIds(EmergencyWidget::class.java)
                glanceIds.forEach { glanceId ->
                    EmergencyWidget().update(context,glanceId)
                }
            }
        }

    }
}