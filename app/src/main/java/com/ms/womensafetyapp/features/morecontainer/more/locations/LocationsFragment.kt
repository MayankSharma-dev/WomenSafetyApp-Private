package com.ms.womensafetyapp.features.morecontainer.more.locations

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.ms.womensafetyapp.R
import com.ms.womensafetyapp.databinding.FragmentLocationsBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

//@Keep
@AndroidEntryPoint
class LocationsFragment : Fragment(R.layout.fragment_locations) {

    private var _binding: FragmentLocationsBinding? = null
    private val binding: FragmentLocationsBinding get() = _binding!!

    private val viewModel: LocationsViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentLocationsBinding.bind(view)

        binding.apply {

            val locationsAdapter = LocationsAdapter(onLocationClicked = {
                //val locString = "http://maps.google.com/maps?q=loc:${it.latitude},${it.longitude}"
                val locString = "https://maps.google.com/?q=${it.latitude},${it.longitude}"
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(locString))
                startActivity(intent)
            })

            locationsRecycler.apply {
                adapter = locationsAdapter
                setHasFixedSize(true)
            }

            viewLifecycleOwner.lifecycleScope.launch {
                viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                    viewModel.allLocations.collect {
                        if (it.isNotEmpty()) {
                            emptyList.visibility = View.GONE
                            emptyLocation.visibility = View.GONE
                        } else {
                            emptyList.visibility = View.VISIBLE
                            emptyLocation.visibility = View.VISIBLE
                        }
                        locationsAdapter.submitList(viewModel.convertLocationsTime(it))
                    }
                }
            }

            deleteAllContact.setOnClickListener {
                lifecycleScope.launch {
                    viewModel.deleteAllLocations()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}