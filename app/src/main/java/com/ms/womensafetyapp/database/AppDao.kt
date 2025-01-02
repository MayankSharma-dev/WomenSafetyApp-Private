package com.ms.womensafetyapp.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

//@Keep
@Dao
interface LocationDao {

    @Query("SELECT * FROM locations_table ORDER BY time DESC")
    fun getAllLocation(): Flow<List<LocationEntity>>

    @Query("SELECT * FROM locations_table ORDER BY time DESC LIMIT 10")
    fun getLastTenLocationsFlow(): Flow<List<LocationEntity>>

    @Query("SELECT * FROM locations_table ORDER BY time DESC LIMIT 1")
    fun getLastLocationFlow(): Flow<LocationEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocations(location: LocationEntity)

    @Query("DELETE FROM locations_table")
    suspend fun deleteAllLocations()

}

//@Keep
@Dao
interface ContactDao{

    @Query("SELECT * FROM contacts_table ORDER BY contactName")
    fun getAllContacts(): Flow<List<ContactEntity>>

    @Query("SELECT * FROM contacts_table ORDER BY contactName")
    suspend fun getAllContactsList(): List<ContactEntity>

    @Query("SELECT * FROM contacts_table WHERE isFavourite = 1 LIMIT 1")
    suspend fun getFavouriteContact(): ContactEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContact(contact: ContactEntity)

    @Query("DELETE FROM contacts_table WHERE contactNumber =:query ")
    suspend fun deleteContact(query: String)

    @Update
    suspend fun updateContact(contact: ContactEntity)

    @Query("UPDATE contacts_table SET isFavourite = CASE contactNumber WHEN :number THEN 1 ELSE 0 END")
    suspend fun updateFavouriteContact(number: String)

    @Query("DELETE FROM contacts_table")
    suspend fun deleteAllContacts()

}