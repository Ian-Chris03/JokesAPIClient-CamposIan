package ph.edu.comteq.jokesapiclient_camposian

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface JokesAPIService {
    @GET("jokes_api/")
    suspend fun getJokes(): List<joke>

    @POST("jokes_api/")
    suspend fun addJokes(@Body joke: joke): joke

    @DELETE("jokes_api/{id}")
    suspend fun deleteJokes(@Path("id") id: Int): joke

    @PUT ("jokes_api/{id}")
    suspend fun updateJoke(@Path("id") id: Int, @Body joke: joke): joke
}