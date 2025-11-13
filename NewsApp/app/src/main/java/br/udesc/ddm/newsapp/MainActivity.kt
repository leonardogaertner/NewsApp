package br.udesc.ddm.newsapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import br.udesc.ddm.newsapp.ui.NewsViewModel
import br.udesc.ddm.newsapp.ui.screens.ArticleDetailScreen
import br.udesc.ddm.newsapp.ui.screens.ExploreScreen // Importar a nova tela
import br.udesc.ddm.newsapp.ui.screens.NewsScreen
import br.udesc.ddm.newsapp.ui.theme.NewsAppTheme
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

// --- Definição das Rotas (ATUALIZADA) ---
sealed class Screen(val route: String) {
    // Rota antiga, não usada
    object NewsList : Screen("news_list")

    // Rota para a tela de Detalhe
    object ArticleDetail : Screen("article_detail/{articleUrl}") {
        fun createRoute(articleUrl: String): String {
            val encodedUrl = URLEncoder.encode(articleUrl, StandardCharsets.UTF_8.toString())
            return "article_detail/$encodedUrl"
        }
    }

    // Rota para a tela de Explorar Categorias
    object Explore : Screen("explore_screen")

    // Rota para a lista de notícias (vinda do Explorar)
    object NewsByCategory : Screen("news_by_category_screen")
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

// --- Controlador de Navegação (CORRIGIDO) ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val viewModel: NewsViewModel = viewModel()

    val bottomNavItems = listOf(
        BottomNavScreen.Recentes,
        BottomNavScreen.Destaques,
        BottomNavScreen.Regional,
    )

    // Converte a lista de rotas da BottomNav num Set (para consulta rápida)
    val bottomNavRoutes = bottomNavItems.map { it.route }.toSet()

    // Observa a rota atual
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // *** A CORREÇÃO ESTÁ AQUI ***
    // Determina se a BottomBar deve ser visível
    val showBottomBar = currentRoute in bottomNavRoutes

    Scaffold(
        modifier = Modifier.fillMaxSize(),

        // Mostra a BottomBar APENAS se showBottomBar for true
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { screen ->
                        NavigationBarItem(
                            icon = { Icon(screen.icon, contentDescription = screen.title) },
                            label = { Text(screen.title) },
                            selected = currentRoute == screen.route,
                            onClick = {
                                viewModel.onCategoryChanged(screen.category)
                                navController.navigate(screen.route) {
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
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = BottomNavScreen.Recentes.route,
            modifier = Modifier.padding(innerPadding) // Padding aplicado pelo Scaffold
        ) {

            // Rotas da BottomNav (usam o NewsScreen)
            composable(BottomNavScreen.Recentes.route) {
                NewsScreen(navController = navController, viewModel = viewModel)
            }
            composable(BottomNavScreen.Destaques.route) {
                NewsScreen(navController = navController, viewModel = viewModel)
            }
            composable(BottomNavScreen.Regional.route) {
                NewsScreen(navController = navController, viewModel = viewModel)
            }

            // Rota de Detalhe (Sem BottomBar)
            composable(
                route = Screen.ArticleDetail.route,
                arguments = listOf(navArgument("articleUrl") { type = NavType.StringType })
            ) { backStackEntry ->
                val encodedUrl = backStackEntry.arguments?.getString("articleUrl") ?: ""
                val articleUrl = URLDecoder.decode(encodedUrl, StandardCharsets.UTF_8.toString())

                ArticleDetailScreen(
                    articleUrl = articleUrl,
                    onBackClick = { navController.popBackStack() }
                )
            }

            // Rota para "Explorar" (Sem BottomBar)
            composable(route = Screen.Explore.route) {
                ExploreScreen(
                    navController = navController,
                    viewModel = viewModel,
                    onBackClick = { navController.popBackStack() }
                )
            }

            // Rota para "Notícias por Categoria" (Sem BottomBar)
            // Reutiliza o NewsScreen
            composable(route = Screen.NewsByCategory.route) {
                NewsScreen(
                    navController = navController,
                    viewModel = viewModel
                )
            }
        }
    }
}