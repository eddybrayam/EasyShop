package com.example.easyshop.components

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color // Importante para usar colores fijos
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore

@Composable
fun HeaderView(modifier: Modifier = Modifier) {
    var name by remember { mutableStateOf("") }

    // Tu lógica original para obtener el nombre
    LaunchedEffect(Unit) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid != null) {
            Firebase.firestore.collection("users")
                .whereEqualTo("uid", uid)
                .get()
                .addOnSuccessListener { documents ->
                    if (!documents.isEmpty) {
                        name = documents.first().get("name").toString().split(" ").get(0)
                    }
                }
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.Black) // <--- ¡AQUÍ ESTABA EL FALLO! Forzamos fondo negro
            .padding(horizontal = 24.dp, vertical = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            verticalArrangement = Arrangement.Center
        ) {
            // Subtítulo en Gris Claro (para que se vea en fondo negro)
            Text(
                text = "Welcome back",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray // Color fijo para Dark Mode
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Animación suave del nombre
            Crossfade(targetState = name, label = "NameFade") { currentName ->
                Text(
                    text = if (currentName.isEmpty()) "..." else currentName,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp // Un poco más grande para impacto
                    ),
                    color = Color.White // Texto Blanco Brillante
                )
            }
        }

        // Botón de Búsqueda (Círculo blanco con lupa negra)
        // Usamos Box o IconButton con background para el contraste
        IconButton(
            onClick = { /* Acción buscar */ },
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Color.White) // El botón blanco resalta sobre el fondo negro
        ) {
            Icon(
                imageVector = Icons.Rounded.Search,
                contentDescription = "Search",
                tint = Color.Black // Icono negro
            )
        }
    }
}