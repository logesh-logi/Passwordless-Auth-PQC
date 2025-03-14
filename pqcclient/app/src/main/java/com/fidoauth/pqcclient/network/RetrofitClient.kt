package com.fidoauth.pqcclient.network

import android.content.Context
import com.fidoauth.pqcclient.auth.SecureStorage
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "http://10.0.2.2:8080/"

    // Logging Interceptor for debugging
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    // Authorization Interceptor to add Bearer Token in headers
    private fun getAuthInterceptor(context: Context): Interceptor {
        return Interceptor { chain ->
            val token = SecureStorage.getAuthToken(context)
            val request: Request = chain.request().newBuilder().apply {
                token?.let { addHeader("Authorization", "Bearer $it") }
            }.build()
            chain.proceed(request)
        }
    }

    // Function to create Retrofit instance with both interceptors
    fun create(context: Context): AuthService {
        val client = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor) // Logs API calls
            .addInterceptor(getAuthInterceptor(context)) // Adds Auth Token
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()

        return retrofit.create(AuthService::class.java)
    }
}
