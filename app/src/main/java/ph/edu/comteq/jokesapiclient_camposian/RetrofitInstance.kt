package ph.edu.comteq.jokesapiclient_camposian

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import ph.edu.comteq.jokesapiclient_camposian.JokesAPIService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
    //base url
    private const val BASE_URL = "https://programmingwizards.tech/"

    //logging interceptor
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    //OkHttp client
    private val client = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor).build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val jokeAPI: JokesAPIService = retrofit.create(JokesAPIService::class.java)
}