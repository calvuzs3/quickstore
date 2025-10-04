package net.calvuz.quickstore.domain.usecase.movement

import net.calvuz.quickstore.domain.model.Movement
import net.calvuz.quickstore.domain.model.MovementType
import net.calvuz.quickstore.domain.repository.MovementRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use Case per recuperare movimentazioni dal magazzino
 */
class GetMovementsUseCase @Inject constructor(
    private val movementRepository: MovementRepository
) {
    /**
     * Ottiene una movimentazione per UUID
     */
    suspend fun getByUuid(uuid: String): Result<Movement?> {
        if (uuid.isBlank()) {
            return Result.failure(IllegalArgumentException("UUID cannot be blank"))
        }
        return movementRepository.getMovementByUuid(uuid)
    }

    /**
     * Osserva tutte le movimentazioni di un articolo
     * (ordinate per timestamp DESC - più recenti prima)
     */
    fun observeByArticle(articleUuid: String): Flow<List<Movement>> {
        return movementRepository.observeMovementsByArticle(articleUuid)
    }

    /**
     * Ottiene tutte le movimentazioni di un articolo
     */
    suspend fun getByArticle(articleUuid: String): Result<List<Movement>> {
        return movementRepository.getMovementsByArticle(articleUuid)
    }

    /**
     * Osserva tutte le movimentazioni (storico generale)
     */
    fun observeAll(): Flow<List<Movement>> {
        return movementRepository.observeAllMovements()
    }

    /**
     * Osserva movimentazioni filtrate per tipo
     */
    fun observeByType(type: MovementType): Flow<List<Movement>> {
        return movementRepository.observeMovementsByType(type)
    }

    /**
     * Osserva movimentazioni in un intervallo di tempo
     *
     * @param startTimestamp Timestamp UTC inizio (milliseconds)
     * @param endTimestamp Timestamp UTC fine (milliseconds)
     */
    fun observeByDateRange(
        startTimestamp: Long,
        endTimestamp: Long
    ): Flow<List<Movement>> {
        return movementRepository.observeMovementsByDateRange(startTimestamp, endTimestamp)
    }

    /**
     * Osserva solo le entrate
     */
    fun observeIncoming(): Flow<List<Movement>> {
        return observeByType(MovementType.IN)
    }

    /**
     * Osserva solo le uscite
     */
    fun observeOutgoing(): Flow<List<Movement>> {
        return observeByType(MovementType.OUT)
    }
}