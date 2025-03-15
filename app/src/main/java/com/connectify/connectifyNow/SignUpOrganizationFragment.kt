package com.connectify.connectifyNow

import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.connectify.connectifyNow.databinding.CustomInputFieldPasswordBinding
import com.connectify.connectifyNow.databinding.CustomInputFieldTextBinding
import com.connectify.connectifyNow.databinding.FragmentSignUpOrganizationBinding
import com.connectify.connectifyNow.helpers.DialogHelper
import com.connectify.connectifyNow.helpers.DynamicTextHelper
import com.connectify.connectifyNow.helpers.ImageHelper
import com.connectify.connectifyNow.helpers.ImageUploadListener
import com.connectify.connectifyNow.helpers.ValidationHelper
import com.connectify.connectifyNow.helpers.navigate
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
    private lateinit var chooseOnMap: Button

    private lateinit var dynamicTextHelper: DynamicTextHelper
    private lateinit var organizationViewModel: OrganizationViewModel
    private lateinit var userAuthViewModel: AuthViewModel
    private lateinit var imageHelper: ImageHelper
    private lateinit var addressAutoComplete: AutoCompleteTextView
    private lateinit var loadingOverlay: LinearLayout

    private var binding: FragmentSignUpOrganizationBinding? = null

    private var organizationLocation: OrganizationLocation = OrganizationLocation(
        "Unknown",
        GeoPoint(0.0, 0.0),
        GeoHash(0.0, 0.0)
    )

    private val FORM_STATE_KEY = "form_state"
    private val ORG_NAME_KEY = "org_name"
    private val EMAIL_KEY = "email"
    private val PASSWORD_KEY = "password"
    private val BIO_KEY = "bio"


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSignUpOrganizationBinding.inflate(layoutInflater, container, false)
        view = binding?.root as View
        organizationViewModel = OrganizationViewModel()
        userAuthViewModel = AuthViewModel()
        dynamicTextHelper = DynamicTextHelper(view)

        loadingOverlay = view.findViewById(R.id.signup_organization_loading_overlay)
        loadingOverlay.visibility = View.INVISIBLE

        initLocationsAutoComplete()

        setHints()
        setEventListeners()
        restoreFormState()

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
            val selectedLocation = locationsSuggestions[i]
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
        val navController = findNavController()

        navController.currentBackStackEntry?.savedStateHandle?.getLiveData<Bundle>("location_data")?.observe(
            viewLifecycleOwner
        ) { result ->
            val latitude = result.getDouble("latitude")
            val longitude = result.getDouble("longitude")
            val address = result.getString("address")

            Log.d("SignUpFragment", "Received address: $address")

            binding?.organizationSuggestion?.setText(address)

            organizationLocation = OrganizationLocation(
                address.toString(),
                GeoPoint(latitude, longitude),
                GeoHash(latitude, longitude)
            )
            navController.currentBackStackEntry?.savedStateHandle?.remove<Bundle>("location_data")
        }

        signUpOrganization = view.findViewById(R.id.sign_up_organization_btn)
        imageView = view.findViewById(R.id.logo_organization)

        imageHelper = ImageHelper(this, imageView, object : ImageUploadListener {
            override fun onImageUploaded(imageUrl: String) {
                loadingOverlay.visibility = View.INVISIBLE
                Log.d("YourFragment", "Image uploaded: $imageUrl")
            }

            override fun onUploadFailed(error: String) {
                loadingOverlay.visibility = View.INVISIBLE
                Log.e("YourFragment", "Upload failed: $error")
            }
        })

        imageHelper.setImageViewClickListener {
            loadingOverlay.visibility = View.VISIBLE
        }

        chooseOnMap = view.findViewById(R.id.choose_on_map_button)

        chooseOnMap.setOnClickListener {
            saveFormState()

            val args = Bundle()
            args.putBoolean("editMode", true)
            Navigation.findNavController(view)
                .navigate(R.id.action_signUpOrganizationFragment_to_mapViewFragment, args)
        }

        signUpOrganization.setOnClickListener {
            val organizationNameGroup = binding?.organizationNameGroup
            val emailGroup = binding?.emailOrganization
            val bioGroup = binding?.bioGroup
            val passwordGroup = binding?.passwordOrganization
            val address = binding?.organizationSuggestion?.text
            val logo = imageHelper.getImageUrl()
            if (organizationNameGroup!=null && emailGroup!=null && bioGroup!=null &&passwordGroup!=null && address!=null && logo!=null &&  isValidInputs(emailGroup, passwordGroup, organizationNameGroup, address, bioGroup)) {
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
        context?.let { it ->
            LocationsApiCall().getLocationsByQuery(it, query) { locations ->

                locationsAdapter.clear()
                locationsSuggestions = ArrayList()

                val filteredLocationsArray = ArrayList<String>()
                locations.forEach {
                    filteredLocationsArray.add(it.title.plus(" - ").plus(it.address))
                    locationsSuggestions.add(it)
                }

                locationsAdapter = ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_spinner_dropdown_item,
                    filteredLocationsArray
                )

                locationsAdapter.notifyDataSetChanged()
                addressAutoComplete.setAdapter(locationsAdapter)
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
                        binding?.addEditTextLine as View,
                        binding?.inputSuggestions as TextView,
                        requireContext()
                    )
                }
        )
        return validationResults.all { it }
    }

    private fun saveFormState() {
        val navController = findNavController()
        val currentBackStackEntry = navController.currentBackStackEntry
        val savedStateHandle = currentBackStackEntry?.savedStateHandle

        savedStateHandle?.let { handle ->
            val formBundle = Bundle().apply {
                putString(ORG_NAME_KEY, binding?.organizationNameGroup?.editTextField?.text.toString())
                putString(EMAIL_KEY, binding?.emailOrganization?.editTextField?.text.toString())
                putString(PASSWORD_KEY, binding?.passwordOrganization?.editTextField?.text.toString())
                putString(BIO_KEY, binding?.bioGroup?.editTextField?.text.toString())
            }

            handle[FORM_STATE_KEY] = formBundle
            Log.d("SignUpFragment", "Form state saved")
        }
    }

    private fun restoreFormState() {
        val navController = findNavController()
        val currentBackStackEntry = navController.currentBackStackEntry
        val savedStateHandle = currentBackStackEntry?.savedStateHandle

        savedStateHandle?.get<Bundle>(FORM_STATE_KEY)?.let { formBundle ->
            view.post {
                binding?.let { binding ->
                    formBundle.getString(ORG_NAME_KEY)?.let { name ->
                        binding.organizationNameGroup.editTextField.setText(name)
                    }

                    formBundle.getString(EMAIL_KEY)?.let { email ->
                        binding.emailOrganization.editTextField.setText(email)
                    }

                    formBundle.getString(PASSWORD_KEY)?.let { password ->
                        binding.passwordOrganization.editTextField.setText(password)
                    }

                    formBundle.getString(BIO_KEY)?.let { bio ->
                        binding.bioGroup.editTextField.setText(bio)
                    }

                    Log.d("SignUpFragment", "Form state restoration complete")
                } ?: Log.e("SignUpFragment", "Binding is null during restore")
            }
        } ?: Log.d("SignUpFragment", "No saved form state found")
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}