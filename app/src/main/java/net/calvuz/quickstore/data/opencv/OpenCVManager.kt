package net.calvuz.quickstore.data.opencv

import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import org.opencv.android.OpenCVLoader
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manager per l'inizializzazione di OpenCV
 */
@Singleton
class OpenCVManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var isInitialized = false

    /**
     * Inizializza OpenCV
     * Deve essere chiamato all'avvio dell'app
     */
    fun initialize(): Boolean {
        if (isInitialized) {
            Log.d(TAG, "OpenCV already initialized")
            return true
        }

        return try {
            val success = OpenCVLoader.initDebug()
            if (success) {
                isInitialized = true
                Log.i(TAG, "OpenCV initialized successfully")
            } else {
                Log.e(TAG, "OpenCV initialization failed")
            }
            success
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing OpenCV", e)
            false
        }
    }

    /**
     * Verifica se OpenCV è stato inizializzato
     */
    fun isInitialized(): Boolean = isInitialized

    /**
     * Ottiene la versione di OpenCV
     */
    fun getOpenCVVersion(): String {
        return try {
            org.opencv.core.Core.getVersionString()
        } catch (e: Exception) {
            "Unknown"
        }
    }

    companion object {
        private const val TAG = "OpenCVManager"
    }
}