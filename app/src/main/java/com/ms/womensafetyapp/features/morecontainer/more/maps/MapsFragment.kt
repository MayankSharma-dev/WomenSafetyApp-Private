package com.ms.womensafetyapp.features.morecontainer.more.maps

/*
import android.content.Context.MODE_PRIVATE
import android.graphics.Rect
import android.location.GpsStatus
import android.os.Bundle
import android.view.View
import androidx.annotation.DrawableRes
import androidx.annotation.Keep
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.ms.womensafetyapp.R
import com.ms.womensafetyapp.database.LocationEntity
import com.ms.womensafetyapp.databinding.FragmentMapsBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker


//@Keep
@AndroidEntryPoint
class MapsFragment : Fragment(R.layout.fragment_maps),GpsStatus.Listener,MapListener {

    private var _binding: FragmentMapsBinding? = null
    private val binding: FragmentMapsBinding get() = _binding!!

    private val viewModel: MapsViewModel by viewModels()

    private lateinit var mMap: MapView
//    lateinit var controller: IMapController
//    lateinit var mMyLocationOverlay: MyLocationNewOverlay

    private lateinit var marker: Marker

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentMapsBinding.bind(view)

        Configuration.getInstance().load(
            requireContext().applicationContext,
            requireContext().getSharedPreferences(getString(R.string.app_name), MODE_PRIVATE)
        )
        mMap = binding.osmmap
        mMap.setTileSource(TileSourceFactory.MAPNIK)
        mMap.mapCenter
        mMap.controller.setZoom(14.0)
        mMap.setMultiTouchControls(true)
        mMap.getLocalVisibleRect(Rect())
        marker = Marker(mMap)


        //mMyLocationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(requireContext()), mMap)
//        controller = mMap.controller

//        mMyLocationOverlay.enableMyLocation()
//        mMyLocationOverlay.enableFollowLocation()
        //mMyLocationOverlay.isDrawAccuracyEnabled = true


//        mMyLocationOverlay.runOnFirstFix {
//
////            runOnUiThread {
////
////            }
//            lifecycleScope.launch(Dispatchers.Main) {
//                controller.setCenter(mMyLocationOverlay.myLocation);
//                controller.animateTo(mMyLocationOverlay.myLocation)
//            }
//        }
        // val mapPoint = GeoPoint(latitude, longitude)

//        Log.e("TAG", "onCreate:in ${controller.zoomIn()}")
//        Log.e("TAG", "onCreate: out  ${controller.zoomOut()}")
//        controller.setZoom(6.0)
        // controller.animateTo(mapPoint)
        //mMap.overlays.add(mMyLocationOverlay)

        mMap.addMapListener(this)


        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModel.lastLocation.collect{
                    if(it != null){
                        println(">>>>>>>>>>> newLoc OSP MAP")
                        //val geoPoint = GeoPoint(it.latitude,it.longitude)
                        addMarker(it)
                        }
                    }
                }
            }
        }



    private fun addMarker(locations: LocationEntity/*center: GeoPoint?*/) {
        //marker = Marker(mMap)
        try {
            val center = GeoPoint(locations.latitude,locations.longitude)
            marker.position = center
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            marker.icon = ContextCompat.getDrawable(requireContext(),R.drawable.baseline_my_location_24)
//        marker.setIcon(resources.getDrawable(com.ms.womensafetyapp.R.drawable.icone_gps))
            mMap.overlays.clear()
            mMap.controller.animateTo(center)
//        mMap.controller.setCenter(center)
            mMap.overlays.add(marker)
            mMap.invalidate()
        }
        catch (e: Exception){

        }
    }



    override fun onScroll(event: ScrollEvent?): Boolean {
        // event?.source?.getMapCenter()
//        Log.e("TAG", "onCreate:la ${event?.source?.getMapCenter()?.latitude}")
//        Log.e("TAG", "onCreate:lo ${event?.source?.getMapCenter()?.longitude}")
        //  Log.e("TAG", "onScroll   x: ${event?.x}  y: ${event?.y}", )
        return true
    }

    override fun onZoom(event: ZoomEvent?): Boolean {
        //  event?.zoomLevel?.let { controller.setZoom(it) }
        //Log.e("TAG", "onZoom zoom level: ${event?.zoomLevel}   source:  ${event?.source}")
        return false;
    }

    override fun onGpsStatusChanged(event: Int) {
    }
}*/