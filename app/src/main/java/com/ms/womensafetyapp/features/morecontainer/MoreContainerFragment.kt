package com.ms.womensafetyapp.features.morecontainer

import android.os.Bundle
import android.view.View
import androidx.annotation.Keep
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import com.ms.womensafetyapp.R

//@Keep
class MoreContainerFragment : Fragment(R.layout.fragment_more_container) {

    override fun onPause() {
        super.onPause()
        println(">>>>>>>>>>>>>> onPause MoreContainerFrag")
    }

    override fun onResume() {
        super.onResume()
        println(">>>>>>>>>>>>>> onResume MoreContainerFrag")
    }

    override fun onStop() {
        super.onStop()
        println(">>>>>>>>>>>>>> onStop MoreContainerFrag")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        println(">>>>>>>>>>>>>> viewCreated MoreContainerFrag")

        val navHostFragment =
            childFragmentManager.findFragmentById(R.id.nav_host_container) as NavHostFragment
        val navController = navHostFragment.navController
        //navController.enableOnBackPressed(true)

        val appBarConfiguration = AppBarConfiguration(setOf(R.id.moreFragment))

        //NavigationUI.setupWithNavController(navController)
        (activity as? AppCompatActivity)?.setupActionBarWithNavController(
            navController,
            appBarConfiguration
        )

        //(activity as? AppCompatActivity)?.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        //setupActionBarWithNavController(navController, appBarConfiguration)

        /*
        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner) {
            println(">>>>>>>>>>>>>>>>>>>>> ! onBackPressedDispatcher")
            when (navController.currentBackStackEntry?.id?.equals(R.id.moreFragment)) {
                true -> {
                    println(">>>>>>>>>>> ! MoreFragments Back")
                    //activity?.supportFragmentManager.findFragmentById(R.id.)
                }

                false -> {
                    println(">>>>>>>>>>> ! false")

                    navController.navigateUp()
                }

                null -> {
                    println(">>>>>>>>>>> onHomeFragment Back")
                    activity?.finish()
                }

            }
        }*/
    }

}
