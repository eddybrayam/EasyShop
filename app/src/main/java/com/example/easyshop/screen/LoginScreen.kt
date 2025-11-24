package com.example.easyshop.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.easyshop.ui.theme.Bear
import com.example.easyshop.viewmodel.BearViewModel
import com.example.easyshop.AppUtil
import com.example.easyshop.viewmodel.AuthViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel(),
    bearViewModel: BearViewModel = viewModel()
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()

    // Estados
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = Color.Black // FONDO BASE NEGRO
    ) { innerPadding ->

        // DEGRADADO DE FONDO SUTIL
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF1A1A1A), Color.Black)
                    )
                )
                .padding(innerPadding)
        ) {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState())
            ) {

                // --- 1. TÍTULO NEÓN 3D ---
                Box(contentAlignment = Alignment.Center) {
                    // Sombra difusa (Resplandor)
                    Text(
                        text = "Welcome Back!",
                        style = TextStyle(
                            fontSize = 36.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF3344CC).copy(alpha = 0.5f),
                            shadow = Shadow(
                                color = Color(0xFF3344CC),
                                offset = Offset(0f, 0f),
                                blurRadius = 20f // Efecto Neón
                            )
                        ),
                        modifier = Modifier.offset(y = 2.dp)
                    )
                    // Texto Principal Blanco Brillante
                    Text(
                        text = "Welcome Back!",
                        style = TextStyle(
                            fontSize = 36.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // --- 2. OSO (FONDO CIRCULAR BLANCO) ---
                // Ponemos un círculo blanco detrás del oso para que resalte en el fondo negro
                Box(
                    modifier = Modifier
                        .size(280.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.05f)) // Círculo sutil detrás
                        .padding(10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Bear(
                        viewModel = bearViewModel,
                        modifier = Modifier
                            .height(250.dp)
                            .fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(30.dp))

                // --- 3. INPUT EMAIL (ESTILO DARK) ---
                OutlinedTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        bearViewModel.hlook = it.length.toFloat()
                    },
                    label = { Text("Email", color = Color.Gray) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { bearViewModel.checking = it.isFocused },
                    singleLine = true,
                    shape = RoundedCornerShape(50.dp), // Píldora redonda
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF1E1E1E),
                        unfocusedContainerColor = Color(0xFF121212),
                        focusedBorderColor = Color(0xFF3344CC), // Borde azul al enfocar
                        unfocusedBorderColor = Color.DarkGray,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.LightGray,
                        cursorColor = Color(0xFF3344CC)
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // --- 4. INPUT PASSWORD (ESTILO DARK) ---
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Contraseña", color = Color.Gray) },
                    visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { focusState ->
                            bearViewModel.handUp = focusState.isFocused
                            bearViewModel.checking = isPasswordVisible
                        },
                    shape = RoundedCornerShape(50.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF1E1E1E),
                        unfocusedContainerColor = Color(0xFF121212),
                        focusedBorderColor = Color(0xFF3344CC),
                        unfocusedBorderColor = Color.DarkGray,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.LightGray,
                        cursorColor = Color(0xFF3344CC)
                    ),
                    trailingIcon = {
                        IconButton(onClick = {
                            isPasswordVisible = !isPasswordVisible
                            bearViewModel.checking = isPasswordVisible
                            if (!isPasswordVisible && bearViewModel.handUp) {
                                bearViewModel.handUp = true
                            }
                        }) {
                            Icon(
                                if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = null,
                                tint = Color.Gray
                            )
                        }
                    }
                )

                Spacer(modifier = Modifier.height(40.dp))

                // --- 5. BOTÓN 3D CON RELIEVE ---
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(55.dp)
                        .shadow(10.dp, RoundedCornerShape(50.dp), spotColor = Color(0xFF3344CC))
                        .clip(RoundedCornerShape(50.dp))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(Color(0xFF4A00E0), Color(0xFF8E2DE2)) // Degradado Azul/Morado Neon
                            )
                        )
                        .border(1.dp, Color.White.copy(0.2f), RoundedCornerShape(50.dp))
                        .clickable(enabled = !isLoading) {
                            focusManager.clearFocus()
                            isLoading = true
                            authViewModel.login(email, password) { success, errorMessage ->
                                isLoading = false
                                if (success) {
                                    bearViewModel.success()
                                    scope.launch {
                                        delay(1500)
                                        navController.navigate("home") {
                                            popUpTo("auth") { inclusive = true }
                                        }
                                    }
                                } else {
                                    bearViewModel.fail()
                                    AppUtil.showToast(context, errorMessage ?: "Error al iniciar sesión")
                                }
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                    } else {
                        Text(
                            "INICIAR SESIÓN",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // --- 6. PIE DE PÁGINA ---
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("¿No tienes cuenta?", color = Color.Gray, fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Regístrate",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        modifier = Modifier.clickable { navController.navigate("signup") }
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}