package com.ms.womensafetyapp.preference

import androidx.annotation.Keep
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

//@Keep
class UserPreferencesStore @Inject constructor(private val dataStore: DataStore<Preferences>) {

    private val durationPreference = intPreferencesKey(INT_DURATIONS)
    private val emergencyNumberPreference = intPreferencesKey(INT_EMERGENCY_NUMBER)
    private val sendSMSPreference = booleanPreferencesKey(BOOLEAN_SEND_SMS)

    val isSendSMS: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[sendSMSPreference] ?: DEFAULT_SHOULD_SEND_SMS
    }


    suspend fun shouldSendSms(smsSend: Boolean){
        dataStore.edit { mutablePreferences ->
            mutablePreferences[sendSMSPreference] = smsSend
        }
    }

    val duration: Flow<Int> = dataStore.data.map { preferences ->
        preferences[durationPreference] ?: DEFAULT_DURATION
    }

    suspend fun setDuration(timeDuration: Int) {
        dataStore.edit { mutablePreferences ->
            mutablePreferences[durationPreference] = timeDuration
        }
    }

    val emergencyNumber: Flow<Int> = dataStore.data.map { preferences ->
        preferences[emergencyNumberPreference] ?: DEFAULT_EMERGENCY_NUMBER
    }

    suspend fun setEmergencyNumber(number: Int) {
        dataStore.edit { mutablePreferences ->
            mutablePreferences[emergencyNumberPreference] = number
        }
    }

    /*
    fun getCounterValue(context: Context): Int {
        return runBlocking {
            context.dataStore.data.map { preferences ->
                preferences[EXAMPLE_COUNTER] ?: 0
            }.first()
        }
    }*/

    companion object{
        const val INT_DURATIONS = "INT_DURATIONS"
        const val INT_EMERGENCY_NUMBER = "INT_EMERGENCY_NUMBER"
        const val BOOLEAN_SEND_SMS = "BOOLEAN_SEND_SMS"

        const val DEFAULT_DURATION: Int = 5
        const val DEFAULT_EMERGENCY_NUMBER: Int = 911 //112
        const val DEFAULT_SHOULD_SEND_SMS = false
    }

}
