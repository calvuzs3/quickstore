package net.calvuz.quickstore.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import net.calvuz.quickstore.data.local.entity.ArticleEntity
import net.calvuz.quickstore.data.local.entity.ArticleImageEntity
import net.calvuz.quickstore.data.local.entity.InventoryEntity
import net.calvuz.quickstore.data.local.entity.MovementEntity

/**
 * Database Room principale dell'applicazione
 */
@Database(
    entities = [
        ArticleEntity::class,
        InventoryEntity::class,
        MovementEntity::class,
        ArticleImageEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class QuickStoreDatabase: RoomDatabase() {

    abstract fun articleDao(): ArticleDao
    abstract fun inventoryDao(): InventoryDao
    abstract fun movementDao(): MovementDao
    abstract fun articleImageDao(): ArticleImageDao

    companion object {
        const val DATABASE_NAME = "warehouse_database"
    }
}