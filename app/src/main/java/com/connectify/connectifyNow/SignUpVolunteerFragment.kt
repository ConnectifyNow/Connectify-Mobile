package com.connectify.connectifyNow

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.connectify.connectifyNow.databinding.CustomInputFieldPasswordBinding
import com.connectify.connectifyNow.databinding.CustomInputFieldTextBinding
import com.connectify.connectifyNow.databinding.FragmentSignUpOrganizationBinding
import com.connectify.connectifyNow.databinding.FragmentSignUpVolunteerBinding
import com.connectify.connectifyNow.helpers.ActionBarHelpers
import com.connectify.connectifyNow.helpers.DialogHelper
import com.connectify.connectifyNow.helpers.DynamicTextHelper
import com.connectify.connectifyNow.helpers.ImageHelper
import com.connectify.connectifyNow.helpers.ImageUploadListener
import com.connectify.connectifyNow.helpers.ValidationHelper
import com.connectify.connectifyNow.helpers.navigate
import com.connectify.connectifyNow.models.Volunteer
import com.connectify.connectifyNow.viewModel.VolunteerViewModel


class SignUpVolunteerFragment : Fragment() {
    private var binding: FragmentSignUpVolunteerBinding? = null

    private lateinit var view: View
    private lateinit var signUpBtn: Button
    private lateinit var imageHelper: ImageHelper
    private lateinit var imageView: ImageView
    private lateinit var volunteer: Volunteer
    private lateinit var volunteerViewModel: VolunteerViewModel
    private lateinit var dynamicTextHelper: DynamicTextHelper
    private lateinit var loadingOverlay: LinearLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSignUpVolunteerBinding.inflate(layoutInflater, container, false)
        view = binding?.root as View
        dynamicTextHelper = DynamicTextHelper(view)

        loadingOverlay = view.findViewById(R.id.signup_volunteer_loading_overlay);
        loadingOverlay?.visibility = View.INVISIBLE

        setHints()
        setEventListeners()

        ActionBarHelpers.hideActionBarAndBottomNavigationView((requireActivity() as? AppCompatActivity))

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val backButton = view.findViewById<ImageView>(R.id.back_button)


        backButton.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }

    private fun setHints() {
        dynamicTextHelper.setTextViewText(R.id.email_volunteer, R.string.user_email_title)
        dynamicTextHelper.setTextViewText(R.id.password_volunteer, R.string.password)
        dynamicTextHelper.setTextViewText(R.id.institution, R.string.institution_title)
        dynamicTextHelper.setTextViewText(R.id.name_volunteer, R.string.user_name_title)
        dynamicTextHelper.setTextViewText(R.id.bio_volunteer, R.string.bio_title)
    }

    private fun setEventListeners() {
        signUpBtn = view.findViewById(R.id.sign_up_volunteer_btn)
        imageView = view.findViewById(R.id.volunteerImage)

        imageHelper = ImageHelper(this, imageView, object : ImageUploadListener {
            override fun onImageUploaded(imageUrl: String) {
                loadingOverlay.visibility = View.INVISIBLE
            }

            override fun onUploadFailed(error: String) {
                loadingOverlay?.visibility = View.INVISIBLE
                Toast.makeText(context, "Upload failed: $error", Toast.LENGTH_SHORT).show()
            }
        })

        imageHelper.setImageViewClickListener {
            loadingOverlay.visibility = View.VISIBLE
        }

        volunteerViewModel = VolunteerViewModel()

        signUpBtn.setOnClickListener {
            val email = binding?.emailVolunteer
            val password = binding?.passwordVolunteer
            val name = binding?.nameVolunteer
            val institution = binding?.institution
            val bio = binding?.bioVolunteer
            val logo = imageHelper.getImageUrl() ?: "test"
            if (email!=null && password!=null && name!=null && institution!=null && bio!=null && logo!=null && isValidInputs(
                    email,
                    password,
                    name,
                    institution,
                    bio
                )
            ) {
                val name = name.editTextField.text.toString()
                val email = email.editTextField.text.toString()
                val bio = bio.editTextField.text.toString()
                val password = password.editTextField.text.toString()
                val institution = institution.editTextField.text.toString()

                volunteer = Volunteer(
                    name = name,
                    email = email,
                    institution = institution,
                    image = logo,
                    bio = bio
                )

                volunteerViewModel.createUserAsVolunteer(email,password, onSuccess, onError)
            }
        }

    }

    private val onError: (String?) -> Unit = {
        val dialogHelper = DialogHelper("Sorry,", requireContext(), it)
        dialogHelper.showDialogMessage()
    }

    private val onSuccess: (String?) -> Unit = { userId ->
        userId?.let { id ->
            volunteerViewModel.addVolunteer(volunteer.copy(id = id))

            requireActivity().runOnUiThread {
                Toast.makeText(
                    requireContext(),
                    "Account created successfully",
                    Toast.LENGTH_SHORT
                ).show()
                view.navigate(R.id.action_signUpVolunteerFragment_to_signInFragment)
            }
        }
    }

    private fun isValidInputs(
        email: CustomInputFieldTextBinding,
        password: CustomInputFieldPasswordBinding,
        name: CustomInputFieldTextBinding,
        institution: CustomInputFieldTextBinding,
        bio: CustomInputFieldTextBinding
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

        validationResults.add(
            ValidationHelper.isValidString(name.editTextField.text.toString()).also { isValid ->
                ValidationHelper.handleValidationResult(isValid, name, requireContext())
            }
        )

        validationResults.add(
            ValidationHelper.isValidField(institution.editTextField.text.toString())
                .also { isValid ->
                    ValidationHelper.handleValidationResult(isValid, institution, requireContext())
                }
        )

        validationResults.add(
            ValidationHelper.isValidField(bio.editTextField.text.toString()).also { isValid ->
                ValidationHelper.handleValidationResult(isValid, bio, requireContext())
            }
        )

        return validationResults.all { it }
    }
}
