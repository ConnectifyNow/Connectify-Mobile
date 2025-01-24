package com.connectify.connectifyNow

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.cardview.widget.CardView
import androidx.navigation.fragment.findNavController
import com.connectify.connectifyNow.databinding.FragmentPickUserTypeBinding

class PickUserTypeFragment : BaseFragment() {
    private lateinit var organizationCard: CardView
    private lateinit var volunteerCard: CardView

    private lateinit var view: View
    private var _binding: FragmentPickUserTypeBinding? = null
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPickUserTypeBinding.inflate(inflater, container, false)
        view = binding.root
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

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private fun setEventListeners() {
        organizationCard = view.findViewById(R.id.organization_card)
        volunteerCard = view.findViewById(R.id.volunteer_card)

        organizationCard.setOnClickListener {
            view.navigate(R.id.action_pickUserTypeFragment_to_signUpOrganizationFragment)
        }

        volunteerCard.setOnClickListener {
            view.navigate(R.id.action_pickUserTypeFragment_to_signUpVolunteerFragment)
        }
    }
}