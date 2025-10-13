package net.calvuz.quickstore.data.local.database

import androidx.room.*
import net.calvuz.quickstore.data.local.entity.ArticleEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO per operazioni sulla tabella articles
 */
@Dao
interface ArticleDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(article: ArticleEntity)

    @Update
    suspend fun update(article: ArticleEntity)

    @Delete
    suspend fun delete(article: ArticleEntity)

    @Query("SELECT * FROM articles WHERE uuid = :uuid")
    suspend fun getByUuid(uuid: String): ArticleEntity?

    @Query("SELECT * FROM articles WHERE uuid = :uuid")
    fun observeByUuid(uuid: String): Flow<ArticleEntity?>

    @Query("SELECT * FROM articles ORDER BY name ASC")
    fun observeAll(): Flow<List<ArticleEntity>>

    @Query("SELECT * FROM articles ORDER BY name ASC")
    suspend fun getAll(): List<ArticleEntity>

    @Query("SELECT * FROM articles WHERE name LIKE '%' || :searchQuery || '%' ORDER BY name ASC")
    fun searchByName(searchQuery: String): List<ArticleEntity>

    /**
     * Recupera articoli per categoria
     */
    @Query("SELECT * FROM articles WHERE category = :category ORDER BY name ASC")
    suspend fun getByCategory(category: String): List<ArticleEntity>

    /**
     * Recupera articolo per SKU
     */
    @Query("SELECT * FROM articles WHERE sku = :sku")
    suspend fun getBySku(sku: String): ArticleEntity?

    /**
     * Recupera articolo per barcode
     */
    @Query("SELECT * FROM articles WHERE barcode = :barcode")
    suspend fun getByBarcode(barcode: String): ArticleEntity?

    /**
     * Conta articoli totali
     */
    @Query("SELECT COUNT(*) FROM articles")
    suspend fun count(): Int

    @Query("SELECT COUNT(*) FROM articles")
    suspend fun getCount(): Int

    /**
     * Conta articoli per categoria
     */
    @Query("SELECT COUNT(*) FROM articles WHERE category = :category")
    suspend fun countByCategory(category: String): Int

    /**
     * Verifica esistenza articolo per UUID
     */
    @Query("SELECT EXISTS(SELECT 1 FROM articles WHERE uuid = :uuid)")
    suspend fun exists(uuid: String): Boolean

    /**
     * Recupera tutte le categorie uniche
     */
    @Query("SELECT DISTINCT category FROM articles WHERE category != '' ORDER BY category ASC")
    suspend fun getAllCategories(): List<String>
}