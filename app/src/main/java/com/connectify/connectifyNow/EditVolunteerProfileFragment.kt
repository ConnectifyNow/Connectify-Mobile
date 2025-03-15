package com.connectify.connectifyNow

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation
import com.connectify.connectifyNow.databinding.CustomInputFieldTextBinding
import com.connectify.connectifyNow.databinding.FragmentEditVolunteerProfileBinding
import com.connectify.connectifyNow.helpers.ActionBarHelpers
import com.connectify.connectifyNow.helpers.DynamicTextHelper
import com.connectify.connectifyNow.helpers.ImageHelper
import com.connectify.connectifyNow.helpers.ImageUploadListener
import com.connectify.connectifyNow.helpers.ValidationHelper
import com.connectify.connectifyNow.models.Volunteer
import com.connectify.connectifyNow.viewModel.AuthViewModel
import com.connectify.connectifyNow.viewModel.VolunteerViewModel
import com.squareup.picasso.Picasso

class EditVolunteerProfileFragment : Fragment() {
    private val userAuthViewModel: AuthViewModel by activityViewModels()
    private val volunteerViewModel: VolunteerViewModel by activityViewModels()

    private var binding: FragmentEditVolunteerProfileBinding? = null

    private lateinit var view: View
    private lateinit var saveBtn: Button
    private lateinit var dynamicTextHelper: DynamicTextHelper
    private lateinit var emailAddress: String

    private lateinit var profileImageUrl: String
    private lateinit var imageHelper: ImageHelper
    private lateinit var imageView: ImageView
    private  lateinit var loadingOverlay: LinearLayout

    private var profileImage: ImageView? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentEditVolunteerProfileBinding.inflate(layoutInflater, container, false)
        view = binding?.root as View

        dynamicTextHelper = DynamicTextHelper(view)


        loadingOverlay = view.findViewById(R.id.edit_image_loading_overlay);
        loadingOverlay?.visibility = View.INVISIBLE



        setHints()
        setEventListeners()
        setUserData()

        ActionBarHelpers.hideActionBarAndBottomNavigationView((requireActivity() as? AppCompatActivity))

        val backButton = view.findViewById<ImageView>(R.id.back_button)
        backButton.setOnClickListener {
            Navigation.findNavController(it)
                .navigate(R.id.action_editVolunteerProfileFragment_to_ProfileFragment)
        }

        return view
    }


    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }

    private fun setHints() {
        dynamicTextHelper.setTextViewText(R.id.volunteer_institution, R.string.institution_title)
        dynamicTextHelper.setTextViewText(R.id.volunteer_name, R.string.user_name_title)
        dynamicTextHelper.setTextViewText(R.id.volunteer_bio, R.string.bio_title)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        ActionBarHelpers.showActionBarAndBottomNavigationView(requireActivity() as? AppCompatActivity)
        super.onCreate(savedInstanceState)
    }

    private fun setEventListeners() {
        saveBtn = view.findViewById(R.id.save_volunteer_btn)

        imageView = view.findViewById(R.id.volunteerImage)

        imageHelper = ImageHelper(this, imageView, object : ImageUploadListener {
            override fun onImageUploaded(imageUrl: String) {
                loadingOverlay?.visibility = View.INVISIBLE
            }

            override fun onUploadFailed(error: String) {
                // Handle the error (e.g., show an error message or log it)
                loadingOverlay?.visibility = View.INVISIBLE
                // You can also show a message to the user here
                Toast.makeText(context, "Upload failed: $error", Toast.LENGTH_SHORT).show()
            }
        })

        imageHelper.setImageViewClickListener {
            loadingOverlay?.visibility = View.VISIBLE
        }

        saveBtn.setOnClickListener {
            val volunteerName = binding?.volunteerName
            val volunteerInstitution = binding?.volunteerInstitution
            val volunteerBio = binding?.volunteerBio


            if (volunteerName!= null && volunteerInstitution!= null && volunteerBio!= null&&  isValidInputs(volunteerName, volunteerInstitution,volunteerBio)) {
                val name = volunteerName.editTextField.text.toString()
                val institution = volunteerInstitution.editTextField.text.toString()
                val bio = volunteerBio.editTextField.text.toString()

                val userId = userAuthViewModel.getUserId().toString()
                val updatedVolunteer = Volunteer(
                    id = userId,
                    name = name,
                    institution = institution,
                    bio = bio,
                    email = emailAddress,
                    image = imageHelper.getImageUrl() ?:profileImageUrl
                )

                volunteerViewModel.update(updatedVolunteer,updatedVolunteer.json,
                    onSuccessCallBack = {
                        Toast.makeText(context, "The data has been successfully updated", Toast.LENGTH_SHORT).show()
                        Log.d("EditProfilePage", "Success in update")
                    },
                    onFailureCallBack = {
                        Toast.makeText(context, "An error occurred while updating the data, please try again", Toast.LENGTH_SHORT).show()
                        Log.d("EditProfilePage", "Error in update")
                    }
                )
            }
        }
    }

    private fun isValidInputs(
        volunteerName: CustomInputFieldTextBinding,
        volunteerInstitution: CustomInputFieldTextBinding,
        volunteerBio: CustomInputFieldTextBinding
    ): Boolean {
        val validationResults = mutableListOf<Boolean>()
        validationResults.add(
            ValidationHelper.isValidString(volunteerName.editTextField.text.toString()).also { isValid ->
                ValidationHelper.handleValidationResult(isValid, volunteerName, requireContext())
            }
        )

        validationResults.add(
            ValidationHelper.isValidField(volunteerInstitution.editTextField.text.toString()).also { isValid ->
                ValidationHelper.handleValidationResult(isValid, volunteerInstitution, requireContext())
            }
        )
        validationResults.add(
            ValidationHelper.isValidField(volunteerBio.editTextField.text.toString()).also { isValid ->
                ValidationHelper.handleValidationResult(isValid, volunteerBio, requireContext())
            }
        )

        return validationResults.all { it }
    }

    private fun setUserData() {
        val userId = userAuthViewModel.getUserId().toString()
        volunteerViewModel.getVolunteer(userId) { volunteer ->
            binding?.volunteerName?.editTextField?.setText(volunteer.name)
            binding?.volunteerInstitution?.editTextField?.setText(volunteer.institution)
            binding?.volunteerBio?.editTextField?.setText(volunteer.bio)

            emailAddress = volunteer.email
            profileImageUrl = volunteer.image

            profileImage = view.findViewById(R.id.volunteerImage)

            if (profileImage != null) {
                if (volunteer.image.isEmpty())
                    Picasso.get().load(R.drawable.user_avatar).into(profileImage)
                else
                    Picasso.get().load(volunteer.image).into(profileImage)
            }
        }
    }
}