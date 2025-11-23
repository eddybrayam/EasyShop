package com.example.easyshop

import com.google.gson.annotations.SerializedName
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

// 1. Estructura de lo que enviamos
data class OpenAIRequest(
    val model: String = "gpt-3.5-turbo", // O "gpt-4" si lo tienes pagado
    val messages: List<Message>
)

data class Message(
    val role: String, // "system", "user", o "assistant"
    val content: String
)

// 2. Estructura de lo que recibimos
data class OpenAIResponse(
    val choices: List<Choice>
)

data class Choice(
    val message: Message
)

// 3. La Interfaz de Conexión
interface OpenAIApi {
    @Headers("Content-Type: application/json")
    @POST("v1/chat/completions")
    suspend fun chat(@Body request: OpenAIRequest): OpenAIResponse
}

// 4. El objeto para crear la conexión (Singleton)
object RetrofitClient {
    private const val BASE_URL = "https://api.openai.com/"

    val api: OpenAIApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(
                okhttp3.OkHttpClient.Builder()
                    .addInterceptor { chain ->
                        val request = chain.request().newBuilder()
                            // ¡AQUÍ VA TU API KEY DE OPENAI!
                            .addHeader("Authorization", "Bearer sk-api")
                            .build()
                        chain.proceed(request)
                    }.build()
            )
            .build()
            .create(OpenAIApi::class.java)
    }
}