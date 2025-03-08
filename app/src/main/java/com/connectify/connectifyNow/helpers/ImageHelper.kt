package com.connectify.connectifyNow.helpers

import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.connectify.connectifyNow.models.CloudinaryModel
import com.connectify.connectifyNow.models.CloudinaryUploadListener
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import java.util.UUID


interface ImageUploadListener {
    fun onImageUploaded(imageUrl: String)
    fun onUploadFailed(error: String)
}

class ImageHelper(val fragment: Fragment, private val imageView: ImageView, private val uploadListener: ImageUploadListener) {
    private val cloudinaryModel = CloudinaryModel()
    private var imageUrl: String? = null

    private val startForResult =
        fragment.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == android.app.Activity.RESULT_OK) {
                val selectedImageUri = result.data?.data
                selectedImageUri?.let { uri ->
                    displayImage(uri)
                    // Upload it to Cloudinary
                    cloudinaryModel.uploadImage(
                        uri,
                        "image_${System.currentTimeMillis()}",
                        object : CloudinaryUploadListener {
                            override fun onImageUploaded(imageUrl: String) {
                                this@ImageHelper.imageUrl = imageUrl

                                // Notify the fragment through the callback
                                Handler(Looper.getMainLooper()).post {
                                    uploadListener.onImageUploaded(imageUrl)
                                }
                            }

                            override fun onUploadFailed(error: String) {
                                Handler(Looper.getMainLooper()).post {
                                    uploadListener.onUploadFailed(error)
                                }
                            }

                            override fun onUploadStarted() {
                                Log.d("ImageHelper", "Upload started")
                            }

                            override fun onUploadProgress(progress: Int) {
                                Log.d("ImageHelper", "Upload progress: $progress%")
                            }
                        }
                    )
                }
            }
        }
    fun isImageSelected(): Boolean {
        return !imageUrl.isNullOrEmpty()
    }

    fun getImageUrl(): String? {
        return imageUrl
    }

    fun setImageViewClickListener(callback: () -> Unit) {
        imageView.setOnClickListener {
            uploadImage()
            callback()
        }
    }


    // Display image in ImageView
    private fun displayImage(uri: Uri) {
        try {
            Picasso.get()
                .load(uri)
                .fit()
                .centerCrop()
                .into(imageView)
        } catch (e: Exception) {
            Log.e("ImageHelper", "Error loading image: ${e.message}")
        }
    }

    // Display remote image in ImageView
    fun displayRemoteImage(url: String) {
        try {
            Picasso.get()
                .load(url)
                .fit()
                .centerCrop()
                .into(imageView)
        } catch (e: Exception) {
            Log.e("ImageHelper", "Error loading remote image: ${e.message}")
        }
    }

    private fun uploadImage() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startForResult.launch(intent)
    }
}
