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
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
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
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
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
    object NewsList : Screen("news_list") // Rota antiga, não mais usada como start
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

// --- Controlador de Navegação (MODIFICADO) ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    // 1. Instanciamos o ViewModel aqui (estado elevado)
    // Ele será compartilhado por todas as telas da bottom nav.
    val viewModel: NewsViewModel = viewModel()

    // 2. Lista de telas da BottomNav
    val bottomNavItems = listOf(
        BottomNavScreen.Recentes,
        BottomNavScreen.Destaques,
        BottomNavScreen.Regional,
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            // 3. Renderiza a BottomBar
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                bottomNavItems.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title) },
                        selected = currentRoute == screen.route,
                        onClick = {
                            // 4. Ao clicar, atualiza o ViewModel e navega
                            viewModel.onCategoryChanged(screen.category)
                            navController.navigate(screen.route) {
                                // Evita empilhar telas
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        // 5. O NavHost agora usa o padding do Scaffold
        NavHost(
            navController = navController,
            startDestination = BottomNavScreen.Recentes.route, // Começa em "Recentes"
            modifier = Modifier.padding(innerPadding)
        ) {

            // 6. Criamos uma rota para CADA item da bottom nav
            // Todas apontam para a MESMA NewsScreen, passando o ViewModel

            composable(BottomNavScreen.Recentes.route) {
                NewsScreen(
                    navController = navController,
                    viewModel = viewModel // Passa o ViewModel compartilhado
                )
            }
            composable(BottomNavScreen.Destaques.route) {
                NewsScreen(
                    navController = navController,
                    viewModel = viewModel // Passa o ViewModel compartilhado
                )
            }
            composable(BottomNavScreen.Regional.route) {
                NewsScreen(
                    navController = navController,
                    viewModel = viewModel // Passa o ViewModel compartilhado
                )
            }

            // 7. A tela de Detalhe continua igual
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
}

// --- Tela 1: Lista de Notícias (MODIFICADA) ---

// A lista 'categories' foi removida daqui

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