package com.connectify.connectifyNow.helpers

import android.content.Context
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class DialogHelper(private val title: String, private val context: Context, private val message: String?) {

    fun showDialogMessage() {
        MaterialAlertDialogBuilder(context)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
}