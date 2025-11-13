package br.udesc.ddm.newsapp.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import br.udesc.ddm.newsapp.Screen
import br.udesc.ddm.newsapp.data.model.Article

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