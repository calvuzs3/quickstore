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
    fun searchByName(searchQuery: String): Flow<List<ArticleEntity>>

    @Query("SELECT * FROM articles WHERE category = :category ORDER BY name ASC")
    fun getByCategory(category: String): Flow<List<ArticleEntity>>

    @Query("SELECT COUNT(*) FROM articles")
    suspend fun getCount(): Int
}