package com.connectify.connectifyNow.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.connectify.connectifyNow.viewModules.PostViewModel
import com.connectify.connectifyNow.models.Post
import com.connectify.connectifyNow.R
import com.connectify.connectifyNow.viewModules.AuthViewModel
import com.connectify.connectifyNow.utils.DynamicText
import com.connectify.connectifyNow.utils.Image
import com.connectify.connectifyNow.utils.ImageUploadListener

class NewPostFragment : Fragment() {
    private lateinit var view: View
    private lateinit var postViewModel: PostViewModel
    private lateinit var imageView: ImageView
    private lateinit var image: Image
    private lateinit var dynamicText: DynamicText
    private  lateinit var loadingOverlay: LinearLayout

    private var _binding: FragmentNewPostBinding? = null
    private val binding get() = _binding!!
    private val authViewModel: AuthViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNewPostBinding.inflate(layoutInflater, container, false)
        view = binding.root
        imageView = binding.imageToUpload
        dynamicText = DynamicText(view)

        loadingOverlay = view.findViewById(R.id.new_post_loading_overlay);
        loadingOverlay.visibility = View.INVISIBLE

        image = Image(this, imageView,  object : ImageUploadListener {
            override fun onImageUploaded(imageUrl: String) {
                // Perform actions after image upload completes
                loadingOverlay.visibility = View.INVISIBLE
            }
        })
        image.setImageViewClickListener {
            loadingOverlay.visibility = View.VISIBLE
        }

        postViewModel = PostViewModel()

        setHints()
        setEventListeners()

        return view
    }

    private fun setHints() {
        dynamicText.setHintForEditText(R.id.post_title_group, R.string.project_name_hint, R.string.project_name_title)
        dynamicText.setHintForEditText(R.id.post_description_group, R.string.project_description_hint, R.string.project_description)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private fun setEventListeners() {
        binding.postButton.setOnClickListener {
            val titleGroup = binding.postTitleGroup
            val contentGroup = binding.postDescriptionGroup

            if (isValidInputs(titleGroup, contentGroup)) {
                val title = titleGroup.editTextField.text.toString()
                val content = contentGroup.editTextField.text.toString()

                // Check if an image has been selected
                if (image.isImageSelected()) {
                    // If an image is selected, create the post with the existing image URL
                    createPost(title, content, image.getImageUrl())
                } else {
                    // If no image is selected, create the post without an image URL
                    createPost(title, content, "")
                }
            }
        }
    }

    private fun createPost(title: String, content: String, imageUrl: String?) {
        val post = Post(
            ownerId = authViewModel.getUserId().toString(),
            title = title,
            content = content,
            imagePath = imageUrl ?: ""
        )

        addPost(post)
    }

    private fun addPost(post: Post) {
        postViewModel.addPost(post) { success ->
            if (success) {
                Toast.makeText(requireContext(), "Post added successfully", Toast.LENGTH_SHORT).show()
                binding.postTitleGroup.editTextField.text = null
                binding.postDescriptionGroup.editTextField.text = null
                binding.imageToUpload.setImageResource(R.drawable.default_post)
            } else Toast.makeText(requireContext(), "Failed to add post", Toast.LENGTH_SHORT).show()
        }
    }

    private fun isValidInputs(
        title: CustomInputFieldTextBinding,
        content: CustomInputFieldTextBinding,
    ): Boolean {
        val validationResults = mutableListOf<Boolean>()
        validationResults.add(
            Validation.isValidField(title.editTextField.text.toString()).also { isValid ->
                Validation.handleValidationResult(isValid, title, requireContext())
            }
        )

        validationResults.add(
            Validation.isValidField(content.editTextField.text.toString()).also { isValid ->
                Validation.handleValidationResult(isValid, content, requireContext())
            }
        )

        return validationResults.all { it }
    }
}