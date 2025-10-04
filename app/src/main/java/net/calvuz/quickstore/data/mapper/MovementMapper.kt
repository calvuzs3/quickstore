package net.calvuz.quickstore.data.mapper

import net.calvuz.quickstore.data.local.entity.MovementEntity
import net.calvuz.quickstore.domain.model.Movement

/**
 * Mapper per convertire tra MovementEntity (data layer) e Movement (domain layer)
 */
object MovementMapper {

    /**
     * Converte da Entity a Domain Model
     */
    fun toDomain(entity: MovementEntity): Movement {
        return Movement(
            uuid = entity.uuid,
            articleUuid = entity.articleUuid,
            type = entity.type,
            quantity = entity.quantity,
            note = entity.note,
            timestamp = entity.timestamp
        )
    }

    /**
     * Converte da Domain Model a Entity
     */
    fun toEntity(domain: Movement): MovementEntity {
        return MovementEntity(
            uuid = domain.uuid,
            articleUuid = domain.articleUuid,
            type = domain.type,
            quantity = domain.quantity,
            note = domain.note,
            timestamp = domain.timestamp
        )
    }

    /**
     * Converte una lista di Entity in lista di Domain Models
     */
    fun toDomainList(entities: List<MovementEntity>): List<Movement> {
        return entities.map { toDomain(it) }
    }
}