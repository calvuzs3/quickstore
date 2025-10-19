package net.calvuz.quickstore.data.mapper

import net.calvuz.quickstore.data.local.entity.ArticleEntity
import net.calvuz.quickstore.domain.model.Article
import javax.inject.Inject

/**
 * Mapper per convertire tra ArticleEntity (data layer) e Article (domain layer)
 */
class ArticleMapper @Inject constructor() {

    /**
     * Converte da Entity a Domain Model
     */
    fun toDomain(entity: ArticleEntity): Article {
        return Article(
            uuid = entity.uuid,
            name = entity.name,
            description = entity.description,
            category = entity.category,
            unitOfMeasure = entity.unitOfMeasure,
            reorderLevel = entity.reorderLevel,
            notes = entity.notes,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt
        )
    }

    /**
     * Converte da Domain Model a Entity
     */
    fun toEntity(domain: Article): ArticleEntity {
        return ArticleEntity(
            uuid = domain.uuid,
            name = domain.name,
            description = domain.description,
            category = domain.category,
            unitOfMeasure = domain.unitOfMeasure,
            reorderLevel = domain.reorderLevel,
            notes = domain.notes,
            createdAt = domain.createdAt,
            updatedAt = domain.updatedAt
        )
    }

    /**
     * Lista Entity → Lista Domain
     */
    fun toDomainList(entities: List<ArticleEntity>): List<Article> {
        return entities.map { toDomain(it) }
    }

    /**
     * Lista Domain → Lista Entity
     */
    fun toEntityList(domains: List<Article>): List<ArticleEntity> {
        return domains.map { toEntity(it) }
    }
}