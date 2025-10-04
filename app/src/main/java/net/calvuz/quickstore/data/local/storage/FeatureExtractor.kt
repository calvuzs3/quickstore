package net.calvuz.quickstore.data.opencv

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import org.opencv.android.Utils
import org.opencv.core.*
import org.opencv.features2d.ORB
import org.opencv.imgproc.Imgproc
import java.io.ByteArrayOutputStream
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Estrattore di features OpenCV per il riconoscimento immagini
 * Utilizza ORB (Oriented FAST and Rotated BRIEF) algorithm
 */
@Singleton
class FeatureExtractor @Inject constructor(
    private val openCVManager: OpenCVManager
) {
    private val orb: ORB by lazy {
        ORB.create(
            MAX_FEATURES,           // Numero massimo di features
            1.2f,                   // Scale factor
            8,                      // Numero di livelli piramide
            31,                     // Edge threshold
            0,                      // First level
            2,                      // WTA_K
            ORB.HARRIS_SCORE,       // Score type
            31,                     // Patch size
            20                      // Fast threshold
        )
    }

    /**
     * Estrae features da un'immagine
     *
     * @param imageData ByteArray dell'immagine (JPEG/PNG)
     * @return ByteArray dei descriptors serializzati, null se fallisce
     */
    fun extractFeatures(imageData: ByteArray): Result<ByteArray> {
        if (!openCVManager.isInitialized()) {
            return Result.failure(IllegalStateException("OpenCV not initialized"))
        }

        return try {
            // Decodifica immagine
            val bitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.size)
                ?: return Result.failure(IllegalArgumentException("Invalid image data"))

            val features = extractFeaturesFromBitmap(bitmap)
            bitmap.recycle()

            features

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Estrae features da un Bitmap
     */
    fun extractFeaturesFromBitmap(bitmap: Bitmap): Result<ByteArray> {
        if (!openCVManager.isInitialized()) {
            return Result.failure(IllegalStateException("OpenCV not initialized"))
        }

        var mat: Mat? = null
        var grayMat: Mat? = null
        var descriptors: Mat? = null

        return try {
            // Converti Bitmap a Mat
            mat = Mat()
            Utils.bitmapToMat(bitmap, mat)

            // Converti a grayscale
            grayMat = Mat()
            Imgproc.cvtColor(mat, grayMat, Imgproc.COLOR_RGBA2GRAY)

            // Estrai keypoints e descriptors
            val keypoints = MatOfKeyPoint()
            descriptors = Mat()

            orb.detectAndCompute(grayMat, Mat(), keypoints, descriptors)

            // Verifica che ci siano features
            if (descriptors.empty() || keypoints.empty()) {
                return Result.failure(IllegalStateException("No features detected in image"))
            }

            // Serializza descriptors
            val serialized = serializeMat(descriptors)
            Result.success(serialized)

        } catch (e: Exception) {
            Result.failure(e)
        } finally {
            // Cleanup
            mat?.release()
            grayMat?.release()
            descriptors?.release()
        }
    }

    /**
     * Deserializza descriptors da ByteArray a Mat
     */
    fun deserializeDescriptors(data: ByteArray): Result<Mat> {
        return try {
            val mat = deserializeMat(data)
            Result.success(mat)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Serializza Mat in ByteArray
     */
    private fun serializeMat(mat: Mat): ByteArray {
        val outputStream = ByteArrayOutputStream()

        // Scrivi dimensioni
        outputStream.write(intToBytes(mat.rows()))
        outputStream.write(intToBytes(mat.cols()))
        outputStream.write(intToBytes(mat.type()))

        // Scrivi dati
        val data = ByteArray(mat.total().toInt() * mat.elemSize().toInt())
        mat.get(0, 0, data)
        outputStream.write(data)

        return outputStream.toByteArray()
    }

    /**
     * Deserializza ByteArray in Mat
     */
    private fun deserializeMat(data: ByteArray): Mat {
        var offset = 0

        // Leggi dimensioni
        val rows = bytesToInt(data, offset)
        offset += 4
        val cols = bytesToInt(data, offset)
        offset += 4
        val type = bytesToInt(data, offset)
        offset += 4

        // Crea Mat
        val mat = Mat(rows, cols, type)

        // Leggi dati
        val matData = data.copyOfRange(offset, data.size)
        mat.put(0, 0, matData)

        return mat
    }

    /**
     * Converte Int in ByteArray (big endian)
     */
    private fun intToBytes(value: Int): ByteArray {
        return byteArrayOf(
            (value shr 24).toByte(),
            (value shr 16).toByte(),
            (value shr 8).toByte(),
            value.toByte()
        )
    }

    /**
     * Converte ByteArray in Int (big endian)
     */
    private fun bytesToInt(bytes: ByteArray, offset: Int): Int {
        return ((bytes[offset].toInt() and 0xFF) shl 24) or
                ((bytes[offset + 1].toInt() and 0xFF) shl 16) or
                ((bytes[offset + 2].toInt() and 0xFF) shl 8) or
                (bytes[offset + 3].toInt() and 0xFF)
    }

    companion object {
        private const val MAX_FEATURES = 500 // Numero massimo di features per immagine
    }
}