package com.ms.womensafetyapp.features.morecontainer.more

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.ms.womensafetyapp.BuildConfig
import com.ms.womensafetyapp.R
import com.ms.womensafetyapp.database.LocationEntity
import com.ms.womensafetyapp.features.dialog.MapDialogFragment
import com.ms.womensafetyapp.features.morecontainer.more.locations.LocationsFragment
import com.ms.womensafetyapp.features.morecontainer.more.phonenumber.PhoneNumberFragment
import com.tomtom.sdk.location.GeoPoint
import com.tomtom.sdk.map.display.MapOptions
import com.tomtom.sdk.map.display.TomTomMap
import com.tomtom.sdk.map.display.camera.CameraOptions
import com.tomtom.sdk.map.display.image.ImageFactory
import com.tomtom.sdk.map.display.location.LocationMarkerOptions
import com.tomtom.sdk.map.display.marker.Label
import com.tomtom.sdk.map.display.marker.MarkerOptions
import com.tomtom.sdk.map.display.style.StandardStyles
import com.tomtom.sdk.map.display.style.StyleMode
import com.tomtom.sdk.map.display.ui.MapFragment
import com.tomtom.sdk.map.display.ui.MapView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

//@Keep
@AndroidEntryPoint
class MoreFragment : Fragment(R.layout.fragment_more) {

//    private var _binding: FragmentMoreBinding? = null
//    private val binding: FragmentMoreBinding get() = _binding!!

    private var phoneNumberFragment: PhoneNumberFragment? = null

    private var locationFragment: LocationsFragment? = null

    private var mapView: MapView? = null

    private var mapFragment: MapFragment? =null
    private var tomMap: TomTomMap? = null

    private val viewModel: MoreViewModel by viewModels()

    private var locationEntity: LocationEntity? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        println(">>>>>>>>>>>>>>>>>> onCreateView More Fragment")

//        mapView  = view?.findViewById(R.id.map_container)
//        mapView?.onCreate(savedInstanceState)


        val mapOptions =
            MapOptions(mapKey = BuildConfig.TOMTOM_API_KEY, mapStyle = StandardStyles.SATELLITE)


        mapFragment = childFragmentManager.findFragmentByTag(TAG_MAPS_FRAGMENT) as? MapFragment
            ?: MapFragment.newInstance(mapOptions = mapOptions)

        childFragmentManager.beginTransaction()
            .replace(R.id.map_container, mapFragment!!,TAG_MAPS_FRAGMENT)
            .commit()


        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //_binding = FragmentMoreBinding.bind(view)

        println(">>>>>>>>>>> onViewCreated More Fragment")

        //mapFragment?.onViewCreated(view,savedInstanceState)



        phoneNumberFragment =
            childFragmentManager.findFragmentById(R.id.phone_contact_fragment) as? PhoneNumberFragment
                ?: PhoneNumberFragment().also {
                    childFragmentManager.beginTransaction()
                        .add(R.id.phone_contact_fragment, it, TAG_PHONE_FRAGMENT)
                        .commit()
                }

        /*
        val mapOptions =
            MapOptions(mapKey = BuildConfig.TOMTOM_API_KEY, mapStyle = StandardStyles.SATELLITE)
        mapFragment = childFragmentManager.findFragmentByTag(TAG_MAPS_FRAGMENT) as? MapFragment
            ?: MapFragment.newInstance(mapOptions = mapOptions).also {
                println(">>>>>>>>>>>>>>> Second Map frag null")
                childFragmentManager.beginTransaction()
                    .replace(R.id.map_container, it, TAG_MAPS_FRAGMENT)
                    .commit()
            }
        */

        val locationMarkerOptions =
            LocationMarkerOptions(
                type = LocationMarkerOptions.Type.Pointer,
            )

        mapFragment?.getMapAsync { map ->
            map.enableLocationMarker(locationMarkerOptions)
            map.isZoomEnabled = true
            tomMap = map
        }

        /*
        mapView?.getMapAsync{ map ->
            map.setStyleMode(StyleMode.DARK)
            map.enableLocationMarker(locationMarkerOptions)
            map.isZoomEnabled = true
            tomMap = map
        }*/

        locationFragment =
            childFragmentManager.findFragmentById(R.id.locations_fragment) as? LocationsFragment
                ?: LocationsFragment().also {
                    childFragmentManager.beginTransaction()
                        .add(R.id.locations_fragment, it, TAG_LOCATION_FRAGMENT)
                        .commit()
                }


        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                viewModel.locations.collect {
                    println(">>>>>>>>>>>>> More Frag <Location Collected>: $it")
                    if (it == null)
                        return@collect
                    locationEntity = LocationEntity(it.latitude, it.longitude)
                    val loc = GeoPoint(it.latitude, it.longitude)
                    val markerOptions = MarkerOptions(
                        loc,
                        ImageFactory.fromResource(R.drawable.baseline_my_location_24),
                        label = Label("Your Location", textColor = Color.WHITE, outlineWidth = 1.2, outlineColor = Color.GRAY),
                        balloonText = "Your Location"
                    )

                    tomMap?.removeMarkers()
                    tomMap?.addMarker(markerOptions)
                    val cameraOptions =
                        CameraOptions(
                            position = loc,
                            zoom = 15.0,//18.0
                            tilt = 00.0,
                            rotation = 90.0,
                        )
                    tomMap?.moveCamera(cameraOptions)
                    //tomMap?.zoomToMarkers()
                }
            }
        }

        val showPhoneFrag: ImageButton = view.findViewById(R.id.show_phone_number)
        showPhoneFrag.setOnClickListener {
            findNavController().navigate(R.id.action_moreFragment_to_phoneNumberFragment)
        }

        val nearByStationMapFrag: Button = view.findViewById(R.id.nearby_station)
        nearByStationMapFrag.setOnClickListener {
            //findNavController().navigate(R.id.action_moreFragment_to_mapsFragment)

            if (childFragmentManager.findFragmentByTag(MapDialogFragment.TAG)?.isAdded != true) {
                //EditDialog.newInstance(it.contactName, it.contactNumber, it.id)
                MapDialogFragment.newInstance(
                    locationEntity?.latitude ?: 0.0,
                    locationEntity?.longitude ?: 0.0
                )
                    .show(childFragmentManager, MapDialogFragment.TAG)
            }
        }

        val showLocation: ImageButton = view.findViewById(R.id.show_location)
        showLocation.setOnClickListener {
            findNavController().navigate(R.id.action_moreFragment_to_locationsFragment)
        }

    }

    /*
    override fun onStart() {
        super.onStart()
        //mapView?.onStart()
        mapFragment?.onStart()
    }

    override fun onResume() {
        super.onResume()
        //mapView?.onResume()
        mapFragment?.onResume()
    }

    override fun onStop() {
        super.onStop()
        println(">>>>>>>>>>>>> MoreFragment Stopped")
        //mapView?.onStop()
        mapFragment?.onStop()
    }
    */


    override fun onDestroyView() {
        super.onDestroyView()
        tomMap = null
        //mapFragment?.onDestroyView()
    }


    /*
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        //mapView?.onSaveInstanceState(outState)
        mapFragment?.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        super.onDestroy()
        //mapView?.onDestroy()
        mapFragment?.onDestroy()
    }*/

}

private const val TAG_PHONE_FRAGMENT = "TAG_PHONE_FRAGMENT"
private const val TAG_MAPS_FRAGMENT = "TAG_MAPS_FRAGMENT"
private const val TAG_LOCATION_FRAGMENT = "TAG_LOCATION_FRAGMENT"