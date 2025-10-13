package net.calvuz.quickstore.presentation.ui.articles.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import net.calvuz.quickstore.domain.model.Article
import net.calvuz.quickstore.domain.usecase.article.DeleteArticleUseCase
import net.calvuz.quickstore.domain.usecase.article.GetArticleUseCase
import javax.inject.Inject

/**
 * ViewModel per Article List Screen
 *
 * Gestisce:
 * - Lista articoli
 * - Search
 * - Filtri per categoria
 * - Eliminazione articoli
 */
@HiltViewModel
class ArticleListViewModel @Inject constructor(
    private val getArticleUseCase: GetArticleUseCase,
    private val deleteArticleUseCase: DeleteArticleUseCase
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()

    private val _uiState = MutableStateFlow<ArticleListUiState>(ArticleListUiState.Loading)
    val uiState: StateFlow<ArticleListUiState> = _uiState.asStateFlow()

    // Osserva articoli e applica filtri
    val articles: StateFlow<List<Article>> = combine(
        getArticleUseCase.observeAll(),
        _searchQuery,
        _selectedCategory
    ) { articles, query, category ->
        var filtered = articles

        // Filtra per categoria
        if (category != null) {
            filtered = filtered.filter { it.category.equals(category, ignoreCase = true) }
        }

        // Filtra per search query
        if (query.isNotBlank()) {
            filtered = filtered.filter { article ->
                article.name.contains(query, ignoreCase = true) ||
                        article.sku.contains(query, ignoreCase = true) ||
                        article.barcode.contains(query, ignoreCase = true) ||
                        article.description.contains(query, ignoreCase = true)
            }
        }

        filtered
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Estrae categorie uniche dagli articoli
    val categories: StateFlow<List<String>> = getArticleUseCase.observeAll()
        .map { articles ->
            articles.map { it.category }
                .filter { it.isNotBlank() }
                .distinct()
                .sorted()
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        loadArticles()
    }

    /**
     * Carica articoli
     */
    private fun loadArticles() {
        viewModelScope.launch {
            _uiState.value = ArticleListUiState.Loading

            getArticleUseCase.observeAll()
                .catch { e ->
                    _uiState.value = ArticleListUiState.Error(
                        e.message ?: "Errore nel caricamento"
                    )
                }
                .collect {
                    _uiState.value = ArticleListUiState.Success
                }
        }
    }

    /**
     * Aggiorna search query
     */
    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    /**
     * Seleziona categoria per filtrare
     */
    fun selectCategory(category: String?) {
        _selectedCategory.value = category
    }

    /**
     * Reset filtri
     */
    fun clearFilters() {
        _searchQuery.value = ""
        _selectedCategory.value = null
    }

    /**
     * Elimina articolo
     */
    fun deleteArticle(articleUuid: String) {
        viewModelScope.launch {
            deleteArticleUseCase(articleUuid)
                .onFailure { error ->
                    // TODO: Mostra errore in UI
                    _uiState.value = ArticleListUiState.Error(
                        error.message ?: "Errore nell'eliminazione"
                    )
                }
        }
    }

    /**
     * Refresh lista
     */
    fun refresh() {
        loadArticles()
    }
}

/**
 * Stati UI per Article List
 */
sealed class ArticleListUiState {
    data object Loading : ArticleListUiState()
    data object Success : ArticleListUiState()
    data class Error(val message: String) : ArticleListUiState()
}