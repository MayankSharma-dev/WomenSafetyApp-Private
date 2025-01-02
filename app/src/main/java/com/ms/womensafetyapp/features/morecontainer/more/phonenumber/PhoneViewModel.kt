package com.ms.womensafetyapp.features.morecontainer.more.phonenumber

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ms.womensafetyapp.database.ContactEntity
import com.ms.womensafetyapp.database.LocationDatabase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

//@Keep
@HiltViewModel
class PhoneViewModel @Inject constructor(locationDatabase: LocationDatabase): ViewModel() {

    private val contactDao = locationDatabase.contactDao()

    val contacts = contactDao.getAllContacts()
        .stateIn(viewModelScope, SharingStarted.Lazily,null)

    fun onInsertContact(contact: ContactEntity){
        viewModelScope.launch(Dispatchers.IO) {
            val contactsList = contactDao.getAllContactsList()
            val isDuplicate = contactsList.any { it.contactNumber == contact.contactNumber }
            if(!isDuplicate)
                contactDao.insertContact(contact)
        }
    }

    fun onUpdateFavourite(contact: ContactEntity){
        if (contact.isFavourite){
            viewModelScope.launch {
                contactDao.updateContact(ContactEntity(contact.contactName,contact.contactNumber,false))
            }
        }else{
            viewModelScope.launch {
                contactDao.updateFavouriteContact(contact.contactNumber)
            }
        }
    }

    /*
    // Original before Duplicate checking.
    fun onInsertContact(contact: ContactEntity){
        viewModelScope.launch {
            contactDao.insertContact(contact)
        }
    }
     */

    fun onUpdateContact(contact: ContactEntity){
        println(">>>>>>>>>>. Update Contact request.")
        viewModelScope.launch {
            contactDao.updateContact(contact)
        }
    }

    fun onDeleteContact(number: String){
        viewModelScope.launch {
            contactDao.deleteContact(number)
        }
    }

    fun onDeleteAllContacts(){
        viewModelScope.launch {
            contactDao.deleteAllContacts()
        }
    }

}