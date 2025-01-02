package com.ms.womensafetyapp.features.morecontainer.more.locations

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.Keep
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ms.womensafetyapp.databinding.ItemLocationLayoutBinding
import com.ms.womensafetyapp.util.LocationInfo

//@Keep
class LocationsAdapter(val onLocationClicked: (LocationInfo) -> Unit) :
    ListAdapter<LocationInfo, LocationsAdapter.LocationsViewHolder>(LocationsComparator()) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocationsViewHolder {
        val binding =
            ItemLocationLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return LocationsViewHolder(binding, onItemClicked = { position ->
            val locationInfo = getItem(position)
            if (locationInfo != null) {
                onLocationClicked(locationInfo)
            }
        })
    }

    override fun onBindViewHolder(holder: LocationsViewHolder, position: Int) {
        val currentItem = getItem(position)
        if (currentItem != null) {
            holder.bind(currentItem)
        }
    }


    inner class LocationsViewHolder(
        private val binding: ItemLocationLayoutBinding,
        val onItemClicked: (Int) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(currentItem: LocationInfo) {
            binding.apply {
                val loc = "LatLang: ${currentItem.latitude} , ${currentItem.longitude}"
                latLang.text = loc
                val dateTime = "Time: ${currentItem.time}"
                time.text = dateTime
            }
        }

        init {
            binding.openLocation.setOnClickListener {
                val position = bindingAdapterPosition
                if(position != RecyclerView.NO_POSITION){
                    onItemClicked(position)
                }
            }
        }
    }
}

class LocationsComparator : DiffUtil.ItemCallback<LocationInfo>() {
    override fun areItemsTheSame(oldItem: LocationInfo, newItem: LocationInfo): Boolean {
        return oldItem.time == newItem.time
    }

    override fun areContentsTheSame(oldItem: LocationInfo, newItem: LocationInfo): Boolean {
        return oldItem == newItem
    }
}