package com.connectify.connectifyNow

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.connectify.connectifyNow.viewModel.AuthViewModel
import com.connectify.connectifyNow.databinding.FragmentEditPostBinding
import com.connectify.connectifyNow.helpers.DynamicTextHelper
import com.connectify.connectifyNow.helpers.ImageHelper
import com.connectify.connectifyNow.helpers.ImageUploadListener
import com.connectify.connectifyNow.models.Post
import com.connectify.connectifyNow.viewModel.PostViewModel
import com.squareup.picasso.Picasso
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class EditPost : Fragment() {
    private var binding: FragmentEditPostBinding? = null

    private lateinit var view: View
    private lateinit var dynamicTextHelper: DynamicTextHelper
    private lateinit var postViewModel: PostViewModel
    private lateinit var imageHelper: ImageHelper
    private lateinit var imageView: ImageView
    private  lateinit var loadingOverlay: LinearLayout

    private val userAuthViewModel: AuthViewModel by activityViewModels()
    private var titleConstraintLayout: ConstraintLayout? = null
    private var detailsConstraintlayout: ConstraintLayout? = null

    private var postId = ""
    var title: TextView? = null
    private var details: TextView? = null
    private var updatePost: Button? = null
    private var imagePost = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentEditPostBinding.inflate(layoutInflater, container, false)
        view = binding?.root as View
        dynamicTextHelper = DynamicTextHelper(view)

        postViewModel = PostViewModel()

        val args = arguments
        postId = args?.getString("postId").toString()

        titleConstraintLayout = view.findViewById(R.id.edit_post_title)
        detailsConstraintlayout = view.findViewById(R.id.edit_multi_line_post_description)
        imageView = view.findViewById(R.id.edit_image_to_upload)
        updatePost = view.findViewById(R.id.save_post)

        loadingOverlay = view.findViewById(R.id.edit_post_loading_overlay)
        loadingOverlay.visibility = View.INVISIBLE

        imageHelper = ImageHelper(this, imageView, object : ImageUploadListener {
            override fun onImageUploaded(imageUrl: String) {

                loadingOverlay.visibility = View.INVISIBLE
            }

            override fun onUploadFailed(error: String) {
                // Handle the error (e.g., show an error message or log it)
                loadingOverlay.visibility = View.INVISIBLE
                // You can also show a message to the user here
                Toast.makeText(context, "Upload failed: $error", Toast.LENGTH_SHORT).show()
            }
        })
        imageHelper.setImageViewClickListener {
            loadingOverlay.visibility = View.VISIBLE
        }

        val backButton = view.findViewById<ImageView>(R.id.back_button)
        backButton.setOnClickListener {
            findNavController().navigateUp()
        }

        title = titleConstraintLayout?.findViewById(R.id.edit_text_field)
        details = detailsConstraintlayout?.findViewById(R.id.edit_text_field)

        addEventListeners()
        setPostData(postId)
        setHints()

        return view
    }


    private fun addEventListeners() {
        updatePost?.setOnClickListener {
            val updatedPost =  Post(
                ownerId = userAuthViewModel.getUserId().toString(),
                title = title?.text.toString(),
                content = details?.text.toString(),
                imagePath = if (imageHelper.isImageSelected()) {
                    imageHelper.getImageUrl() ?: ""
                } else imagePost
            )

            updatedPost.id = postId
            lifecycleScope.launch {
                val result = postViewModel.update(postId, updatedPost.json)
                if (result) {
                    postViewModel.refreshPosts()

                    delay(500) // Short delay to ensure data is refreshed
                    Toast.makeText(
                        requireContext(),
                        "Post updated successfully",
                        Toast.LENGTH_SHORT
                    ).show()

                    Navigation.findNavController(view).navigate(
                        R.id.action_editPost_to_profileFragment,
                    )
                } else {
                    Toast.makeText(requireContext(), "Failed to update post", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setHints() {
        dynamicTextHelper.setHintForEditText(R.id.edit_post_title, R.string.post_title_hint, R.string.post_title)
        dynamicTextHelper.setHintForEditText(R.id.edit_multi_line_post_description, R.string.post_description_hint, R.string.post_description)
    }


    private fun setPostData(postId: String) {
        postViewModel.getPostById(postId) { postData ->
            title?.text = postData?.title
            details?.text = postData?.content
            if(postData?.imagePath != "") {
                if (postData != null) {
                    imagePost = postData.imagePath
                }
                Picasso.get().load(postData?.imagePath).into(imageView)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }

}