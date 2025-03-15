package com.connectify.connectifyNow.helpers

import android.annotation.SuppressLint
import android.content.Context
import android.text.Editable
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.viewbinding.ViewBinding
import com.connectify.connectifyNow.R
import com.connectify.connectifyNow.databinding.CustomInputFieldPasswordBinding
import com.connectify.connectifyNow.databinding.CustomInputFieldTextBinding

object ValidationHelper {

    fun handleValidationResult(
        isValid: Boolean,
        inputGroup: ViewBinding,
        context: Context
    ) {
        if (inputGroup is CustomInputFieldTextBinding) {
            if(inputGroup.editTextField.text.isNullOrEmpty()) {
                showTextError(inputGroup, context,"Required field")
            }
            else if (isValid) {
                showValidInput(inputGroup, context)
            } else {
                showTextError(inputGroup, context)
            }
        } else if (inputGroup is CustomInputFieldPasswordBinding) {
            if(inputGroup.editTextField.text.isNullOrEmpty()) {
                showTextError(inputGroup, context,"Required field")
            } else if (isValid) {
                showValidInput(inputGroup, context)
            } else {
                showTextError(inputGroup, context)
            }

        } else {
            throw IllegalArgumentException("Invalid inputGroup type")
        }
    }

    fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun isValidPassword(password: String): Boolean {
        if (password.length < 6)
            return false

        val letterPattern = Regex("[a-zA-Z]")
        val digitPattern = Regex("[0-9]")

        val containsLetter = letterPattern.containsMatchIn(password)
        val containsDigit = digitPattern.containsMatchIn(password)

        return containsLetter && containsDigit
    }


    fun isValidString(input: String): Boolean {
        val pattern = Regex("^[a-zA-Z,\\. ]+\$")
        val isLengthValid = input.length >= 3
        return pattern.matches(input) && isLengthValid && input.isNotBlank()

    }

    fun isValidField(input: String): Boolean {
        val isLengthValid = input.length >= 3
        return isLengthValid && input.isNotBlank()
    }


    fun isValidAddress(input: Editable): Boolean {
        return input.isNotEmpty()
    }

    private fun showValidInput(
        inputGroup: CustomInputFieldTextBinding,
        context: Context
    ) {

        inputGroup.errorMessage.visibility = View.INVISIBLE
        inputGroup.editTextLine.setBackgroundColor(
            ContextCompat.getColor(
                context,
                R.color.Light_Gray
            )
        )
        inputGroup.editTextLabel.setTextColor(
            ContextCompat.getColor(
                context,
                R.color.Dark_Gray
            )
        )
    }


    @SuppressLint("SetTextI18n")
    private fun showTextError(inputGroup: CustomInputFieldTextBinding, context: Context, s:String? = null ) {
        if(s.isNullOrEmpty()) {
            when (inputGroup.editTextLabel.text) {
                context.getString(R.string.email),context.getString(R.string.user_email_title) -> {
                    inputGroup.errorMessage.text = "Invalid Email Address"
                }
                context.getString(R.string.password) -> {
                    inputGroup.errorMessage.text = "Password must be at least 6 characters long and contain at least one letter and one number"
                }
                context.getString(R.string.organization_name_title),
                context.getString(R.string.user_name_title)
                    -> {
                    inputGroup.errorMessage.text = "Field must be at least 6 characters long and contain only letters"
                }
                context.getString(R.string.post_title),
                context.getString(R.string.bio_title),
                context.getString(R.string.institution_title),
                context.getString(R.string.post_description) -> {
                    inputGroup.errorMessage.text = "Field must be at least 3 characters long"
                }
                else -> {
                    inputGroup.errorMessage.text = "Invalid ${inputGroup.editTextLabel.text}"
                }
            }
        }
        else
            inputGroup.errorMessage.text = s;

        inputGroup.errorMessage.visibility = View.VISIBLE
        inputGroup.editTextLine.setBackgroundColor(
            ContextCompat.getColor(
                context,
                R.color.red
            )
        )
        inputGroup.editTextLabel.setTextColor(
            ContextCompat.getColor(
                context,
                R.color.Dark_Gray
            )
        )
    }

    /*password handling*/
    private fun showValidInput(
        inputGroup: CustomInputFieldPasswordBinding,
        context: Context
    ) {

        inputGroup.errorMessage.visibility = View.INVISIBLE
        inputGroup.editTextLine.setBackgroundColor(
            ContextCompat.getColor(
                context,
                R.color.Light_Gray
            )
        )
        inputGroup.editTextLabel.setTextColor(
            ContextCompat.getColor(
                context,
                R.color.Dark_Gray
            )
        )
    }


    @SuppressLint("SetTextI18n")
    private fun showTextError(inputGroup: CustomInputFieldPasswordBinding, context: Context, s:String? = null) {
        if(s.isNullOrEmpty()) {
            inputGroup.errorMessage.text = "Password must be at least 6 characters long and contain at least one letter and one number"
        }
        else
            inputGroup.errorMessage.text = s;


        inputGroup.errorMessage.visibility = View.VISIBLE
        inputGroup.editTextLine.setBackgroundColor(
            ContextCompat.getColor(
                context,
                R.color.red
            )
        )
        inputGroup.editTextLabel.setTextColor(
            ContextCompat.getColor(
                context,
                R.color.Dark_Gray
            )
        )
    }

    /*address validation*/
    fun handleValidationResult(
        isValid: Boolean,
        addEditTextLine: View,
        inputSuggestions: TextView,
        context: Context,

        ) {
        if (!isValid) {
            addEditTextLine.setBackgroundColor(
                ContextCompat.getColor(
                    context,
                    R.color.red
                )
            )
            inputSuggestions.setTextColor(
                ContextCompat.getColor(
                    context,
                    R.color.red
                )
            )
        } else {
            addEditTextLine.setBackgroundColor(
                ContextCompat.getColor(
                    context,
                    R.color.Light_Gray
                )
            )
            inputSuggestions.setTextColor(
                ContextCompat.getColor(
                    context,
                    R.color.Dark_Gray
                )
            )
        }
    }
}
