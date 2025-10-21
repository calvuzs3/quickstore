package net.calvuz.quickstore.domain.usecase.settings

import net.calvuz.quickstore.domain.model.RecognitionSettings
import net.calvuz.quickstore.domain.repository.RecognitionSettingsRepository
import javax.inject.Inject

/**
 * Use case per aggiornare le impostazioni
 */
class UpdateRecognitionSettingsUseCase @Inject constructor(
    private val repository: RecognitionSettingsRepository
) {
    suspend operator fun invoke(settings: RecognitionSettings): Result<Unit> {
        return try {
            // Valida prima di salvare
            val validationErrors = settings.validate()
            if (validationErrors.isNotEmpty()) {
                return Result.failure(
                    IllegalArgumentException("Parametri non validi: ${validationErrors.joinToString()}")
                )
            }

            repository.updateSettings(settings)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}