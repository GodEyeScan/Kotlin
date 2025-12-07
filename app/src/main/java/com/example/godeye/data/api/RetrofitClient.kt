package com.example.godeye.data.api

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Objeto singleton para proveer instancias de Retrofit y la API
 *
 * Base URL de la API en la nube: https://gateway.helmer-pardo.com
 */
object RetrofitClient {

 /**
 * URL base de la API en la nube
 *
 * Variables de entorno de Postman:
 * - web: https://gateway.helmer-pardo.com
 * - local: (ahora apunta a la misma base de datos en la nube)
 */
 private const val BASE_URL = "https://gateway.helmer-pardo.com/"

 /**
 * Configuración de Gson para parsear JSON
 */
 private val gson: Gson by lazy {
 GsonBuilder()
 .setLenient() // Permite JSON más flexible
 .create()
 }

 /**
 * Interceptor para logging de peticiones HTTP
 *
 * En producción, considerar desactivar o usar BASIC level
 */
 private val loggingInterceptor: HttpLoggingInterceptor by lazy {
 HttpLoggingInterceptor().apply {
 level = HttpLoggingInterceptor.Level.BODY
 }
 }

 /**
 * Cliente OkHttp configurado con timeouts e interceptors
 */
 private val okHttpClient: OkHttpClient by lazy {
 OkHttpClient.Builder()
 .addInterceptor(loggingInterceptor)
 .connectTimeout(30, TimeUnit.SECONDS)
 .readTimeout(30, TimeUnit.SECONDS)
 .writeTimeout(30, TimeUnit.SECONDS)
 .retryOnConnectionFailure(true)
 .build()
 }

 /**
 * Instancia de Retrofit configurada
 */
 private val retrofit: Retrofit by lazy {
 Retrofit.Builder()
 .baseUrl(BASE_URL)
 .client(okHttpClient)
 .addConverterFactory(GsonConverterFactory.create(gson))
 .build()
 }

 /**
 * Instancia del servicio de API
 *
 * Uso:
 * ```
 * val apiService = RetrofitClient.apiService
 * val response = apiService.register(RegisterRequest("email@test.com", "pass123"))
 * ```
 */
 val apiService: GodEyeApiService by lazy {
 retrofit.create(GodEyeApiService::class.java)
 }
}

