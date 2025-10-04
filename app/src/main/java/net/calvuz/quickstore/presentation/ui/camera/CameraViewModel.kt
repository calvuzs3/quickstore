package net.calvuz.quickstore.presentation.ui.camera

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import net.calvuz.quickstore.domain.usecase.recognition.SaveArticleImageUseCase
import net.calvuz.quickstore.domain.usecase.recognition.SearchArticleByImageUseCase
import net.calvuz.quickstore.domain.model.Article
import javax.inject.Inject

/**
 * ViewModel per Camera Screen
 *
 * Gestisce:
 * - Cattura foto
 * - Ricerca articoli per immagine
 * - Salvataggio immagine articolo
 */
@HiltViewModel
class CameraViewModel @Inject constructor(
    private val saveArticleImageUseCase: SaveArticleImageUseCase,
    private val searchArticleByImageUseCase: SearchArticleByImageUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<CameraUiState>(CameraUiState.Ready)
    val uiState: StateFlow<CameraUiState> = _uiState.asStateFlow()

    /**
     * Cerca articoli che corrispondono alla foto catturata
     */
    fun searchByImage(imageBytes: ByteArray, threshold: Double = 0.7) {
        viewModelScope.launch {
            _uiState.value = CameraUiState.Searching

            searchArticleByImageUseCase(imageBytes, threshold)
                .onSuccess { articleUuids ->
                    if (articleUuids.isEmpty()) {
                        _uiState.value = CameraUiState.NoResults
                    } else {
                        _uiState.value = CameraUiState.SearchSuccess(articleUuids)
                    }
                }
                .onFailure { error ->
                    _uiState.value = CameraUiState.Error(
                        error.message ?: "Errore durante la ricerca"
                    )
                }
        }
    }

    /**
     * Salva immagine per un articolo esistente
     */
    fun saveImage(articleUuid: String, imageBytes: ByteArray) {
        viewModelScope.launch {
            _uiState.value = CameraUiState.Saving

            saveArticleImageUseCase(articleUuid, imageBytes)
                .onSuccess { savedImage ->
                    _uiState.value = CameraUiState.SaveSuccess(savedImage.id)
                }
                .onFailure { error ->
                    _uiState.value = CameraUiState.Error(
                        error.message ?: "Errore durante il salvataggio"
                    )
                }
        }
    }

    /**
     * Reset stato a Ready
     */
    fun resetState() {
        _uiState.value = CameraUiState.Ready
    }
}

/**
 * Stati UI della Camera Screen
 */
sealed class CameraUiState {
    /** Pronto per catturare foto */
    data object Ready : CameraUiState()

    /** Ricerca in corso */
    data object Searching : CameraUiState()

    /** Salvataggio in corso */
    data object Saving : CameraUiState()

    /** Ricerca completata con successo */
    data class SearchSuccess(val articles: List<Article>) : CameraUiState()

    /** Nessun risultato trovato */
    data object NoResults : CameraUiState()

    /** Salvataggio completato */
    data class SaveSuccess(val imageId: Long) : CameraUiState()

    /** Errore */
    data class Error(val message: String) : CameraUiState()
}