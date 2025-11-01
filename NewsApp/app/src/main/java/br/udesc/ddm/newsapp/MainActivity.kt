package br.udesc.ddm.newsapp

import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import br.udesc.ddm.newsapp.data.model.Article
import br.udesc.ddm.newsapp.data.model.Source
import br.udesc.ddm.newsapp.ui.NewsUiState
import br.udesc.ddm.newsapp.ui.NewsViewModel
import br.udesc.ddm.newsapp.ui.theme.NewsAppTheme
import coil.compose.AsyncImage
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

// --- Definição das Rotas (Sem alteração) ---
sealed class Screen(val route: String) {
    object NewsList : Screen("news_list")
    object ArticleDetail : Screen("article_detail/{articleUrl}") {
        fun createRoute(articleUrl: String): String {
            val encodedUrl = URLEncoder.encode(articleUrl, StandardCharsets.UTF_8.toString())
            return "article_detail/$encodedUrl"
        }
    }
}

// --- MainActivity (Sem alteração) ---
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NewsAppTheme {
                AppNavigation()
            }
        }
    }
}

// --- Controlador de Navegação (Sem alteração) ---
@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.NewsList.route
    ) {
        composable(Screen.NewsList.route) {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                topBar = {
                    AppBar(title = "News App")
                }
            ) { innerPadding ->
                NewsScreen(
                    modifier = Modifier.padding(innerPadding),
                    navController = navController
                )
            }
        }

        composable(
            route = Screen.ArticleDetail.route,
            arguments = listOf(navArgument("articleUrl") { type = NavType.StringType })
        ) { backStackEntry ->
            val encodedUrl = backStackEntry.arguments?.getString("articleUrl") ?: ""
            val articleUrl = URLDecoder.decode(encodedUrl, StandardCharsets.UTF_8.toString())

            ArticleDetailScreen(
                articleUrl = articleUrl,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}

// --- Tela 1: Lista de Notícias (LAYOUT CORRIGIDO) ---

// GNews usa 'category', não 'topic'. E 'nation' é 'national' no Brasil.
val categories = listOf(
    "general", "world", "nation", "business", "technology",
    "entertainment", "sports", "science", "health"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsScreen(
    modifier: Modifier = Modifier,
    viewModel: NewsViewModel = viewModel(),
    navController: NavController
) {
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // --- 1. BARRA DE PESQUISA ---
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

        // --- 2. FILTRO DE CATEGORIA ---
        val selectedCategoryIndex = categories.indexOf(selectedCategory)
        ScrollableTabRow(
            selectedTabIndex = selectedCategoryIndex,
            modifier = Modifier.fillMaxWidth()
        ) {
            categories.forEachIndexed { index, category ->
                Tab(
                    selected = index == selectedCategoryIndex,
                    // Desabilita as abas se o usuário estiver pesquisando
                    enabled = searchQuery.isBlank(),
                    onClick = { viewModel.onCategoryChanged(categories[index]) },
                    text = { Text(category.replaceFirstChar { it.titlecase() }) }
                )
            }
        }

        // --- 3. LISTA DE NOTÍCIAS (Box com weight) ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
                .weight(1f) // <-- *** CORREÇÃO DE SCROLL ***
            // Isto dá ao Box um tamanho finito e
            // permite que a LazyColumn role dentro dele.
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
                            // A lista agora preenche o Box (que tem weight)
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

// --- NewsList (Sem alteração) ---
@Composable
fun NewsList(
    articles: List<Article>,
    modifier: Modifier = Modifier,
    navController: NavController
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(articles) { article ->
            ArticleItem(
                article = article,
                onArticleClick = { url ->
                    if (url.isNotBlank()) {
                        navController.navigate(Screen.ArticleDetail.createRoute(url))
                    }
                }
            )
        }
    }
}

// --- ArticleItem (Sem alteração) ---
@Composable
fun ArticleItem(
    article: Article,
    modifier: Modifier = Modifier,
    onArticleClick: (String) -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable {
                onArticleClick(article.url ?: "")
            }
    ) {
        Column {
            AsyncImage(
                model = article.image,
                contentDescription = "Imagem do artigo",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                contentScale = ContentScale.Crop,
            )
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = article.title ?: "Sem Título",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = article.description ?: "Sem Descrição",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = article.source?.name ?: "Fonte Desconhecida",
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

// --- Tela 2: Detalhe do Artigo (Sem alteração) ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleDetailScreen(
    articleUrl: String,
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = {
            AppBar(
                title = "Notícia",
                showBackButton = true,
                onBackClick = onBackClick
            )
        }
    ) { innerPadding ->
        AndroidView(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            factory = { context ->
                WebView(context).apply {
                    webViewClient = WebViewClient()
                    settings.javaScriptEnabled = true
                    loadUrl(articleUrl)
                }
            }
        )
    }
}

// --- Componente Reutilizável: AppBar (Sem alteração) ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppBar(
    title: String,
    showBackButton: Boolean = false,
    onBackClick: () -> Unit = {}
) {
    TopAppBar(
        title = { Text(text = title) },
        navigationIcon = {
            if (showBackButton) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Voltar"
                    )
                }
            }
        }
    )
}

// --- Previews (Sem alteração) ---
@Preview(showBackground = true)
@Composable
fun ArticleItemPreview() {
    val previewArticle = Article(
        title = "Este é um título de pré-visualização",
        description = "Esta é uma descrição de pré-visualização.",
        content = "",
        url = "",
        image = null,
        publishedAt = "",
        source = Source(name = "Preview Source", url = "")
    )
    NewsAppTheme {
        ArticleItem(article = previewArticle, onArticleClick = {})
    }
}