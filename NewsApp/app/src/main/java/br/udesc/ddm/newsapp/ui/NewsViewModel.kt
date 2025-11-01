package br.udesc.ddm.newsapp.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.udesc.ddm.newsapp.data.model.Article
import br.udesc.ddm.newsapp.data.repository.NewsRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn

// Define os estados da UI (Sem alteração)
sealed class NewsUiState {
    object Loading : NewsUiState()
    data class Success(val articles: List<Article>) : NewsUiState()
    data class Error(val message: String) : NewsUiState()
}

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class NewsViewModel(private val repository: NewsRepository = NewsRepository()) : ViewModel() {

    // StateFlow para a CONSULTA DE PESQUISA
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // StateFlow para a CATEGORIA SELECIONADA
    private val _selectedCategory = MutableStateFlow("general") // "general" é o padrão
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    // --- LÓGICA DE BUSCA CORRIGIDA ---
    val uiState: StateFlow<NewsUiState> = combine(
        _searchQuery
            .debounce(500), // Debounce de 500ms
        _selectedCategory
    ) { query, category ->
        Pair(query, category)
    }.flatMapLatest { (query, category) ->
        flow {
            emit(NewsUiState.Loading)

            // --- DECISÃO DE QUAL ENDPOINT CHAMAR ---
            val result = if (query.isBlank()) {
                // Pesquisa está vazia: Chama /top-headlines por CATEGORIA
                Log.d("NewsViewModel", "Buscando... Categoria: $category")
                repository.getTopHeadlines(category = category)
            } else {
                // Pesquisa está preenchida: Chama /search por QUERY
                Log.d("NewsViewModel", "Buscando... Pesquisa: $query")
                repository.searchNews(query = query) // Este erro desaparecerá
            }

            result.fold(
                onSuccess = { articles ->
                    Log.d("NewsViewModel", "Sucesso: Recebidos ${articles.size} artigos")
                    emit(NewsUiState.Success(articles))
                },
                onFailure = { error ->
                    Log.e("NewsViewModel", "Falha ao buscar notícias", error)
                    emit(NewsUiState.Error(error.message ?: "Erro desconhecido"))
                }
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = NewsUiState.Loading
    )

    /**
     * Chamado pela UI quando o texto na barra de pesquisa muda.
     */
    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    /**
     * Chamado pela UI quando o usuário clica em uma aba de categoria.
     */
    fun onCategoryChanged(category: String) {
        _selectedCategory.value = category
        // Limpa a pesquisa ao trocar de categoria para evitar confusão
        if (_searchQuery.value.isNotBlank()) {
            _searchQuery.value = ""
        }
    }
}