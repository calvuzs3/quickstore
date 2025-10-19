package net.calvuz.quickstore.presentation.ui.articles.detail

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
import net.calvuz.quickstore.domain.model.Inventory
import net.calvuz.quickstore.domain.model.Movement
import net.calvuz.quickstore.domain.usecase.article.DeleteArticleUseCase
import net.calvuz.quickstore.domain.usecase.article.GetArticleUseCase
import net.calvuz.quickstore.domain.usecase.movement.GetMovementsByArticleUseCase
import net.calvuz.quickstore.domain.usecase.recognition.GetArticleImagesUseCase
import net.calvuz.quickstore.domain.usecase.recognition.DeleteArticleImageUseCase
import javax.inject.Inject

data class ArticleDetailState(
    val article: Article? = null,
    val inventory: Inventory? = null,
    val movements: List<Movement> = emptyList(),
    val images: List<ArticleImage> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val showDeleteDialog: Boolean = false,
    val isDeleting: Boolean = false
)

sealed interface ArticleDetailEvent {
    data object NavigateBack : ArticleDetailEvent
    data object NavigateToEdit : ArticleDetailEvent
    data object NavigateToAddMovement : ArticleDetailEvent
    data class ShowError(val message: String) : ArticleDetailEvent
}

@HiltViewModel
class ArticleDetailViewModel @Inject constructor(
    private val getArticleUseCase: GetArticleUseCase,
    private val deleteArticleUseCase: DeleteArticleUseCase,
    private val getMovementsByArticleUseCase: GetMovementsByArticleUseCase,
    private val getArticleImagesUseCase: GetArticleImagesUseCase,
    private val deleteArticleImageUseCase: DeleteArticleImageUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val articleId: String = checkNotNull(savedStateHandle.get<String>("articleId"))

    private val _state = MutableStateFlow(ArticleDetailState())
    val state: StateFlow<ArticleDetailState> = _state.asStateFlow()

    private val _events = MutableStateFlow<ArticleDetailEvent?>(null)
    val events: StateFlow<ArticleDetailEvent?> = _events.asStateFlow()

    init {
        loadArticle()
        loadMovements()
        loadImages()
        observeArticleChanges()
    }

    private fun observeArticleChanges() {
        viewModelScope.launch {
            getArticleUseCase.observeByUuid(articleId).collect { article ->
                _state.update { it.copy(article = article) }
            }
        }

        viewModelScope.launch {
            getArticleUseCase.observeInventory(articleId).collect { inventory ->
                _state.update { it.copy(inventory = inventory) }
            }
        }
    }

    private fun loadArticle() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            val articleResult = getArticleUseCase.getByUuid(articleId)
            val inventoryResult = getArticleUseCase.getInventory(articleId)

            if (articleResult.isSuccess && inventoryResult.isSuccess) {
                _state.update {
                    it.copy(
                        article = articleResult.getOrNull(),
                        inventory = inventoryResult.getOrNull(),
                        isLoading = false
                    )
                }
            } else {
                val error = articleResult.exceptionOrNull() ?: inventoryResult.exceptionOrNull()
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = error?.message ?: "Errore nel caricamento"
                    )
                }
                _events.value = ArticleDetailEvent.ShowError(
                    error?.message ?: "Errore nel caricamento dell'articolo"
                )
            }
        }
    }

    private fun loadMovements() {
        viewModelScope.launch {
            getMovementsByArticleUseCase(articleId)
                .onSuccess { movements ->
                    _state.update { it.copy(movements = movements) }
                }
                .onFailure { throwable ->
                    // Non blocchiamo l'UI per errori sui movimenti
                    _events.value = ArticleDetailEvent.ShowError(
                        "Errore nel caricamento storico: ${throwable.message}"
                    )
                }
        }
    }

    private fun loadImages() {
        viewModelScope.launch {
            getArticleImagesUseCase(articleId)
                .onSuccess { images ->
                    _state.update { it.copy(images = images) }
                }
                .onFailure { throwable ->
                    // Non blocchiamo l'UI per errori sulle immagini
                    _events.value = ArticleDetailEvent.ShowError(
                        "Errore nel caricamento foto: ${throwable.message}"
                    )
                }
        }
    }

    fun onDeleteImage(imageId: Long) {
        viewModelScope.launch {
            deleteArticleImageUseCase(imageId)
                .onSuccess {
                    // Rimuovi immagine dallo stato
                    _state.update {
                        it.copy(
                            images = it.images.filter { img -> img.id != imageId }
                        )
                    }
                }
                .onFailure { throwable ->
                    _events.value = ArticleDetailEvent.ShowError(
                        "Errore nell'eliminazione foto: ${throwable.message}"
                    )
                }
        }
    }

    fun onEditClick() {
        _events.value = ArticleDetailEvent.NavigateToEdit
    }

    fun onDeleteClick() {
        _state.update { it.copy(showDeleteDialog = true) }
    }

    fun onDeleteConfirm() {
        viewModelScope.launch {
            _state.update { it.copy(isDeleting = true, showDeleteDialog = false) }

            deleteArticleUseCase(articleId)
                .onSuccess {
                    _events.value = ArticleDetailEvent.NavigateBack
                }
                .onFailure { throwable ->
                    _state.update { it.copy(isDeleting = false) }
                    _events.value = ArticleDetailEvent.ShowError(
                        throwable.message ?: "Errore nell'eliminazione"
                    )
                }
        }
    }

    fun onDeleteDismiss() {
        _state.update { it.copy(showDeleteDialog = false) }
    }

    fun onAddMovementClick() {
        _events.value = ArticleDetailEvent.NavigateToAddMovement
    }

    fun onRefresh() {
        loadArticle()
        loadMovements()
        loadImages()
    }

    fun onEventConsumed() {
        _events.value = null
    }
}