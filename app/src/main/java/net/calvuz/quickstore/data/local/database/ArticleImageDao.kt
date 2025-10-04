package net.calvuz.quickstore.data.local.database

import androidx.room.*
import net.calvuz.quickstore.data.local.entity.ArticleImageEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO per operazioni sulla tabella article_images
 */
@Dao
interface ArticleImageDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(image: ArticleImageEntity): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertAll(images: List<ArticleImageEntity>)

    @Delete
    suspend fun delete(image: ArticleImageEntity)

    @Query("DELETE FROM article_images WHERE id = :imageId")
    suspend fun deleteById(imageId: Long)

    @Query("SELECT * FROM article_images WHERE article_uuid = :articleUuid")
    suspend fun getByArticleUuid(articleUuid: String): List<ArticleImageEntity>

    @Query("SELECT * FROM article_images WHERE article_uuid = :articleUuid")
    fun observeByArticleUuid(articleUuid: String): Flow<List<ArticleImageEntity>>

    @Query("SELECT * FROM article_images WHERE id = :imageId")
    suspend fun getById(imageId: Long): ArticleImageEntity?

    @Query("SELECT * FROM article_images")
    suspend fun getAll(): List<ArticleImageEntity>

    @Query("SELECT COUNT(*) FROM article_images WHERE article_uuid = :articleUuid")
    suspend fun getCountByArticleUuid(articleUuid: String): Int
}