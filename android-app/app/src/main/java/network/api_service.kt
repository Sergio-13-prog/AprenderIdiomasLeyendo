package network

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.Response
import okhttp3.logging.HttpLoggingInterceptor

object ApiClient {
    //private const val BASE_URL = "http://192.168.174.218:8080/"
    private const val BASE_URL = "http://10.0.2.2:8080/"



    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
interface ApiService {
    @POST("process")
    suspend fun processText(@Body request: ProcessRequest): Response<ProcessResponse>
}


