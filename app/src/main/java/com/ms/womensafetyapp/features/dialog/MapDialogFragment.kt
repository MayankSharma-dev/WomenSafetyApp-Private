package com.ms.womensafetyapp.features.dialog

import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.Keep

import androidx.fragment.app.DialogFragment
import com.ms.womensafetyapp.BuildConfig
import com.ms.womensafetyapp.R
import com.tomtom.sdk.location.GeoPoint
import com.tomtom.sdk.map.display.MapOptions
import com.tomtom.sdk.map.display.TomTomMap
import com.tomtom.sdk.map.display.camera.CameraOptions
import com.tomtom.sdk.map.display.image.ImageFactory
import com.tomtom.sdk.map.display.location.LocationMarkerOptions
import com.tomtom.sdk.map.display.marker.Label
import com.tomtom.sdk.map.display.marker.Marker
import com.tomtom.sdk.map.display.marker.MarkerClickListener
import com.tomtom.sdk.map.display.marker.MarkerOptions
import com.tomtom.sdk.map.display.style.StandardStyles
import com.tomtom.sdk.map.display.ui.MapFragment
import com.tomtom.sdk.search.SearchCallback
import com.tomtom.sdk.search.SearchOptions
import com.tomtom.sdk.search.SearchResponse
import com.tomtom.sdk.search.common.error.SearchFailure
import com.tomtom.sdk.search.online.OnlineSearch
import kotlinx.coroutines.launch
import java.util.concurrent.Executors

//@Keep
class MapDialogFragment : DialogFragment(){

    companion object {
        const val TAG = "MapDialog"
        const val TAG_MAPS_DIALOG_FRAGMENT = "TAG_MAPS_DIALOG_FRAGMENT"
        private const val KEY_LAT = "KEY_LAT"
        private const val KEY_LANG = "KEY_LANG"

        fun newInstance(
           lat: Double, lang: Double
        ): MapDialogFragment {
            val args = Bundle()
            args.putDouble(KEY_LAT, lat)
            //args.putInt(KEY_INT,id)
            args.putDouble(KEY_LANG, lang)

            val fragment = MapDialogFragment()
            fragment.arguments = args
            return fragment
        }
    }

    private var mapFragment: MapFragment? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_full_screen_dialog, container, false)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

//        val dialog = super.onCreateDialog(savedInstanceState)
//        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
//        return dialog
        return object : Dialog(requireActivity(), theme) {
            @Deprecated("Deprecated in Java")
            override fun onBackPressed() {
                // Handle back press in the dialog, e.g., dismiss the dialog
                println(">>>>>>>>>> Dialog back press")
                dismiss()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mapOptions =
            MapOptions(mapKey = BuildConfig.TOMTOM_API_KEY, mapStyle = StandardStyles.SATELLITE)

        //mapFragment = childFragmentManager.findFragmentByTag(TAG_MAPS_FRAGMENT) as? MapFragment

        mapFragment =
            childFragmentManager.findFragmentByTag(TAG_MAPS_DIALOG_FRAGMENT) as? MapFragment
                ?: MapFragment.newInstance(mapOptions = mapOptions).also {
                    println(">>>>>>>>>>>>>>> Second Map DIALOG frag null")
                    childFragmentManager.beginTransaction()
                        .replace(R.id.map_container_dialog, it, TAG_MAPS_DIALOG_FRAGMENT)
                        .commit()
                }

        val locationMarkerOptions =
            LocationMarkerOptions(
                type = LocationMarkerOptions.Type.Pointer,
            )

        mapFragment?.getMapAsync { map ->
            map.enableLocationMarker(locationMarkerOptions)
            map.isZoomEnabled = true

            val lat = arguments?.getDouble(KEY_LAT) ?: 0.0
            val lang = arguments?.getDouble(KEY_LANG) ?: 0.0


            if (lat == 0.0){
                println(">>>>>>>>>>>>.... Location empty")
                println(">>>>>>>>>>>>.... Location empty")
                return@getMapAsync
            }

            println(">>>>>>>> Maps lat: $lat")
            println(">>>>>>>> Maps lang: $lang")

            val loc = GeoPoint(lat,lang)
            val markerOptions = MarkerOptions(
                loc,
                ImageFactory.fromResource(R.drawable.baseline_my_location_24),
                balloonText = "Your Location",
                label = Label("Your Location", textColor = Color.WHITE, outlineWidth = 1.2, outlineColor = Color.GRAY)
            )

            //map.removeMarkers()
            map.addMarker(markerOptions)
            val cameraOptions =
                CameraOptions(
                    position = loc,
                    zoom = 15.0,
                    tilt = 00.0,
                    rotation = 90.0,
                )

            map.moveCamera(cameraOptions)

            searchLocation(map,loc)

            map.addMarkerClickListener(object: MarkerClickListener{
                override fun onMarkerClicked(marker: Marker) {
                    if (!marker.isSelected()) {
                        marker.select()
                    }else{
                        marker.deselect()
                    }
                }
            })

        }

    }

    private fun searchLocation(map: TomTomMap, geoPoint: GeoPoint){

        val searchApi = OnlineSearch.create(requireContext(), BuildConfig.TOMTOM_API_KEY)

        val searchOption =  SearchOptions(
            query = "Police",
            geoBias = geoPoint,
            limit = 5
        )

        searchApi.search(options = searchOption, object: SearchCallback{
            override fun onFailure(failure: SearchFailure) {
                println(">>>>>>>>>>>>>>>>>>>. Map Error: Enable to Find Location")
                println(">>>>>>> Error: ${failure.message}")
            }

            override fun onSuccess(result: SearchResponse) {
                println(">>>>>>>>>>>>>>> OnSearch Success")
                val results = result.results
                results.forEach { places ->

                    val markerOptions = MarkerOptions(
                        places.place.coordinate,
                        ImageFactory.fromResource(R.drawable.baseline_local_pd_24),
                        balloonText = "Police Station",
                        label = Label("Police Station", textColor = Color.WHITE, outlineWidth = 1.2, outlineColor = Color.GRAY)

                    )
                    try {
                        if(isAdded && isVisible)
                            map.addMarker(markerOptions)
                    }catch (e: Exception){
                        e.printStackTrace()
                    }
                }
            }
        })
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_FRAME, R.style.FullScreenDialogStyle)
    }
}