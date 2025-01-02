package com.ms.womensafetyapp.features.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.NumberPicker
import androidx.annotation.Keep
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.google.android.material.button.MaterialButton
import com.google.android.material.materialswitch.MaterialSwitch
import com.ms.womensafetyapp.R
import com.ms.womensafetyapp.features.home.HomeViewModel

//@Keep
class TrackSettingsDialog : DialogFragment(R.layout.fragment_track_setting_dialog) {

    companion object {

        const val TAG = "TrackSettingsDialog"
        const val KEY_SWITCH = "KEY_SWITCH"
        const val KEY_NUMBER_PICKER = "KEY_NUMBER_PICKER"

        fun newInstance(isSwitch:Boolean, duration: Int = 5): TrackSettingsDialog {
            val args = Bundle()
            args.putBoolean(KEY_SWITCH,isSwitch)
            args.putInt(KEY_NUMBER_PICKER, duration)

            val fragment = TrackSettingsDialog()
            fragment.arguments = args
            return fragment
        }
    }

    private val viewModels:HomeViewModel by viewModels({requireParentFragment()})


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

    override fun getTheme(): Int {
        return R.style.AdvanceDialogTheme
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val numberPicker: NumberPicker = view.findViewById(R.id.number_picker)
        val switch: MaterialSwitch = view.findViewById(R.id.tracking_sms_switch)
        val saveButton: MaterialButton = view.findViewById(R.id.save_interval_btn)

        numberPicker.minValue = 5
        numberPicker.maxValue = 60

        numberPicker.value = arguments?.getInt(KEY_NUMBER_PICKER, 10) ?: 10

        var numberPickerValue = numberPicker.value

        val isSwitchEnabled = arguments?.getBoolean(KEY_SWITCH) ?: false

        switch.isChecked = isSwitchEnabled
        //numberPicker.isEnabled = isSwitchEnabled

        switch.setOnCheckedChangeListener { _, isChecked ->
            //numberPicker.isEnabled = isChecked
            //viewModels.updateSwitchStateLiveData(isChecked)
            viewModels.updateUserStoreSendSms(isChecked)
        }

        numberPicker.setOnValueChangedListener { picker, oldVal, newVal ->
            numberPickerValue = newVal
            //viewModels.updateDurationStateLiveData(newVal)
        }

        saveButton.setOnClickListener{
            //viewModels.updateDurationStateLiveData(numberPickerValue)
            viewModels.updateUserStoreDuration(numberPickerValue)
        }
    }

    /*
override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
): View? {

    val view = inflater.inflate(R.layout.fragment_track_setting_dialog, container, false)
    dialog?.apply {
        isCancelable = true
        setCanceledOnTouchOutside(true)
    }
    return view
}*/


}