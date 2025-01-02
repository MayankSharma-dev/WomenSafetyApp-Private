package com.ms.womensafetyapp

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewTreeObserver
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.snackbar.Snackbar
import com.ms.womensafetyapp.databinding.ActivityMainBinding
import com.ms.womensafetyapp.features.dialog.About
import com.ms.womensafetyapp.service.EmergencyService
import com.ms.womensafetyapp.service.LocationService
import com.ms.womensafetyapp.service.LocationService.Companion.ACTION_START
import com.ms.womensafetyapp.service.NormalSmsService
import com.ms.womensafetyapp.util.ActivityToFragEvent
import com.ms.womensafetyapp.util.IntentExtraCodes.ERROR_TYPES
import com.ms.womensafetyapp.util.IntentExtraCodes.STATUS_TYPES
import com.ms.womensafetyapp.util.PermissionManager
import com.ms.womensafetyapp.util.ServiceErrorType.ERROR_GPS_DISABLED
import com.ms.womensafetyapp.util.ServiceEventTypes.ERROR_EVENT_WS
import com.ms.womensafetyapp.util.ServiceEventTypes.SERVICE_STATUS_EVENT_WS
import com.ms.womensafetyapp.util.ServiceStatusTypes.EMERGENCY_SERVICE_NOT_RUNNING
import com.ms.womensafetyapp.util.ServiceStatusTypes.EMERGENCY_SERVICE_RUNNING
import com.ms.womensafetyapp.util.ServiceStatusTypes.LOCATION_SERVICE_NOT_RUNNING
import com.ms.womensafetyapp.util.ServiceStatusTypes.LOCATION_SERVICE_RUNNING
import com.ms.womensafetyapp.util.ServiceTypes.EMERGENCY_SERVICE
import com.ms.womensafetyapp.util.ServiceTypes.LOCATIONS_SERVICE
import com.ms.womensafetyapp.viewpager.ViewPagerAdapter
import dagger.hilt.android.AndroidEntryPoint

