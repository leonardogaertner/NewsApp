package br.udesc.ddm.newsapp.data.model

data class NewsResponse(
    val totalArticles: Int,
    val articles: List<Article>
)
