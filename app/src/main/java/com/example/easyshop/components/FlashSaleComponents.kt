package com.example.easyshop.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.easyshop.GlobalNavigation
import com.example.easyshop.model.ProductModel

// --- 1. EL TEMPORIZADOR ROJO ---
@Composable
fun CountDownTimer() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .background(Color(0xFFD32F2F), RoundedCornerShape(4.dp)) // Rojo
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Icon(
            painter = androidx.compose.ui.res.painterResource(android.R.drawable.ic_menu_recent_history), // O usa Icons.Default.Timer
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "01 : 31 : 21", // Esto podría ser dinámico luego
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

// --- 2. LA TARJETA DE FLASH SALE (Pequeña y con barra) ---
@Composable
fun FlashSaleItem(product: ProductModel) {
    Card(
        modifier = Modifier
            .width(140.dp) // Más estrecha que la normal
            .padding(end = 12.dp)
            .clickable {
                GlobalNavigation.navController.navigate("product-details/${product.id}")
            },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            // IMAGEN + BADGE DE DESCUENTO
            Box(modifier = Modifier.height(100.dp).fillMaxWidth()) {
                AsyncImage(
                    model = product.images.firstOrNull(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                // Badge Rojo de %
                Box(
                    modifier = Modifier
                        .background(Color(0xFFFF4081), RoundedCornerShape(topStart = 8.dp, bottomEnd = 8.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text("20%", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // BARRA DE PROGRESO (STOCK)
            LinearProgressIndicator(
                progress = 0.7f, // Simulado al 70%
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = Color(0xFFD32F2F), // Rojo
                trackColor = Color(0xFFFFCDD2) // Rojo claro
            )

            Spacer(modifier = Modifier.height(4.dp))

            // PRECIOS
            Text(
                text = "$${product.actualPrice}",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Text(
                text = "$${product.price}",
                textDecoration = TextDecoration.LineThrough,
                color = Color.Gray,
                fontSize = 12.sp
            )
        }
    }
}