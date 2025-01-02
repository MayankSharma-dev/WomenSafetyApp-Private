package com.ms.womensafetyapp.features.home

import androidx.annotation.Keep
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ms.womensafetyapp.preference.UserPreferencesStore
import com.ms.womensafetyapp.service.EmergencyService
import com.ms.womensafetyapp.service.LocationService
import com.ms.womensafetyapp.util.ServiceTypes.EMERGENCY_SERVICE
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.launch
import javax.inject.Inject

//@Keep
@HiltViewModel
class HomeViewModel @Inject constructor(private val userPreferencesStore: UserPreferencesStore) :
    ViewModel() {

    // simple values to be always updated only by userStoreRepository.
    private var _isSwitchEnabled: Boolean = false
    private var _durationTime: Int = 10

    val isSwitchEnabled: Boolean
        get() = _isSwitchEnabled
    val durationTime: Int
        get() = _durationTime

    init {
        viewModelScope.launch {
            launch {
                userPreferencesStore.isSendSMS.collect(FlowCollector {
                    _isSwitchEnabled = it
                })
            }
            launch {
                userPreferencesStore.duration.collect {
                    _durationTime = it
                }
            }
        }
    }

    private val _showEmergencyCard: MutableLiveData<Boolean> = MutableLiveData<Boolean>(false)

    val showEmergencyCard: LiveData<Boolean> = _showEmergencyCard

    private val _showLocationCard: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>(false)
    }

    val showLocationCard: LiveData<Boolean> = _showLocationCard

    fun checkServiceRunning() {

        _showEmergencyCard.postValue(EmergencyService.isServiceCreated())
        _showLocationCard.postValue(LocationService.isServiceCreated())
        /*
        if (EmergencyService.isServiceCreated()) {
            _showEmergencyCard.postValue(true)
        } else {
            _showEmergencyCard.postValue(false)
        }
        if (LocationService.isServiceCreated()) {
            _showLocationCard.postValue(true)
        } else {
            _showLocationCard.postValue(false)
        }*/
    }

    fun updateServiceInfo(isRunning: Boolean, serviceName: Int) {
        if (serviceName == EMERGENCY_SERVICE) {
            _showEmergencyCard.postValue(isRunning)
        } else {
            _showLocationCard.postValue(isRunning)
        }
    }

    fun updateUserStoreDuration(value: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            userPreferencesStore.setDuration(value)
            //_durationTime = value
        }
    }

    fun updateUserStoreSendSms(value: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            userPreferencesStore.shouldSendSms(value)
            //_isSwitchEnabled = value
        }
    }
}