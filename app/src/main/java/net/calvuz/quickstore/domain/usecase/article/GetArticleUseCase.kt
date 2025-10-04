package net.calvuz.quickstore.domain.usecase.article

import net.calvuz.quickstore.domain.model.Article
import net.calvuz.quickstore.domain.repository.ArticleRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use Case per recuperare articoli dal magazzino
 */
class GetArticleUseCase @Inject constructor(
    private val articleRepository: ArticleRepository
) {
    /**
     * Ottiene un articolo per UUID
     */
    suspend fun getByUuid(uuid: String): Result<Article?> {
        if (uuid.isBlank()) {
            return Result.failure(IllegalArgumentException("UUID cannot be blank"))
        }
        return articleRepository.getArticleByUuid(uuid)
    }

    /**
     * Osserva un articolo per UUID (Flow reattivo)
     */
    fun observeByUuid(uuid: String): Flow<Article?> {
        return articleRepository.observeArticleByUuid(uuid)
    }

    /**
     * Osserva tutti gli articoli
     */
    fun observeAll(): Flow<List<Article>> {
        return articleRepository.observeAllArticles()
    }

    /**
     * Cerca articoli per nome
     */
    fun searchByName(query: String): Flow<List<Article>> {
        return articleRepository.searchArticlesByName(query.trim())
    }

    /**
     * Ottiene articoli per categoria
     */
    fun getByCategory(category: String): Flow<List<Article>> {
        return articleRepository.getArticlesByCategory(category.trim())
    }

    /**
     * Ottiene il conteggio totale degli articoli
     */
    suspend fun getCount(): Result<Int> {
        return articleRepository.getArticlesCount()
    }
}