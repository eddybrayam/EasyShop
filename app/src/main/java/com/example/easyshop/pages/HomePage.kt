package com.example.easyshop.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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

    // 1. Estado para todos los productos (lista original)
    val allProducts = remember { mutableStateListOf<ProductModel>() }

    // 2. Estado para el texto de búsqueda
    var searchText by remember { mutableStateOf("") }

    // 3. Productos filtrados (se calcula automáticamente)
    val filteredProducts = remember(searchText, allProducts) {
        if (searchText.isBlank()) {
            allProducts // Si no hay texto, muestra todo
        } else {
            allProducts.filter { product ->
                // Filtra si el título contiene el texto (ignorando mayúsculas/minúsculas)
                product.title.contains(searchText, ignoreCase = true)
            }
        }
    }

    // Cargar productos de Firebase
    LaunchedEffect(Unit) {
        Firebase.firestore.collection("data").document("stock")
            .collection("products")
            .get()
            .addOnSuccessListener {
                val products = it.toObjects<ProductModel>()
                allProducts.clear()
                allProducts.addAll(products)
            }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black),
        contentPadding = PaddingValues(16.dp)
    ) {

        item {
            HeaderView()
            Spacer(modifier = Modifier.height(16.dp))
        }

        // --- BARRA DE BÚSQUEDA FUNCIONAL ---
        item {
            OutlinedTextField(
                value = searchText,
                onValueChange = { searchText = it },
                placeholder = { Text("Buscar productos...", color = Color.Gray) },
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1E1E1E), RoundedCornerShape(16.dp)), // Fondo gris oscuro
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF00E8FF), // Cyan al enfocar
                    unfocusedBorderColor = Color.Transparent, // Sin borde al soltar
                    focusedContainerColor = Color(0xFF1E1E1E),
                    unfocusedContainerColor = Color(0xFF1E1E1E),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = Color(0xFF00E8FF)
                ),
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray)
                },
                trailingIcon = {
                    // Botón 'X' para borrar búsqueda
                    if (searchText.isNotEmpty()) {
                        IconButton(onClick = { searchText = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear", tint = Color.White)
                        }
                    }
                },
                singleLine = true
            )
            Spacer(modifier = Modifier.height(20.dp))
        }

        // --- LÓGICA DE VISUALIZACIÓN ---

        if (searchText.isNotEmpty()) {
            // SI ESTÁ BUSCANDO: Muestra solo resultados en Grid
            item {
                Text(
                    "Resultados (${filteredProducts.size})",
                    style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            if (filteredProducts.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                        Text("No se encontraron productos", color = Color.Gray)
                    }
                }
            } else {
                // Grid de resultados
                items(filteredProducts.chunked(2)) { rowProducts ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        for (product in rowProducts) {
                            Box(modifier = Modifier.weight(1f)) {
                                ProductItemView(product = product)
                            }
                        }
                        if (rowProducts.size == 1) Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }

        } else {
            // SI NO ESTÁ BUSCANDO: Muestra el Home normal (Banner, Categorías, Flash Sale)

            item {
                BannerView(modifier = Modifier.height(180.dp))
                Spacer(modifier = Modifier.height(20.dp))
            }

            item {
                Text(
                    "Categorías",
                    style = TextStyle(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))
                CategoriesView()
                Spacer(modifier = Modifier.height(24.dp))
            }

            // FLASH SALE
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
                            color = Color.White
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

                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Usamos allProducts aquí para mostrar ofertas
                    items(allProducts.take(5)) { product ->
                        FlashSaleItem(product)
                    }
                }

                Spacer(modifier = Modifier.height(30.dp))
            }

            // RECOMENDADOS
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
                        color = Color.White
                    )
                    Text("Ver Todo ->", fontSize = 12.sp, color = Color.Gray)
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Grid normal de recomendados
            items(allProducts.chunked(2)) { rowProducts ->
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
}