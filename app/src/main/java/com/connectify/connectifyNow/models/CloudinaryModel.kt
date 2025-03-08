package com.connectify.connectifyNow.models
import android.graphics.BitmapFactory
import android.net.Uri
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.cloudinary.android.policy.GlobalUploadPolicy
import com.connectify.connectifyNow.BuildConfig
import com.connectify.connectifyNow.base.MyApplication
import com.connectify.connectifyNow.utils.extensions.toFile
import java.io.File
import java.io.InputStream

class CloudinaryModel {

    init {
        val config = mapOf(
            "cloud_name" to BuildConfig.CLOUD_NAME,
            "api_key" to BuildConfig.API_KEY,
            "api_secret" to BuildConfig.API_SECRET
        )

        MyApplication.Globals.appContext?.let {
            MediaManager.init(it, config)
            MediaManager.get().globalUploadPolicy = GlobalUploadPolicy.defaultPolicy()
        }
    }

    fun uploadImage(
        uri: Uri,
        name: String,
    ) {
        val context = MyApplication.Globals.appContext ?: return
        val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
        val bitmap = BitmapFactory.decodeStream(inputStream)
        val file: File = bitmap.toFile(context, name)

        MediaManager.get().upload(file.path)
            .option("folder", "images")
            .callback(object  : UploadCallback {
                override fun onStart(requestId: String?) {

                }

                override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {

                }

                override fun onSuccess(requestId: String?, resultData: Map<*, *>) {
                }

                override fun onError(requestId: String?, error: ErrorInfo?) {
                }

                override fun onReschedule(requestId: String?, error: ErrorInfo?) {

                }

            })
            .dispatch()
    }
}