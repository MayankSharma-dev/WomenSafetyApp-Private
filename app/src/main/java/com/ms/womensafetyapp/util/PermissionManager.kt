package com.ms.womensafetyapp.util


import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.provider.ContactsContract
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.ms.womensafetyapp.MainActivity

//@Keep
class PermissionManager(
    caller: ActivityResultCaller,
    private val context: Context,
    private val fragmentManager: FragmentManager,
    private val shouldShowPermissionRationale: (permission: String) -> Boolean
) {
    constructor(activity: ComponentActivity) : this(
        caller = activity,
        context = activity,
        fragmentManager = (activity as AppCompatActivity).supportFragmentManager,
        shouldShowPermissionRationale = { activity.shouldShowRequestPermissionRationale(it) })

    constructor(fragment: Fragment) : this(
        caller = fragment,
        context = fragment.requireContext(),
        fragmentManager = fragment.parentFragmentManager,
        shouldShowPermissionRationale = { fragment.shouldShowRequestPermissionRationale(it) })


    private var onPermissionsGranted: ((isGranted: Boolean, fromWhere: String) -> Unit)? = null

    private var onReceiveContactDetails: ((isReceived: Boolean, name: String, number: String) -> Unit)? =
        null

    private var onLocationSettingClient: ((isEnabled: Boolean, fromWhere: String) -> Unit)? = null

    private val requestPermissionLauncher =
        caller.registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (!isGranted) {
                // Explain to the user that the feature is unavailable because the
                // feature requires a permission that the user has denied. At the
                // same time, respect the user's decision. Don't link to system
                // settings in an effort to convince the user to change their
                // decision.
                val message =
                    "This permission is necessary for the core functionality of the app. Please allow them App Settings."
                //val title = "Permission Required."
                showDialog(message)
            }
            onPermissionsGranted?.invoke(isGranted, "SingleRequest")
        }

    private val requestMultiplePermissionLauncher =
        caller.registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
            val isGranted = result.values.all { it }
            println(">>>>> Multiple permissions: $result")
            if (!isGranted) {
                val deniedPermission = result.getAllDeniedPermissions()
                val message =
                    "$deniedPermission permissions are necessary for the core functionality of the app. Please allow them in the App Settings."
                showDialog(message)
            }
            onPermissionsGranted?.invoke(isGranted, "MultipleRequest")
        }


    @SuppressLint("Range")
    val openContacts = caller.registerForActivityResult(ActivityResultContracts.PickContact()) {
        println(">>>>>>>>> inside Open contracts")
        if (it == null) {
            return@registerForActivityResult
        }

        var phoneNumber: String? = null
        var contactName: String? = null

        val contactData: Uri = it
        val phone: Cursor? = context?.contentResolver?.query(contactData, null, null, null, null)
        if (phone!!.moveToFirst()) {
//            val contactName: String =
//                phone.getString(phone.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
            contactName =
                phone.getString(phone.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
            // To get number - runtime permission is mandatory.
            val id: String = phone.getString(phone.getColumnIndex(ContactsContract.Contacts._ID))
            if (phone.getString(phone.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))
                    .toInt() > 0
            ) {
                val phones = context?.contentResolver?.query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    null,
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + id,
                    null,
                    null
                )
                while (phones!!.moveToNext()) {
//                    val phoneNumber =
//                        phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                    phoneNumber =
                        phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                    Log.d("## Number", phoneNumber)
                    break
                }

                phones.close()
            }
            //Log.d("## Contact Name", contactName)
        }
        println(">>>>>>>>>> Phone Details : \n name: $contactName \n number : $phoneNumber")
        onReceiveContactDetails?.invoke(true, contactName!!, phoneNumber!!)
    }

    private val locationSettingsLauncher: ActivityResultLauncher<IntentSenderRequest> = caller.registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result: ActivityResult ->
        if (result.resultCode== Activity.RESULT_OK) {
            onLocationSettingClient?.invoke(true, "fromLocationSettingsLauncher")
        } else if (result.resultCode == Activity.RESULT_CANCELED) {
            onLocationSettingClient?.invoke(false, "fromLocationSettingsLauncher")
        }
    }

    private fun requestPermissions(permissionsToBeRequested: List<String>) {
        if (permissionsToBeRequested.size > 1) {
            println(">>>>>>>>>>>>>>>>>> Request Multiple Permission")
            requestMultiplePermissionLauncher.launch(permissionsToBeRequested.toTypedArray())
        } else {
            println(">>>>>>>>>>>>>>>>>> Request Single Permission")
            permissionsToBeRequested.firstOrNull()?.let { requestPermissionLauncher.launch(it) }
        }
    }

    fun checkPermissions(
        vararg permissions: String,
        onPermissionsGranted: ((isGranted: Boolean, fromWhere: String) -> Unit)? = null,
        onReceiveContactDetails: ((isReceived: Boolean, name: String, number: String) -> Unit)? = null
    ) {

        //fromWhere parameter is used here so that I can know how current is flowing and from where onPermissionsGranted is invoked..

        this.onPermissionsGranted = onPermissionsGranted

        //for contactDetails Receiving.
        this.onReceiveContactDetails = onReceiveContactDetails

        val permissionsToBeRequested = permissions.filter { permission ->
            ContextCompat.checkSelfPermission(
                context, permission
            ) != PackageManager.PERMISSION_GRANTED
        }

        val shouldShowRequestPermissionRationale = permissionsToBeRequested.any {
            shouldShowPermissionRationale(it)
        }

        when {
            permissionsToBeRequested.isEmpty() -> {
                onPermissionsGranted?.invoke(true, "AlreadyGranted")
            }

            shouldShowRequestPermissionRationale -> {
                // In an educational UI, explain to the user why your app requires this
                // permission for a specific feature to behave as expected, and what
                // features are disabled if it's declined.

                val permissionsRationaleToBeShown = permissionsToBeRequested.filter {
                    shouldShowPermissionRationale(it)
                }.joinToString(", ") { it.removePrefix("android.permission.") }

                println(">>>>>>>>>> Permission Rationale to be shown $permissionsRationaleToBeShown")

                val message =
                    when (permissionsRationaleToBeShown.contains("ACCESS_BACKGROUND_LOCATION")) {
                        true -> {
                            "Please enable 'Allow all the time' in Location permission in App permissions which is necessary for the core functionality of the app. Please allow them in the App Settings."
                        }

                        else -> {
                            "$permissionsRationaleToBeShown permissions are necessary for the core functionality of the app. Please allow them in the App Settings."
                        }
                    }

                //val message = "$permissionsRationaleToBeShown permissions are necessary for the core functionality of the app. Please allow them in the App Settings."
                //val title = "Permission Required."
                showDialog(message)
                this.onPermissionsGranted?.invoke(false, "PermissionDenied")
            }

            else -> requestPermissions(permissionsToBeRequested)
        }
    }

   fun requestLocationSettingsClient(onLocationSettingClient: ((isEnabled: Boolean, fromWhere: String) -> Unit)? = null){
       this.onLocationSettingClient = onLocationSettingClient

       val locationRequest =
           LocationRequest.create()/*.setInterval(10000L).setFastestInterval(10000L)*/
               .setPriority(Priority.PRIORITY_HIGH_ACCURACY)

       val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
       val locationSettingsRequest = builder.build()
       val settingClient = LocationServices.getSettingsClient(context)

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
                   /*
                   exception.startResolutionForResult(
                       this@MainActivity, REQUEST_CHECK_SETTINGS
                   )*/

                   val intentSenderRequest =
                       IntentSenderRequest.Builder(exception.resolution).build()
                   locationSettingsLauncher.launch(intentSenderRequest)

               } catch (sendEx: IntentSender.SendIntentException) {
                   // Ignore the error.
               }
           }
       }
   }

    private fun showDialog(
        message: String/*, permissionRequired: String*/
    ) {
        val builder = MaterialAlertDialogBuilder(context)
        builder.setMessage(message).setTitle("Permission Required.")
            .setNeutralButton("Go to App Settings") { _, _ ->
                context.startActivity(
                    Intent(
                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.fromParts("package", context.packageName, null)
                    )
                )
            }

        val dialog = builder.create()
        dialog.show()
    }

    private fun Map<String, Boolean>.getAllDeniedPermissions(): String =
        filterNot { it.value }.keys.joinToString(", ") { it.removePrefix("android.permission.") }

    /*
    private fun Map<String,Boolean>.getDeniedPermissions(): String{
        val deniedPermissions = StringBuilder()
        this.forEach {
            if(!it.value){
                deniedPermissions.append(it.key.removePrefix("android.permission."))
            }
        }
        return deniedPermissions.toString()
    }*/
}