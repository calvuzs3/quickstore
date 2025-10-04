package net.calvuz.quickstore.domain.usecase.article

import net.calvuz.quickstore.domain.model.Article
import net.calvuz.quickstore.domain.repository.ArticleRepository
import java.util.UUID
import javax.inject.Inject

/**
 * Use Case per aggiungere un nuovo articolo con inventario iniziale
 */
class AddArticleUseCase @Inject constructor(
    private val articleRepository: ArticleRepository
) {
    /**
     * Aggiunge un nuovo articolo al magazzino
     *
     * @param name Nome articolo (obbligatorio)
     * @param description Descrizione (opzionale)
     * @param unitOfMeasure Unità di misura (es: "PZ", "KG", "L")
     * @param category Categoria (opzionale)
     * @param initialQuantity Quantità iniziale in magazzino
     * @return Result con Article creato, errore altrimenti
     */
    suspend operator fun invoke(
        name: String,
        description: String? = null,
        unitOfMeasure: String,
        category: String? = null,
        initialQuantity: Double = 0.0
    ): Result<Article> {
        // Validazione input
        if (name.isBlank()) {
            return Result.failure(IllegalArgumentException("Article name cannot be blank"))
        }

        if (unitOfMeasure.isBlank()) {
            return Result.failure(IllegalArgumentException("Unit of measure cannot be blank"))
        }

        if (initialQuantity < 0) {
            return Result.failure(IllegalArgumentException("Initial quantity cannot be negative"))
        }

        // Crea articolo
        val currentTime = System.currentTimeMillis()
        val article = Article(
            uuid = UUID.randomUUID().toString(),
            name = name.trim(),
            description = description?.trim(),
            unitOfMeasure = unitOfMeasure.trim().uppercase(),
            category = category?.trim(),
            createdAt = currentTime,
            updatedAt = currentTime
        )

        // Salva nel repository
        return articleRepository.insertArticle(article, initialQuantity)
            .map { article }
    }
}