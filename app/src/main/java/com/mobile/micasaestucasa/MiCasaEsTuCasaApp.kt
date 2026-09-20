package com.mobile.micasaestucasa

import android.app.Application
import android.util.Log
import com.google.android.libraries.places.api.Places
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MiCasaEsTuCasaApp : Application() {
    override fun onCreate() {
        super.onCreate()
        try {
            if (!Places.isInitialized()) {
                Places.initialize(applicationContext, BuildConfig.MAPS_API_KEY)
            }
        } catch (e: Exception) {
            Log.e("MiCasaApp", "Places initialization failed", e)
        }
    }
}
