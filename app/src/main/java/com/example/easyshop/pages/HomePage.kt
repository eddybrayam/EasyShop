package com.example.easyshop.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.easyshop.components.BannerView
import com.example.easyshop.components.CategoriesView
import com.example.easyshop.components.HeaderView
import com.example.easyshop.components.CountDownTimer
import com.example.easyshop.components.FlashSaleItem
import com.example.easyshop.components.ProductItemView
import com.example.easyshop.model.ProductModel
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObjects

@Composable
fun HomePage(modifier: Modifier = Modifier) {

    // 1. Estado para guardar los productos
    val productList = remember { mutableStateListOf<ProductModel>() }

    // 2. Descargar productos
    LaunchedEffect(Unit) {
        Firebase.firestore.collection("data").document("stock")
            .collection("products")
            .get()
            .addOnSuccessListener {
                val products = it.toObjects<ProductModel>()
                productList.clear()
                productList.addAll(products)
            }
    }

    // 3. LazyColumn con Fondo NEGRO
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black), // <--- CAMBIO CLAVE: Fondo Negro
        contentPadding = PaddingValues(16.dp)
    ) {

        // --- TUS COMPONENTES ACTUALES ---
        item {
            HeaderView() // Nota: Asegúrate de que el texto dentro de HeaderView sea blanco también
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            BannerView(modifier = Modifier.height(180.dp))
            Spacer(modifier = Modifier.height(20.dp))
        }

        item {
            Text(
                "Categorías",
                style = TextStyle(
                    fontSize = 20.sp, // Un poco más grande
                    fontWeight = FontWeight.Bold,
                    color = Color.White // <--- Texto Blanco
                )
            )
            Spacer(modifier = Modifier.height(12.dp))
            CategoriesView() // Nota: Revisa que los textos de las categorías se vean bien
            Spacer(modifier = Modifier.height(24.dp))
        }

        // --- SECCIÓN FLASH SALE ---
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Flash Sale",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White // <--- Texto Blanco
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Termina en ", fontSize = 12.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.width(4.dp))
                        CountDownTimer()
                    }
                }
                Text("Ver Todo ->", fontSize = 12.sp, color = Color.Gray, modifier = Modifier.clickable {})
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Lista horizontal de ofertas
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(productList.take(5)) { product ->
                    FlashSaleItem(product)
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }

        // --- SECCIÓN RECOMENDADOS ---
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Recomendado para ti",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White // <--- Texto Blanco
                )
                Text("Ver Todo ->", fontSize = 12.sp, color = Color.Gray)
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Grid de productos (Filas de 2)
        items(productList.chunked(2)) { rowProducts ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                for (product in rowProducts) {
                    Box(modifier = Modifier.weight(1f)) {
                        ProductItemView(product = product)
                    }
                }
                if (rowProducts.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}