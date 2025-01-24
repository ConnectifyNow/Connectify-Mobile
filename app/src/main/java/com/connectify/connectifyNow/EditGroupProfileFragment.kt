package com.connectify.connectifyNow

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation
import com.connectify.connectifyNow.databinding.FragmentEditGroupProfileBinding
import com.connectify.connectifyNow.helpers.DynamicTextHelper
import com.connectify.connectifyNow.helpers.ImageHelper
import com.connectify.connectifyNow.viewModel.AuthViewModel
import com.connectify.connectifyNow.viewModel.VolunteerViewModel

class EditGroupProfileFragment : Fragment() {
    private val userAuthViewModel: AuthViewModel by activityViewModels()
    private val volunteerViewModel: VolunteerViewModel by activityViewModels()

    private var _binding: FragmentEditGroupProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var view: View
    private lateinit var saveBtn: Button
    private lateinit var groupViewModel: VolunteerViewModel
    private lateinit var dynamicTextHelper: DynamicTextHelper
    private lateinit var emailAddress: String

    //allow update image
    private lateinit var profileImageUrl: String
    private lateinit var imageHelper: ImageHelper
    private lateinit var imageView: ImageView
    private  lateinit var loadingOverlay: LinearLayout

    private var profileImage: ImageView? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditGroupProfileBinding.inflate(layoutInflater, container, false)
        view = binding.root
        dynamicTextHelper = DynamicTextHelper(view)


        loadingOverlay = view.findViewById(R.id.edit_image_loading_overlay);
        loadingOverlay?.visibility = View.INVISIBLE



        setHints()
        setEventListeners()
        setUserData()

        // Hide the BottomNavigationView
        ActionsBarHelper.hideActionBarAndBottomNavigationView((requireActivity() as? AppCompatActivity))

        val backButton = view.findViewById<ImageView>(R.id.back_button)
        backButton.setOnClickListener {
            Navigation.findNavController(it)
                .navigate(R.id.action_editGroupProfileFragment_to_ProfileFragment)
        }

        return view
    }


    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private fun setHints() {
        dynamicTextHelper.setTextViewText(R.id.group_institution, R.string.institution_title)
        dynamicTextHelper.setTextViewText(R.id.group_name, R.string.user_name_title)
        dynamicTextHelper.setTextViewText(R.id.group_bio, R.string.bio_title)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        ActionsBarHelper.showActionBarAndBottomNavigationView(requireActivity() as? AppCompatActivity)
        super.onCreate(savedInstanceState)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment EditGroupProfileFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            EditGroupProfileFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}