package com.example.easyshop.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.easyshop.AppUtil
import com.example.easyshop.model.ProductModel
import com.example.easyshop.model.UserModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore

@Composable
fun FavoritePage(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val favoriteProducts = remember { mutableStateListOf<ProductModel>() }
    var isLoading by remember { mutableStateOf(true) }
    val auth = FirebaseAuth.getInstance()

    // Cargar los productos favoritos
    LaunchedEffect(Unit) {
        val uid = auth.currentUser?.uid
        if (uid != null) {
            // 1. Escuchamos cambios en el usuario en tiempo real (para actualizar si quitas un like)
            Firebase.firestore.collection("users").document(uid)
                .addSnapshotListener { snapshot, _ ->
                    val user = snapshot?.toObject(UserModel::class.java)
                    val favIds = user?.favoriteItems ?: emptyList()

                    if (favIds.isEmpty()) {
                        favoriteProducts.clear()
                        isLoading = false
                    } else {
                        // 2. Buscamos los productos que coincidan con esos IDs
                        // Nota: Firestore 'whereIn' soporta máximo 10 items.
                        // Si tienes más, tendrías que hacer lógica extra, pero para este ejemplo sirve.
                        Firebase.firestore.collection("data").document("stock")
                            .collection("products")
                            .whereIn("id", favIds) // Filtramos solo los favoritos
                            .get()
                            .addOnSuccessListener { productsSnapshot ->
                                val products = productsSnapshot.toObjects(ProductModel::class.java)
                                favoriteProducts.clear()
                                favoriteProducts.addAll(products)
                                isLoading = false
                            }
                    }
                }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF9F9F9))
            .padding(16.dp)
    ) {
        Text(
            text = "Mis Favoritos",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF3344CC))
            }
        } else if (favoriteProducts.isEmpty()) {
            // VISTA VACÍA
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.Favorite,
                    contentDescription = null,
                    tint = Color.LightGray,
                    modifier = Modifier.size(80.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text("Aún no tienes favoritos", fontSize = 18.sp, color = Color.Gray)
                Text("Dale ❤️ a los productos que te gusten", fontSize = 14.sp, color = Color.Gray)
            }
        } else {
            // GRILLA DE PRODUCTOS
            LazyVerticalGrid(
                columns = GridCells.Fixed(2), // 2 columnas
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(favoriteProducts) { product ->
                    FavoriteProductItem(
                        product = product,
                        onRemoveClick = {
                            // Llamamos a la función de AppUtil para quitarlo
                            AppUtil.toggleFavorite(context, product.id)
                        },
                        onAddToCart = {
                            AppUtil.addToCart(context, product.id)
                        }
                    )
                }
            }
        }
    }
}

// --- TARJETA DE PRODUCTO FAVORITO ---
@Composable
fun FavoriteProductItem(
    product: ProductModel,
    onRemoveClick: () -> Unit,
    onAddToCart: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp)
            .shadow(4.dp, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp)
    ) {
        Box {
            Column {
                // Imagen del producto
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = product.images.firstOrNull() ?: "", // Primera imagen o vacío
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                // Información
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = product.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "$${product.actualPrice}",
                        color = Color(0xFF3344CC),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Botón Agregar al Carrito
                    // --- REEMPLAZA EL BOTÓN NEGRO POR ESTE DE ESTILO 3D ---
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp) // Altura ajustada para la tarjeta pequeña
                            // 1. Sombra un poco más sutil
                            .shadow(
                                elevation = 6.dp,
                                shape = RoundedCornerShape(50.dp),
                                spotColor = Color(0xFFFF3366),
                                ambientColor = Color(0xFFFF3366)
                            )
                            .clip(RoundedCornerShape(50.dp))
                            // 2. El mismo degradado vertical (Volumen)
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xFFFF758C), // Rosa claro
                                        Color(0xFFFF0040)  // Rojo intenso
                                    )
                                )
                            )
                            // 3. Borde de brillo superior
                            .border(
                                width = 1.dp,
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color.White.copy(alpha = 0.5f),
                                        Color.Transparent
                                    )
                                ),
                                shape = RoundedCornerShape(50.dp)
                            )
                            .clickable { onAddToCart() },
                        contentAlignment = Alignment.Center
                    ) {
                        // Contenido: Texto + Icono
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Agregar",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                style = androidx.compose.ui.text.TextStyle(
                                    shadow = androidx.compose.ui.graphics.Shadow(
                                        color = Color.Black.copy(alpha = 0.2f), blurRadius = 2f
                                    )
                                )
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.ShoppingCart,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }

            // Botón de Eliminar (Corazón o Basura) arriba a la derecha
            IconButton(
                onClick = onRemoveClick,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .size(30.dp)
                    .background(Color.White.copy(alpha = 0.7f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete, // O Icons.Default.Favorite con tint rojo
                    contentDescription = "Eliminar",
                    tint = Color.Red,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}