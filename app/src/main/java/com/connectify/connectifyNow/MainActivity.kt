package com.connectify.connectifyNow

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.connectify.connectifyNow.helpers.ActionBarHelpers
import com.connectify.connectifyNow.viewModel.AuthViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private var navController: NavController? = null
    private var userAuthViewModel: AuthViewModel? = null
    private var userTypeProfile = "ORGANIZATION"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        userAuthViewModel = AuthViewModel()

        ActionBarHelpers.showActionBarAndBottomNavigationView(this)

        val navHostFragment: NavHostFragment? =
            supportFragmentManager.findFragmentById(R.id.navHostMain) as? NavHostFragment
        navController = navHostFragment?.navController

        val bottomNavigationView: BottomNavigationView =
            findViewById(R.id.mainActivityBottomNavigationView)
        navController?.let { NavigationUI.setupWithNavController(bottomNavigationView, it) }
    }

    fun setProfile(userType: String) {
        userTypeProfile = userType
        invalidateOptionsMenu()
    }
    

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                navController?.navigateUp()
                true
            }
            R.id.profileFragment -> {
                // Clear any backstack and load the profile fragment with the current user
                supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
                navController?.navigate(R.id.profileFragment)
                true
            }
            else -> navController?.let { NavigationUI.onNavDestinationSelected(item, it) } ?: super.onOptionsItemSelected(item)
        }
    }
}
