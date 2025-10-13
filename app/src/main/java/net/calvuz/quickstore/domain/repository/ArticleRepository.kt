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
     * Aggiunge un nuovo articolo con inventario iniziale
     * Operazione transazionale
     */
    suspend fun addArticle(article: Article, initialQuantity: Double): Result<String>

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
    suspend fun deleteArticle(uuid: String): Result<Unit>

    /**
     * Recupera tutti gli articoli
     */
    suspend fun getAll(): Result<List<Article>>

    /**
     * Ottiene un articolo per UUID
     */
    suspend fun getArticleByUuid(uuid: String): Result<Article?>

    /**
     * Ottiene un articolo per UUID
     */
    suspend fun getByUuid(uuid: String): Result<Article?>

    /**
     * Cerca articoli per nome
     */
    suspend fun searchByName(query: String): Result<List<Article>>

    /**
     * Recupera articoli per categoria
     */
    suspend fun getByCategory(category: String): Result<List<Article>>

    /**
     * Recupera articoli per SKU
     */
    suspend fun getBySku(sku: String): Result<Article?>

    /**
     * Recupera articoli per barcode
     */
    suspend fun getByBarcode(barcode: String): Result<Article?>

    /**
     * Osserva tutti gli articoli
     */
    fun observeAll(): Flow<List<Article>>

    /**
     * Osserva un articolo per UUID (Flow reattivo)
     */
    fun observeArticleByUuid(uuid: String): Flow<Article?>

    /**
     * Osserva tutti gli articoli ordinati per nome
     */
    fun observeAllArticles(): Flow<List<Article>>

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