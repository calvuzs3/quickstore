package net.calvuz.quickstore.data.opencv

import org.opencv.core.Mat
import org.opencv.core.MatOfDMatch
import org.opencv.features2d.BFMatcher
import org.opencv.features2d.DescriptorMatcher
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Matcher per confrontare features OpenCV
 * Utilizza Brute-Force Matcher con Hamming distance (ottimale per ORB)
 */
@Singleton
class ImageMatcher @Inject constructor(
    private val openCVManager: OpenCVManager
) {
    private val matcher: BFMatcher by lazy {
        BFMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMING, true)
    }

    /**
     * Confronta due set di features e calcola similarità
     *
     * @param descriptors1 Descriptors della prima immagine
     * @param descriptors2 Descriptors della seconda immagine
     * @return Similarity score (0.0 - 1.0), dove 1.0 = match perfetto
     */
    fun matchFeatures(descriptors1: Mat, descriptors2: Mat): Result<Double> {
        if (!openCVManager.isInitialized()) {
            return Result.failure(IllegalStateException("OpenCV not initialized"))
        }

        return try {
            // Verifica che ci siano descriptors
            if (descriptors1.empty() || descriptors2.empty()) {
                return Result.success(0.0)
            }

            // Match descriptors
            val matches = MatOfDMatch()
            matcher.match(descriptors1, descriptors2, matches)

            if (matches.empty()) {
                return Result.success(0.0)
            }

            // Calcola similarity score basato su distanza e numero di match
            val matchesList = matches.toList()
            val similarity = calculateSimilarity(
                matchesList,
                descriptors1.rows(),
                descriptors2.rows()
            )

            matches.release()

            Result.success(similarity)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Confronta un set di features con una lista di altri set
     * Ritorna gli indici ordinati per similarità (più simili prima)
     *
     * @param queryDescriptors Descriptors dell'immagine da cercare
     * @param databaseDescriptors Lista di descriptors del database
     * @param threshold Soglia minima di similarità (0.0 - 1.0)
     * @return Lista di indici ordinati per similarità decrescente
     */
    fun findBestMatches(
        queryDescriptors: Mat,
        databaseDescriptors: List<Mat>,
        threshold: Double
    ): Result<List<MatchResult>> {
        if (!openCVManager.isInitialized()) {
            return Result.failure(IllegalStateException("OpenCV not initialized"))
        }

        return try {
            val results = mutableListOf<MatchResult>()

            // Confronta con ogni set di descriptors nel database
            databaseDescriptors.forEachIndexed { index, dbDescriptors ->
                val similarity = matchFeatures(queryDescriptors, dbDescriptors)
                    .getOrElse { 0.0 }

                if (similarity >= threshold) {
                    results.add(MatchResult(index, similarity))
                }
            }

            // Ordina per similarità decrescente (più simili prima)
            results.sortByDescending { it.similarity }

            Result.success(results)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Calcola similarity score basato su match distance e count
     *
     * Algoritmo:
     * 1. Filtra good matches (distance < threshold)
     * 2. Calcola ratio: good_matches / min(features1, features2)
     * 3. Considera anche la quality delle distanze
     */
    private fun calculateSimilarity(
        matches: List<org.opencv.core.DMatch>,
        features1Count: Int,
        features2Count: Int
    ): Double {
        if (matches.isEmpty()) return 0.0

        // Filtra good matches (distanza < threshold)
        val goodMatches = matches.filter { it.distance < DISTANCE_THRESHOLD }

        if (goodMatches.isEmpty()) return 0.0

        // Calcola ratio di match
        val minFeatures = minOf(features1Count, features2Count)
        val matchRatio = goodMatches.size.toDouble() / minFeatures

        // Calcola quality score basato sulla distanza media
        val avgDistance = goodMatches.map { it.distance }.average()
        val maxDistance = DISTANCE_THRESHOLD.toDouble()
        val distanceQuality = 1.0 - (avgDistance / maxDistance)

        // Combina ratio e quality (weighted average)
        val similarity = (matchRatio * MATCH_RATIO_WEIGHT) +
                (distanceQuality * DISTANCE_QUALITY_WEIGHT)

        // Normalizza tra 0 e 1
        return similarity.coerceIn(0.0, 1.0)
    }

    /**
     * Risultato di un match
     */
    data class MatchResult(
        val index: Int,         // Indice nel database
        val similarity: Double  // Similarità (0.0 - 1.0)
    )

    companion object {
        // Soglia distanza Hamming per considerare un match "buono"
        private const val DISTANCE_THRESHOLD = 50f

        // Pesi per calcolo similarity
        private const val MATCH_RATIO_WEIGHT = 0.7      // 70% peso al numero di match
        private const val DISTANCE_QUALITY_WEIGHT = 0.3  // 30% peso alla qualità
    }
}