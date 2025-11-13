package br.udesc.ddm.newsapp.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import br.udesc.ddm.newsapp.Screen // Importar o Screen de MainActivity
import br.udesc.ddm.newsapp.ui.NewsViewModel
import br.udesc.ddm.newsapp.ui.components.AppBar

// 1. Um data class simples para guardar o nome de exibição e a chave da API
private data class Category(
    val displayName: String,
    val apiKey: String
)

// 2. A lista de categorias que queremos mostrar
private val categories = listOf(
    Category("Tecnologia", "technology"),
    Category("Desporto", "sports"),
    Category("Ciência", "science"),
    Category("Saúde", "health"),
    Category("Entretenimento", "entertainment"),
    Category("Mundo", "world"),
    // Podes adicionar mais ou remover categorias que já estão na BottomNav
    Category("Geral", "general"),
    Category("Negócios", "business"),
    Category("Nação", "nation")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreScreen(
    navController: NavController,
    viewModel: NewsViewModel,
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = {
            // 3. Usamos o nosso AppBar reutilizável
            AppBar(
                title = "Explorar Categorias",
                showBackButton = true,
                onBackClick = onBackClick
            )
        }
    ) { innerPadding ->

        // 4. Uma grelha vertical com 2 colunas
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp) // Adiciona padding à volta da grelha
        ) {
            items(categories) { category ->
                CategoryItem(
                    category = category,
                    onClick = {
                        // 5. Quando clicado:
                        // - Atualiza a categoria no ViewModel partilhado
                        viewModel.onCategoryChanged(category.apiKey)
                        // - Navega para o ecrã de notícias por categoria
                        navController.navigate(Screen.NewsByCategory.route) {
                            // Limpa a pilha de navegação para evitar loops
                            launchSingleTop = true
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun CategoryItem(
    category: Category,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .padding(8.dp) // Espaçamento entre os cards
            .aspectRatio(1f) // Faz o card ser um quadrado perfeito
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = category.displayName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}