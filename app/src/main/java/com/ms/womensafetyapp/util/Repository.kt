package com.ms.womensafetyapp.util

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.location.Location
import android.telephony.SmsManager
import com.ms.womensafetyapp.database.ContactEntity
import com.ms.womensafetyapp.database.LocationDatabase
import com.ms.womensafetyapp.database.LocationEntity
import com.ms.womensafetyapp.preference.UserPreferencesStore
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.concurrent.Executors
import javax.inject.Inject

//@Keep
class Repository @Inject constructor(
    locationDatabase: LocationDatabase,/* private val smsManager: SmsManager,*/
    private val userPreferencesStore: UserPreferencesStore
) {

    private val locationDao = locationDatabase.locationDao()
    private val contactsDao = locationDatabase.contactDao()

    private var smsManager: SmsManager? = null

    //private val smsDispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
    private val smsDispatcher by lazy {
        Executors.newSingleThreadExecutor().asCoroutineDispatcher()
    }

    suspend fun saveLocations(location: Location) {
        try {
            locationDao.insertLocations(LocationEntity(location.latitude, location.longitude))
        } catch (_: IOException) {
        }
    }

    suspend fun sendEmergencySms(
        appContext: Context,
        location: Location? = null,
        onError: (message: String) -> Unit = {}
    ) {
        println(">>>>>>> Emergency Message")
        try {
            //delay(10000L)
            smsManager = appContext.applicationContext.getSystemService(SmsManager::class.java) as SmsManager

            println(">>>>>>>>>>>> Message Send.")
            val contacts = contactsDao.getAllContacts().firstOrNull()

            val countryCode = appContext.getCurrentCountryCode()
            // This code might give an RunTimeException as getCountryEmergencyCode might give empty string
            val emergencyNumber = getCountryEmergencyCode("SS")
            val emergencyContact = ContactEntity(
                "Country-Emergency-Code",
               emergencyNumber,
                false
            )

            val allContacts = contacts?.plus(emergencyContact) ?: listOf(emergencyContact)

            // or this both are same, plus sign (+) in the code utilizes Kotlin's operator overloading mechanism and is converted to a call to the Collection.plus() function.
            // val updatedContacts = contacts + ContactEntity("Country-Emergency-Code", getCountryEmergencyCode("US"), false)

            val sendPI: PendingIntent = PendingIntent.getBroadcast(
                appContext, 99, Intent("SMS_SENT"), PendingIntent.FLAG_IMMUTABLE
            )

            val message= if(location!= null ) "Help! Help! Help! SOS. This is my current location: https://maps.google.com/?q=${location.latitude},${location.longitude} ,please help." else "Help! Help! Help! SOS. Please help."

            sendingSms(allContacts, "Just Checking functionality. $message", sendPI, onError = {
                onError(it) // This Error Handling needs to be reviewed and changed Imp.
            })

            smsManager = null
        } catch (e: Exception) { // This Error Handling needs to be reviewed and changed Imp.
            when (e) {
                is IOException -> {
                    // Handle network or IO errors
                    println("Error sending SMS: ${e.message}")
                }

                is IllegalArgumentException -> {
                    // Handle invalid phone number or message
                    println("Invalid input for SMS: ${e.message}")
                }

                else -> {// Handle other exceptions
                    e.printStackTrace()
                }
            }
        }
    }

    suspend fun sendTrackingSms(
        appContext: Context,
        location: Location,
        onError: (message: String) -> Unit
    ) {
        println(">>>>>>>>>>>>> Sending Message Called.")
        try {

            smsManager = appContext.applicationContext.getSystemService(SmsManager::class.java) as SmsManager

            //val contacts = contactsDao.getAllContacts().firstOrNull()
            val favouriteContact = contactsDao.getFavouriteContact()
            if (favouriteContact == null) {
                onError("No Favourite Contact found.") // This Error Handling needs to be reviewed and changed Imp.
                println(">>>>>>>>>>>> The Favourite Contact empty in sending SMS")
                return
            }

            val sendPI: PendingIntent = PendingIntent.getBroadcast(
                appContext, 99, Intent("SMS_SENT"), PendingIntent.FLAG_IMMUTABLE
            )

            val message = "Sharing my current location: https://maps.google.com/?q=${location.latitude},${location.longitude} for tracking and safety."
            sendingSms(listOf<ContactEntity>(favouriteContact), message, sendPI, onError = {
                onError(it)
            })
            smsManager = null
        } catch (e: Exception) {
            onError(e.message ?: "Error while sending message.")
            return
        }
    }

    suspend fun sendHealthySms(appContext: Context,  onError: (message: String) -> Unit){
        try {
            println(">>>>>>>>>>>> Healthy Message Send.")

            smsManager = appContext.applicationContext.getSystemService(SmsManager::class.java) as SmsManager

            val contacts = contactsDao.getAllContacts().firstOrNull() ?: listOf(
                ContactEntity(
                    "Country-Emergency-Code",
                    getCountryEmergencyCode("SS"),
                    false
                )
            )

            val updatedContacts = contacts.plus(
                ContactEntity(
                    "Country-Emergency-Code",
                    getCountryEmergencyCode("SS"),
                    false
                )
            )
            // or this both are same, plus sign (+) in the code utilizes Kotlin's operator overloading mechanism and is converted to a call to the Collection.plus() function.
            // val updatedContacts = contacts + ContactEntity("Country-Emergency-Code", getCountryEmergencyCode("US"), false)

            val sendPI: PendingIntent = PendingIntent.getBroadcast(
                appContext, 99, Intent("SMS_SENT"), PendingIntent.FLAG_IMMUTABLE
            )

            sendingSms(updatedContacts, "Checking Normal Message Functionality.", sendPI, onError = {
                onError(it) // This Error Handling needs to be reviewed and changed Imp.
            })

            smsManager= null

        } catch (e: Exception) { // This Error Handling needs to be reviewed and changed Imp.
            when (e) {
                is IOException -> {
                    // Handle network or IO errors
                    println("Error sending SMS: ${e.message}")
                }

                is IllegalArgumentException -> {
                    // Handle invalid phone number or message
                    println("Invalid input for SMS: ${e.message}")
                }

                else -> {// Handle other exceptions
                    e.printStackTrace()
                }
            }
        }
    }

    private suspend fun sendingSms(
        contacts: List<ContactEntity>,
        message: String,
        sendPI: PendingIntent,
        onError: (message: String) -> Unit
    ) {
        contacts.forEach { contactNumber ->
            println(">>>>>>>>>>>>> Sending Message.")
            try {
                withContext(smsDispatcher) {
                    smsManager?.sendTextMessage(
                        contactNumber.contactNumber,
                        null,
                        message,
                        sendPI,
                        null
                    )
                }
            } catch (e: Exception) {
                onError("Error sending SMS to ${contactNumber.contactNumber}: ${e.message}") // This Error Handling needs to be reviewed and changed Imp.
            }
        }
    }


    suspend fun favouriteNumber(): String? {
        val contactList = contactsDao.getAllContacts().firstOrNull()

        val favourite = contactList?.let { contacts ->
            contacts.firstOrNull { it.isFavourite }?.contactNumber ?: contacts.first().contactNumber
        }
        return favourite
    }

//    fun favouriteNumber(): Deferred<String> = CoroutineScope(Dispatchers.IO).async {
//        val contacts = contactsDao.getAllContacts().firstOrNull() ?: return@async DEFAULT_CONTACT_ENTITY.first().contactNumber
//        return@async contacts.firstOrNull { it.isFavourite }?.contactNumber ?: DEFAULT_CONTACT_ENTITY.first().contactNumber
//    }

    /*
    suspend fun favouriteNumber(): String {
        val contacts = contactsDao.getAllContacts().firstOrNull()
            ?: return DEFAULT_CONTACT_ENTITY.first().contactNumber

        return contacts.firstOrNull { it.isFavourite }?.contactNumber
            ?: DEFAULT_CONTACT_ENTITY.first().contactNumber
    }*/


}