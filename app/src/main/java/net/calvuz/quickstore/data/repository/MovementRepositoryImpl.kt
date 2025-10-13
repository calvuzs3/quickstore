package net.calvuz.quickstore.data.repository

import androidx.room.withTransaction
import net.calvuz.quickstore.data.local.database.QuickStoreDatabase
import net.calvuz.quickstore.data.local.database.MovementDao
import net.calvuz.quickstore.data.local.database.InventoryDao
import net.calvuz.quickstore.data.mapper.MovementMapper
import net.calvuz.quickstore.domain.model.Movement
import net.calvuz.quickstore.domain.model.MovementType
import net.calvuz.quickstore.domain.repository.MovementRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject


/**
 * Implementazione del repository per movimentazioni
 */
class MovementRepositoryImpl @Inject constructor(
    private val database: QuickStoreDatabase,
    private val movementDao: MovementDao,
    private val inventoryDao: InventoryDao,
    private val movementMapper: MovementMapper
) : MovementRepository {

    override suspend fun addMovement(movement: Movement): Result<Unit> {
        return try {
            // Operazione transazionale: inserisci movimento + aggiorna inventario
            database.withTransaction {
                // Inserisci movimento
                movementDao.insert(movementMapper.toEntity(movement))

                // Aggiorna inventario
                val inventory = inventoryDao.getByArticleUuid(movement.articleUuid)
                    ?: throw IllegalStateException("Inventory not found for article ${movement.articleUuid}")

                val newQuantity = when (movement.type) {
                    MovementType.IN -> inventory.currentQuantity + movement.quantity
                    MovementType.OUT -> {
                        val result = inventory.currentQuantity - movement.quantity
                        if (result < 0) {
                            throw IllegalArgumentException("Insufficient quantity. Current: ${inventory.currentQuantity}, Requested: ${movement.quantity}")
                        }
                        result
                    }
                }

                val updatedInventory = inventory.copy(
                    currentQuantity = newQuantity,
                    lastMovementAt = movement.createdAt
                )

                inventoryDao.update(updatedInventory)
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Recupera tutti i movimenti
     */
    override suspend fun getAllMovements(): Result<List<Movement>> {
        TODO("Not yet implemented")
    }

    override suspend fun getMovementByUuid(uuid: String): Result<Movement?> {
        return try {
            val entity = movementDao.getById(uuid)
            val movement = entity?.let { movementMapper.toDomain(it) }
            Result.success(movement)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun observeMovementsByArticle(articleUuid: String): Flow<List<Movement>> {
        return movementDao.observeByArticleUuid(articleUuid).map { entities ->
            movementMapper.toDomainList(entities)
        }
    }

    override suspend fun getMovementsByArticle(articleUuid: String): Result<List<Movement>> {
        return try {
            val entities = movementDao.getByArticleUuid(articleUuid)
            val movements = movementMapper.toDomainList(entities)
            Result.success(movements)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Recupera ultimi N movimenti ordinati per data (desc)
     */
    override suspend fun getRecentMovements(limit: Int): Result<List<Movement>> {
        return try {
            val entities = movementDao.getRecentMovements(limit)
            val movements = entities.map { movementMapper.toDomain(it) }
            Result.success(movements)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun observeAllMovements(): Flow<List<Movement>> {
        return movementDao.observeAll().map { entities ->
            movementMapper.toDomainList(entities)
        }
    }

    override fun observeMovementsByType(type: MovementType): Flow<List<Movement>> {
        return movementDao.observeByType(type).map { entities ->
            movementMapper.toDomainList(entities)
        }
    }

    override fun observeMovementsByDateRange(
        startTimestamp: Long,
        endTimestamp: Long
    ): Flow<List<Movement>> {
        return movementDao.observeByDateRange(startTimestamp, endTimestamp).map { entities ->
            movementMapper.toDomainList(entities)
        }
    }

    override suspend fun deleteMovement(uuid: String): Result<Unit> {
        return try {
            val movement = movementDao.getById(uuid)
            if (movement != null) {
                movementDao.delete(movement)
                Result.success(Unit)
            } else {
                Result.failure(IllegalArgumentException("Movement not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}