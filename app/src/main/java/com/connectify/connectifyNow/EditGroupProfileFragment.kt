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
import com.connectify.connectifyNow.databinding.FragmentEditGroupProfileBinding
import com.connectify.connectifyNow.helpers.ActionBarHelpers
import com.connectify.connectifyNow.helpers.DynamicTextHelper
import com.connectify.connectifyNow.helpers.ImageHelper
import com.connectify.connectifyNow.helpers.ImageUploadListener
import com.connectify.connectifyNow.helpers.ValidationHelper
import com.connectify.connectifyNow.models.Volunteer
import com.connectify.connectifyNow.viewModel.AuthViewModel
import com.connectify.connectifyNow.viewModel.VolunteerViewModel
import com.squareup.picasso.Picasso

class EditGroupProfileFragment : Fragment() {
    private val userAuthViewModel: AuthViewModel by activityViewModels()
    private val volunteerViewModel: VolunteerViewModel by activityViewModels()

    private var binding: FragmentEditGroupProfileBinding? = null

    private lateinit var view: View
    private lateinit var saveBtn: Button
    private lateinit var groupViewModel: VolunteerViewModel
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
        binding = FragmentEditGroupProfileBinding.inflate(layoutInflater, container, false)
        view = binding?.root
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
                .navigate(R.id.action_editGroupProfileFragment_to_ProfileFragment)
        }

        return view
    }


    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }

    private fun setHints() {
        dynamicTextHelper.setTextViewText(R.id.group_institution, R.string.institution_title)
        dynamicTextHelper.setTextViewText(R.id.group_name, R.string.user_name_title)
        dynamicTextHelper.setTextViewText(R.id.group_bio, R.string.bio_title)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        ActionBarHelpers.showActionBarAndBottomNavigationView(requireActivity() as? AppCompatActivity)
        super.onCreate(savedInstanceState)
    }

    private fun setEventListeners() {
        saveBtn = view.findViewById(R.id.save_group_btn)

        groupViewModel = VolunteerViewModel()

        imageView = view.findViewById(R.id.volunteerImage)

        imageHelper = ImageHelper(this, imageView, object : ImageUploadListener {
            override fun onImageUploaded(imageUrl: String) {
                loadingOverlay?.visibility = View.INVISIBLE
            }
        })

        imageHelper.setImageViewClickListener {
            loadingOverlay?.visibility = View.VISIBLE
        }

        saveBtn.setOnClickListener {
            val groupName = binding.groupName
            val groupInstitution = binding.groupInstitution
            val groupBio = binding.groupBio


            if (isValidInputs(groupName, groupInstitution,groupBio)) {
                val name = groupName.editTextField.text.toString()
                val institution = groupInstitution.editTextField.text.toString()
                val bio = groupBio.editTextField.text.toString()

                val userId = userAuthViewModel.getUserId().toString()
                val updatedVolunteer = Volunteer(
                    id = userId,
                    name = name,
                    institution = institution,
                    bio = bio,
                    email = emailAddress,
                    image = imageHelper.getImageUrl() ?:profileImageUrl
                )

                groupViewModel.update(updatedVolunteer,updatedVolunteer.json,
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
        groupName: CustomInputFieldTextBinding,
        groupInstitution: CustomInputFieldTextBinding,
        groupBio: CustomInputFieldTextBinding
    ): Boolean {
        val validationResults = mutableListOf<Boolean>()
        validationResults.add(
            ValidationHelper.isValidString(groupName.editTextField.text.toString()).also { isValid ->
                ValidationHelper.handleValidationResult(isValid, groupName, requireContext())
            }
        )

        validationResults.add(
            ValidationHelper.isValidField(groupInstitution.editTextField.text.toString()).also { isValid ->
                ValidationHelper.handleValidationResult(isValid, groupInstitution, requireContext())
            }
        )
        validationResults.add(
            ValidationHelper.isValidField(groupBio.editTextField.text.toString()).also { isValid ->
                ValidationHelper.handleValidationResult(isValid, groupBio, requireContext())
            }
        )

        return validationResults.all { it }
    }

    private fun setUserData() {
        val userId = userAuthViewModel.getUserId().toString()
        volunteerViewModel.getVolunteer(userId) { volunteer ->
            binding.groupName.editTextField.setText(volunteer.name)
            binding.groupInstitution.editTextField.setText(volunteer.institution)
            binding.groupBio.editTextField.setText(volunteer.bio)

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