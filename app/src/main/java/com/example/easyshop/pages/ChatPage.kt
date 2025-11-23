package com.example.easyshop.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.easyshop.ChatViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatPage(navController: NavController, viewModel: ChatViewModel = viewModel()) {
    var textInput by remember { mutableStateOf("") }

    // Colores estilo iOS Dark
    val iosBgGradient = Brush.verticalGradient(
        colors = listOf(Color(0xFF1A0033), Color.Black) // Morado muy oscuro a negro
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color(0xFF3A3A3C), CircleShape) // Gris estilo iOS
                                .padding(8.dp)
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Asistente EasyShop", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Text("En línea", color = Color(0xFF32D74B), fontSize = 12.sp) // Verde iOS
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null, tint = Color(0xFF0A84FF)) // Azul iOS
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black)
            )
        }
    ) { padding ->
        // FONDO CON DEGRADADO SUTIL
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(iosBgGradient)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // --- LISTA DE MENSAJES ---
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    reverseLayout = false,
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    item {
                        BotBubbleIOS(text = "¡Hola! 👋 Soy tu vendedor experto. ¿Qué buscas hoy?")
                    }
                    items(viewModel.messages) { message ->
                        if (message.isUser) {
                            UserBubbleIOS(text = message.text)
                        } else {
                            BotBubbleIOS(text = message.text)
                        }
                    }
                }

                // --- BARRA DE ENTRADA ESTILO CRISTAL (iMessage) ---
                iOSInputBar(
                    textValue = textInput,
                    onTextChanged = { textInput = it },
                    onSendClick = {
                        if (textInput.isNotBlank()) {
                            viewModel.sendMessage(textInput)
                            textInput = ""
                        }
                    }
                )
            }
        }
    }
}

// --- COMPONENTE DE BARRA DE ENTRADA TIPO iOS ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun iOSInputBar(
    textValue: String,
    onTextChanged: (String) -> Unit,
    onSendClick: () -> Unit
) {
    // Contenedor principal que simula el cristal translúcido
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1E1E1E).copy(alpha = 0.7f)) // Fondo translúcido
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Botón "+" circular a la izquierda
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(Color(0xFF3A3A3C)), // Gris oscuro iOS
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add", tint = Color.Gray, modifier = Modifier.size(20.dp))
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Campo de texto redondeado tipo píldora
        TextField(
            value = textValue,
            onValueChange = onTextChanged,
            placeholder = { Text("iMessage", color = Color.Gray) },
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(30.dp)) // Píldora muy redonda
                .background(Color.Black.copy(alpha = 0.3f)) // Fondo interior más oscuro y translúcido
                .border(1.dp, Color.White.copy(0.1f), RoundedCornerShape(30.dp)), // Borde sutil
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                cursorColor = Color(0xFF0A84FF), // Cursor azul iOS
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            maxLines = 4,
            trailingIcon = {
                // Icono de Micrófono (o Enviar si hay texto) dentro del campo
                IconButton(onClick = { if (textValue.isNotBlank()) onSendClick() }) {
                    Icon(
                        // Si hay texto muestra enviar, si no, micrófono (como iMessage)
                        imageVector = if(textValue.isNotBlank()) Icons.Default.AutoAwesome else Icons.Default.Mic,
                        contentDescription = "Send/Mic",
                        tint = if(textValue.isNotBlank()) Color(0xFF0A84FF) else Color.Gray
                    )
                }
            }
        )
    }
}

// --- BURBUJA USUARIO (Morado/Rosa iOS) ---
@Composable
fun UserBubbleIOS(text: String) {
    val purpleGradient = Brush.horizontalGradient(
        colors = listOf(Color(0xFF8E2DE2), Color(0xFF4A00E0)) // Degradado morado estilo iMessage
    )
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalAlignment = Alignment.End) {
        Box(
            modifier = Modifier
                .padding(start = 60.dp)
                .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 20.dp, bottomEnd = 4.dp))
                .background(purpleGradient)
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Text(text = text, color = Color.White, fontSize = 16.sp)
        }
        // Pequeña "colita" de la burbuja
        Text("Entregado", color = Color.Gray, fontSize = 10.sp, modifier = Modifier.padding(end = 4.dp, top = 2.dp))
    }
}

// --- BURBUJA BOT (Gris translúcido iOS) ---
@Composable
fun BotBubbleIOS(text: String) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalAlignment = Alignment.Start) {
        Row(verticalAlignment = Alignment.Bottom) {
            // Avatar pequeño
            Box(modifier = Modifier.size(28.dp).clip(CircleShape).background(Color(0xFF3A3A3C)).padding(4.dp)) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .padding(end = 60.dp)
                    .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 4.dp, bottomEnd = 20.dp))
                    .background(Color(0xFF2C2C2C).copy(alpha = 0.9f)) // Gris oscuro casi opaco
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Text(text = text, color = Color.White, fontSize = 16.sp, lineHeight = 22.sp)
            }
        }
    }
}