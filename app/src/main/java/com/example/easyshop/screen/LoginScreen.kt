package com.example.easyshop.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
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

    Scaffold { innerPadding ->
        Column(
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(innerPadding)
                .padding(24.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()) // Permite scroll si el teclado tapa
        ) {

            // --- 1. TÍTULO 3D "WELCOME BACK" ---
            Box(contentAlignment = Alignment.Center) {
                // Capa de Sombra (Desplazada)
                Text(
                    text = "Welcome Back!",
                    style = TextStyle(
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF3344CC).copy(alpha = 0.3f) // Azul oscuro transparente
                    ),
                    modifier = Modifier.offset(x = 4.dp, y = 4.dp) // Desplazamiento para efecto 3D
                )
                // Capa Principal (Frente)
                Text(
                    text = "Welcome Back!",
                    style = TextStyle(
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF3344CC), // Azul EasyShop
                        // Sombra extra para suavizar
                        shadow = Shadow(
                            color = Color.Black.copy(alpha = 0.1f),
                            offset = Offset(2f, 2f),
                            blurRadius = 2f
                        )
                    )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // --- 2. LA ANIMACIÓN DEL OSO ---
            Bear(
                viewModel = bearViewModel,
                modifier = Modifier
                    .height(300.dp)
                    .fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            // --- 3. INPUTS ---
            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    bearViewModel.hlook = it.length.toFloat()
                },
                label = { Text("Email Address") },
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { bearViewModel.checking = it.isFocused },
                singleLine = true,
                shape = MaterialTheme.shapes.medium
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { focusState ->
                        bearViewModel.handUp = focusState.isFocused
                        bearViewModel.checking = isPasswordVisible
                    },
                shape = MaterialTheme.shapes.medium,
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
                            contentDescription = null
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(32.dp))

            // --- 4. BOTÓN LOGIN ---
            Button(
                onClick = {
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
                enabled = email.isNotBlank() && password.isNotBlank() && !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3344CC))
            ) {
                AnimatedVisibility(visible = isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(20.dp).padding(end = 8.dp),
                        strokeWidth = 2.dp
                    )
                }
                Text(text = if (isLoading) "Verificando..." else "Login", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- 5. PIE DE PÁGINA: CREAR CUENTA ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "¿No tienes cuenta? ",
                    color = Color.Gray,
                    fontSize = 14.sp
                )
                Text(
                    text = "Regístrate aquí",
                    color = Color(0xFF3344CC), // Azul EasyShop
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    modifier = Modifier.clickable {
                        // Navegación a la pantalla de registro
                        navController.navigate("signup")
                    }
                )
            }
        }
    }
}