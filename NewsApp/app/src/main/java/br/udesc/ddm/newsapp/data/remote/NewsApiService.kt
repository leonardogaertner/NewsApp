package br.udesc.ddm.newsapp.data.remote

import br.udesc.ddm.newsapp.BuildConfig
import br.udesc.ddm.newsapp.data.model.NewsResponse
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface NewsApiService {

    // 1. Endpoint para Categorias (top-headlines)
    @GET("v4/top-headlines")
    suspend fun getTopHeadlines(
        @Query("country") country: String = "br",
        @Query("lang") lang: String = "pt",
        @Query("token") apiKey: String = BuildConfig.NEWS_API_KEY,
        @Query("category") category: String // GNews usa 'category'
    ): NewsResponse

    // 2. Endpoint para Pesquisa (search)
    @GET("v4/search")
    suspend fun searchNews(
        @Query("country") country: String = "br",
        @Query("lang") lang: String = "pt",
        @Query("token") apiKey: String = BuildConfig.NEWS_API_KEY,
        @Query("q") query: String
    ): NewsResponse
}

object RetrofitClient {
    private const val BASE_URL = "https://gnews.io/api/"

    val instance: NewsApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        retrofit.create(NewsApiService::class.java)
    }
}