//@Keep
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewPager: ViewPager2
    private lateinit var permissionManager: PermissionManager

    private var pressedTime: Long = 0
    private var selectedIndex = 0
    private var isLocationDialogShown = false
    private var mActivityToFrag: ActivityToFragEvent? = null
    private val localBroadcastManager: LocalBroadcastManager by lazy {
        LocalBroadcastManager.getInstance(this)
    }

    private val eventReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent != null) {

                val eventType = intent.getIntExtra(EVENTS_TYPES_WS, 0)

                if (eventType == ERROR_EVENT_WS) {
                    val message = intent.getStringExtra(ERROR_TYPES) ?: "Error in Service"
                    runOnUiThread {
                        if (message.contentEquals(ERROR_GPS_DISABLED)) {
                            println(">>>>>>>>>> show location dialog")
                            if (!isLocationDialogShown) {
                                isLocationDialogShown = true
                                showLocationSettingsDialog()
                            }
                        } else {
                            showSnackBar(message)
                        }
                    }
                }

                if (eventType == SERVICE_STATUS_EVENT_WS) {
                    val extra = intent.getIntExtra(STATUS_TYPES, 0)
                    when (extra) {

                        EMERGENCY_SERVICE_RUNNING -> {
                            mActivityToFrag?.eventReceivedServiceInfo(true, EMERGENCY_SERVICE)
                            println(">>>>>>>>>>>> $  inActivity LocalBroadcast: Running.")
                        }

                        EMERGENCY_SERVICE_NOT_RUNNING -> {
                            mActivityToFrag?.eventReceivedServiceInfo(false, EMERGENCY_SERVICE)
                            println(">>>>>>>>>>>> $ inActivity LocalBroadcast: is not Running.")
                        }

                        LOCATION_SERVICE_RUNNING -> {
                            mActivityToFrag?.eventReceivedServiceInfo(true, LOCATIONS_SERVICE)
                        }

                        LOCATION_SERVICE_NOT_RUNNING -> {
                            mActivityToFrag?.eventReceivedServiceInfo(false, LOCATIONS_SERVICE)
                        }
                    }
                }
            }
        }
    }


    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        permissionManager = PermissionManager(this)

        viewPager = binding.viewPager
        viewPager.adapter = ViewPagerAdapter(supportFragmentManager, lifecycle)
        viewPager.offscreenPageLimit = 2
        viewPager.isUserInputEnabled = false

        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.main -> {
                    viewPager.currentItem = 0
                    selectedIndex = 0
                }

                R.id.more -> {
                    viewPager.currentItem = 1
                    selectedIndex = 1
                }

                else -> throw IllegalArgumentException("Unexpected itemID")
            }
            true
        }

        localBroadcastManager.registerReceiver(eventReceiver, IntentFilter(EVENTS_WS))

        binding.root.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener{
            override fun onGlobalLayout() {
                binding.root.viewTreeObserver.removeOnGlobalLayoutListener(this)
                if (savedInstanceState == null) {
                    println(">>>>>>>>>>>>>>>. If condition")
                    permissionManager.checkPermissions(Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.POST_NOTIFICATIONS,
                        Manifest.permission.READ_PHONE_STATE,
                        Manifest.permission.SEND_SMS,
                        Manifest.permission.CALL_PHONE,
                        Manifest.permission.READ_CONTACTS,
                        onPermissionsGranted = { isGranted, _ ->
                            if (isGranted) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                    permissionManager.checkPermissions(Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                                        onPermissionsGranted = { isAllowed, _ ->
                                            if (isAllowed) {
                                                val intent = Intent(this@MainActivity, EmergencyService::class.java)
                                                startForegroundService(intent)
                                            }
                                        })
                                }else{
                                    val intent = Intent(this@MainActivity, EmergencyService::class.java)
                                    startForegroundService(intent)
                                }
                            }
                        })
                }
            }
        })

        //startActivity(Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS))

        /*
        // onBackPressed
        if (Build.VERSION.SDK_INT >= 33) {
            onBackInvokedDispatcher.registerOnBackInvokedCallback(
                OnBackInvokedDispatcher.PRIORITY_DEFAULT
            ) {
                exitOnBackPressed()
            }
        } else {
            onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    exitOnBackPressed()
                }
            })
        }// \\onBackPressed
        */
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.about -> {
                if (supportFragmentManager.findFragmentByTag(About.TAG)?.isAdded != true) {
                    About.newInstance().show(supportFragmentManager, About.TAG)
                }
                true
            }

            R.id.normal_message -> {

                permissionManager.checkPermissions(Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.POST_NOTIFICATIONS,
                    Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.SEND_SMS,
                    Manifest.permission.CALL_PHONE,
                    Manifest.permission.READ_CONTACTS,
                    onPermissionsGranted = { isGranted, _ ->
                        if(isGranted){
                            if (!NormalSmsService.isServiceCreated()) {
                                val intent = Intent(applicationContext, NormalSmsService::class.java)
                                intent.action = ACTION_START
                                startForegroundService(intent)
                            }
                        }
                    })
                true
            }

            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_container)
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(KEY_SELECTED_INDEX, selectedIndex)
    }

    override fun onResume() {
        super.onResume()
        mInstance = this
        localBroadcastManager.registerReceiver(eventReceiver, IntentFilter(EVENTS_WS))
    }

    override fun onPause() {
        super.onPause()
        mInstance = null
        localBroadcastManager.unregisterReceiver(eventReceiver)
    }

    fun showSnackBar(message: String) {
        val rootView = findViewById<View>(android.R.id.content)
        Snackbar.make(rootView, message, Snackbar.LENGTH_SHORT).show()
    }

    private fun showLocationSettingsDialog() {
        permissionManager.requestLocationSettingsClient( onLocationSettingClient = { isEnabled, _ ->
            isLocationDialogShown = false
            if(isEnabled){
                permissionManager.checkPermissions(Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.READ_CONTACTS,
                    Manifest.permission.SEND_SMS,
                    Manifest.permission.CALL_PHONE,
                    /*if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) */
                    Manifest.permission.POST_NOTIFICATIONS,
                    onPermissionsGranted = { isGranted, _ ->
                        if (isGranted) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                permissionManager.checkPermissions(
                                    Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                                    onPermissionsGranted = { isAllowed, _ ->
                                        if (isAllowed)
                                            if (!LocationService.isServiceCreated()) {
                                                val intent = Intent(
                                                    applicationContext,
                                                    LocationService::class.java
                                                )
                                                intent.action = ACTION_START
                                                startForegroundService(intent)
                                                showSnackBar("Tracking Started.")
                                            }
                                    })
                            } else {
                                if (!LocationService.isServiceCreated()) {
                                    val intent =
                                        Intent(applicationContext, LocationService::class.java)
                                    intent.action = ACTION_START
                                    startForegroundService(intent)
                                    showSnackBar("Tracking Started.")
                                }
                            }
                        }
                    })
            }else{
                showSnackBar("Please enable GPS to start the service.")
            }
        })

        /*
        val locationRequest =
            LocationRequest.create()/*.setInterval(10000L).setFastestInterval(10000L)*/
                .setPriority(Priority.PRIORITY_HIGH_ACCURACY)

        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val locationSettingsRequest = builder.build()
        val settingClient = LocationServices.getSettingsClient(this@MainActivity)

        val task = settingClient.checkLocationSettings(locationSettingsRequest)

        task.addOnSuccessListener { locationSettingsResponse ->
            println(">>>>>>>>>> task success")
        }

        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {
                // Location settings are not satisfied. But could be fixed by showing the user a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    println(">>>>>>>>>> task failure")
                    /*
                    exception.startResolutionForResult(
                        this@MainActivity, REQUEST_CHECK_SETTINGS
                    )*/

                    val intentSenderRequest =
                        IntentSenderRequest.Builder(exception.resolution).build()
                    permissionManager.locationSettingsLauncher.launch(intentSenderRequest)
                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore the error.
                }
            }
        }
        */
    }

    fun setOnActivityToFragmentInterface(mActivityToFragEvent: ActivityToFragEvent) {
        mActivityToFrag = mActivityToFragEvent
    }

    override fun onDestroy() {
        super.onDestroy()
        mInstance = null
        //localBroadcastManager.unregisterReceiver(eventReceiver)
    }

    @Deprecated("This method has been deprecated in favor of using the\n      {@link OnBackPressedDispatcher} via {@link #getOnBackPressedDispatcher()}.\n      The OnBackPressedDispatcher controls how back button events are dispatched\n      to one or more {@link OnBackPressedCallback} objects.")
    override fun onBackPressed() {
        println(">>>>>>>>>>>>> ! onBackPressed")
        if (selectedIndex == 0) {
            if (pressedTime + 2000 > System.currentTimeMillis()) {
//                finishAffinity()
//                finish()
                super.onBackPressed()
            } else {
                Toast.makeText(this, "Press back again to exit", Toast.LENGTH_SHORT).show()
            }
            pressedTime = System.currentTimeMillis()
        } else {
            val navController = findNavController(R.id.nav_host_container)

            when (navController.currentDestination?.id == R.id.moreFragment) {
                true -> {
                    println(">>>>>>>>>>> ! MoreFragments Back")
                    binding.bottomNav.selectedItemId = R.id.main
                    //title = "Women Safety"
                    println(">>>>>>>>>>>>>>>>>>> $title")
                }

                false -> {
                    println(">>>>>>>>>>> ! false")
                    navController.navigateUp()
                }

                null -> {
                    println(">>>>>>>>>>> ! null")
                }
            }
        }
    }

    companion object {

        const val REQUEST_CHECK_SETTINGS = 0x1

        const val EVENTS_WS = "EVENTS_WS"
        const val EVENTS_TYPES_WS = "EVENTS_TYPES_WS"

        const val TAG_HOME_FRAGMENT = "HOME_FRAGMENT"
        const val TAG_MORE_FRAGMENT = "MORE_FRAGMENT"
        const val KEY_SELECTED_INDEX = "KEY_SELECTED_INDEX"

        private var mInstance: MainActivity? = null

        fun isActivityIsVisible(): Boolean {
            try {
                return mInstance != null && mInstance!!.ping()
            } catch (e: Exception) {
                println(">>>>>>>>>>>> MainActivity not Active error:: $e")
                return false
            }
        }
    }

    private fun ping() = true

}


