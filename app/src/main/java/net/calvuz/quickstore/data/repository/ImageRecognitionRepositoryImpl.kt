package net.calvuz.quickstore.data.repository

import net.calvuz.quickstore.data.local.database.ArticleImageDao
import net.calvuz.quickstore.data.local.entity.ArticleImageEntity
import net.calvuz.quickstore.data.local.storage.ImageStorageManager
import net.calvuz.quickstore.data.mapper.ArticleImageMapper
import net.calvuz.quickstore.data.opencv.FeatureExtractor
import net.calvuz.quickstore.data.opencv.ImageMatcher
import net.calvuz.quickstore.data.opencv.OpenCVManager
import net.calvuz.quickstore.domain.model.ArticleImage
import net.calvuz.quickstore.domain.repository.ImageRecognitionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Implementazione completa del repository per riconoscimento immagini con OpenCV
 */
class ImageRecognitionRepositoryImpl @Inject constructor(
    private val articleImageDao: ArticleImageDao,
    private val imageStorageManager: ImageStorageManager,
    private val featureExtractor: FeatureExtractor,
    private val imageMatcher: ImageMatcher,
    private val openCVManager: OpenCVManager
) : ImageRecognitionRepository {

    override suspend fun saveArticleImage(
        articleUuid: String,
        imageData: ByteArray
    ): Result<ArticleImage> {
        return try {
            // Verifica OpenCV inizializzato
            if (!openCVManager.isInitialized()) {
                return Result.failure(IllegalStateException("OpenCV not initialized"))
            }

            // 1. Salva immagine su file system
            val imagePath = imageStorageManager.saveImage(imageData, articleUuid)
                .getOrElse { return Result.failure(it) }

            // 2. Estrai features OpenCV
            val featuresData = featureExtractor.extractFeatures(imageData)
                .getOrElse {
                    // Cleanup: elimina immagine se estrazione fallisce
                    imageStorageManager.deleteImage(imagePath)
                    return Result.failure(it)
                }

            // 3. Crea entity e salva su database
            val entity = ArticleImageEntity(
                articleUuid = articleUuid,
                imagePath = imagePath,
                featuresData = featuresData,
                createdAt = System.currentTimeMillis()
            )

            val imageId = articleImageDao.insert(entity)

            // 4. Ritorna domain model con ID generato
            val savedEntity = entity.copy(id = imageId)
            val articleImage = ArticleImageMapper.toDomain(savedEntity)

            Result.success(articleImage)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getArticleImages(articleUuid: String): Result<List<ArticleImage>> {
        return try {
            val entities = articleImageDao.getByArticleUuid(articleUuid)
            val images = ArticleImageMapper.toDomainList(entities)
            Result.success(images)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    override suspend fun getArticleImageById(imageId: Long): Result<ArticleImage?> {
        return try {
            val entity = articleImageDao.getById(imageId)
            val domainModel = entity?.let { mapper.toDomain(it) }
            Result.success(domainModel)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun observeArticleImages(articleUuid: String): Flow<List<ArticleImage>> {
        return articleImageDao.observeByArticleUuid(articleUuid).map { entities ->
            ArticleImageMapper.toDomainList(entities)
        }
    }

    override suspend fun deleteImage(imageId: Long): Result<Unit> {
        return try {
            val image = articleImageDao.getById(imageId)
            if (image != null) {
                // Elimina file fisico
                imageStorageManager.deleteImage(image.imagePath)
                    .getOrElse {
                        // Log warning ma continua comunque
                    }

                // Elimina da database
                articleImageDao.delete(image)
                Result.success(Unit)
            } else {
                Result.failure(IllegalArgumentException("Image not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteImages(articleUuid: String): Result<Int> {
        return try {
            // Recupera tutte le immagini
            val images = articleImageDao.getByArticleUuid(articleUuid)

            // Elimina tutti i file
            images.forEach { image ->
                imageStorageManager.deleteImage(image.imagePath)
            }

            // Elimina da DB
            val deletedCount = articleImageDao.deleteByArticleUuid(articleUuid)

            Result.success(deletedCount)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun searchArticlesByImage(
        imageData: ByteArray,
        threshold: Double
    ): Result<List<String>> {
        return try {
            // Verifica OpenCV inizializzato
            if (!openCVManager.isInitialized()) {
                return Result.failure(IllegalStateException("OpenCV not initialized"))
            }

            // 1. Estrai features dall'immagine query
            val queryFeatures = featureExtractor.extractFeatures(imageData)
                .getOrElse { return Result.failure(it) }

            val queryDescriptors = featureExtractor.deserializeDescriptors(queryFeatures)
                .getOrElse { return Result.failure(it) }

            // 2. Ottieni tutte le immagini salvate
            val allImages = articleImageDao.getAll()

            if (allImages.isEmpty()) {
                queryDescriptors.release()
                return Result.success(emptyList())
            }

            // 3. Deserializza tutti i descriptors
            val databaseDescriptors = allImages.mapNotNull { image ->
                featureExtractor.deserializeDescriptors(image.featuresData)
                    .getOrNull()
            }

            // 4. Trova best matches
            val matchResults = imageMatcher.findBestMatches(
                queryDescriptors,
                databaseDescriptors,
                threshold
            ).getOrElse {
                // Cleanup
                queryDescriptors.release()
                databaseDescriptors.forEach { it.release() }
                return Result.failure(it)
            }

            // 5. Mappa indici a articleUuid
            val matchedArticleUuids = matchResults.map { result ->
                allImages[result.index].articleUuid
            }.distinct() // Rimuovi duplicati (stesso articolo può avere più immagini)

            // 6. Cleanup OpenCV Mats
            queryDescriptors.release()
            databaseDescriptors.forEach { it.release() }

            Result.success(matchedArticleUuids)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getImagePath(imageId: Long): Result<String?> {
        return try {
            val image = articleImageDao.getById(imageId)
            val fullPath = image?.let {
                imageStorageManager.getFullPath(it.imagePath)
            }
            Result.success(fullPath)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}