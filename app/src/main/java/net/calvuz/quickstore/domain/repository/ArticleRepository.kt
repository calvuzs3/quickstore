package net.calvuz.quickstore.domain.repository

import net.calvuz.quickstore.domain.model.Article
import net.calvuz.quickstore.domain.model.Inventory
import kotlinx.coroutines.flow.Flow

/**
 * Repository Interface per gestione articoli e inventario
 *
 * Definisce i contratti per l'accesso ai dati degli articoli.
 * L'implementazione sarà nel data layer.
 */
interface ArticleRepository {

    /**
     * Inserisce un nuovo articolo con inventario iniziale
     * @return true se l'inserimento è riuscito, false altrimenti
     */
    suspend fun insertArticle(article: Article, initialQuantity: Double): Result<Unit>

    /**
     * Aggiorna i dati anagrafici di un articolo
     */
    suspend fun updateArticle(article: Article): Result<Unit>

    /**
     * Elimina un articolo (cascade su inventory, movements, images)
     */
    suspend fun deleteArticle(articleUuid: String): Result<Unit>

    /**
     * Ottiene un articolo per UUID
     */
    suspend fun getArticleByUuid(uuid: String): Result<Article?>

    /**
     * Ottiene un articolo per UUID
     */
    suspend fun getByUuid(uuid: String): Result<Article?>


    /**
     * Osserva un articolo per UUID (Flow reattivo)
     */
    fun observeArticleByUuid(uuid: String): Flow<Article?>

    /**
     * Osserva tutti gli articoli ordinati per nome
     */
    fun observeAllArticles(): Flow<List<Article>>

    /**
     * Cerca articoli per nome
     */
    fun searchArticlesByName(query: String): Flow<List<Article>>

    /**
     * Ottiene articoli per categoria
     */
    fun getArticlesByCategory(category: String): Flow<List<Article>>

    /**
     * Ottiene l'inventario di un articolo
     */
    suspend fun getInventory(articleUuid: String): Result<Inventory?>

    /**
     * Osserva l'inventario di un articolo
     */
    fun observeInventory(articleUuid: String): Flow<Inventory?>

    /**
     * Ottiene il conteggio totale degli articoli
     */
    suspend fun getArticlesCount(): Result<Int>
}