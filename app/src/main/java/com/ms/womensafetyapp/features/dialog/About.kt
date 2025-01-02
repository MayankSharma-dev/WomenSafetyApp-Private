package com.ms.womensafetyapp.features.dialog

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import androidx.fragment.app.DialogFragment
import com.ms.womensafetyapp.R

class About : DialogFragment(R.layout.fragment_about_dialog) {
    companion object {
        const val TAG = "AboutDialog"
        fun newInstance(): About {
            return About()
        }
    }


    override fun getTheme(): Int {
        return R.style.AboutDialogTheme
    }

    override fun onResume() {
        super.onResume()
        val params: WindowManager.LayoutParams? = dialog?.window?.attributes
        params?.width = WindowManager.LayoutParams.WRAP_CONTENT
        params?.height = WindowManager.LayoutParams.WRAP_CONTENT
        dialog?.window?.attributes = params
        dialog?.apply {
            isCancelable = true
            setCanceledOnTouchOutside(true)
        }
        //dialog?.window?.decorView?.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val gmail: ImageView
        val playStore: ImageView
        view.apply {
            gmail = findViewById(R.id.gmail)
            playStore = findViewById(R.id.play_store)
        }

        gmail.setOnClickListener {
            val emailIntent = Intent(
                Intent.ACTION_SENDTO, Uri.fromParts(
                    "mailto", "sharma.mayank.9211@gmail.com", null
                )
            )
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Package Name Scripter Feedback.")
            requireContext().startActivity(Intent.createChooser(emailIntent, null))
        }

        playStore.setOnClickListener {
            try {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("market://details?id=" + "in.ms.packagenamesscripter")
                    )
                )
            } catch (anfe: ActivityNotFoundException) {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("http://play.google.com/store/apps/details?id=" + "in.ms.packagenamesscripter")
                    )
                )
            }
        }
    }
}