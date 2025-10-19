package net.calvuz.quickstore.presentation.ui.articles.add

import android.graphics.Bitmap
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import net.calvuz.quickstore.domain.model.Article
import net.calvuz.quickstore.domain.model.ArticleImage
import net.calvuz.quickstore.domain.usecase.article.AddArticleUseCase
import net.calvuz.quickstore.domain.usecase.article.GetArticleUseCase
import net.calvuz.quickstore.domain.usecase.article.UpdateArticleUseCase
import net.calvuz.quickstore.domain.usecase.recognition.GetArticleImagesUseCase
import net.calvuz.quickstore.domain.usecase.recognition.SaveArticleImageUseCase
import net.calvuz.quickstore.domain.usecase.recognition.DeleteArticleImageUseCase
import net.calvuz.quickstore.util.BitmapUtils
import javax.inject.Inject

data class CapturedImage(
    val id: String,
    val bitmap: Bitmap,
    val isSaved: Boolean = false
)

data class AddArticleState(
    val isEditMode: Boolean = false,
    val articleUuid: String? = null,

    // Form fields
    val name: String = "",
    val description: String = "",
    val category: String = "",
    val unitOfMeasure: String = "pz",
    val reorderLevel: String = "0",
    val notes: String = "",
    val initialQuantity: String = "0",  // Solo per creazione

    // Images
    val capturedImages: List<CapturedImage> = emptyList(),
    val savedImages: List<ArticleImage> = emptyList(),
    val showPhotoDialog: Boolean = false,

    // Validation errors
    val nameError: String? = null,
    val unitError: String? = null,
    val reorderLevelError: String? = null,
    val initialQuantityError: String? = null,

    // UI State
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null
)

sealed interface AddArticleEvent {
    data object NavigateBack : AddArticleEvent
    data class ShowError(val message: String) : AddArticleEvent
    data class ShowSuccess(val message: String) : AddArticleEvent
}

