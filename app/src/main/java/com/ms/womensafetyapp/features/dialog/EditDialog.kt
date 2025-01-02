package com.ms.womensafetyapp.features.dialog


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import com.ms.womensafetyapp.R


//@Keep
class EditDialog : DialogFragment() {

    companion object {
        const val TAG = "EditDialog"
        private const val KEY_NAME = "KEY_NAME"
        private const val KEY_INT = "KEY_INT"
        private const val KEY_NUMBER = "KEY_NUMBER"
        private const val KEY_IS_FAVOURITE = "KEY_IS_FAVOURITE"

        fun newInstance(
            name: String, number: String, isFavourite: Boolean /*id: Int*/
        ): EditDialog {
            val args = Bundle()
            args.putString(KEY_NAME, name)
            //args.putInt(KEY_INT,id)
            args.putString(KEY_NUMBER, number)
            args.putBoolean(KEY_IS_FAVOURITE, isFavourite)

            val fragment = EditDialog()
            fragment.arguments = args
            return fragment
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, R.style.AboutDialogTheme)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_edit_dialog, container, false)
        dialog?.apply {
            isCancelable = true
            setCanceledOnTouchOutside(true)
        }
        return view
    }

//    override fun onResume() {
//        super.onResume()
//        val params: WindowManager.LayoutParams? = dialog?.window?.attributes
//        params?.width = WindowManager.LayoutParams.MATCH_PARENT
//        params?.height = WindowManager.LayoutParams.WRAP_CONTENT
//        dialog?.window?.attributes = params
//    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val save: ImageView
        val editName: EditText
        val editNumber: EditText


        view.apply {
            save = findViewById(R.id.edit_save)
            editName = findViewById(R.id.edit_name)
            editName.setText(arguments?.getString(KEY_NAME) ?: "")
            editNumber = findViewById(R.id.edit_number)
            editNumber.setText(arguments?.getString(KEY_NUMBER) ?: "")
            editNumber.isEnabled = false
        }

        save.setOnClickListener {
            val name = editName.getText().toString()
            //val number = editNumber.getText().toString()
            if (name.isNotEmpty()/* && number.isNotEmpty()*/) {
                val bundle = Bundle()
                bundle.putString("name", name)
                //bundle.putString("number", number)
                bundle.putString("number", arguments?.getString(KEY_NUMBER) ?: "")
                //bundle.putInt("id",arguments?.getInt(KEY_INT) ?: 0)
                bundle.putBoolean("isFavourite", arguments?.getBoolean(KEY_IS_FAVOURITE) ?: false)
                setFragmentResult("requestKey", bundle)
                dismiss()
            }
        }

    }
}