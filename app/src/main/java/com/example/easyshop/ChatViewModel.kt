package com.example.easyshop

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.easyshop.model.ProductModel
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.launch

data class ChatMessage(val text: String, val isUser: Boolean)

class ChatViewModel : ViewModel() {
    val messages = mutableStateListOf<ChatMessage>()

    // Historial de la conversación para enviarlo a OpenAI
    private val conversationHistory = mutableListOf<Message>()

    init {
        loadProductsAndInitSystem()
    }

    private fun loadProductsAndInitSystem() {
        // 1. Bajar productos de Firebase
        Firebase.firestore.collection("data").document("stock")
            .collection("products").get()
            .addOnSuccessListener { result ->
                val products = result.toObjects(ProductModel::class.java)

                // 2. Crear el inventario en texto
                val inventoryText = products.joinToString("\n") {
                    "- ${it.title}: $${it.actualPrice} (${it.description})"
                }

                // 3. Configurar la personalidad del Vendedor (MODO ESTRICTO)
                val systemPrompt = """
                    ACTÚA COMO: Un asistente de ventas exclusivo de la tienda "EasyShop".
                    
                    TU INVENTARIO DISPONIBLE ES ÚNICAMENTE ESTE:
                    $inventoryText
                    
                    TUS REGLAS INQUEBRANTABLES:
                    1. SOLO puedes recomendar productos que estén en la lista de arriba.
                    2. Si el usuario pregunta por un producto que NO está en la lista (ej: "Tienes iPhones?" y no está en la lista), di amablemente que no lo tienes en stock y ofrece una alternativa de la lista.
                    3. NO respondas preguntas sobre temas generales (historia, matemáticas, clima, política, cocina, etc.). Si te preguntan eso, responde: "Lo siento, solo puedo ayudarte con productos de tecnología de EasyShop".
                    4. NO inventes precios ni características. Usa solo los datos del inventario.
                    5. Sé persuasivo, breve y usa emojis ocasionalmente.
                    6. Si te preguntan quién eres, di "Soy el asistente virtual de EasyShop".
                """.trimIndent()

                // Agregamos la instrucción inicial
                conversationHistory.add(Message("system", systemPrompt))

                // Agregamos la instrucción inicial (el usuario no ve esto)
                conversationHistory.add(Message("system", systemPrompt))
            }
            .addOnFailureListener {
                messages.add(ChatMessage("Error al cargar inventario.", false))
            }
    }

    fun sendMessage(userText: String) {
        // 1. Mostrar mensaje del usuario en pantalla
        messages.add(ChatMessage(userText, true))

        // 2. Agregar al historial de la API
        conversationHistory.add(Message("user", userText))

        viewModelScope.launch {
            try {
                // 3. Enviar a OpenAI
                val request = OpenAIRequest(messages = conversationHistory)
                val response = RetrofitClient.api.chat(request)

                // 4. Obtener respuesta
                val botReply = response.choices.first().message.content

                // 5. Mostrar respuesta en pantalla
                messages.add(ChatMessage(botReply, false))
                conversationHistory.add(Message("assistant", botReply))

            } catch (e: Exception) {
                e.printStackTrace()
                messages.add(ChatMessage("Error: ${e.localizedMessage}", false))
            }
        }
    }
}