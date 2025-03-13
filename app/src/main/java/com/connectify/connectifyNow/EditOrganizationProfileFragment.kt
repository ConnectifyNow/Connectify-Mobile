package com.connectify.connectifyNow

import android.net.Uri
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

    private var _binding: FragmentEditOrganizationProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var view: View
    private lateinit var saveBtn: Button
    private lateinit var oragnizationViewModel: OrganizationViewModel
    private lateinit var dynamicTextHelper: DynamicTextHelper
    private lateinit var email_address: String
    private lateinit var oragnizationLocation: OrganizationLocation

    private lateinit var profileImageUrl: String
    private lateinit var imageHelper: ImageHelper
    private lateinit var imageView: ImageView
    private  lateinit var loadingOverlay: LinearLayout

    private var profileImage: ImageView? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentEditOrganizationProfileBinding.inflate(layoutInflater, container, false)
        view = binding.root
        dynamicTextHelper = DynamicTextHelper(view)


        loadingOverlay = view.findViewById(R.id.edit_image_loading_overlay);
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
        _binding = null
    }

    private fun setHints() {
        dynamicTextHelper.setTextViewText(R.id.oragnization_name, R.string.organization_name_title)
        dynamicTextHelper.setTextViewText(R.id.oragnization_bio, R.string.bio_title)
    }

    private fun setEventListeners() {
        saveBtn = view.findViewById(R.id.save_group_btn)

        oragnizationViewModel = OrganizationViewModel()

        //allow update image
        imageView = view.findViewById(R.id.oragnizationImage)

        imageHelper = ImageHelper(this, imageView, object : ImageUploadListener {
            override fun onImageUploaded(imageUrl: String) {
                loadingOverlay.visibility = View.INVISIBLE
            }

            override fun onUploadFailed(error: String) {
                TODO("Not yet implemented")
            }
        })

        imageHelper.setImageViewClickListener {
            loadingOverlay.visibility = View.VISIBLE
        }

        saveBtn.setOnClickListener {
            val oragnizationName = binding.oragnizationName
            val oragnizationBio = binding.oragnizationBio

            if (isValidInputs(oragnizationName,oragnizationBio)) {
                val name = oragnizationName.editTextField.text.toString()
                val bio = oragnizationBio.editTextField.text.toString()

                val updatedoragnization = Organization(
                    id = userAuthViewModel.getUserId().toString(),
                    name = name,
                    location = oragnizationLocation,
                    bio = bio,
                    email = email_address,
                    logo = imageHelper.getImageUrl() ?:profileImageUrl
                )

                oragnizationViewModel.update(updatedoragnization,updatedoragnization.json,
                    onSuccessCallBack = {
                        Toast.makeText(context, "oragnization Details have been updated successfully", Toast.LENGTH_SHORT).show()
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
        oragnizationName: CustomInputFieldTextBinding,
        bioGroup: CustomInputFieldTextBinding
    ): Boolean {
        val validationResults = mutableListOf<Boolean>()


        validationResults.add(
            ValidationHelper.isValidString(oragnizationName.editTextField.text.toString())
                .also { isValid ->
                    ValidationHelper.handleValidationResult(isValid, oragnizationName, requireContext())
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
        compViewModel.getOrganization(userId) { oragnization ->
            binding.oragnizationName.editTextField.setText(oragnization.name)
            binding.oragnizationBio.editTextField.setText(oragnization.bio)
            email_address = oragnization.email
            oragnizationLocation = oragnization.location
            profileImageUrl = oragnization.logo


            profileImage = view.findViewById(R.id.oragnizationImage)

            if (profileImage != null) {
                if (oragnization.logo.isEmpty())
                    Picasso.get().load(R.drawable.user_avatar).into(profileImage)
                else
                    Picasso.get().load(oragnization.logo).into(profileImage)
            }

        }
    }
}