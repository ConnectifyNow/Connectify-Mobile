package com.connectify.connectifyNow

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation
import com.connectify.connectifyNow.viewModel.AuthViewModel
import com.connectify.connectifyNow.viewModel.PostViewModel
import com.connectify.connectifyNow.databinding.CustomInputFieldTextBinding
import com.connectify.connectifyNow.databinding.FragmentMapBinding
import com.connectify.connectifyNow.databinding.FragmentNewPostBinding
import com.connectify.connectifyNow.helpers.DynamicTextHelper
import com.connectify.connectifyNow.helpers.ImageHelper
import com.connectify.connectifyNow.helpers.ImageUploadListener
import com.connectify.connectifyNow.helpers.ValidationHelper
import com.connectify.connectifyNow.models.Post

class NewPostFragment : Fragment() {
    private lateinit var view: View
    private lateinit var postViewModel: PostViewModel
    private lateinit var imageView: ImageView
    private lateinit var imageHelper: ImageHelper
    private lateinit var dynamicTextHelper: DynamicTextHelper
    private  lateinit var loadingOverlay: LinearLayout
    private var binding: FragmentNewPostBinding? = null

    private val userAuthViewModel: AuthViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNewPostBinding.inflate(layoutInflater, container, false)
        view = binding?.root as View
        imageView = binding?.imageToUpload as ImageView
        dynamicTextHelper = DynamicTextHelper(view)

        loadingOverlay = view.findViewById(R.id.new_post_loading_overlay);
        loadingOverlay.visibility = View.INVISIBLE

        imageHelper = ImageHelper(this, imageView,  object : ImageUploadListener {
            override fun onImageUploaded(imageUrl: String) {
                loadingOverlay.visibility = View.INVISIBLE
            }

            override fun onUploadFailed(error: String) {
                // Handle the error (e.g., show an error message or log it)
                loadingOverlay?.visibility = View.INVISIBLE
                // You can also show a message to the user here
                Toast.makeText(context, "Upload failed: $error", Toast.LENGTH_SHORT).show()
            }
        })

        imageHelper.setImageViewClickListener {
            loadingOverlay.visibility = View.VISIBLE
        }

        postViewModel = PostViewModel()

        setHints()
        setEventListeners()

        return view
    }

    private fun setHints() {
        dynamicTextHelper.setHintForEditText(R.id.post_title_group, R.string.post_title_hint, R.string.post_title)
        dynamicTextHelper.setHintForEditText(R.id.post_description_group, R.string.post_description_hint, R.string.post_description)
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }

    private fun setEventListeners() {
        binding?.postButton?.setOnClickListener {
            val titleGroup = binding?.postTitleGroup
            val contentGroup = binding?.postDescriptionGroup

            if (titleGroup != null && contentGroup != null &&  isValidInputs(titleGroup, contentGroup)) {
                val title = titleGroup.editTextField.text.toString()
                val content = contentGroup.editTextField.text.toString()

                if (imageHelper.isImageSelected()) {
                    createPost(title, content, imageHelper.getImageUrl())
                } else {
                    createPost(title, content, "")
                }
            }
        }
    }


    private fun createPost(title: String, content: String, imageUrl: String?) {
        val post = Post(
            ownerId = userAuthViewModel.getUserId().toString(),
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
                binding?.postTitleGroup?.editTextField?.text = null
                binding?.postDescriptionGroup?.editTextField?.text = null
                binding?.imageToUpload?.setImageResource(R.drawable.default_post)
            } else Toast.makeText(requireContext(), "Failed to add post", Toast.LENGTH_SHORT).show()
        }
    }

    private fun isValidInputs(
        title: CustomInputFieldTextBinding,
        content: CustomInputFieldTextBinding,
    ): Boolean {
        val validationResults = mutableListOf<Boolean>()
        validationResults.add(
            ValidationHelper.isValidField(title.editTextField.text.toString()).also { isValid ->
                ValidationHelper.handleValidationResult(isValid, title, requireContext())
            }
        )

        validationResults.add(
            ValidationHelper.isValidField(content.editTextField.text.toString()).also { isValid ->
                ValidationHelper.handleValidationResult(isValid, content, requireContext())
            }
        )

        return validationResults.all { it }
    }
}
