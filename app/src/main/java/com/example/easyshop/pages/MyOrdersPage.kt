package com.example.easyshop.pages

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
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

// PALETA PREMIUM
private object OrdersColors {
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

    // Estados de pedido
    val StatusDelivered = Color(0xFF00FF9C) // Verde neón
    val StatusPending = Color(0xFFFFAA00) // Naranja
    val StatusCancelled = Color(0xFFFF006E) // Rosa/Rojo
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyOrdersPage(navController: NavController) {
    val orderList = remember { mutableStateListOf<OrderModel>() }
    var isLoading by remember { mutableStateOf(true) }

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
                    isLoading = false
                }
        }
    }

    Scaffold(
        topBar = {
            PremiumOrdersTopBar(
                onBackClick = { navController.popBackStack() }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(OrdersColors.DarkBg)
                .padding(padding)
        ) {
            AnimatedContent(
                targetState = isLoading to orderList.isEmpty(),
                label = "OrdersContentAnimation"
            ) { (loading, empty) ->
                when {
                    loading -> PremiumLoadingState()
                    empty -> PremiumEmptyState()
                    else -> PremiumOrdersList(orderList)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PremiumOrdersTopBar(
    onBackClick: () -> Unit
) {
    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .width(5.dp)
                        .height(28.dp)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    OrdersColors.CyanAccent,
                                    OrdersColors.CyanAccent.copy(alpha = 0.5f)
                                )
                            )
                        )
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    "Mis Pedidos",
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = OrdersColors.TextPrimary
                )
            }
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = "Atrás",
                    tint = OrdersColors.CyanAccent
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = OrdersColors.DarkSurface
        ),
        modifier = Modifier.border(
            width = 1.dp,
            color = OrdersColors.Divider
        )
    )
}

@Composable
private fun PremiumLoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Surface(
                modifier = Modifier
                    .size(60.dp)
                    .shadow(
                        elevation = 16.dp,
                        shape = CircleShape,
                        spotColor = OrdersColors.CyanAccent.copy(alpha = 0.3f)
                    ),
                shape = CircleShape,
                color = OrdersColors.MediumSurface.copy(alpha = 0.8f)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = OrdersColors.CyanAccent,
                        strokeWidth = 2.5.dp,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Cargando pedidos...",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = OrdersColors.TextSecondary
            )
        }
    }
}

@Composable
private fun PremiumEmptyState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AnimatedVisibility(
            visible = true,
            enter = fadeIn() + scaleIn(initialScale = 0.8f)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Surface(
                    modifier = Modifier
                        .size(100.dp)
                        .shadow(
                            elevation = 16.dp,
                            shape = CircleShape,
                            spotColor = OrdersColors.CyanAccent.copy(alpha = 0.3f)
                        ),
                    shape = CircleShape,
                    color = OrdersColors.MediumSurface
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        OrdersColors.LightSurface,
                                        OrdersColors.MediumSurface
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.ShoppingBag,
                            contentDescription = null,
                            tint = OrdersColors.CyanAccent.copy(alpha = 0.5f),
                            modifier = Modifier.size(50.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                Text(
                    text = "Aún no has realizado compras",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = OrdersColors.TextPrimary
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Explora nuestro catálogo y realiza tu primera compra",
                    fontSize = 14.sp,
                    color = OrdersColors.TextSecondary
                )
            }
        }
    }
}

@Composable
private fun PremiumOrdersList(orderList: List<OrderModel>) {
    LazyColumn(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        items(orderList) { order ->
            PremiumOrderItem(order)
        }
    }
}

@Composable
private fun PremiumOrderItem(order: OrderModel) {
    val dateString = try {
        SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(order.date))
    } catch (e: Exception) {
        "Fecha desconocida"
    }

    val statusColor = when (order.status) {
        "Entregado" -> OrdersColors.StatusDelivered
        "Cancelado" -> OrdersColors.StatusCancelled
        else -> OrdersColors.StatusPending
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = OrdersColors.CyanAccent.copy(alpha = 0.1f)
            ),
        shape = RoundedCornerShape(16.dp),
        color = OrdersColors.MediumSurface
    ) {
        Column(
            modifier = Modifier
                .border(
                    width = 1.dp,
                    color = OrdersColors.Divider,
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(16.dp)
        ) {
            // HEADER
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Pedido #${order.orderId.takeLast(6)}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = OrdersColors.TextPrimary
                    )
                    Text(
                        dateString,
                        fontSize = 12.sp,
                        color = OrdersColors.TextSecondary
                    )
                }

                Surface(
                    modifier = Modifier
                        .shadow(
                            elevation = 4.dp,
                            shape = RoundedCornerShape(12.dp),
                            spotColor = statusColor.copy(alpha = 0.3f)
                        ),
                    shape = RoundedCornerShape(12.dp),
                    color = statusColor.copy(alpha = 0.15f)
                ) {
                    Box(
                        modifier = Modifier
                            .border(
                                width = 1.dp,
                                color = statusColor.copy(alpha = 0.3f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            order.status,
                            color = statusColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
                    .height(1.dp)
                    .background(OrdersColors.Divider)
            )

            // PRODUCTOS
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                order.products.forEach { product ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = product.title,
                            fontSize = 14.sp,
                            color = OrdersColors.TextPrimary,
                            modifier = Modifier.weight(1f),
                            maxLines = 1
                        )
                        Text(
                            text = "$${product.actualPrice}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = OrdersColors.CyanAccent
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
                    .height(1.dp)
                    .background(OrdersColors.Divider)
            )

            // TOTAL Y DIRECCIÓN
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Total Pagado:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = OrdersColors.TextSecondary
                )
                Text(
                    "$${order.totalPrice}",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp,
                    color = OrdersColors.CyanAccent
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = OrdersColors.LightSurface
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "📍",
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        order.address,
                        fontSize = 12.sp,
                        color = OrdersColors.TextSecondary,
                        maxLines = 1
                    )
                }
            }
        }
    }
}