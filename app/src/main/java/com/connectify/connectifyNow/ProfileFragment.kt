package com.connectify.connectifyNow

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.connectify.connectifyNow.viewModel.OrganizationViewModel
import com.connectify.connectifyNow.viewModel.PostViewModel
import com.connectify.connectifyNow.viewModel.VolunteerViewModel
import com.connectify.connectifyNow.viewModel.AuthViewModel
import com.connectify.connectifyNow.adapters.PostAdapter
import com.connectify.connectifyNow.databinding.FragmentProfileBinding
import com.connectify.connectifyNow.helpers.ActionBarHelpers
import com.connectify.connectifyNow.models.UserInfo
import com.squareup.picasso.Picasso

class ProfileFragment : Fragment() {
    private val authViewModel: AuthViewModel by activityViewModels()

    private lateinit var organizationViewModel: OrganizationViewModel
    private lateinit var volunteerViewModel: VolunteerViewModel

    private lateinit var postViewModel: PostViewModel

    private var binding: FragmentProfileBinding? = null

    private lateinit var view: View

    private lateinit var postsRecyclerView: RecyclerView
    private lateinit var postAdapter: PostAdapter

    private var profileImage: ImageView? = null
    private var profileImageBackgroundElement: ImageView? = null
    private var nameTV: TextView? = null
    private var bioTV: TextView? = null
    private var additionalInfoTV: TextView? = null
    private var noPostsWarningTV: TextView? = null

    private var editButton: ImageView? = null
    private var logoutButton: ImageView? = null
    private var backButton: ImageView? = null

    private var loadingOverlay: LinearLayout? = null

    private var isPostsLoaded: Boolean = false
    private var isProfileDataLoaded: Boolean = false

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        organizationViewModel = OrganizationViewModel()
        volunteerViewModel = VolunteerViewModel()
        postViewModel = PostViewModel()

        binding = FragmentProfileBinding.inflate(inflater, container, false)
        view  = binding?.root as View

        ActionBarHelpers.showActionBarAndBottomNavigationView(requireActivity() as? AppCompatActivity)

        val args = arguments
        val userId = args?.getString("userId") ?: authViewModel.getUserId()

        if (args?.getString("userId") !== null) {
            ActionBarHelpers.hideActionBarAndBottomNavigationView((requireActivity() as? AppCompatActivity))
        }

        val isFromArgs = args != null
        postAdapter = PostAdapter(mutableListOf(), false, isFromArgs)

        loadingOverlay = view.findViewById(R.id.profile_loading_overlay)
        handleLoading()

        if (userId !== null) {
            authViewModel.getInfoOnUser(userId) { userInfo, _ ->
                when (userInfo) {
                    is UserInfo.UserVolunteer -> {
                        getGroup(userId)
                    }
                    is UserInfo.UserOrganization -> {
                        getOrganization(userId)
                    }

                    null -> Log.d("UserInfo", "we have identify unknown user")
                }
            }

            handleProfileActionButtons(userId)
            fillPosts(userId)
        }

        if(args?.getString("userId")?.isNotEmpty() == true) {
            backButton = view.findViewById(R.id.back_button)
            backButton?.visibility = View.VISIBLE

            backButton?.setOnClickListener {
                ActionBarHelpers.showActionBarAndBottomNavigationView((requireActivity() as? AppCompatActivity))

                requireActivity().supportFragmentManager.popBackStack(
                    "profileBackStack",
                    FragmentManager.POP_BACK_STACK_INCLUSIVE
                )
            }
        }

        val mapButton = binding?.exploreOrganizations

        if(args?.getString("userId") != null) {
            mapButton?.visibility = View.GONE
        } else {
            mapButton?.visibility = View.VISIBLE
            mapButton?.setOnClickListener {
                val args = Bundle()
                args.putBoolean("editMode", false)
                Navigation.findNavController(it)
                    .navigate(R.id.action_profileFragment_to_mapFragment, args)
            }
        }

