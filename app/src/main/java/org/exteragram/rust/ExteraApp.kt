package org.exteragram.rust

import android.app.Application
import android.util.Log
import org.exteragram.rust.core.ExteraBridge

class ExteraApp : Application() {
    override fun onCreate() {
        super.onCreate()
        try {
            System.loadLibrary("extera_core")
            ExteraBridge.init()
            Log.i("ExteraApp", "Extera Rust Core library loaded successfully.")
        } catch (e: UnsatisfiedLinkError) {
            Log.w("ExteraApp", "libextera_core.so not found, falling back to pure Kotlin mock mode: ${e.message}")
        }
    }
}
