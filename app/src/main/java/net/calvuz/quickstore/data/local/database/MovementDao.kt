package net.calvuz.quickstore.data.local.database

import androidx.room.*
import net.calvuz.quickstore.data.local.entity.MovementEntity
import net.calvuz.quickstore.data.local.entity.MovementType
import kotlinx.coroutines.flow.Flow

/**
 * DAO per operazioni sulla tabella movements
 */
@Dao
interface MovementDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(movement: MovementEntity)

    @Query("SELECT * FROM movements WHERE uuid = :uuid")
    suspend fun getByUuid(uuid: String): MovementEntity?

    @Query("SELECT * FROM movements WHERE article_uuid = :articleUuid ORDER BY timestamp DESC")
    fun observeByArticleUuid(articleUuid: String): Flow<List<MovementEntity>>

    @Query("SELECT * FROM movements WHERE article_uuid = :articleUuid ORDER BY timestamp DESC")
    suspend fun getByArticleUuid(articleUuid: String): List<MovementEntity>

    @Query("SELECT * FROM movements ORDER BY timestamp DESC LIMIT :limit")
    fun observeRecent(limit: Int = 10): Flow<List<MovementEntity>>

    @Query("""
        SELECT * FROM movements 
        WHERE timestamp >= :startTimestamp AND timestamp <= :endTimestamp 
        ORDER BY timestamp DESC
    """)
    fun observeByPeriod(startTimestamp: Long, endTimestamp: Long): Flow<List<MovementEntity>>

    @Query("""
        SELECT * FROM movements 
        WHERE article_uuid = :articleUuid AND type = :type 
        ORDER BY timestamp DESC
    """)
    fun observeByArticleAndType(articleUuid: String, type: MovementType): Flow<List<MovementEntity>>

    @Query("SELECT COUNT(*) FROM movements WHERE timestamp >= :todayStart")
    suspend fun getTodayCount(todayStart: Long): Int

    @Query("""
        SELECT SUM(CASE WHEN type = 'IN' THEN quantity ELSE -quantity END) 
        FROM movements 
        WHERE article_uuid = :articleUuid
    """)
    suspend fun calculateTotalQuantity(articleUuid: String): Double?
}