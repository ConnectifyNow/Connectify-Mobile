package com.connectify.connectifyNow.base

import android.app.Application
import android.content.Context
import com.cloudinary.android.MediaManager
import com.connectify.connectifyNow.BuildConfig

class MyApplication: Application() {

    object Globals {
        var appContext: Context? = null
    }

    override fun onCreate() {
        super.onCreate()
        val config = mapOf(
            "cloud_name" to BuildConfig.CLOUD_NAME,
            "api_key" to BuildConfig.API_KEY,
            "api_secret" to BuildConfig.API_SECRET
        )

        MediaManager.init(this, config)


        Globals.appContext = applicationContext
    }
}