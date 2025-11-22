package com.example.easyshop.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.easyshop.AppUtil
import com.example.easyshop.GlobalNavigation
import com.example.easyshop.model.ProductModel
import com.example.easyshop.model.UserModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ProductDetailsView(modifier: Modifier = Modifier, productId: String) {
    val context = LocalContext.current
    var product by remember { mutableStateOf(ProductModel()) }
    var isFavorite by remember { mutableStateOf(false) }

    // Carga de datos
    LaunchedEffect(key1 = Unit) {
        val db = Firebase.firestore
        val uid = FirebaseAuth.getInstance().currentUser?.uid

        db.collection("data").document("stock").collection("products").document(productId).get()
            .addOnSuccessListener {
                val result = it.toObject(ProductModel::class.java)
                if (result != null) product = result
            }

        if (uid != null) {
            db.collection("users").document(uid).addSnapshotListener { snapshot, _ ->
                val user = snapshot?.toObject(UserModel::class.java)
                isFavorite = user?.favoriteItems?.contains(productId) == true
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // 1. CARRUSEL DE IMÁGENES (HorizontalPager)
        val pagerState = rememberPagerState(pageCount = { product.images.size.takeIf { it > 0 } ?: 1 })

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(550.dp) // ESTA ES LA ALTURA FIJA QUE TODAS RESPETARÁN
                .background(Color.Black) // Fondo negro por si la imagen tarda en cargar
        ) {
            if (product.images.isNotEmpty()) {
                HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
                    AsyncImage(
                        model = product.images[page],
                        contentDescription = null,
                        // ESTAS DOS LÍNEAS SON LA CLAVE:
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop // Recorta la imagen para llenar todo el hueco sin deformarla
                    )
                }

                // Indicador de página (ej: 1/3)
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 24.dp, bottom = 120.dp)
                        .background(Color.Black.copy(0.6f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "${pagerState.currentPage + 1}/${product.images.size}",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                // Imagen por defecto si no hay lista
                Box(modifier = Modifier.fillMaxSize().background(Color.DarkGray))
            }

            // DEGRADADO NEGRO INFERIOR
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.6f),
                                Color.Black
                            ),
                            startY = 300f
                        )
                    )
            )
        }

        // 2. BOTONES FLOTANTES SUPERIORES
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 48.dp, start = 24.dp, end = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            CircularBlurButton(icon = Icons.Default.ArrowBack) {
                GlobalNavigation.navController.popBackStack()
            }
            // Botón de favoritos Arriba (Opcional, también está abajo)
            CircularBlurButton(
                icon = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                tint = if (isFavorite) Color.Red else Color.White
            ) {
                AppUtil.toggleFavorite(context, productId)
            }
        }

        // 3. TÍTULO GIGANTE
        // Usamos Box para asegurar que quede encima de la imagen pero detrás del scroll
        Column(
            modifier = Modifier
                .padding(top = 380.dp, start = 24.dp) // Bajé un poco el texto
                .width(320.dp)
        ) {
            Text(
                text = product.title,
                color = Color.White,
                fontSize = 36.sp, // Ajustado para títulos largos
                fontWeight = FontWeight.ExtraBold,
                lineHeight = 40.sp
            )
        }

        // 4. PANEL DE DETALLES DESLIZABLE
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .fillMaxHeight(0.45f) // Ocupa el 45% inferior
                .clip(RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF1E1E1E), Color.Black)
                    )
                )
                .padding(horizontal = 24.dp, vertical = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // FILA DE PRECIO
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text("Precio", color = Color.Gray, fontSize = 12.sp)
                    Text(
                        text = "$${product.actualPrice}",
                        color = Color.White,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                if (product.price.isNotEmpty()) {
                    Text(
                        text = "$${product.price}",
                        color = Color.Gray,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        style = androidx.compose.ui.text.TextStyle(textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough),
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // --- ESPECIFICACIONES DINÁMICAS (Solo si existen) ---
            if (product.otherDetails.isNotEmpty()) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(product.otherDetails.toList()) { (key, value) ->
                        SpecCard(
                            title = value,  // Ej: "Apple A18 Pro" (Valor grande)
                            subtitle = key  // Ej: "Modelo de CPU" (Subtítulo pequeño)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            // DESCRIPCIÓN
            Text("Descripción", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = product.description.ifEmpty { "Sin descripción disponible." },
                color = Color.Gray,
                fontSize = 14.sp,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(30.dp))

            // BOTÓN COMPRAR
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp)
                    .shadow(10.dp, RoundedCornerShape(50.dp), spotColor = Color(0xFFFF3366))
                    .clip(RoundedCornerShape(50.dp))
                    .background(Brush.horizontalGradient(listOf(Color(0xFFFF758C), Color(0xFFFF0040))))
                    .clickable { AppUtil.addToCart(context, productId) },
                contentAlignment = Alignment.Center
            ) {
                Text("Añadir al Carrito", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// --- COMPONENTES AUXILIARES ---

@Composable
fun CircularBlurButton(icon: ImageVector, tint: Color = Color.White, onClick: () -> Unit) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(45.dp)
            .border(1.dp, Color.White.copy(0.2f), CircleShape)
            .background(Color.Black.copy(alpha = 0.4f), CircleShape)
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = tint)
    }
}

@Composable
fun SpecCard(title: String, subtitle: String) {
    Box(
        modifier = Modifier
            .size(110.dp, 90.dp) // Rectángulo un poco más ancho para texto largo
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(Color(0xFF2C2C2C), Color(0xFF1A1A1A))
                )
            )
            .border(1.dp, Color.White.copy(0.1f), RoundedCornerShape(16.dp))
            .padding(12.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center) {
            // Subtítulo (Clave: ej "CPU")
            Text(
                text = subtitle.uppercase(),
                color = Color.Gray,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(4.dp))
            // Título (Valor: ej "A18 Pro")
            Text(
                text = title,
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                maxLines = 2, // Permitimos 2 líneas para que quepan nombres largos
                lineHeight = 16.sp
            )
        }
    }
}