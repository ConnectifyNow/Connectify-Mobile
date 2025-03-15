package com.connectify.connectifyNow

import android.graphics.RenderEffect
import android.graphics.Shader
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

    private var loadingOverlay: LinearLayout? = null;

    private var isPostsLoaded: Boolean = false;
    private var isProfileDataLoaded: Boolean = false;

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

        loadingOverlay = view.findViewById(R.id.profile_loading_overlay);
        handleLoading();

        if (userId !== null) {
            authViewModel.getInfoOnUser(userId) { userInfo, error ->
                when (userInfo) {
                    is UserInfo.UserVolunteer -> {
                        getGroup(userId)
                    }
                    is UserInfo.UserOrganization -> {
                        getOrganization(userId);
                    }

                    null -> Log.d("UserInfo", "we have identify unknown user")
                }
            }

            handleProfileActionButtons(userId)
            fillPosts(userId)
        }

        if(args?.getString("userId")?.isNotEmpty() == true) {
            backButton = view.findViewById(R.id.back_button)
            backButton?.setVisibility(View.VISIBLE)

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
            postViewModel.refreshPosts() // This will reload the posts data
        }
    }

    private fun getOrganization(organizationId: String): Unit {
        organizationViewModel.getOrganization(organizationId) {
            fillProfileDetails(it.name, it.bio, it.location.address, it.logo);
        }
    }

    private fun getGroup(groupId: String): Unit {
        volunteerViewModel.getVolunteer(groupId) {
            fillProfileDetails(it.name, it.bio, it.institution, it.image);
        }
    }

    private fun handleProfileActionButtons(userId: String) {
        val connectedUserId = authViewModel.getUserId();
        editButton = view.findViewById(R.id.profile_edit_button);
        logoutButton = view.findViewById(R.id.logout_button);

        if (userId === connectedUserId) {
            editButton?.visibility = View.VISIBLE;
            logoutButton?.visibility = View.VISIBLE;

            editButton?.setOnClickListener {
                val args = Bundle()
                args.putString("userId", userId)

                authViewModel.getInfoOnUser(userId) { userInfo, error ->
                    when (userInfo) {
                        is UserInfo.UserVolunteer -> {
                            Navigation.findNavController(view).navigate(
                                R.id.action_profileFragment_to_editGroupProfileFragment,
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
                authViewModel.logOutUser();
                Navigation.findNavController(it)
                    .navigate(R.id.action_profileFragment_to_landingPageFragment)
            }

        } else {
            editButton?.visibility = View.GONE;
            logoutButton?.visibility = View.GONE;
        }
    }

    private fun fillProfileDetails(name: String, bio: String, additionalInfo: String, image: String): Unit {
            nameTV = view.findViewById(R.id.profile_name)
            nameTV?.text = name

            bioTV = view.findViewById(R.id.profile_bio)
            bioTV?.text = bio

            additionalInfoTV = view.findViewById(R.id.profile_additional_info)
            additionalInfoTV?.text = additionalInfo;

            profileImage = view.findViewById(R.id.profile_image)

            if (profileImage != null && profileImageBackgroundElement != null) {
                val imageUrl = if (image.isEmpty())
                    "data:image/png;base64,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"
                else image

                Picasso.get().load(imageUrl).into(profileImage)

            }

        isProfileDataLoaded = true;
        handleLoading();
    }


    private fun fillPosts(userId: String): Unit {
        postsRecyclerView = view.findViewById(R.id.profile_posts_recycler_view)
        noPostsWarningTV = view.findViewById(R.id.profile_no_posts_text)

        postViewModel.getPostsByOwnerId(userId) {
            if (it.isEmpty()) {
                postsRecyclerView.visibility = View.GONE;
                noPostsWarningTV?.visibility = View.VISIBLE;
            } else {
                postsRecyclerView.visibility = View.VISIBLE;
                noPostsWarningTV?.visibility = View.GONE;

                postsRecyclerView.setPadding(0, 0, 0, 250)
                postAdapter.clear()
                postAdapter.addAll(it.toMutableList());
                postsRecyclerView.layoutManager = LinearLayoutManager(context)
                postsRecyclerView.adapter = postAdapter
            }

            isPostsLoaded = true;
            handleLoading();
        }
        postViewModel.getPostsByOwnerId(userId) { /* This will trigger the observer */ }

    }

    private fun handleLoading(): Unit {
        if (isPostsLoaded && isProfileDataLoaded) {
            loadingOverlay?.visibility = View.INVISIBLE;
        } else {
            loadingOverlay?.visibility = View.VISIBLE;
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}
