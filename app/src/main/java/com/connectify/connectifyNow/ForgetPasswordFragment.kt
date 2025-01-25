package com.connectify.connectifyNow

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.navigation.Navigation
import com.connectify.connectifyNow.databinding.FragmentForgetPasswordBinding
import com.connectify.connectifyNow.helpers.DialogHelper
import com.connectify.connectifyNow.viewModel.AuthViewModel

class ForgetPasswordFragment : BaseFragment() {

    private var _binding: FragmentForgetPasswordBinding? = null
    private val binding get() = _binding!!

    private lateinit var view: View
    private lateinit var userAuthViewModel: AuthViewModel
    private var backToSignIn: TextView? = null
    private var resetPassword: Button? = null
    private var email: TextView? = null
    private var emailLayout: View? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentForgetPasswordBinding.inflate(layoutInflater, container, false)
        view = binding.root
        userAuthViewModel = AuthViewModel()

        setEventListeners(view)

        return view
    }

    private fun resetPassword() {
        val emailGroup = view.findViewById<ConstraintLayout>(R.id.email)
        val email = emailGroup?.findViewById<TextView>(R.id.edit_text_field)?.text.toString()

        userAuthViewModel.resetPassword(email, onSuccess, onError)
    }

    private val onSuccess: () -> Unit = {
        val dialogHelper = DialogHelper("Hey you,", requireContext(), "We send you an email to recover your password. Please follow the instructions.")
        dialogHelper.showDialogMessage()
    }

    private val onError: (String?) -> Unit = {
        val dialogHelper = DialogHelper("Sorry,", requireContext(), it)
        dialogHelper.showDialogMessage()
    }

    private fun setEventListeners(view: View) {
        resetPassword = view.findViewById(R.id.reset_password)
        backToSignIn = view.findViewById(R.id.back_to_sign_in)
        emailLayout = view.findViewById(R.id.email)

        resetPassword?.setOnClickListener {
            resetPassword()
        }

        backToSignIn?.setOnClickListener {
            Navigation.findNavController(it)
                .navigate(R.id.action_forgetPasswordFragment_to_signInFragment)
        }

        if (emailLayout != null) {
            email = emailLayout!!.findViewById(R.id.edit_text_field)

            resetPassword?.apply {
                isEnabled = false
                alpha = 0.8f

                email?.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(
                        s: CharSequence?,
                        start: Int,
                        count: Int,
                        after: Int
                    ) {  }

                    override fun onTextChanged(
                        s: CharSequence?,
                        start: Int,
                        before: Int,
                        count: Int
                    ) {
                        val isValidEmail = android.util.Patterns.EMAIL_ADDRESS.matcher(s.toString()).matches()

                        resetPassword?.isEnabled = s?.isNotEmpty() == true && isValidEmail
                        resetPassword?.alpha = if (resetPassword?.isEnabled == true) 1.0f else 0.8f
                    }

                    override fun afterTextChanged(s: Editable?) {}
                })
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}

