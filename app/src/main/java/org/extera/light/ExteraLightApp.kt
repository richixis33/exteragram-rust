package org.extera.light

import android.app.Application
import android.util.Log

class ExteraLightApp : Application() {
    override fun onCreate() {
        super.onCreate()
        try {
            System.loadLibrary("tdjni")
            Log.i("ExteraLightApp", "TDLib native library (libtdjni.so) loaded successfully.")
        } catch (e: UnsatisfiedLinkError) {
            Log.e("ExteraLightApp", "Failed to load libtdjni.so: ${e.message}")
        }
    }
}