        return view
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        ActionBarHelpers.showActionBarAndBottomNavigationView(requireActivity() as? AppCompatActivity)
        super.onCreate(savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        val userId = arguments?.getString("userId") ?: authViewModel.getUserId()
        if (userId != null) {
            postViewModel.refreshPosts()
        }
    }

    private fun getOrganization(organizationId: String) {
        organizationViewModel.getOrganization(organizationId) {
            fillProfileDetails(it.name, it.bio, it.location.address, it.logo)
        }
    }

    private fun getGroup(groupId: String) {
        volunteerViewModel.getVolunteer(groupId) {
            fillProfileDetails(it.name, it.bio, it.institution, it.image)
        }
    }

    private fun handleProfileActionButtons(userId: String) {
        val connectedUserId = authViewModel.getUserId()
        editButton = view.findViewById(R.id.profile_edit_button)
        logoutButton = view.findViewById(R.id.logout_button)

        if (userId === connectedUserId) {
            editButton?.visibility = View.VISIBLE
            logoutButton?.visibility = View.VISIBLE

            editButton?.setOnClickListener {
                val args = Bundle()
                args.putString("userId", userId)

                authViewModel.getInfoOnUser(userId) { userInfo, _ ->
                    when (userInfo) {
                        is UserInfo.UserVolunteer -> {
                            Navigation.findNavController(view).navigate(
                                R.id.action_profileFragment_to_editVolunteerProfileFragment,
                                args
                            )
                        }
                        is UserInfo.UserOrganization -> {
                            Navigation.findNavController(view).navigate(
                                R.id.action_profileFragment_to_editOrganizationProfileFragment,
                                args
                            )
                        }
                        null -> {
                        }
                    }
                }
            }

            logoutButton?.setOnClickListener {
                authViewModel.logOutUser()
                Navigation.findNavController(it)
                    .navigate(R.id.action_profileFragment_to_landingPageFragment)
            }

        } else {
            editButton?.visibility = View.GONE
            logoutButton?.visibility = View.GONE
        }
    }

