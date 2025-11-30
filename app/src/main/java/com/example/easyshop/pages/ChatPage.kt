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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.easyshop.viewmodel.ChatViewModel

// PALETA PREMIUM
private object ChatColors {
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

    // Para burbujas de usuario
    val UserBubbleGradient = listOf(
        Color(0xFF00E8FF),
        Color(0xFF0080FF)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatPage(navController: NavController, viewModel: ChatViewModel = viewModel()) {
    var textInput by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            PremiumChatTopBar(
                onBackClick = { navController.popBackStack() }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            ChatColors.DarkBg,
                            ChatColors.Black
                        )
                    )
                )
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // LISTA DE MENSAJES
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    reverseLayout = false,
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    item {
                        PremiumBotBubble(text = "¡Hola! 👋 Soy tu vendedor experto. ¿Qué buscas hoy?")
                    }
                    items(viewModel.messages) { message ->
                        if (message.isUser) {
                            PremiumUserBubble(text = message.text)
                        } else {
                            PremiumBotBubble(text = message.text)
                        }
                    }
                }

                // BARRA DE ENTRADA
                PremiumInputBar(
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PremiumChatTopBar(
    onBackClick: () -> Unit
) {
    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier
                        .size(40.dp)
                        .shadow(
                            elevation = 8.dp,
                            shape = CircleShape,
                            spotColor = ChatColors.CyanAccent.copy(alpha = 0.3f)
                        ),
                    shape = CircleShape,
                    color = ChatColors.MediumSurface
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = ChatColors.CyanAccent,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        "Asistente EasyShop",
                        color = ChatColors.TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(ChatColors.CyanAccent)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "En línea",
                            color = ChatColors.CyanAccent,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = null,
                    tint = ChatColors.CyanAccent
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = ChatColors.DarkSurface
        ),
        modifier = Modifier.border(
            width = 1.dp,
            color = ChatColors.Divider
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PremiumInputBar(
    textValue: String,
    onTextChanged: (String) -> Unit,
    onSendClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                spotColor = ChatColors.CyanAccent.copy(alpha = 0.1f)
            ),
        color = ChatColors.DarkSurface.copy(alpha = 0.95f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Botón "+"
            Surface(
                modifier = Modifier
                    .size(36.dp)
                    .shadow(
                        elevation = 4.dp,
                        shape = CircleShape,
                        spotColor = ChatColors.CyanAccent.copy(alpha = 0.2f)
                    ),
                shape = CircleShape,
                color = ChatColors.MediumSurface
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Add",
                        tint = ChatColors.TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Campo de texto
            TextField(
                value = textValue,
                onValueChange = onTextChanged,
                placeholder = {
                    Text(
                        "Escribe tu mensaje...",
                        color = ChatColors.TextSecondary
                    )
                },
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(30.dp))
                    .border(
                        width = 1.dp,
                        color = ChatColors.Divider,
                        shape = RoundedCornerShape(30.dp)
                    ),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = ChatColors.MediumSurface,
                    unfocusedContainerColor = ChatColors.MediumSurface,
                    focusedTextColor = ChatColors.TextPrimary,
                    unfocusedTextColor = ChatColors.TextPrimary,
                    cursorColor = ChatColors.CyanAccent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                maxLines = 4,
                trailingIcon = {
                    IconButton(onClick = { if (textValue.isNotBlank()) onSendClick() }) {
                        Icon(
                            imageVector = if (textValue.isNotBlank()) Icons.Default.AutoAwesome else Icons.Default.Mic,
                            contentDescription = "Send/Mic",
                            tint = if (textValue.isNotBlank()) ChatColors.CyanAccent else ChatColors.TextSecondary
                        )
                    }
                }
            )
        }
    }
}

@Composable
private fun PremiumUserBubble(text: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.End
    ) {
        Surface(
            modifier = Modifier
                .padding(start = 60.dp)
                .shadow(
                    elevation = 8.dp,
                    shape = RoundedCornerShape(
                        topStart = 20.dp,
                        topEnd = 20.dp,
                        bottomStart = 20.dp,
                        bottomEnd = 4.dp
                    ),
                    spotColor = ChatColors.CyanAccent.copy(alpha = 0.3f)
                ),
            shape = RoundedCornerShape(
                topStart = 20.dp,
                topEnd = 20.dp,
                bottomStart = 20.dp,
                bottomEnd = 4.dp
            ),
            color = Color.Transparent
        ) {
            Box(
                modifier = Modifier
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = ChatColors.UserBubbleGradient
                        )
                    )
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Text(
                    text = text,
                    color = Color.White,
                    fontSize = 16.sp
                )
            }
        }
        Text(
            "Entregado",
            color = ChatColors.TextSecondary,
            fontSize = 10.sp,
            modifier = Modifier.padding(end = 4.dp, top = 2.dp)
        )
    }
}

@Composable
private fun PremiumBotBubble(text: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Row(verticalAlignment = Alignment.Bottom) {
            // Avatar
            Surface(
                modifier = Modifier
                    .size(28.dp)
                    .shadow(
                        elevation = 4.dp,
                        shape = CircleShape,
                        spotColor = ChatColors.CyanAccent.copy(alpha = 0.2f)
                    ),
                shape = CircleShape,
                color = ChatColors.MediumSurface
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = ChatColors.CyanAccent,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Surface(
                modifier = Modifier
                    .padding(end = 60.dp)
                    .shadow(
                        elevation = 6.dp,
                        shape = RoundedCornerShape(
                            topStart = 20.dp,
                            topEnd = 20.dp,
                            bottomStart = 4.dp,
                            bottomEnd = 20.dp
                        ),
                        spotColor = ChatColors.CyanAccent.copy(alpha = 0.1f)
                    ),
                shape = RoundedCornerShape(
                    topStart = 20.dp,
                    topEnd = 20.dp,
                    bottomStart = 4.dp,
                    bottomEnd = 20.dp
                ),
                color = ChatColors.MediumSurface
            ) {
                Box(
                    modifier = Modifier
                        .border(
                            width = 1.dp,
                            color = ChatColors.Divider,
                            shape = RoundedCornerShape(
                                topStart = 20.dp,
                                topEnd = 20.dp,
                                bottomStart = 4.dp,
                                bottomEnd = 20.dp
                            )
                        )
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = text,
                        color = ChatColors.TextPrimary,
                        fontSize = 16.sp,
                        lineHeight = 22.sp
                    )
                }
            }
        }
    }
}