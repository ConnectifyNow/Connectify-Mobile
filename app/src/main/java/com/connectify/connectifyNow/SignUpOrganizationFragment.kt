package com.connectify.connectifyNow

import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.navigation.fragment.findNavController
import com.connectify.connectifyNow.databinding.CustomInputFieldPasswordBinding
import com.connectify.connectifyNow.databinding.CustomInputFieldTextBinding
import com.connectify.connectifyNow.databinding.FragmentSignUpOrganizationBinding
import com.connectify.connectifyNow.helpers.DialogHelper
import com.connectify.connectifyNow.helpers.DynamicTextHelper
import com.connectify.connectifyNow.helpers.ImageHelper
import com.connectify.connectifyNow.helpers.ImageUploadListener
import com.connectify.connectifyNow.helpers.ValidationHelper
import com.connectify.connectifyNow.models.Location
import com.connectify.connectifyNow.models.Organization
import com.connectify.connectifyNow.models.OrganizationLocation
import com.connectify.connectifyNow.services.LocationsApiCall
import com.connectify.connectifyNow.viewModel.OrganizationViewModel
import com.connectify.connectifyNow.viewModel.AuthViewModel
import com.firebase.geofire.core.GeoHash
import com.google.firebase.firestore.GeoPoint

class SignUpOrganizationFragment : BaseFragment() {
    private lateinit var locationsAdapter: ArrayAdapter<String>
    private lateinit var locationsSuggestions: ArrayList<Location>
    private lateinit var imageView: ImageView
    private lateinit var organization: Organization
    private lateinit var view: View
    private lateinit var signUpOrganization: Button
    private lateinit var dynamicTextHelper: DynamicTextHelper
    private lateinit var organizationViewModel: OrganizationViewModel
    private lateinit var userAuthViewModel: AuthViewModel
    private lateinit var imageHelper: ImageHelper
    private lateinit var addressAutoComplete: AutoCompleteTextView
    private  lateinit var loadingOverlay: LinearLayout

