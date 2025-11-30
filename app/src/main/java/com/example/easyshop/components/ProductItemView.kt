package com.example.easyshop.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
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

// PALETA PREMIUM
private object ProductColors {
    val Black = Color(0xFF000000)
    val DarkBg = Color(0xFF0A0A0A)
    val DarkSurface = Color(0xFF111111)
    val MediumSurface = Color(0xFF1A1A1A)
    val LightSurface = Color(0xFF2A2A2A)
    val TextPrimary = Color(0xFFFFFFFF)
    val TextSecondary = Color(0xFFB5B5B5)
    val Divider = Color(0xFF262626)

    val CyanAccent = Color(0xFF00E8FF)
    val CyanLight = Color(0xFF00E8FF).copy(alpha = 0.15f)
    val CyanMuted = Color(0xFF00E8FF).copy(alpha = 0.3f)

    val FavoriteActive = Color(0xFFFF006E)
}

@Composable
fun ProductItemView(modifier: Modifier = Modifier, product: ProductModel) {
    val context = LocalContext.current
    var isFavorite by remember { mutableStateOf(false) }
    var isPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        label = "ProductScale"
    )

    LaunchedEffect(Unit) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid != null) {
            Firebase.firestore.collection("users").document(uid)
                .addSnapshotListener { snapshot, _ ->
                    val user = snapshot?.toObject(UserModel::class.java)
                    isFavorite = user?.favoriteItems?.contains(product.id) == true
                }
        }
    }

    Surface(
        modifier = modifier
            .width(170.dp)
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(20.dp),
                spotColor = ProductColors.CyanAccent.copy(alpha = 0.1f)
            )
            .graphicsLayer(scaleX = scale, scaleY = scale),
        shape = RoundedCornerShape(20.dp),
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            ProductColors.MediumSurface,
                            ProductColors.DarkSurface
                        )
                    )
                )
                .border(
                    width = 1.dp,
                    color = ProductColors.Divider,
                    shape = RoundedCornerShape(20.dp)
                )
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) {
                    isPressed = true
                    GlobalNavigation.navController.navigate("product-details/${product.id}")
                }
        ) {
            Column {
                // IMAGEN
                Box(
                    modifier = Modifier
                        .height(160.dp)
                        .fillMaxWidth()
                        .background(ProductColors.DarkBg)
                ) {
                    AsyncImage(
                        model = product.images.firstOrNull(),
                        contentDescription = product.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

                    // Overlay gradiente sutil
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color.Black.copy(alpha = 0.3f)
                                    )
                                )
                            )
                    )
                }

                // INFO
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Text(
                        text = product.title,
                        color = ProductColors.TextPrimary,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        fontSize = 14.sp,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // PRECIOS
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            Text(
                                text = "$${product.actualPrice}",
                                color = ProductColors.CyanAccent,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp
                            )

                            if (product.price.isNotEmpty() &&
                                product.price.toDoubleOrNull() ?: 0.0 > product.actualPrice.toDoubleOrNull() ?: 0.0) {
                                Text(
                                    text = "$${product.price}",
                                    fontSize = 11.sp,
                                    color = ProductColors.TextSecondary,
                                    style = TextStyle(
                                        textDecoration = TextDecoration.LineThrough
                                    )
                                )
                            }
                        }

                        // Badge de descuento
                        if (product.price.isNotEmpty() &&
                            product.price.toDoubleOrNull() ?: 0.0 > product.actualPrice.toDoubleOrNull() ?: 0.0) {
                            val originalPrice = product.price.toDoubleOrNull() ?: 0.0
                            val actualPrice = product.actualPrice.toDoubleOrNull() ?: 0.0
                            val discount = ((originalPrice - actualPrice) / originalPrice * 100).toInt()

                            if (discount > 0) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = ProductColors.FavoriteActive.copy(alpha = 0.15f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .border(
                                                width = 1.dp,
                                                color = ProductColors.FavoriteActive.copy(alpha = 0.3f),
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "-$discount%",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = ProductColors.FavoriteActive
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // BOTÓN FAVORITO FLOTANTE
            Surface(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .size(32.dp)
                    .shadow(
                        elevation = 8.dp,
                        shape = CircleShape,
                        spotColor = if (isFavorite) ProductColors.FavoriteActive.copy(alpha = 0.4f)
                        else ProductColors.CyanAccent.copy(alpha = 0.2f)
                    ),
                shape = CircleShape,
                color = ProductColors.MediumSurface.copy(alpha = 0.9f)
            ) {
                IconButton(
                    onClick = { AppUtil.toggleFavorite(context, product.id) },
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = null,
                        tint = if (isFavorite) ProductColors.FavoriteActive else ProductColors.TextPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}