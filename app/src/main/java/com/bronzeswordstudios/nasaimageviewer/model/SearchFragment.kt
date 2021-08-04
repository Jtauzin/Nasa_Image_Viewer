package com.bronzeswordstudios.nasaimageviewer.model

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import com.bronzeswordstudios.nasaimageviewer.R

class SearchFragment : DialogFragment() {
    private lateinit var listener: SearchDialogListener
    private lateinit var searchString: String

    interface SearchDialogListener {
        fun onDialogPositiveClick(dialog: SearchFragment)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            listener = context as SearchDialogListener
        } catch (e: ClassCastException) {
            // The activity must implement the interface or throw an error
            throw ClassCastException(
                (context.toString() +
                        " must implement NoticeDialogListener")
            )
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            // Get the layout inflater
            val inflater = requireActivity().layoutInflater
            val searchView = inflater.inflate(R.layout.search_dialog, null)
            val searchText = searchView.findViewById<EditText>(R.id.search_input)

            // Inflate and set the layout for the dialog - from documentation
            // Pass null as the parent view because its going in the dialog layout - from documentation
            builder.setView(searchView)
                .setTitle(R.string.search)
                // Add action buttons
                .setPositiveButton(
                    R.string.submit
                ) { dialog, id ->
                    searchString = searchText.text.toString()
                    // link the listener to the interface here
                    listener.onDialogPositiveClick(this)
                }
                .setNegativeButton(
                    R.string.cancel
                ) { dialog, id ->
                    getDialog()?.cancel()
                }
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    fun getString(): String {
        return searchString
    }
}