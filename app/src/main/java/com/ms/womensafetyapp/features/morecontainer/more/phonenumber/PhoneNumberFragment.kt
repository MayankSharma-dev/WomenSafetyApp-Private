package com.ms.womensafetyapp.features.morecontainer.more.phonenumber

import android.Manifest
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.ms.womensafetyapp.R
import com.ms.womensafetyapp.database.ContactEntity
import com.ms.womensafetyapp.databinding.FragmentPhoneNumberBinding
import com.ms.womensafetyapp.features.dialog.EditDialog
import com.ms.womensafetyapp.util.PermissionManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

//@Keep
@AndroidEntryPoint
class PhoneNumberFragment : Fragment(R.layout.fragment_phone_number) {


    private var _binding: FragmentPhoneNumberBinding? = null
    private val binding: FragmentPhoneNumberBinding get() = _binding!!

    private val viewModel: PhoneViewModel by viewModels()

    //private lateinit var phoneAdapter: PhoneNumberAdapter
    private lateinit var permissionManager: PermissionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        permissionManager = PermissionManager(this)

        childFragmentManager.setFragmentResultListener("requestKey", this) { _, bundle ->
            val name = bundle.getString("name")
            val number = bundle.getString("number")
            //val id = bundle.getInt("id")
            val isFavourite = bundle.getBoolean("isFavourite")
            println(">>>>>>>>>>>. bundle $name")
            println(">>>>>>>>>>>. bundle $number")
            //viewModel.onUpdateContact(ContactEntity(name!!,number!!,id))
            viewModel.onUpdateContact(ContactEntity(name!!, number!!, isFavourite))

        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentPhoneNumberBinding.bind(view)

        println(">>>>>>>>>> onViewCreated Phone Number.-")

        val phoneAdapter = PhoneNumberAdapter(onItemEdit = {
            println(">>>>>>>> onItemEdit")

            if (childFragmentManager.findFragmentByTag(EditDialog.TAG)?.isAdded != true) {
                //EditDialog.newInstance(it.contactName, it.contactNumber, it.id)
                EditDialog.newInstance(it.contactName, it.contactNumber, it.isFavourite)
                    .show(childFragmentManager, EditDialog.TAG)
            }

        }, onSelectFavourite = {
                               viewModel.onUpdateFavourite(it)
        },
            onItemDelete = {
                viewModel.onDeleteContact(it.contactNumber)
            })

        binding.apply {
            contactRecycler.apply {
                adapter = phoneAdapter
                setHasFixedSize(true)
            }

            viewLifecycleOwner.lifecycleScope.launch {
                viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                    viewModel.contacts.collectLatest {
                        if (it != null) {
                            if(it.isNotEmpty()){
                                emptyList.visibility = View.GONE
                                emptyAddContact.visibility = View.GONE
                            }
                            else{
                                emptyList.visibility = View.VISIBLE
                                emptyAddContact.visibility = View.VISIBLE
                            }
                            println(">>>>>>>>>>> new Contact collect")
                            phoneAdapter.submitList(it)
                        }
                    }
                }
            }

            addContact.setOnClickListener {
                permissionManager.checkPermissions(
                    Manifest.permission.READ_CONTACTS,
                    onPermissionsGranted = { isGranted, fromWhere ->
                        if (isGranted) {
                            println(">>>>>>>>>>>>>>>> $fromWhere  test click")
                            permissionManager.openContacts.launch(null)
                        }
                    },
                    onReceiveContactDetails = { isReceived, name, number ->
                        println(">>>>>>>>>>> Fragment $name, $number")
                        viewModel.onInsertContact(ContactEntity(name, number, false))
                    })
            }

            deleteAllContact.setOnClickListener {
                viewModel.onDeleteAllContacts()
            }

            emptyAddContact.setOnClickListener {
                permissionManager.checkPermissions(
                    Manifest.permission.READ_CONTACTS,
                    onPermissionsGranted = { isGranted, fromWhere ->
                        if (isGranted) {
                            println(">>>>>>>>>>>>>>>> $fromWhere  test click")
                            permissionManager.openContacts.launch(null)
                        }
                    },
                    onReceiveContactDetails = { isReceived, name, number ->
                        println(">>>>>>>>>>> Fragment $name, $number")
                        viewModel.onInsertContact(ContactEntity(name, number, false))
                    })
            }
        }

    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}