    private var _binding: FragmentSignUpOrganizationBinding? = null
    private val binding get() = _binding!!
    private var organizationLocation: OrganizationLocation = OrganizationLocation(
        "Unknown",
        GeoPoint(0.0, 0.0),
        GeoHash(0.0, 0.0)
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSignUpOrganizationBinding.inflate(layoutInflater, container, false)
        view = binding.root
        organizationViewModel = OrganizationViewModel()
        userAuthViewModel = AuthViewModel()
        dynamicTextHelper = DynamicTextHelper(view)

        loadingOverlay = view.findViewById(R.id.signup_organization_loading_overlay);
        loadingOverlay?.visibility = View.INVISIBLE

        initLocationsAutoComplete()

        setHints()
        setEventListeners()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val backButton = view.findViewById<ImageView>(R.id.back_button)

        backButton.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun initLocationsAutoComplete() {
        addressAutoComplete = view.findViewById(R.id.organizationSuggestion)

        locationsAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            ArrayList()
        )

        addressAutoComplete.setAdapter(locationsAdapter)
        addressAutoComplete.setOnItemClickListener { _, _, i, _ ->
            val selectedLocation = locationsSuggestions[i];
            val latitude = selectedLocation.latitude.toDouble()
            val longitude = selectedLocation.longitude.toDouble()

            organizationLocation = OrganizationLocation(
                selectedLocation.address,
                GeoPoint(latitude, longitude),
                GeoHash(latitude, longitude)
            )
        }

        addressAutoComplete.addTextChangedListener(object : TextWatcher {
            @RequiresApi(Build.VERSION_CODES.O_MR1)
            override fun afterTextChanged(s: Editable?) {
                if (!s.isNullOrEmpty()) {
                    searchLocations(s.toString())
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private val onError: (String?) -> Unit = {
        val dialogHelper = DialogHelper("Sorry", requireContext(), it)
        dialogHelper.showDialogMessage()
    }

    private val onSuccess: (String?) -> Unit = { userId ->
        userId?.let { id ->
            organizationViewModel.addOrganization(organization.copy(id = id))
            requireActivity().runOnUiThread {
                Toast.makeText(
                    requireContext(),
                    "Account created successfully",
                    Toast.LENGTH_SHORT
                ).show()
                view.navigate(R.id.action_signUpOrganizationFragment_to_signInFragment)
            }
        }
    }

    private fun setEventListeners() {
        signUpOrganization = view.findViewById(R.id.sign_up_organization_btn)
        imageView = view.findViewById(R.id.logo_organization)

        imageHelper = ImageHelper(this, imageView , object : ImageUploadListener {
            override fun onImageUploaded(imageUrl: String) {
                // Perform actions after image upload completes
                loadingOverlay?.visibility = View.INVISIBLE
            }
        })
        imageHelper.setImageViewClickListener {
            loadingOverlay?.visibility = View.VISIBLE
        }

        signUpOrganization.setOnClickListener {
            val organizationNameGroup = binding.organizationNameGroup
            val emailGroup = binding.emailOrganization
            val bioGroup = binding.bioGroup
            val passwordGroup = binding.passwordOrganization
            val address = binding.organizationSuggestion.text
            val logo = imageHelper.getImageUrl()
            if (isValidInputs(emailGroup, passwordGroup, organizationNameGroup, address,bioGroup)) {
                val email = emailGroup.editTextField.text.toString()
                val password = passwordGroup.editTextField.text.toString()
                val name = organizationNameGroup.editTextField.text.toString()
                val bio = bioGroup.editTextField.text.toString()

                organization = Organization(
                    id = userAuthViewModel.getUserId().toString(),
                    name = name,
                    email = email,
                    logo = logo.toString(),
                    location = organizationLocation,
                    bio = bio
                )
                organizationViewModel.createUserAsOrganizationOwner(email, password, onSuccess, onError)
            }
        }
    }

    private fun setHints() {
        dynamicTextHelper.setTextViewText(R.id.organization_name_group, R.string.organization_name_title)
        dynamicTextHelper.setTextViewText(R.id.email_organization, R.string.email)
        dynamicTextHelper.setTextViewText(R.id.password_organization, R.string.password)
        dynamicTextHelper.setTextViewText(R.id.bio_group, R.string.bio_title)
    }

    @RequiresApi(Build.VERSION_CODES.O_MR1)
    fun searchLocations(query: String) {
        context?.let {
            LocationsApiCall().getLocationsByQuery(it, query) { locations ->

                locationsAdapter.clear();
                locationsSuggestions = ArrayList();

                // Filter invalid addresses (with missing fields)
                val filteredLocationsArray = ArrayList<String>();
                locations.forEach {
                    if (it.address != null && it.longitude != null && it.latitude != null) {
                        filteredLocationsArray.add(it.title.plus(" - ").plus(it.address))
                        locationsSuggestions.add(it);
                    }
                }

//                Log.d("locationsApi", filteredLocationsArray.size.toString())

                locationsAdapter = ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_spinner_dropdown_item,
                    filteredLocationsArray
                )

                locationsAdapter.notifyDataSetChanged()
                addressAutoComplete?.setAdapter(locationsAdapter)
            }
        }
    }

    private fun isValidInputs(
        email: CustomInputFieldTextBinding,
        password: CustomInputFieldPasswordBinding,
        organizationName: CustomInputFieldTextBinding,
        organizationLocation: Editable,
        bioGroup: CustomInputFieldTextBinding
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

        validationResults.add(
            ValidationHelper.isValidAddress(organizationLocation)
                .also { isValid ->
                    ValidationHelper.handleValidationResult(
                        isValid,
                        binding.addEditTextLine,
                        binding.inputSuggestions,
                        requireContext()
                    )
                }
        )
        return validationResults.all { it }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
