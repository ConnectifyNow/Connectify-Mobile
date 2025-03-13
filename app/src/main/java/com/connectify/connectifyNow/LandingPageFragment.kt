package com.connectify.connectifyNow

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import com.connectify.connectifyNow.databinding.FragmentForgetPasswordBinding
import com.connectify.connectifyNow.databinding.FragmentLandingPageBinding
import com.connectify.connectifyNow.viewModel.AuthViewModel
import com.google.firebase.auth.FirebaseAuth

class LandingPageFragment : BaseFragment() {

    private var userAuthViewModel: AuthViewModel? = null

    private var binding: FragmentLandingPageBinding? = null

    private lateinit var view: View
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLandingPageBinding.inflate(inflater, container, false)
        view = binding?.root as View
        userAuthViewModel = AuthViewModel()

        setEventListeners()
        return view
    }

    private fun setEventListeners() {

        binding!!.buttonSignIn.setOnClickListener {
            Navigation.findNavController(view)
                .navigate(R.id.action_landingPageFragment_to_signInFragment)
        }

        binding!!.buttonSignUp.setOnClickListener {
            Navigation.findNavController(view)
                .navigate(R.id.action_landingPageFragment_to_pickUserTypeFragment)
        }

        view.post {
            val firebaseAuth = FirebaseAuth.getInstance()
            val currentUser = firebaseAuth.currentUser
            if (currentUser != null) {
                userAuthViewModel?.setCurrentUserId()
                Navigation.findNavController(requireView())
                    .navigate(R.id.action_landingPageFragment_to_feedFragment)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}
