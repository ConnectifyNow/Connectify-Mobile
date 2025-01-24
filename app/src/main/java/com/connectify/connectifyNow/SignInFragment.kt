package com.connectify.connectifyNow

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation
import com.connectify.connectifyNow.databinding.CustomInputFieldPasswordBinding
import com.connectify.connectifyNow.databinding.CustomInputFieldTextBinding
import com.connectify.connectifyNow.databinding.FragmentSignInBinding
import com.connectify.connectifyNow.helpers.ValidationHelper
import com.connectify.connectifyNow.viewModel.AuthViewModel


class SignInFragment : BaseFragment() {
    private lateinit var view: View
    private var _binding: FragmentSignInBinding? = null
    private val binding get() = _binding!!

    private val userAuthViewModel: AuthViewModel by activityViewModels()
    private var errorMessage: TextView? = null

    private var emailLayout: View? = null
    private var passwordLayout: View? = null
    private var emailLabel: TextView? = null
    private var passwordLabel: TextView? = null
    private var forgetPassword: TextView? = null
    private var register: ConstraintLayout? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSignInBinding.inflate(layoutInflater, container, false)
        view = binding.root

        setUpDynamicTextFields()
        signInUser()
        setEventsListeners()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val backButton = view.findViewById<ImageView>(R.id.back_button)
        errorMessage = view.findViewById(R.id.login_error_attempt)
        backButton.setOnClickListener {
            Navigation.findNavController(it)
                .navigate(R.id.startPageFragment)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private fun setEventsListeners() {
        forgetPassword = view.findViewById(R.id.forget_password_link)
        forgetPassword?.setOnClickListener {
            Navigation.findNavController(it)
                .navigate(R.id.action_signInFragment_to_forgetPasswordFragment)
        }

        register = view.findViewById(R.id.register_group)
        register?.setOnClickListener {
            Navigation.findNavController(it)
                .navigate(R.id.action_signInFragment_to_pickUserTypeFragment)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setUpDynamicTextFields() {
        emailLayout = view.findViewById(R.id.email_log_in)
        passwordLayout = view.findViewById(R.id.password_log_in)

        emailLabel = emailLayout?.findViewById(R.id.edit_text_label)
        passwordLabel = passwordLayout?.findViewById(R.id.edit_text_label)

        emailLabel!!.text = "Email"
        passwordLabel!!.text = "Password"
    }

    private fun signInUser() {
        binding.signInButton.setOnClickListener {
            val email = binding.emailLogIn
            val password = binding.passwordLogIn
            if (isValidInputs(email, password)) {

                val emailTextField = email.editTextField.text.toString()
                val passwordTextField = password.editTextField.text.toString()

                userAuthViewModel.signInUser(emailTextField, passwordTextField, onSuccess, onError)
            }
        }
    }

    private val onSuccess: () -> Unit = {
        view.navigate(R.id.action_signInFragment_to_mapViewFragment)
    }

    private val onError: (String?) -> Unit = {
        errorMessage?.text = "One or more of the credentials you entered are incorrect. Please try again."
        errorMessage?.visibility = View.VISIBLE
    }

    private fun isValidInputs(
        email: CustomInputFieldTextBinding,
        password: CustomInputFieldPasswordBinding
    ): Boolean {
        val validationResults = mutableListOf<Boolean>()
        validationResults.add(
            ValidationHelper.isValidEmail(email.editTextField.text.toString()).also { isValid ->
                ValidationHelper.handleValidationResult(isValid, email, requireContext())
            }
        )

        validationResults.add(
            ValidationHelper.isValidPassword(password.editTextField.text.toString())
                .also { isValid ->
                    ValidationHelper.handleValidationResult(isValid, password, requireContext())
                }
        )

        return validationResults.all { it }
    }

}
