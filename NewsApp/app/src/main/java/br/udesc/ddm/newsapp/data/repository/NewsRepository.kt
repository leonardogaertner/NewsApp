package br.udesc.ddm.newsapp.data.repository

import br.udesc.ddm.newsapp.data.model.Article
import br.udesc.ddm.newsapp.data.remote.NewsApiService
import br.udesc.ddm.newsapp.data.remote.RetrofitClient

class NewsRepository(private val apiService: NewsApiService = RetrofitClient.instance) {

    // Método 1: Buscar por categoria
    suspend fun getTopHeadlines(category: String): Result<List<Article>> {
        return try {
            val response = apiService.getTopHeadlines(category = category)
            Result.success(response.articles)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Método 2: Buscar por termo de pesquisa
    suspend fun searchNews(query: String): Result<List<Article>> {
        return try {
            val response = apiService.searchNews(query = query)
            Result.success(response.articles)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}