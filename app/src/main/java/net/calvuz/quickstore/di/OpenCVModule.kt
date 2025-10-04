package net.calvuz.quickstore.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.calvuz.quickstore.data.opencv.FeatureExtractor
import net.calvuz.quickstore.data.opencv.ImageMatcher
import net.calvuz.quickstore.data.opencv.OpenCVManager
import javax.inject.Singleton

/**
 * Modulo Hilt per fornire dipendenze OpenCV
 */
@Module
@InstallIn(SingletonComponent::class)
object OpenCVModule {

    @Provides
    @Singleton
    fun provideFeatureExtractor(
        openCVManager: OpenCVManager
    ): FeatureExtractor {
        return FeatureExtractor(openCVManager)
    }

    @Provides
    @Singleton
    fun provideImageMatcher(
        openCVManager: OpenCVManager
    ): ImageMatcher {
        return ImageMatcher(openCVManager)
    }
}