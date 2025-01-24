package com.connectify.connectifyNow.helpers

import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.connectify.connectifyNow.R

class DynamicTextHelper(private val view: View) {
    fun setTextViewText(parentId: Int, stringResourceId: Int): TextView {
        val parentGroup = view.findViewById<ConstraintLayout>(parentId)
        val textView = parentGroup.findViewById<TextView>(R.id.edit_text_label)
        textView.text = view.context.getString(stringResourceId)
        return textView
    }

    fun setHintForEditText(editTextGroupId: Int, hintResourceId: Int? = null, inputTitleResourceId: Int? = null) {
        val editTextGroup = view.findViewById<View>(editTextGroupId)
        val editTextLabel = editTextGroup?.findViewById<TextView>(R.id.edit_text_label)
        val editTextField = editTextGroup?.findViewById<EditText>(R.id.edit_text_field)

        inputTitleResourceId?.let {
            editTextLabel?.text = view.context.getString(it)
        }

        hintResourceId?.let {
            editTextField?.hint = view.context.getString(it)
        }
    }
}