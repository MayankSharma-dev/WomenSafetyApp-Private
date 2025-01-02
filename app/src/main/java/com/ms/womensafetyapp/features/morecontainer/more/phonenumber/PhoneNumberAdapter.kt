package com.ms.womensafetyapp.features.morecontainer.more.phonenumber

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ms.womensafetyapp.R
import com.ms.womensafetyapp.database.ContactEntity
import com.ms.womensafetyapp.databinding.ItemLayoutBinding

//@Keep
class PhoneNumberAdapter(
    private val onItemEdit: (ContactEntity) -> Unit,
    private val onSelectFavourite: (ContactEntity) -> Unit,
    private val onItemDelete: (ContactEntity) -> Unit
) : ListAdapter<ContactEntity, PhoneNumberAdapter.PhoneNumberViewHolder>(PhoneNumberComparator()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhoneNumberViewHolder {
        val binding = ItemLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return  PhoneNumberViewHolder(binding,
            onItemEdit= {position ->
                val contact = getItem(position)
                if(contact!=null){
                    onItemEdit(contact)
                }
            },
            onFavouriteClicked = {position ->
                val contact = getItem(position)
                if(contact!=null){
                    onSelectFavourite(contact)
                }
            },
            onItemDelete = {position ->
                val contact = getItem(position)
                if(contact!=null){
                    onItemDelete(contact)
                }
            })
    }

    override fun onBindViewHolder(holder: PhoneNumberViewHolder, position: Int) {
       val currentItem = getItem(position)
        if(currentItem!=null){
            holder.bind(currentItem)
        }
    }

    inner class PhoneNumberViewHolder(
        private val binding: ItemLayoutBinding,
        private val onItemEdit: (Int) -> Unit,
        private val onFavouriteClicked: (Int) -> Unit,
        private val onItemDelete: (Int) -> Unit
    ) :
        RecyclerView.ViewHolder(binding.root) {

            fun bind(contactEntity: ContactEntity){
                binding.apply {
                    contactName.text = contactEntity.contactName
                    contactNumber.text = contactEntity.contactNumber.toString()
                    isFavourite.setImageResource(
                        when{
                            contactEntity.isFavourite -> R.drawable.baseline_star_24
                            else -> R.drawable.baseline_star_outline_24
                        }
                    )
                }
            }

        init {
            binding.apply {
                editContact.setOnClickListener {
                    val position = bindingAdapterPosition
                    if(position!= RecyclerView.NO_POSITION){
                        onItemEdit(position)
                    }
                }
                deleteContact.setOnClickListener {
                    val position = bindingAdapterPosition
                    if(position!= RecyclerView.NO_POSITION){
                        onItemDelete(position)
                    }
                }
                isFavourite.setOnClickListener {
                    val position = bindingAdapterPosition
                    if(position!=RecyclerView.NO_POSITION){
                        onFavouriteClicked(position)
                    }
                }
            }
        }
    }
}


class PhoneNumberComparator : DiffUtil.ItemCallback<ContactEntity>() {
    override fun areItemsTheSame(oldItem: ContactEntity, newItem: ContactEntity): Boolean {
        return oldItem.contactNumber == newItem.contactNumber
    }

    override fun areContentsTheSame(oldItem: ContactEntity, newItem: ContactEntity): Boolean {
        return oldItem == newItem
    }
}