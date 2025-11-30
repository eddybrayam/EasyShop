package com.example.easyshop.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

// PALETA PREMIUM
private object CartItemColors {
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
}

@Composable
fun CartItemView(modifier: Modifier = Modifier, productId: String, qty: Long) {

    var product by remember {
        mutableStateOf(ProductModel())
    }

    LaunchedEffect(key1 = Unit) {
        Firebase.firestore.collection("data")
            .document("stock")
            .collection("products")
            .document(productId).get().addOnCompleteListener {
                if (it.isSuccessful) {
                    val result = it.result.toObject(ProductModel::class.java)
                    if (result != null) {
                        product = result
                    }
                }
            }
    }

    val context = LocalContext.current

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = CartItemColors.CyanAccent.copy(alpha = 0.1f)
            ),
        shape = RoundedCornerShape(16.dp),
        color = CartItemColors.MediumSurface
    ) {
        Row(
            modifier = Modifier
                .border(
                    width = 1.dp,
                    color = CartItemColors.Divider,
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // IMAGEN DEL PRODUCTO
            Surface(
                modifier = Modifier
                    .size(100.dp)
                    .shadow(
                        elevation = 6.dp,
                        shape = RoundedCornerShape(12.dp),
                        spotColor = CartItemColors.CyanAccent.copy(alpha = 0.15f)
                    ),
                shape = RoundedCornerShape(12.dp),
                color = CartItemColors.DarkSurface
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    CartItemColors.LightSurface,
                                    CartItemColors.DarkSurface
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = product.images.firstOrNull(),
                        contentDescription = product.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // INFO DEL PRODUCTO
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 4.dp)
            ) {
                Text(
                    text = product.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = CartItemColors.TextPrimary,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "$${product.actualPrice}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = CartItemColors.CyanAccent
                )

                Spacer(modifier = Modifier.height(8.dp))

                // CONTROLES DE CANTIDAD
                Surface(
                    modifier = Modifier
                        .shadow(
                            elevation = 4.dp,
                            shape = RoundedCornerShape(20.dp),
                            spotColor = CartItemColors.CyanAccent.copy(alpha = 0.1f)
                        ),
                    shape = RoundedCornerShape(20.dp),
                    color = CartItemColors.LightSurface
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier
                            .border(
                                width = 1.dp,
                                color = CartItemColors.Divider,
                                shape = RoundedCornerShape(20.dp)
                            )
                            .padding(horizontal = 4.dp)
                    ) {
                        // Botón -
                        Surface(
                            modifier = Modifier
                                .size(28.dp)
                                .clickable(
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() }
                                ) {
                                    AppUtil.removeFromCart(context, productId)
                                },
                            shape = CircleShape,
                            color = CartItemColors.DarkSurface
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Text(
                                    text = "−",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CartItemColors.TextPrimary
                                )
                            }
                        }

                        // Cantidad
                        Text(
                            text = "$qty",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = CartItemColors.TextPrimary,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )

                        // Botón +
                        Surface(
                            modifier = Modifier
                                .size(28.dp)
                                .clickable(
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() }
                                ) {
                                    AppUtil.addToCart(context, productId)
                                },
                            shape = CircleShape,
                            color = CartItemColors.CyanAccent
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Text(
                                    text = "+",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // BOTÓN ELIMINAR
            Surface(
                modifier = Modifier
                    .size(36.dp)
                    .shadow(
                        elevation = 6.dp,
                        shape = CircleShape,
                        spotColor = Color(0xFFFF006E).copy(alpha = 0.3f)
                    ),
                shape = CircleShape,
                color = CartItemColors.LightSurface
            ) {
                IconButton(
                    onClick = {
                        AppUtil.removeFromCart(context, productId, removeAll = true)
                    },
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Remove from cart",
                        tint = Color(0xFFFF006E),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}