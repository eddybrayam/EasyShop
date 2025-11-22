package com.example.easyshop

import android.content.Context
import android.widget.Toast
import com.example.easyshop.model.UserModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.firestore

object AppUtil {
    fun showToast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }

    fun addToCart(context: Context, productId: String) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid == null) {
            showToast(context, "Inicia sesión para añadir artículos al carrito.")
            return
        }

        val userDoc = Firebase.firestore.collection("users").document(uid)

        Firebase.firestore.runTransaction { transaction ->
            val snapshot = transaction.get(userDoc)
            val cartItems = (snapshot.get("cartItems") as? Map<String, Long>)?.toMutableMap() ?: mutableMapOf()

            val currentQuantity = cartItems[productId] ?: 0L
            cartItems[productId] = currentQuantity + 1

            transaction.set(userDoc, mapOf("cartItems" to cartItems), SetOptions.merge())
            null
        }.addOnSuccessListener {
            showToast(context, "Artículo añadido al carrito")
        }.addOnFailureListener {
            showToast(context, "Error al añadir el artículo al carrito")
        }
    }

    fun removeFromCart(context: Context, productId: String, removeAll: Boolean = false) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid == null) {
            showToast(context, "Por favor, inicie sesión.")
            return
        }
        val userDoc = Firebase.firestore.collection("users").document(uid)

        userDoc.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val document = task.result
                if (document != null && document.exists()) {
                    val currentCart = document.get("cartItems") as? Map<String, Long> ?: emptyMap()
                    val currentQuantity = currentCart[productId] ?: 0

                    if (currentQuantity > 0) {
                        val updatedQuantity = currentQuantity - 1
                        val update = if (updatedQuantity <= 0 || removeAll) {
                            mapOf("cartItems.$productId" to FieldValue.delete())
                        } else {
                            mapOf("cartItems.$productId" to updatedQuantity)
                        }

                        userDoc.update(update)
                            .addOnSuccessListener {
                                showToast(context, "Artículo eliminado del carrito.")
                            }
                            .addOnFailureListener {
                                showToast(context, "Error al eliminar el artículo.")
                            }
                    }
                }
            } else {
                showToast(context, "Error al obtener los datos del carrito.")
            }
        }
    }

    fun getDiscountPercentage(): Float {
        return 10.0f
    }

    fun getTaxPercentage(): Float {
        return 13.0f
    }
    // Función para dar/quitar Like
    fun toggleFavorite(context: Context, productId: String) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val userRef = Firebase.firestore.collection("users").document(uid)

        userRef.get().addOnSuccessListener { document ->
            val user = document.toObject(UserModel::class.java) ?: return@addOnSuccessListener
            val currentFavs = user.favoriteItems.toMutableList()

            if (currentFavs.contains(productId)) {
                // Si ya existe, lo quitamos
                currentFavs.remove(productId)
                showToast(context, "Eliminado de favoritos")
            } else {
                // Si no existe, lo agregamos
                currentFavs.add(productId)
                showToast(context, "Agregado a favoritos ❤️")
            }

            // Guardamos la lista actualizada en Firebase
            userRef.update("favoriteItems", currentFavs)
        }
    }
}
