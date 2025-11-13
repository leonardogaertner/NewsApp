package br.udesc.ddm.newsapp.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps // Ícone de grelha
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import br.udesc.ddm.newsapp.Screen // Importar o Screen de MainActivity
import br.udesc.ddm.newsapp.ui.NewsUiState
import br.udesc.ddm.newsapp.ui.NewsViewModel
import br.udesc.ddm.newsapp.ui.components.AppBar // Importar o AppBar
import br.udesc.ddm.newsapp.ui.components.NewsList

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsScreen(
    modifier: Modifier = Modifier,
    viewModel: NewsViewModel,
    navController: NavController
) {
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    // --- ESTA É A MUDANÇA ---
    // 1. Verifica se existe um ecrã anterior na pilha de navegação
    val canPop = navController.previousBackStackEntry != null

    Scaffold(
        topBar = {
            AppBar(
                title = "NewsApp",

                // 2. Mostra o botão "Voltar" apenas se 'canPop' for verdadeiro
                showBackButton = canPop,

                // 3. Define a ação de clique para o botão "Voltar"
                onBackClick = {
                    navController.popBackStack() // Ação de voltar
                },

                actions = {
                    IconButton(onClick = {
                        navController.navigate(Screen.Explore.route)
                    }) {
                        Icon(
                            imageVector = Icons.Default.Apps,
                            contentDescription = "Explorar Categorias"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {

            // --- BARRA DE PESQUISA ---
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

            // --- LISTA DE NOTÍCIAS ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .weight(1f)
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
}