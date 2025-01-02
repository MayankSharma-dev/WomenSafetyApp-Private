package com.ms.womensafetyapp.viewpager

import androidx.annotation.Keep
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.ms.womensafetyapp.features.morecontainer.MoreContainerFragment
import com.ms.womensafetyapp.features.home.HomeFragment

//@Keep
class ViewPagerAdapter(fragment: FragmentManager, lifecycle: Lifecycle) :
    FragmentStateAdapter(fragment, lifecycle) {
        private val numberOfFragments = 2
    override fun getItemCount(): Int = numberOfFragments

    override fun createFragment(position: Int): Fragment {
        var frag: Fragment = HomeFragment()
        when(position){
            0 -> frag = HomeFragment()
            1 -> frag = MoreContainerFragment()
        }
        return frag
    }
}