package com.ms.womensafetyapp.features.home

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.ms.womensafetyapp.MainActivity
import com.ms.womensafetyapp.R
import com.ms.womensafetyapp.databinding.FragmentHomeBinding
import com.ms.womensafetyapp.features.dialog.TrackSettingsDialog
import com.ms.womensafetyapp.service.EmergencyService
import com.ms.womensafetyapp.service.LocationService
import com.ms.womensafetyapp.service.LocationService.Companion.ACTION_EMERGENCY
import com.ms.womensafetyapp.service.LocationService.Companion.ACTION_START
import com.ms.womensafetyapp.service.LocationService.Companion.ACTION_STOP
import com.ms.womensafetyapp.util.ActivityToFragEvent
import com.ms.womensafetyapp.util.PermissionManager
import dagger.hilt.android.AndroidEntryPoint

//@Keep
@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home), ActivityToFragEvent {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels()

    private lateinit var permissionManager: PermissionManager
    //private var shortAnimationDuration: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        permissionManager = PermissionManager(this@HomeFragment)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // Setting Event Interface between MainActivity and Fragment.
        (activity as MainActivity).setOnActivityToFragmentInterface(this)

        return super.onCreateView(inflater, container, savedInstanceState)
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentHomeBinding.bind(view)

        println(">>>>>>>>>>>> onViewCreated HomeFragment")
        //shortAnimationDuration = resources.getInteger(android.R.integer.config_shortAnimTime)
        viewModel.showEmergencyCard.observe(viewLifecycleOwner, Observer {
            //crossFadeEmergencyText(it)
            if (it) binding.cardEmergency.visibility = View.VISIBLE
            else binding.cardEmergency.visibility = View.GONE
        })

        viewModel.showLocationCard.observe(viewLifecycleOwner, Observer {
            //crossFadeLocationText(it)
            if (it) binding.cardLocation.visibility = View.VISIBLE
            else binding.cardLocation.visibility = View.GONE
        })


        binding.apply {

            //infoCard.visibility = View.GONE

            sos.setOnClickListener {
                //ActionEvents.setActionType(ACTION_EMERGENCY)
                permissionManager.checkPermissions(Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.POST_NOTIFICATIONS,
                    Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.SEND_SMS,
                    Manifest.permission.CALL_PHONE,
                    Manifest.permission.READ_CONTACTS,
                    //Manifest.permission.WRITE_CONTACTS,
                    onPermissionsGranted = { isGranted, _ ->
                        if (isGranted) {

                            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                                permissionManager.checkPermissions(Manifest.permission.ACCESS_BACKGROUND_LOCATION)

                            val intent = Intent(requireActivity(), EmergencyService::class.java)
                            intent.action = ACTION_EMERGENCY
                            println(">>>>>> Intent sent to start Emergency service")
                            requireActivity().startForegroundService(intent)
                        }
                    })
            }

            stopEmergency.setOnClickListener {

                if (!EmergencyService.isServiceCreated()) return@setOnClickListener

                val intent = Intent(requireActivity(), EmergencyService::class.java)
                intent.action = ACTION_STOP
                requireActivity().startForegroundService(intent)
                println(">>>>>>>>>>> Emergency Service Stopped.")
            }

            start.setOnClickListener {
                //ActionEvents.setActionType(ACTION_START)
                permissionManager.checkPermissions(Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.POST_NOTIFICATIONS,
                    Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.READ_CONTACTS,
                    Manifest.permission.SEND_SMS,
                    Manifest.permission.CALL_PHONE,
                    onPermissionsGranted = { isGranted, _ ->
                        if (isGranted) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                permissionManager.checkPermissions(Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                                    onPermissionsGranted = { isAllowed, _ ->
                                        if (isAllowed) {
                                            if (!LocationService.isServiceCreated()) {
                                                val intent = Intent(
                                                    requireActivity(), LocationService::class.java
                                                )
                                                intent.action = ACTION_START
                                                requireActivity().startForegroundService(intent)
                                            }
                                        }
                                    })
                            } else {
                                if (!LocationService.isServiceCreated()) {
                                    val intent =
                                        Intent(requireActivity(), LocationService::class.java)
                                    intent.action = ACTION_START
                                    requireActivity().startForegroundService(intent)
                                }
                            }
                        }
                    })
            }

            stop.setOnClickListener {
                if (LocationService.isServiceCreated()) {
                    val intent = Intent(requireActivity(), LocationService::class.java)
                    intent.action = ACTION_STOP
                    requireActivity().startForegroundService(intent)
                }
            }

            trackSetting.setOnClickListener {
                if (childFragmentManager.findFragmentByTag(TrackSettingsDialog.TAG)?.isAdded != true) {
                    TrackSettingsDialog.newInstance(
                        viewModel.isSwitchEnabled, viewModel.durationTime
                    ).show(childFragmentManager, TrackSettingsDialog.TAG)
                }
            }

        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.checkServiceRunning()
        (activity as? MainActivity)?.title = "Women Safety Resume"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun eventReceivedServiceInfo(isServiceRunning: Boolean, serviceName: Int) {
        viewModel.updateServiceInfo(isServiceRunning, serviceName)
    }

    /*
    private fun crossFadeEmergencyText(shouldShow: Boolean) {
        binding.cardEmergency.apply {
            if (shouldShow) {
                if (visibility == View.VISIBLE) return
                alpha = 0f
                visibility = View.VISIBLE
                animate().alpha(1f).setDuration(shortAnimationDuration.toLong()).setListener(null)
            } else {
                if (visibility == View.GONE) return
                animate().alpha(0f).setDuration(shortAnimationDuration.toLong())
                    .setListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator) {
                            super.onAnimationEnd(animation)
                            visibility = View.GONE
                        }
                    })
            }
        }
    }


    private fun crossFadeLocationText(shouldShow: Boolean) {
        binding.cardLocation.apply {
            if (shouldShow) {
                if (visibility == View.VISIBLE) return
                alpha = 0f
                visibility = View.VISIBLE
                animate().alpha(1f).setDuration(shortAnimationDuration.toLong()).setListener(null)

            } else {
                if (visibility == View.GONE) return
                animate().alpha(0f).setDuration(shortAnimationDuration.toLong())
                    .setListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator) {
                            super.onAnimationEnd(animation)
                            visibility = View.GONE
                        }
                    })
            }
        }
    }
    */
}