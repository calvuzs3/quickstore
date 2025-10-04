package net.calvuz.quickstore.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.calvuz.quickstore.data.repository.ArticleRepositoryImpl
import net.calvuz.quickstore.data.repository.ImageRecognitionRepositoryImpl
import net.calvuz.quickstore.data.repository.MovementRepositoryImpl
import net.calvuz.quickstore.domain.repository.ArticleRepository
import net.calvuz.quickstore.domain.repository.ImageRecognitionRepository
import net.calvuz.quickstore.domain.repository.MovementRepository

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindArticleRepository(
        impl: ArticleRepositoryImpl
    ): ArticleRepository

    @Binds
    abstract fun bindMovementRepository(
        impl: MovementRepositoryImpl
    ): MovementRepository

    @Binds
    abstract fun bindImageRecognitionRepository(
        impl: ImageRecognitionRepositoryImpl
    ): ImageRecognitionRepository
}