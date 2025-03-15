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
import com.connectify.connectifyNow.databinding.FragmentEditOrganizationProfileBinding
import com.connectify.connectifyNow.helpers.ActionBarHelpers
import com.connectify.connectifyNow.helpers.DynamicTextHelper
import com.connectify.connectifyNow.helpers.ImageHelper
import com.connectify.connectifyNow.helpers.ImageUploadListener
import com.connectify.connectifyNow.helpers.ValidationHelper
import com.connectify.connectifyNow.models.Organization
import com.connectify.connectifyNow.models.OrganizationLocation
import com.connectify.connectifyNow.viewModel.OrganizationViewModel
import com.connectify.connectifyNow.viewModel.AuthViewModel
import com.squareup.picasso.Picasso

class EditOrganizationProfileFragment : Fragment() {
    
    private val userAuthViewModel: AuthViewModel by activityViewModels()
    private val compViewModel: OrganizationViewModel by activityViewModels()

    private var binding: FragmentEditOrganizationProfileBinding? = null

    private lateinit var view: View
    private lateinit var saveBtn: Button
    private lateinit var organizationViewModel: OrganizationViewModel
    private lateinit var dynamicTextHelper: DynamicTextHelper
    private lateinit var emailAddress: String
    private lateinit var organizationLocation: OrganizationLocation

    private lateinit var profileImageUrl: String
    private lateinit var imageHelper: ImageHelper
    private lateinit var imageView: ImageView
    private  lateinit var loadingOverlay: LinearLayout

    private var profileImage: ImageView? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentEditOrganizationProfileBinding.inflate(layoutInflater, container, false)
        view = binding?.root as View
        dynamicTextHelper = DynamicTextHelper(view)


        loadingOverlay = view.findViewById(R.id.edit_image_loading_overlay)
        loadingOverlay.visibility = View.INVISIBLE

        setHints()
        setUserData()
        setEventListeners()

        ActionBarHelpers.hideActionBarAndBottomNavigationView((requireActivity() as? AppCompatActivity))

        val backButton = view.findViewById<ImageView>(R.id.back_button)
        backButton.setOnClickListener {
            Navigation.findNavController(it)
                .navigate(R.id.action_editOrganizationProfileFragment_to_ProfileFragment)
        }

        return view
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        ActionBarHelpers.showActionBarAndBottomNavigationView(requireActivity() as? AppCompatActivity)
        super.onCreate(savedInstanceState)
    }
    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }

    private fun setHints() {
        dynamicTextHelper.setTextViewText(R.id.organization_name, R.string.organization_name_title)
        dynamicTextHelper.setTextViewText(R.id.organization_bio, R.string.bio_title)
    }

    private fun setEventListeners() {
        saveBtn = view.findViewById(R.id.save_group_btn)

        organizationViewModel = OrganizationViewModel()

        imageView = view.findViewById(R.id.organizationImage)

        imageHelper = ImageHelper(this, imageView, object : ImageUploadListener {
            override fun onImageUploaded(imageUrl: String) {
                loadingOverlay.visibility = View.INVISIBLE
            }

            override fun onUploadFailed(error: String) {
                loadingOverlay.visibility = View.INVISIBLE
                Toast.makeText(context, "Upload failed: $error", Toast.LENGTH_SHORT).show()
            }
        })

        imageHelper.setImageViewClickListener {
            loadingOverlay.visibility = View.VISIBLE
        }

        saveBtn.setOnClickListener {
            val organizationName = binding?.organizationName
            val organizationBio = binding?.organizationBio

            if (organizationName != null && organizationBio != null && isValidInputs(organizationName,organizationBio)) {
                val name = organizationName.editTextField.text.toString()
                val bio = organizationBio.editTextField.text.toString()

                val updatedOrganization = Organization(
                    id = userAuthViewModel.getUserId().toString(),
                    name = name,
                    location = organizationLocation,
                    bio = bio,
                    email = emailAddress,
                    logo = imageHelper.getImageUrl() ?:profileImageUrl
                )

                organizationViewModel.update(updatedOrganization,updatedOrganization.json,
                    onSuccessCallBack = {
                        Toast.makeText(context, "organization Details have been updated successfully", Toast.LENGTH_SHORT).show()
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
        organizationName: CustomInputFieldTextBinding,
        bioGroup: CustomInputFieldTextBinding
    ): Boolean {
        val validationResults = mutableListOf<Boolean>()


        validationResults.add(
            ValidationHelper.isValidString(organizationName.editTextField.text.toString())
                .also { isValid ->
                    ValidationHelper.handleValidationResult(isValid, organizationName, requireContext())
                }
        )

        validationResults.add(
            ValidationHelper.isValidField(bioGroup.editTextField.text.toString())
                .also { isValid ->
                    ValidationHelper.handleValidationResult(isValid, bioGroup, requireContext())
                }
        )

        return validationResults.all { it }
    }


    private fun setUserData() {
        val userId = userAuthViewModel.getUserId().toString()
        compViewModel.getOrganization(userId) { organization ->
            binding?.organizationName?.editTextField?.setText(organization.name)
            binding?.organizationBio?.editTextField?.setText(organization.bio)
            emailAddress = organization.email
            organizationLocation = organization.location
            profileImageUrl = organization.logo


            profileImage = view.findViewById(R.id.organizationImage)

            if (profileImage != null) {
                if (organization.logo.isEmpty())
                    Picasso.get().load(R.drawable.user_avatar).into(profileImage)
                else
                    Picasso.get().load(organization.logo).into(profileImage)
            }

        }
    }
}