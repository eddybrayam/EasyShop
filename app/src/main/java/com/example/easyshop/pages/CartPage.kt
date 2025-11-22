package com.example.easyshop.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.request.Disposable
import com.example.easyshop.AppUtil
import com.example.easyshop.GlobalNavigation
import com.example.easyshop.components.CartItemView
import com.example.easyshop.model.UserModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore

@Composable
fun CartPage(modifier: Modifier = Modifier) {
    val userModel = remember {
        mutableStateOf(UserModel())
    }

    DisposableEffect(key1 = Unit) {
        var listener = Firebase.firestore.collection("users")
            .document(FirebaseAuth.getInstance().currentUser?.uid!!)
            .addSnapshotListener{it, _->
                if (it!=null) {
                    val result = it.toObject(UserModel::class.java)
                    if (result != null) {
                        userModel.value = result
                    }
                }
            }
        onDispose {
            listener.remove()
        }

    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Your cart", style = TextStyle(
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
        )
        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {
            items(userModel.value.cartItems.toList(),key = { it.first}) { (productId, qty) ->
                CartItemView(productId = productId, qty = qty)
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp) // Altura ajustada para el efecto
                // 1. Sombra brillante abajo
                .shadow(
                    elevation = 8.dp,
                    shape = RoundedCornerShape(50.dp),
                    spotColor = Color(0xFFFF3366),
                    ambientColor = Color(0xFFFF3366)
                )
                .clip(RoundedCornerShape(50.dp))
                // 2. Degradado Vertical para dar volumen
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFFF758C), // Rosa claro arriba (Luz)
                            Color(0xFFFF0040)  // Rojo intenso abajo (Sombra)
                        )
                    )
                )
                // 3. Borde de luz superior (El "brillo" del plástico)
                .border(
                    width = 1.dp,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.5f), // Blanco transparente
                            Color.Transparent               // Desaparece abajo
                        )
                    ),
                    shape = RoundedCornerShape(50.dp)
                )
                .clickable {
                    GlobalNavigation.navController.navigate("checkout")
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Checkout",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                // Sombra suave al texto para que se lea mejor
                style = androidx.compose.ui.text.TextStyle(
                    shadow = androidx.compose.ui.graphics.Shadow(
                        color = Color.Black.copy(alpha = 0.2f),
                        blurRadius = 4f
                    )
                )
            )
        }

    }

}