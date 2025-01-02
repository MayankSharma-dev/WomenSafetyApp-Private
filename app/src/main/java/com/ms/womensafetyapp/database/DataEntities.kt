package com.ms.womensafetyapp.database

import androidx.room.Entity
import androidx.room.PrimaryKey

//@Keep
@Entity(tableName = "locations_table")
data class LocationEntity(
    val latitude: Double,
    val longitude: Double,
    @PrimaryKey val time: Long = System.currentTimeMillis()
)

//@Keep
@Entity(tableName = "contacts_table")
data class ContactEntity(
    val contactName: String,
    @PrimaryKey val contactNumber: String,
    val isFavourite: Boolean
    //@PrimaryKey(autoGenerate = true)val id: Int = 0
)