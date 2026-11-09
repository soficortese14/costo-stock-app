package cl.duoc.costostock.data.repository

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

    private val client = OkHttpClient.Builder()
        .addInterceptor(logging)
        .build()

    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl("https://api.ejemplo.com/")
        /*
         * Debe terminar en "/" siempre, por el prefix de las APIs después
         * para los GET/POST/PUT/DELETE
         */
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

        val api: ApiService = retrofit.create(ApiService::class.java)

}