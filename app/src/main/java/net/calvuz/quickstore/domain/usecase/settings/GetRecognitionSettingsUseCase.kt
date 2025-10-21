// ===== Use Cases per Recognition Settings =====
package net.calvuz.quickstore.domain.usecase.settings

import kotlinx.coroutines.flow.Flow
import net.calvuz.quickstore.domain.model.RecognitionSettings
import net.calvuz.quickstore.domain.repository.RecognitionSettingsRepository
import javax.inject.Inject

/**
 * Use case per ottenere le impostazioni correnti
 */
class GetRecognitionSettingsUseCase @Inject constructor(
    private val repository: RecognitionSettingsRepository
) {
    operator fun invoke(): Flow<RecognitionSettings> {
        return repository.getSettings()
    }
}
