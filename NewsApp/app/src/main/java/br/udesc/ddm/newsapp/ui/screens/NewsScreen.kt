package br.udesc.ddm.newsapp.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import br.udesc.ddm.newsapp.ui.NewsUiState
import br.udesc.ddm.newsapp.ui.NewsViewModel
import br.udesc.ddm.newsapp.ui.components.NewsList

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsScreen(
    modifier: Modifier = Modifier,
    viewModel: NewsViewModel, // NÃO inicia mais o VM aqui
    navController: NavController
) {
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // --- 1. BARRA DE PESQUISA (Sem alteração) ---
        TextField(
            value = searchQuery,
            onValueChange = { viewModel.onSearchQueryChanged(it) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            placeholder = { Text("Pesquisar notícias...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Pesquisar") },
            singleLine = true
        )

        // --- 2. FILTRO DE CATEGORIA (REMOVIDO) ---
        // A ScrollableTabRow não existe mais

        // --- 3. LISTA DE NOTÍCIAS (Box com weight) ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
                .weight(1f) // <-- *** CORREÇÃO DE SCROLL ***
        ) {
            when (val state = uiState) {
                is NewsUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is NewsUiState.Success -> {
                    if (state.articles.isEmpty()) {
                        Text(
                            text = "Nenhum artigo encontrado.",
                            textAlign = TextAlign.Center,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    } else {
                        NewsList(
                            articles = state.articles,
                            navController = navController,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
                is NewsUiState.Error -> {
                    Text(
                        text = "Falha ao carregar notícias: ${state.message}",
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
    }
}