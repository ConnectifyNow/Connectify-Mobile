package com.connectify.connectifyNow.helpers

import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.util.Log
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.connectify.connectifyNow.models.CloudinaryModel
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import java.util.UUID


interface ImageUploadListener {
    fun onImageUploaded(imageUrl: String)
}

class ImageHelper(val fragment: Fragment, private val imageView: ImageView, private val uploadListener: ImageUploadListener) {
    private val cloudinaryModel = CloudinaryModel()
    private var imageUrl: String? = null
    private val storageRef = FirebaseStorage.getInstance().reference


    private val startForResult =
        fragment.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == android.app.Activity.RESULT_OK) {
                val selectedImageUri = result.data?.data
                selectedImageUri?.let { uri ->
                    cloudinaryModel.uploadImage(uri,"tmp")
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
            Handler().postDelayed({
                callback()
            }, 1000)
        }
    }

//    private fun uploadImageToFirebaseStorage(imageUri: Uri) {
//        val randomKey = UUID.randomUUID().toString()
//        val riversRef = storageRef.child("images/$randomKey")
//        riversRef.putFile(imageUri)
//            .addOnSuccessListener { taskSnapshot ->
//                riversRef.downloadUrl.addOnSuccessListener { uri ->
//                    imageUrl = uri.toString()
//                    Log.d("url", imageUrl.toString())
//
//                    Picasso.get().load(uri).into(imageView)
//
//                    imageUrl?.let { uploadListener.onImageUploaded(it) }
//
//                }
//            }
//            .addOnFailureListener { exception ->
//                Log.e("Upload File", "Upload failed", exception)
//            }
//    }



    private fun uploadImage() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startForResult.launch(intent)
    }
}
