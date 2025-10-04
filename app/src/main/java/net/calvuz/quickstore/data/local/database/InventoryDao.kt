package net.calvuz.quickstore.data.local.database

import androidx.room.*
import net.calvuz.quickstore.data.local.entity.InventoryEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO per operazioni sulla tabella inventory
 */
@Dao
interface InventoryDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(inventory: InventoryEntity)

    @Update
    suspend fun update(inventory: InventoryEntity)

    @Query("SELECT * FROM inventory WHERE article_uuid = :articleUuid")
    suspend fun getByArticleUuid(articleUuid: String): InventoryEntity?

    @Query("SELECT * FROM inventory WHERE article_uuid = :articleUuid")
    fun observeByArticleUuid(articleUuid: String): Flow<InventoryEntity?>

    @Query("SELECT current_quantity FROM inventory WHERE article_uuid = :articleUuid")
    suspend fun getQuantity(articleUuid: String): Double?

    @Query("""
        UPDATE inventory 
        SET current_quantity = :quantity, last_movement_at = :lastMovementAt 
        WHERE article_uuid = :articleUuid
    """)
    suspend fun updateQuantity(articleUuid: String, quantity: Double, lastMovementAt: Long)

    @Query("""
        SELECT * FROM inventory 
        WHERE current_quantity <= :threshold 
        ORDER BY current_quantity ASC
    """)
    fun observeLowStock(threshold: Double): Flow<List<InventoryEntity>>

    @Query("SELECT * FROM inventory")
    fun observeAll(): Flow<List<InventoryEntity>>
}