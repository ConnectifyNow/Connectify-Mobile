package com.connectify.connectifyNow.models
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.cloudinary.android.policy.GlobalUploadPolicy
import com.connectify.connectifyNow.BuildConfig
import com.connectify.connectifyNow.base.MyApplication
import com.connectify.connectifyNow.utils.extensions.toFile
import java.io.File
import java.io.InputStream

interface CloudinaryUploadListener {
    fun onImageUploaded(imageUrl: String)
    fun onUploadFailed(error: String)
    fun onUploadStarted()
    fun onUploadProgress(progress: Int)
}

class CloudinaryModel {

    init {

        MyApplication.Globals.appContext?.let {
            MediaManager.get().globalUploadPolicy = GlobalUploadPolicy.defaultPolicy()
        }
    }

    fun uploadImage(
        uri: Uri,
        name: String,
        listener: CloudinaryUploadListener
    ) {
        val context = MyApplication.Globals.appContext ?: return
        val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
        val bitmap = BitmapFactory.decodeStream(inputStream)
        val file: File = bitmap.toFile(context, name)

        listener.onUploadStarted()

        MediaManager.get().upload(file.path)
            .option("folder", "images")
            .callback(object  : UploadCallback {
                override fun onStart(requestId: String?) {
                    Log.d("CloudinaryModel", "Upload started")
                }

                override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {
                    val progress = if (totalBytes > 0) ((bytes.toFloat() / totalBytes) * 100).toInt() else 0
                    listener.onUploadProgress(progress)
                }

                override fun onSuccess(requestId: String?, resultData: Map<*, *>) {
                    val secureUrl = resultData["secure_url"] as? String
                    if (secureUrl != null) {
                        Log.d("CloudinaryModel", "Upload success: $secureUrl")
                        listener.onImageUploaded(secureUrl)
                    } else {
                        listener.onUploadFailed("No URL returned from Cloudinary")
                    }
                }

                override fun onError(requestId: String?, error: ErrorInfo?) {
                    Log.e("CloudinaryModel", "Upload error: ${error?.description}")
                    listener.onUploadFailed(error?.description ?: "Unknown error")
                }

                override fun onReschedule(requestId: String?, error: ErrorInfo?) {
                    Log.w("CloudinaryModel", "Upload rescheduled: ${error?.description}")
                }

            })
            .dispatch()
    }
}