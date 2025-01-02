package com.ms.womensafetyapp.features.morecontainer.more.locations

import androidx.lifecycle.ViewModel
import com.ms.womensafetyapp.database.LocationDatabase
import com.ms.womensafetyapp.database.LocationEntity
import com.ms.womensafetyapp.util.LocationInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import java.text.DateFormat
import javax.inject.Inject

//@Keep
@HiltViewModel
class LocationsViewModel @Inject constructor(private val locationsDatabase: LocationDatabase): ViewModel() {

    private val locationDao = locationsDatabase.locationDao()

    val allLocations = locationDao.getAllLocation()

    fun convertLocationsTime(list: List<LocationEntity>): List<LocationInfo>{
        val locationList = mutableListOf<LocationInfo>()
        for( i in list){
            locationList.add(LocationInfo(i.latitude,i.longitude,DateFormat.getDateTimeInstance().format(i.time)))
        }
        return locationList
    }

    suspend fun deleteAllLocations(){
        locationDao.deleteAllLocations()
    }
}