    private fun fillProfileDetails(name: String, bio: String, additionalInfo: String, image: String) {
            nameTV = view.findViewById(R.id.profile_name)
            nameTV?.text = name

            bioTV = view.findViewById(R.id.profile_bio)
            bioTV?.text = bio

            additionalInfoTV = view.findViewById(R.id.profile_additional_info)
            additionalInfoTV?.text = additionalInfo

            profileImage = view.findViewById(R.id.profile_image)

            if (profileImage != null && profileImageBackgroundElement != null) {
                val imageUrl = image.ifEmpty {
                "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAMwAAADACAMAAAB/Pny7AAAAMFBMVEXFxcX////CwsLIyMjMzMz8/Pzv7+/5+fnz8/PY2Nj29vbk5OS/v7/h4eHPz8/S0tLbr2APAAAE30lEQVR4nO2djbqkIAiGG9LKn+r+73brNLvNb2MIwpn1vYK+RwFFoKapVCqVSqVSqVQqlUqlUqlUKpVKpVLBATvSn5JH2xo3dkPvfT90wU2mlf4iDND8yLg84VdJ0l93CmgmF4ZnJVc93Ti3v2fPueDfKdnol/Vpf4EcMGOwx1J+GMIs/amfAHBdipTNfNoo/b1HwNz1iVIW7DA2avcamHBCyg+dU7o4cXrrwA5Wx0WVizOmGsvD4hh9atokH/aKQZ1bMwEpZcE76a+/B2MuO70mNTBnaVnWZlRjN5C3LivWaVEDH05iSWpmJWoybH9nmDSoAWR8eVKjIN6Ao9GyRE9xMWAIDGbDirs08+JqjEU83Liz5+QjOtnsQH6EuUU42oyUWoSXZiaz/iujnJaWJFze4icxMTOl9W+ILQ0QW8zKICWmpYr9t0jFmplBy8XLeGcgjTH/kPHOht78V0aJTFpkMP+VTkBLA+RBZqMX2Wc8u2zxZ+VdABgmLZeuvNFwmcyyzwTE8DjmBVt+m0WO8L+JKZ/Z4BNzCeX3GZ+Y4h6ALsP0QkzpbQaOTUv5M0Bkiv8yYgjzZVVMFfNfiPkmB8DqmovHmfmLguZXHWe+66AZuW7NIlcANt9s67U5C5i4xAwCWUCWtPmKxFsgMBmNlahK5zrQyLzQtEyJc5E3DZ4nDStUEMTy2DRIlQJw7DOxugaGuOmFpLA8nQcxMQxFDYI1zuTlJkGwj4u6rEG0EAgm2qURLggmzZ93sq1bQFnW6KUrtcGQHWrkC04pS4EV9GwBunHmQYuCIu0GaEKnV9IM0FKYjZb2JgqXJu3IdrIbaBQ4shsyHbR03fw90GbsNC22v4P3afpaG5eAN6KuaraTq8t+D0SHMJzeKR1zgGrUVqpl3WrzGTm2GzWPa1hnG6SL8S4qbaBfiOCCP7MyfXCgUg4sFuNPn5+tD0af1cTzxn+lD0bb6uCCzFWOYD/TEzDlSNnkqGgGXo9lFFnaflbhpafkQTOHaDjWmNwdttOPsj2nQLQsG8viyG01aDFHyyMGJ2U5y4WMvBTACmVoUQOAPjNMAiE0clXP2bm4Gs6yxtJpJ47m2Z2yiSf6x8x7Sr4GGqJc+VtsKBY/2bUUVMO9xzYK7TSioTkfsCUuObGMllUNe7yJjPHlEe7hh5GlLOsdvGcBYGqbf0PP+szZUtfKfMBzujT+AHOP5avYoppkdkYN1zENyAuyEuB6VaMsk0mHp2qrzCnmGZZzDUNBZhI9w5Nnaa+8Q++fea+Wx5B7NFPcK+9YYh/A1V+SBm2vI2cuJgXSfA3TjKlkSLsdBK1/g+7aWfjg/wrCy4D4wtAtDWG1Lx6qmcGcrf/p0DQ8U9f7I/E0r2oqFoamTYi02j8HkiJu0iHGGVBMc86pJKWly74KiFz8X0OQDlAQMP+SHTg1BMy/DLn3GsYxZqexuS5ASZDZyAw1mnZZ9j4TvmE+krXPuObLYglZzln66x/JWZii72QpZMRNzolsODJmUlH88ocW/AR0kExjvgY/lKrkM3kq6Od0bY55Be2cSZpJiRnQlxp1JrMYDVKKdLb8Ncgcuo582SPI/BnfFPMckKPcorqQueJxYphGfmXSo9wZ5+DvDHB/3BKolEkBV02jMf6voM4AXyWmUXeZ2TiY5vwHme9RB/ByBC0AAAAASUVORK5CYII="
                }

                Picasso.get().load(imageUrl).into(profileImage)
            }

        isProfileDataLoaded = true
        handleLoading()
    }


    private fun fillPosts(userId: String) {
        postsRecyclerView = view.findViewById(R.id.profile_posts_recycler_view)
        noPostsWarningTV = view.findViewById(R.id.profile_no_posts_text)

        postViewModel.getPostsByOwnerId(userId) {
            if (it.isEmpty()) {
                postsRecyclerView.visibility = View.GONE
                noPostsWarningTV?.visibility = View.VISIBLE
            } else {
                postsRecyclerView.visibility = View.VISIBLE
                noPostsWarningTV?.visibility = View.GONE

                postsRecyclerView.setPadding(0, 0, 0, 250)
                postAdapter.clear()
                postAdapter.addAll(it.toMutableList())
                postsRecyclerView.layoutManager = LinearLayoutManager(context)
                postsRecyclerView.adapter = postAdapter
            }

            isPostsLoaded = true
            handleLoading()
        }
        postViewModel.getPostsByOwnerId(userId) { /* This will trigger the observer */ }

    }

    private fun handleLoading() {
        if (isPostsLoaded && isProfileDataLoaded) {
            loadingOverlay?.visibility = View.INVISIBLE
        } else {
            loadingOverlay?.visibility = View.VISIBLE
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}
