package net.calvuz.quickstore

import android.app.Application
import android.util.Log
import dagger.hilt.android.HiltAndroidApp

import net.calvuz.quickstore.data.opencv.OpenCVManager
import javax.inject.Inject

@HiltAndroidApp
class QuickStoreApplication: Application() {
    @Inject
    lateinit var openCVManager: OpenCVManager

    override fun onCreate() {
        super.onCreate()

        // Inizializza OpenCV
        initializeOpenCV()
    }

    private fun initializeOpenCV() {
        val success = openCVManager.initialize()

        if (success) {
            Log.i(TAG, "OpenCV initialized successfully - Version: ${openCVManager.getOpenCVVersion()}")
        } else {
            Log.e(TAG, "Failed to initialize OpenCV - Image recognition features will not work")
        }
    }

    companion object {
        private const val TAG = "QuickStoreApp"
    }
}