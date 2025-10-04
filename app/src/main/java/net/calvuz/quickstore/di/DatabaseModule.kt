package net.calvuz.quickstore.di

import android.content.Context
import androidx.room.Room
import net.calvuz.quickstore.data.local.database.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Modulo Hilt per il database Room
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideQuickStoreDatabase(
        @ApplicationContext context: Context
    ): QuickStoreDatabase {
        return Room.databaseBuilder(
            context,
            QuickStoreDatabase::class.java,
            QuickStoreDatabase.DATABASE_NAME
        )
            .fallbackToDestructiveMigration() // Solo per sviluppo, rimuovere in produzione
            .build()
    }

    @Provides
    @Singleton
    fun provideArticleDao(database: QuickStoreDatabase): ArticleDao {
        return database.articleDao()
    }

    @Provides
    @Singleton
    fun provideInventoryDao(database: QuickStoreDatabase): InventoryDao {
        return database.inventoryDao()
    }

    @Provides
    @Singleton
    fun provideMovementDao(database: QuickStoreDatabase): MovementDao {
        return database.movementDao()
    }

    @Provides
    @Singleton
    fun provideArticleImageDao(database: QuickStoreDatabase): ArticleImageDao {
        return database.articleImageDao()
    }
}