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
                requireActivity().supportFragmentManager.popBackStack(
                    "profileBackStack",
                    FragmentManager.POP_BACK_STACK_INCLUSIVE
                )
            }
        }
        val mapButton = view.findViewById<TextView>(R.id.explore_organizations)

        if(args?.getString("userId") != null) {
            mapButton.visibility = View.GONE
        } else {
            mapButton.visibility = View.VISIBLE
            mapButton.setOnClickListener {
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
            profileImageBackgroundElement = view.findViewById(R.id.profile_background_image);

            if (profileImage != null && profileImageBackgroundElement != null) {
                val imageUrl = if (image.isEmpty())
                    "https://firebasestorage.googleapis.com/v0/b/skills-e4dc8.appspot.com/o/images%2FuserAvater.png?alt=media&token=1fa189ff-b5df-4b1a-8673-2f8e11638acc"
                else image

                Picasso.get().load(imageUrl).into(profileImage)

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    profileImageBackgroundElement?.setRenderEffect(
                        RenderEffect.createBlurEffect(
                            50f, 50f, Shader.TileMode.CLAMP
                        )
                    )
                }
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
