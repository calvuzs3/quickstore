package net.calvuz.quickstore.data.repository

import net.calvuz.quickstore.data.local.database.ArticleDao
import net.calvuz.quickstore.data.local.database.InventoryDao
import net.calvuz.quickstore.data.local.entity.InventoryEntity
import net.calvuz.quickstore.data.mapper.ArticleMapper
import net.calvuz.quickstore.data.mapper.InventoryMapper
import net.calvuz.quickstore.domain.model.Article
import net.calvuz.quickstore.domain.model.Inventory
import net.calvuz.quickstore.domain.repository.ArticleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Implementazione del repository per articoli e inventario
 */
class ArticleRepositoryImpl @Inject constructor(
    private val articleDao: ArticleDao,
    private val inventoryDao: InventoryDao
) : ArticleRepository {

    override suspend fun insertArticle(article: Article, initialQuantity: Double): Result<Unit> {
        return try {
            // Inserisci articolo
            articleDao.insert(ArticleMapper.toEntity(article))

            // Crea inventario iniziale
            val inventory = InventoryEntity(
                articleUuid = article.uuid,
                currentQuantity = initialQuantity,
                lastMovementAt = article.createdAt
            )
            inventoryDao.insert(inventory)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateArticle(article: Article): Result<Unit> {
        return try {
            articleDao.update(ArticleMapper.toEntity(article))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteArticle(articleUuid: String): Result<Unit> {
        return try {
            val article = articleDao.getByUuid(articleUuid)
            if (article != null) {
                articleDao.delete(article)
                Result.success(Unit)
            } else {
                Result.failure(IllegalArgumentException("Article not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getArticleByUuid(uuid: String): Result<Article?> {
        return try {
            val entity = articleDao.getByUuid(uuid)
            val article = entity?.let { ArticleMapper.toDomain(it) }
            Result.success(article)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getByUuid(uuid: String): Result<Article?> {
        return getArticleByUuid(uuid)
    }

    override fun observeArticleByUuid(uuid: String): Flow<Article?> {
        return articleDao.observeByUuid(uuid).map { entity ->
            entity?.let { ArticleMapper.toDomain(it) }
        }
    }

    override fun observeAllArticles(): Flow<List<Article>> {
        return articleDao.observeAll().map { entities ->
            ArticleMapper.toDomainList(entities)
        }
    }

    override fun searchArticlesByName(query: String): Flow<List<Article>> {
        return articleDao.searchByName(query).map { entities ->
            ArticleMapper.toDomainList(entities)
        }
    }

    override fun getArticlesByCategory(category: String): Flow<List<Article>> {
        return articleDao.getByCategory(category).map { entities ->
            ArticleMapper.toDomainList(entities)
        }
    }

    override suspend fun getInventory(articleUuid: String): Result<Inventory?> {
        return try {
            val entity = inventoryDao.getByArticleUuid(articleUuid)
            val inventory = entity?.let { InventoryMapper.toDomain(it) }
            Result.success(inventory)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun observeInventory(articleUuid: String): Flow<Inventory?> {
        return inventoryDao.observeByArticleUuid(articleUuid).map { entity ->
            entity?.let { InventoryMapper.toDomain(it) }
        }
    }

    override suspend fun getArticlesCount(): Result<Int> {
        return try {
            val count = articleDao.getCount()
            Result.success(count)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}