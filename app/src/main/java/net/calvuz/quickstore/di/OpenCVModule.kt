package net.calvuz.quickstore.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.calvuz.quickstore.data.opencv.ConfigurableImageMatcher
import net.calvuz.quickstore.data.opencv.FeatureExtractor
import net.calvuz.quickstore.data.opencv.ImageRecognitionValidator
import net.calvuz.quickstore.data.opencv.OpenCVManager
import net.calvuz.quickstore.domain.repository.RecognitionSettingsRepository
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

//    @Provides
//    @Singleton
//    fun provideImageMatcher(
//        openCVManager: OpenCVManager
//    ): ImageMatcher {
//        return ImageMatcher(openCVManager)
//    }

    // Rimuovi il provide per ImageMatcher, usa solo ConfigurableImageMatcher
    @Provides
    @Singleton
    fun provideConfigurableImageMatcher(
        openCVManager: OpenCVManager,
        settingsRepository: RecognitionSettingsRepository
    ): ConfigurableImageMatcher {
        return ConfigurableImageMatcher(openCVManager, settingsRepository)
    }

    @Provides
    @Singleton
    fun provideImageRecognitionValidator(
        featureExtractor: FeatureExtractor,
        configurableImageMatcher: ConfigurableImageMatcher, // Cambiato parametro
        openCVManager: OpenCVManager,
        articleImageDao: net.calvuz.quickstore.data.local.database.ArticleImageDao
    ): ImageRecognitionValidator {
        return ImageRecognitionValidator(featureExtractor, configurableImageMatcher, openCVManager, articleImageDao)
    }

}