package com.example.easyshop

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.example.easyshop.model.ProductModel
import com.example.easyshop.model.UserModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await

// 1. El Receptor (Puente con el sistema Android)
class FavoritesWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = FavoritesWidget()
}

// 2. La Clase del Widget (Con el permiso especial activado)
@SuppressLint("RestrictedApi")
class FavoritesWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        // Cargar datos de Firebase
        val favoriteProducts = fetchFavorites()

        provideContent {
            // Diseño General: Fondo Negro (Estilo Porsche/Dark)
            Row(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(Color(0xFF121212)) // Fondo casi negro
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                // --- IZQUIERDA: LISTA DE PRODUCTOS ---
                Column(
                    modifier = GlanceModifier.defaultWeight().fillMaxHeight()
                ) {
                    Text(
                        text = "Mis Favoritos ❤️",
                        style = TextStyle(
                            color = ColorProvider(Color.White),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )

                    Spacer(modifier = GlanceModifier.height(8.dp))

                    if (favoriteProducts.isEmpty()) {
                        Text(
                            text = "No tienes favoritos guardados.",
                            style = TextStyle(color = ColorProvider(Color.Gray), fontSize = 12.sp)
                        )
                    } else {
                        // Lista con scroll (LazyColumn de Glance)
                        androidx.glance.appwidget.lazy.LazyColumn {
                            items(favoriteProducts.size) { index ->
                                ProductRow(favoriteProducts[index])
                                Spacer(modifier = GlanceModifier.height(8.dp))
                            }
                        }
                    }
                }

                // --- DERECHA: TU PERSONAJE (ROBOCAT) ---
                // Al tocarlo, abre la App
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = GlanceModifier.width(90.dp).clickable(actionStartActivity<MainActivity>())
                ) {
                    Image(
                        provider = ImageProvider(R.drawable.widget_character), // Asegúrate que este sea tu GIF/PNG en drawable
                        contentDescription = "AI Assistant",
                        modifier = GlanceModifier.size(70.dp)
                    )
                    Spacer(modifier = GlanceModifier.height(4.dp))
                    Text(
                        text = "¡Hola!",
                        style = TextStyle(color = ColorProvider(Color.White), fontSize = 10.sp)
                    )
                }
            }
        }
    }

    // Componente para cada fila (Tarjeta oscura)
    @Composable
    fun ProductRow(product: ProductModel) {
        Row(
            modifier = GlanceModifier
                .fillMaxWidth()
                .background(Color(0xFF1E1E1E)) // Gris oscuro para la tarjeta
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = GlanceModifier.defaultWeight()) {
                Text(
                    text = product.title,
                    style = TextStyle(color = ColorProvider(Color.White), fontSize = 12.sp),
                    maxLines = 1
                )
                Text(
                    text = "$${product.actualPrice}",
                    style = TextStyle(color = ColorProvider(Color(0xFF3344CC)), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                )
            }
        }
    }

    // Lógica de Firebase (Segura ante fallos)
    private suspend fun fetchFavorites(): List<ProductModel> {
        return try {
            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return emptyList()
            val db = Firebase.firestore

            // 1. Buscar IDs
            val userDoc = db.collection("users").document(uid).get().await()
            val user = userDoc.toObject(UserModel::class.java)
            val favIds = user?.favoriteItems ?: emptyList()

            if (favIds.isEmpty()) return emptyList()

            // 2. Buscar Productos (Máximo 10 por limitación de 'in')
            val safeIds = favIds.take(10)
            val productsSnapshot = db.collection("data").document("stock")
                .collection("products")
                .whereIn("id", safeIds)
                .get()
                .await()

            productsSnapshot.toObjects(ProductModel::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }
}