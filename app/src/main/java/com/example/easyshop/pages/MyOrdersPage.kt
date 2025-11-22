package com.example.easyshop.pages

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.easyshop.model.OrderModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObjects
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyOrdersPage(navController: NavController) {
    val orderList = remember { mutableStateListOf<OrderModel>() }
    var isLoading by remember { mutableStateOf(true) }

    // Cargar pedidos desde Firebase
    LaunchedEffect(Unit) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid != null) {
            Firebase.firestore.collection("users").document(uid)
                .collection("orders")
                .orderBy("date", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener {
                    val misPedidos = it.toObjects<OrderModel>()
                    orderList.clear()
                    orderList.addAll(misPedidos)
                    isLoading = false
                }
                .addOnFailureListener {
                    isLoading = false // Dejar de cargar aunque falle
                }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis Pedidos") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (orderList.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Aún no has realizado compras.", color = Color.Gray)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(orderList) { order ->
                        OrderItemView(order)
                    }
                }
            }
        }
    }
}

@Composable
fun OrderItemView(order: OrderModel) {
    // Formatear fecha
    val dateString = try {
        SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(order.date))
    } catch (e: Exception) {
        "Fecha desconocida"
    }

    // Color según estado
    val statusColor = when(order.status) {
        "Entregado" -> Color(0xFF4CAF50) // Verde
        "Cancelado" -> Color.Red
        else -> Color(0xFFFF9800) // Naranja (En camino)
    }

    Card(
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Cabecera del pedido
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Pedido #${order.orderId.takeLast(6)}", fontWeight = FontWeight.Bold)
                Text(order.status, color = statusColor, fontWeight = FontWeight.Bold)
            }
            Text(dateString, fontSize = 12.sp, color = Color.Gray)

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Lista resumen de productos
            order.products.forEach { product ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Usamos 'title' porque así se llama en tu ProductModel
                    Text(
                        text = product.title,
                        fontSize = 14.sp,
                        modifier = Modifier.weight(1f),
                        maxLines = 1
                    )
                    Text(text = "$${product.actualPrice}", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Total y Dirección
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Total Pagado:", fontWeight = FontWeight.Bold)
                Text("$${order.totalPrice}", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF3344CC))
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text("Enviado a: ${order.address}", fontSize = 12.sp, color = Color.Gray)
        }
    }
}