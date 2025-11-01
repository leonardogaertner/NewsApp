package br.udesc.ddm.newsapp.data.model

data class Article(
    val title: String?,
    val description: String?,
    val content: String?,
    val url: String?,
    val image: String?,
    val publishedAt: String?,
    val source: Source?
)