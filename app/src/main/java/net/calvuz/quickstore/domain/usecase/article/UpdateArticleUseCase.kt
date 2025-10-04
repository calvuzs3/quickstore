package net.calvuz.quickstore.domain.usecase.article

import net.calvuz.quickstore.domain.model.Article
import net.calvuz.quickstore.domain.repository.ArticleRepository
import javax.inject.Inject

/**
 * Use Case per aggiornare i dati anagrafici di un articolo
 *
 * Note: Questo aggiorna solo i dati dell'articolo, NON la quantità in magazzino.
 * Per modificare la quantità, usare AddMovementUseCase.
 */
class UpdateArticleUseCase @Inject constructor(
    private val articleRepository: ArticleRepository
) {
    /**
     * Aggiorna un articolo esistente
     *
     * @param article Articolo con dati aggiornati
     * @return Result con Unit in caso di successo, errore altrimenti
     */
    suspend operator fun invoke(article: Article): Result<Unit> {
        // Validazione
        if (article.name.isBlank()) {
            return Result.failure(IllegalArgumentException("Article name cannot be blank"))
        }

        if (article.unitOfMeasure.isBlank()) {
            return Result.failure(IllegalArgumentException("Unit of measure cannot be blank"))
        }

        // Aggiorna il timestamp
        val updatedArticle = article.copy(
            updatedAt = System.currentTimeMillis()
        )

        return articleRepository.updateArticle(updatedArticle)
    }

    /**
     * Aggiorna solo alcuni campi di un articolo
     */
    suspend fun updateFields(
        uuid: String,
        name: String? = null,
        description: String? = null,
        unitOfMeasure: String? = null,
        category: String? = null
    ): Result<Unit> {
        // Recupera l'articolo esistente
        val existingArticle = articleRepository.getArticleByUuid(uuid)
            .getOrElse {
                return Result.failure(IllegalArgumentException("Article not found"))
            } ?: return Result.failure(IllegalArgumentException("Article not found"))

        // Crea versione aggiornata
        val updatedArticle = existingArticle.copy(
            name = name?.trim() ?: existingArticle.name,
            description = description?.trim() ?: existingArticle.description,
            unitOfMeasure = unitOfMeasure?.trim()?.uppercase() ?: existingArticle.unitOfMeasure,
            category = category?.trim() ?: existingArticle.category,
            updatedAt = System.currentTimeMillis()
        )

        return articleRepository.updateArticle(updatedArticle)
    }
}