package com.connectify.connectifyNow

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.connectify.connectifyNow.helpers.ActionBarHelpers

open class BaseFragment : Fragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ActionBarHelpers.hideActionBarAndBottomNavigationView(requireActivity() as? AppCompatActivity)
    }
}
