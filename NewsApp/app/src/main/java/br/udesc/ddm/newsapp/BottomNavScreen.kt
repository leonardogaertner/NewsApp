package br.udesc.ddm.newsapp

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.graphics.vector.ImageVector
import org.chromium.base.Flag

sealed class BottomNavScreen(
    val route: String,
    val title: String,
    val icon: ImageVector,
    val category: String // Categoria da API para esta aba
) {
    object Recentes : BottomNavScreen(
        route = "recentes",
        title = "Recentes",
        icon = Icons.Default.Home,
        category = "general" // "general" é a melhor opção para "recentes"
    )

    object Destaques : BottomNavScreen(
        route = "destaques",
        title = "Destaques",
        icon = Icons.Default.Star,
        category = "business" // Vamos usar "business" como "destaques"
    )

    object Regional : BottomNavScreen(
        route = "regional",
        title = "Regional",
        icon = Icons.Default.Flag,
        category = "nation" // "nation" é perfeito para notícias regionais do país (BR)
    )
}