@HiltViewModel
class AddArticleViewModel @Inject constructor(
    private val getArticleUseCase: GetArticleUseCase,
    private val addArticleUseCase: AddArticleUseCase,
    private val updateArticleUseCase: UpdateArticleUseCase,
    private val saveArticleImageUseCase: SaveArticleImageUseCase,
    private val getArticleImagesUseCase: GetArticleImagesUseCase,
    private val deleteArticleImageUseCase: DeleteArticleImageUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val articleId: String? = savedStateHandle.get<String>("articleId")

    private val _state = MutableStateFlow(AddArticleState())
    val state: StateFlow<AddArticleState> = _state.asStateFlow()

    private val _events = MutableStateFlow<AddArticleEvent?>(null)
    val events: StateFlow<AddArticleEvent?> = _events.asStateFlow()

    init {
        if (articleId != null) {
            _state.update { it.copy(isEditMode = true, articleUuid = articleId) }
            loadArticle(articleId)
        }
    }

    private fun loadArticle(uuid: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            getArticleUseCase.getByUuid(uuid)
                .onSuccess { article ->
                    article?.let { populateForm(it) }
                    loadArticleImages(uuid)
                }
                .onFailure { throwable ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = throwable.message ?: "Errore nel caricamento"
                        )
                    }
                }
        }
    }

    private fun loadArticleImages(uuid: String) {
        viewModelScope.launch {
            getArticleImagesUseCase(uuid)
                .onSuccess { images ->
                    _state.update {
                        it.copy(
                            savedImages = images,
                            isLoading = false
                        )
                    }
                }
                .onFailure {
                    _state.update { it.copy(isLoading = false) }
                }
        }
    }

    private fun populateForm(article: Article) {
        _state.update {
            it.copy(
                name = article.name,
                description = article.description,
                category = article.category,
                unitOfMeasure = article.unitOfMeasure,
                reorderLevel = article.reorderLevel.toString(),
                notes = article.notes
            )
        }
    }

    // Form field updates
    fun onNameChange(value: String) {
        _state.update { it.copy(name = value, nameError = null) }
    }

    fun onDescriptionChange(value: String) {
        _state.update { it.copy(description = value) }
    }

    fun onCategoryChange(value: String) {
        _state.update { it.copy(category = value) }
    }

    fun onUnitOfMeasureChange(value: String) {
        _state.update { it.copy(unitOfMeasure = value, unitError = null) }
    }

    fun onReorderLevelChange(value: String) {
        val filtered = value.filter { it.isDigit() || it == '.' }
        val dotCount = filtered.count { it == '.' }
        val finalValue = if (dotCount > 1) {
            filtered.substring(0, filtered.lastIndexOf('.'))
        } else {
            filtered
        }
        _state.update { it.copy(reorderLevel = finalValue, reorderLevelError = null) }
    }

    fun onNotesChange(value: String) {
        _state.update { it.copy(notes = value) }
    }

    fun onInitialQuantityChange(value: String) {
        val filtered = value.filter { it.isDigit() || it == '.' }
        val dotCount = filtered.count { it == '.' }
        val finalValue = if (dotCount > 1) {
            filtered.substring(0, filtered.lastIndexOf('.'))
        } else {
            filtered
        }
        _state.update { it.copy(initialQuantity = finalValue, initialQuantityError = null) }
    }

    // Photo management
    fun onAddPhotoClick() {
        _state.update { it.copy(showPhotoDialog = true) }
    }

    fun onDismissPhotoDialog() {
        _state.update { it.copy(showPhotoDialog = false) }
    }

    fun onPhotoTaken(bitmap: Bitmap) {
        val resizedBitmap = BitmapUtils.resizeBitmap(bitmap, maxSize = 1024)
        val capturedImage = CapturedImage(
            id = java.util.UUID.randomUUID().toString(),
            bitmap = resizedBitmap
        )

        _state.update {
            it.copy(
                capturedImages = it.capturedImages + capturedImage,
                showPhotoDialog = false
            )
        }

        // Rilascia bitmap originale se diverso
        if (resizedBitmap != bitmap) {
            bitmap.recycle()
        }
    }

    fun onRemoveCapturedImage(imageId: String) {
        val imageToRemove = _state.value.capturedImages.find { it.id == imageId }
        imageToRemove?.bitmap?.recycle()

        _state.update {
            it.copy(capturedImages = it.capturedImages.filter { img -> img.id != imageId })
        }
    }

    fun onRemoveSavedImage(imageId: Long) {
        viewModelScope.launch {
            deleteArticleImageUseCase(imageId)
                .onSuccess {
                    _state.update {
                        it.copy(savedImages = it.savedImages.filter { img -> img.id != imageId })
                    }
                    _events.value = AddArticleEvent.ShowSuccess("Immagine eliminata")
                }
                .onFailure {
                    _events.value = AddArticleEvent.ShowError("Errore nell'eliminazione")
                }
        }
    }

    fun onSaveClick() {
        if (validateForm()) {
            if (_state.value.isEditMode) {
                updateArticle()
            } else {
                createArticle()
            }
        }
    }

    private fun validateForm(): Boolean {
        val currentState = _state.value
        var isValid = true

        // Validate name
        if (currentState.name.isBlank()) {
            _state.update { it.copy(nameError = "Il nome è obbligatorio") }
            isValid = false
        }

        // Validate unit of measure
        if (currentState.unitOfMeasure.isBlank()) {
            _state.update { it.copy(unitError = "L'unità di misura è obbligatoria") }
            isValid = false
        }

        // Validate reorder level
        val reorderLevel = currentState.reorderLevel.toDoubleOrNull()
        if (reorderLevel == null || reorderLevel < 0) {
            _state.update { it.copy(reorderLevelError = "Valore non valido") }
            isValid = false
        }

        // Validate initial quantity (solo in creazione)
        if (!currentState.isEditMode) {
            val initialQty = currentState.initialQuantity.toDoubleOrNull()
            if (initialQty == null || initialQty < 0) {
                _state.update { it.copy(initialQuantityError = "Valore non valido") }
                isValid = false
            }
        }

        return isValid
    }

    private fun createArticle() {
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true) }

            val currentState = _state.value

            addArticleUseCase(
                name = currentState.name.trim(),
                description = currentState.description.trim(),
                recorderLevel = currentState.reorderLevel.toDouble(),
                notes = currentState.notes.trim(),
                unitOfMeasure = currentState.unitOfMeasure.trim(),
                category = currentState.category.trim(),
                initialQuantity = currentState.initialQuantity.toDouble()
            ).onSuccess { article ->
                // Salva le immagini catturate
                saveImages( article.uuid)
            }.onFailure { throwable ->
                _state.update { it.copy(isSaving = false) }
                _events.value = AddArticleEvent.ShowError(
                    throwable.message ?: "Errore nel salvataggio"
                )
            }
        }
    }

    private fun updateArticle() {
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true) }

            val currentState = _state.value
            val uuid = currentState.articleUuid ?: return@launch

            val article = Article(
                uuid = uuid,
                name = currentState.name.trim(),
                description = currentState.description.trim(),
                category = currentState.category.trim(),
                unitOfMeasure = currentState.unitOfMeasure.trim(),
                reorderLevel = currentState.reorderLevel.toDouble(),
                notes = currentState.notes.trim(),
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )

            updateArticleUseCase(article)
                .onSuccess {
                    // Salva eventuali nuove immagini
                    saveImages(uuid)
                }
                .onFailure { throwable ->
                    _state.update { it.copy(isSaving = false) }
                    _events.value = AddArticleEvent.ShowError(
                        throwable.message ?: "Errore nell'aggiornamento"
                    )
                }
        }
    }

    private suspend fun saveImages(articleUuid: String) {
        val imagesToSave = _state.value.capturedImages

        if (imagesToSave.isEmpty()) {
            // Nessuna immagine da salvare, termina
            _state.update { it.copy(isSaving = false) }
            _events.value = AddArticleEvent.NavigateBack
            return
        }

        var savedCount = 0
        var errorCount = 0

        imagesToSave.forEach { capturedImage ->
            val imageData = BitmapUtils.bitmapToByteArray(capturedImage.bitmap)

            saveArticleImageUseCase(
                articleUuid = articleUuid,
                imageData = imageData
            ).onSuccess {
                savedCount++
                capturedImage.bitmap.recycle()
            }.onFailure {
                errorCount++
            }
        }

        _state.update { it.copy(isSaving = false, capturedImages = emptyList()) }

        if (errorCount > 0) {
            _events.value = AddArticleEvent.ShowError(
                "Salvate $savedCount/${imagesToSave.size} immagini"
            )
        } else {
            _events.value = AddArticleEvent.NavigateBack
        }
    }

    fun onEventConsumed() {
        _events.value = null
    }

    override fun onCleared() {
        super.onCleared()
        // Rilascia tutte le bitmap
        _state.value.capturedImages.forEach { it.bitmap.recycle() }
    }
}