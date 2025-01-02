package com.ms.womensafetyapp.features.morecontainer.more

import androidx.annotation.Keep
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ms.womensafetyapp.database.LocationDatabase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

//@Keep
@HiltViewModel
class MoreViewModel @Inject constructor(val database: LocationDatabase) : ViewModel() {

    private val locationsDao = database.locationDao()

    val locations = locationsDao.getLastLocationFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000),null)

}