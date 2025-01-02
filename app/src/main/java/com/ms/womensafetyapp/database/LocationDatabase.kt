package com.ms.womensafetyapp.database

import androidx.room.Database
import androidx.room.RoomDatabase

//@Keep
@Database(entities =
[LocationEntity::class, ContactEntity::class], version = 1)
abstract class LocationDatabase: RoomDatabase() {

    abstract fun locationDao(): LocationDao

    abstract fun contactDao(): ContactDao
}