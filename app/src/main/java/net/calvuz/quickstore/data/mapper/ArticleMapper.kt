package net.calvuz.quickstore.data.mapper

import net.calvuz.quickstore.data.local.entity.ArticleEntity
import net.calvuz.quickstore.domain.model.Article

/**
 * Mapper per convertire tra ArticleEntity (data layer) e Article (domain layer)
 */
object ArticleMapper {

    /**
     * Converte da Entity a Domain Model
     */
    fun toDomain(entity: ArticleEntity): Article {
        return Article(
            uuid = entity.uuid,
            name = entity.name,
            description = entity.description,
            unitOfMeasure = entity.unitOfMeasure,
            category = entity.category,
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
            unitOfMeasure = domain.unitOfMeasure,
            category = domain.category,
            createdAt = domain.createdAt,
            updatedAt = domain.updatedAt
        )
    }

    /**
     * Converte una lista di Entity in lista di Domain Models
     */
    fun toDomainList(entities: List<ArticleEntity>): List<Article> {
        return entities.map { toDomain(it) }
    }
}