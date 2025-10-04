package net.calvuz.quickstore.domain.usecase.recognition

import net.calvuz.quickstore.domain.model.Article
import net.calvuz.quickstore.domain.repository.ArticleRepository
import net.calvuz.quickstore.domain.repository.ImageRecognitionRepository
import javax.inject.Inject

/**
 * Use Case per cercare articoli tramite riconoscimento immagine
 *
 * Utilizza OpenCV per confrontare l'immagine fornita con quelle salvate
 * e ritorna gli articoli corrispondenti ordinati per similarità.
 */
class SearchArticleByImageUseCase @Inject constructor(
    private val imageRecognitionRepository: ImageRecognitionRepository,
    private val articleRepository: ArticleRepository
) {
    /**
     * Cerca articoli tramite immagine
     *
     * @param imageData ByteArray dell'immagine da cercare (JPEG/PNG)
     * @param threshold Soglia di similarità (0.0-1.0), default 0.7
     * @return Result con lista di articoli ordinati per similarità
     */
    suspend operator fun invoke(
        imageData: ByteArray,
        threshold: Double = 0.7
    ): Result<List<Article>> {
        // Validazione
        if (imageData.isEmpty()) {
            return Result.failure(IllegalArgumentException("Image data cannot be empty"))
        }

        if (threshold < 0.0 || threshold > 1.0) {
            return Result.failure(
                IllegalArgumentException("Threshold must be between 0.0 and 1.0")
            )
        }

        // Cerca UUID articoli tramite image matching
        val articleUuids = imageRecognitionRepository.searchArticlesByImage(imageData, threshold)
            .getOrElse {
                return Result.failure(it)
            }

        // Se non ci sono match, ritorna lista vuota
        if (articleUuids.isEmpty()) {
            return Result.success(emptyList())
        }

        // Recupera gli articoli completi
        val articles = mutableListOf<Article>()
        for (uuid in articleUuids) {
            val article = articleRepository.getArticleByUuid(uuid)
                .getOrNull()

            if (article != null) {
                articles.add(article)
            }
        }

        return Result.success(articles)
    }

    /**
     * Cerca articoli con soglia personalizzata bassa (più permissiva)
     */
    suspend fun searchLoose(imageData: ByteArray): Result<List<Article>> {
        return invoke(imageData, threshold = 0.5)
    }

    /**
     * Cerca articoli con soglia personalizzata alta (più restrittiva)
     */
    suspend fun searchStrict(imageData: ByteArray): Result<List<Article>> {
        return invoke(imageData, threshold = 0.85)
    }
}