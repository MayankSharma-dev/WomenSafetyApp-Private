package com.ms.womensafetyapp.features.morecontainer.more.maps

import androidx.annotation.Keep
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
//import com.google.android.gms.maps.model.LatLng
import com.ms.womensafetyapp.database.LocationDatabase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

//@Keep
@HiltViewModel
class MapsViewModel @Inject constructor(val database: LocationDatabase): ViewModel() {

    private val locationDao = database.locationDao()

    val locations = locationDao.getLastTenLocationsFlow().
            stateIn(viewModelScope, SharingStarted.Lazily,null)

    val lastLocation = locationDao.getLastLocationFlow().
    stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000),null)

    /*
    fun convertIntoLatLang(locationEntities: List<LocationEntity>): List<LatLng>{
        val listOfLatLang = mutableListOf<LatLng>()
        for(i in locationEntities){
            listOfLatLang.add(LatLng(i.latitude,i.longitude))
        }
        return listOfLatLang
    }*/
}