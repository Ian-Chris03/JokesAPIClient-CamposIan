package ph.edu.comteq.jokesapiclient_camposian

import retrofit2.http.GET

interface JokesAPIService {
    @GET("jokes_api/")
    suspend fun getJokes(): List<Joke>
}