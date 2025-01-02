package com.ms.womensafetyapp.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.room.Room
import com.google.android.gms.location.LocationServices
import com.ms.womensafetyapp.database.LocationDatabase
import com.ms.womensafetyapp.location.DefaultLocationClient
import com.ms.womensafetyapp.location.NetworkLocationClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

// Preference DataStore Name
private const val USER_PREFERENCES = "user_preferences"

//@Keep
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun providesDefaultLocationClient(@ApplicationContext appContext: Context): DefaultLocationClient =
        DefaultLocationClient(
            appContext,
            LocationServices.getFusedLocationProviderClient(appContext)
        )

    @Singleton
    @Provides
    fun providesNetworkLocationClient(@ApplicationContext appContext: Context): NetworkLocationClient =
        NetworkLocationClient(appContext)

    @Provides
    @Singleton
    fun providesDatabase(@ApplicationContext appContext: Context): LocationDatabase =
        Room.databaseBuilder(appContext, LocationDatabase::class.java, "database")
            .fallbackToDestructiveMigration()
            .build()


    @Singleton
    @Provides
    fun providesPreferencesDataStore(@ApplicationContext appCtx: Context): DataStore<Preferences> =
        PreferenceDataStoreFactory.create(
//            corruptionHandler = ReplaceFileCorruptionHandler(
//                produceNewData = { emptyPreferences() }
//            ),
            produceFile = { appCtx.preferencesDataStoreFile(USER_PREFERENCES) },
            scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        )

}