/*
Please do check for ACCESS_BACKGROUND_LOCATION for SDK, which you have not asked.
Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
*/

/**
 * If this error comes it means you need to turn on Location permission "Always On" not "Allow while using the app."
 * java.lang.RuntimeException: Unable to start service com.ms.womensafetyapp.service.LocationService with Intent { act=ACTION_EMERGENCY cmp=com.ms.womensafetyapp/.service.LocationService }:
 * java.lang.SecurityException: Starting FGS with type location callerApp=ProcessRecord:com.ms.womensafetyapp} targetSDK=34 requires permissions: all of the permissions allOf=true [android.permission.FOREGROUND_SERVICE_LOCATION] any of the permissions allOf=false [android.permission.ACCESS_COARSE_LOCATION, android.permission.ACCESS_FINE_LOCATION]
 * and the app must be in the eligible state/exemptions to access the foreground only permission
 */


/*

private lateinit var homeFragment: HomeFragment
private lateinit var moreFragment: MoreContainerFragment

private val fragments: Array<Fragment>
    get() = arrayOf(
        homeFragment,
        moreFragment
    )

private val selectedFragment get() = fragments[selectedIndex]

private fun selectFragment(selectedFragment: Fragment) {

    if (!supportFragmentManager.fragments.contains(selectedFragment))
        return

    val targetIndex = fragments.indexOf(selectedFragment)
    if (targetIndex == selectedIndex)
        return

    val transactions = supportFragmentManager.beginTransaction()
//            .setCustomAnimations( // Add animations for smoother transitions
//                android.R.anim.slide_in_left,
//                android.R.anim.slide_out_right,
//                android.R.anim.slide_in_left,
//                android.R.anim.slide_out_right,
//            )
    fragments.forEachIndexed { index, fragment ->
        if (selectedFragment == fragment) {
            if (fragment.isAdded) {
                transactions.show(fragment)
            }
            //transactions = transactions.attach(fragment)
            selectedIndex = index
        } else {
            if (fragment.isAdded /*&& fragment.isVisible*/)
                transactions.hide(fragment)
            //transactions = transactions.detach(fragment)
        }
    }
    transactions.commit()

    title = when (selectedFragment) {
        is HomeFragment -> "Women Safety"
        is MoreContainerFragment -> "Women Safety" //"More 1"
        else -> {
            println(">>>>>>>>>>>> Error in SelectFragment Function while setting title.")
            "Women Safety"
        }
    }
}
*/

