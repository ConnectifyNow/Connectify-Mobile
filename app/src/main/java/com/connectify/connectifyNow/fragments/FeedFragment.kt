package com.connectify.connectifyNow.fragments

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.connectify.connectifyNow.models.Post
import com.connectify.connectifyNow.adapters.PostAdapter
import com.connectify.connectifyNow.viewModules.AuthViewModel
import com.connectify.connectifyNow.R

class FeedFragment : Fragment() {
    private lateinit var postsRecyclerView: RecyclerView
    private lateinit var postAdapter: PostAdapter
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var viewModel: PostViewModel
    private lateinit var progressBar: ProgressBar
    private val authViewModel: AuthViewModel by activityViewModels()

    private var _binding: FragmentFeedBinding? = null
    private val binding get() = _binding!!

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFeedBinding.inflate(inflater, container, false)
        val view = binding.root

        // Initialize views
        postsRecyclerView = binding.postsRecyclerView
        swipeRefreshLayout = binding.pullToRefresh
        postAdapter = PostAdapter(mutableListOf(), true)
        viewModel = ViewModelProvider(this)[PostViewModel::class.java]

        postsRecyclerView.setPadding(0, 0, 0, 250)

        postAdapter.posts = viewModel.posts.value?.toMutableList() ?: mutableListOf()
        progressBar = binding.progressBar
        progressBar.visibility = View.VISIBLE


        authViewModel.getInfoOnUser(authViewModel.getUserId().toString()) { userInfo, error ->
            when (userInfo) {
                is UserInfo.UserVolunteer -> {
                    (activity as MainActivity).setProfile("VOLUNTEER")
                }
                is UserInfo.UserOrganization -> {
                    (activity as MainActivity).setProfile("ORGANIZATION")
                }

                null -> Log.d("InfoUser", "we have identify unknown user")
            }
        }

        // Set up RecyclerView
        postsRecyclerView.layoutManager = LinearLayoutManager(context)
        postsRecyclerView.adapter = postAdapter

        swipeRefreshLayout.setOnRefreshListener {
            reloadData()
        }

        // Set up ViewModel
        viewModel.posts.observe(viewLifecycleOwner) { newPosts ->
            // Clear the existing data in the adapter
            if (postAdapter.posts.isNotEmpty()) {
                postAdapter.clear()
            }
            postAdapter.addAll(newPosts)
            postAdapter.notifyDataSetChanged()
            progressBar.visibility = View.GONE
        }

        postAdapter.setOnPostClickListener(object : OnPostClickListener {
            override fun onPostClicked(post: Post?) {
                post?.let {
                    authViewModel.getInfoOnUser(it.ownerId) { userInfo, error ->
                        if (error != null) {
                            Log.e("User Info Error", error)
                        } else {
                            val dialogBinding = UserProfileDialogBinding.inflate(layoutInflater)
                            val dialog = AlertDialog.Builder(requireContext())
                                .setView(dialogBinding.root)
                                .create()

                            when (userInfo) {
                                is UserInfo.UserVolunteer -> {
                                    dialogBinding.textViewName.text = userInfo.volunteerInfo?.name
                                    dialogBinding.textViewEmail.text = userInfo.volunteerInfo?.email
                                    dialogBinding.textViewBio.text = userInfo.volunteerInfo?.bio
                                }

                                is UserInfo.UserOrganization -> {
                                    dialogBinding.textViewName.text = userInfo.organizationInfo?.name
                                    dialogBinding.textViewEmail.text = userInfo.organizationInfo?.email
                                    dialogBinding.textViewBio.text = userInfo.organizationInfo?.bio
                                }

                                null -> throw IllegalArgumentException("unknown userInfo")
                            }

                            val fragmentManager = requireActivity().supportFragmentManager
                            val fragmentTransaction = fragmentManager.beginTransaction()
                            val profileFragment = ProfileFragment()

                            dialogBinding.buttonMoreDetails.setOnClickListener {
                                dialog.dismiss()
                                when (userInfo) {
                                    is UserInfo.UserVolunteer -> {
                                        userInfo.volunteerInfo?.id?.let { userId ->

                                            val args = Bundle()
                                            args.putString("userId", userId)
                                            profileFragment.arguments = args

                                            fragmentTransaction.replace(
                                                R.id.container,
                                                profileFragment
                                            )
                                            fragmentTransaction.addToBackStack("profileBackStack")

                                            fragmentTransaction.commit()
                                        }
                                    }

                                    is UserInfo.UserOrganization -> {
                                        userInfo.organizationInfo?.id?.let { organizationId ->

                                            val args = Bundle()
                                            args.putString("userId", organizationId)
                                            profileFragment.arguments = args

                                            fragmentTransaction.replace(
                                                R.id.container,
                                                profileFragment
                                            )
                                            fragmentTransaction.addToBackStack("profileBackStack")

                                            fragmentTransaction.commit()
                                            dialog.dismiss()
                                        }
                                    }
                                }
                            }
                            dialog.show()
                        }
                    }
                }
            }

            override fun onPostClick(position: Int) {
                Log.d("TAG", "postsRecyclerAdapter: post click position $position")
            }
        })

        return view
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        ActionBarHelper.showActionBarAndBottomNavigationView(requireActivity() as? AppCompatActivity)
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun reloadData() {
        swipeRefreshLayout.isRefreshing = false
        progressBar.visibility = View.VISIBLE
        viewModel.refreshPosts()
        progressBar.visibility = View.GONE

    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onResume() {
        super.onResume()
        reloadData()
    }

    interface OnPostClickListener {
        fun onPostClick(position: Int)
        fun onPostClicked(post: Post?)
    }
}