/*
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)

    if (requestCode == REQUEST_CHECK_SETTINGS) {
        isLocationDialogShown = false
        when (resultCode) {
            Activity.RESULT_OK -> {
                //this@MainActivity.showToast("GPS enabled, Please perform the action again.")
                //showSnackBar("GPS enabled, Please perform the action again.")
                permissionManager.checkPermissions(Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.READ_CONTACTS,
                    Manifest.permission.SEND_SMS,
                    Manifest.permission.CALL_PHONE,
                    /*if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) */
                    Manifest.permission.POST_NOTIFICATIONS,
                    onPermissionsGranted = { isGranted, _ ->
                        if (isGranted) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                permissionManager.checkPermissions(
                                    Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                                    onPermissionsGranted = { isAllowed, _ ->
                                        if (isAllowed)
                                            if (!LocationService.isServiceCreated()) {
                                                val intent = Intent(
                                                    applicationContext,
                                                    LocationService::class.java
                                                )
                                                intent.action = ACTION_START
                                                startForegroundService(intent)
                                            }
                                    })
                            } else {
                                if (!LocationService.isServiceCreated()) {
                                    val intent =
                                        Intent(applicationContext, LocationService::class.java)
                                    intent.action = ACTION_START
                                    startForegroundService(intent)
                                }
                            }
                        }
                    })
            }

            Activity.RESULT_CANCELED -> {
                //this@MainActivity.showToast("GPS disabled, cant start the service.")
                showSnackBar("GPS disabled, cant start the service.")
                //isLocationDialogShown = false
            }
        }
    }
}*/

/*
// inside on Create for fragment management.
if (savedInstanceState == null) {
    println(">>>>>>>>>>>>>>>. If condition")

    permissionManager.checkPermissions(
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.POST_NOTIFICATIONS,
        Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.SEND_SMS,
        Manifest.permission.CALL_PHONE,
        Manifest.permission.READ_CONTACTS,
        //Manifest.permission.WRITE_CONTACTS,
        onPermissionsGranted = { isGranted, _ ->
            if (isGranted) {
                //val intent = Intent(this@MainActivity, LocationService::class.java)
                val intent = Intent(this@MainActivity, EmergencyService::class.java)
                //intent.action = ACTION_TESTING
                println(">>>>>> Intent sent to start service")
                startForegroundService(intent)
            }
        }
    )

    homeFragment = HomeFragment()
    moreFragment = MoreContainerFragment()

    supportFragmentManager.beginTransaction()
        .add(binding.fragmentContainer.id, homeFragment, TAG_HOME_FRAGMENT)
        .add(binding.fragmentContainer.id, moreFragment, TAG_MORE_FRAGMENT)
        .hide(moreFragment)
        .commit()

    // called here because Home title was not showing correctly.
    //selectFragment(homeFragment)

} else {

    println(">>>>>>>>>>>>>>> Else condition.")
    homeFragment =
        supportFragmentManager.findFragmentByTag(TAG_HOME_FRAGMENT) as HomeFragment
    moreFragment =
        supportFragmentManager.findFragmentByTag(TAG_MORE_FRAGMENT) as MoreContainerFragment

    selectedIndex = savedInstanceState.getInt(KEY_SELECTED_INDEX, 0)
}

selectFragment(selectedFragment)

binding.bottomNav.setOnItemSelectedListener { item ->

    val fragment = when (item.itemId) {
        R.id.main -> homeFragment
        R.id.more -> moreFragment
        else -> throw IllegalArgumentException("Unexpected NavId")
    }
    selectFragment(fragment)
    true
}
*/
