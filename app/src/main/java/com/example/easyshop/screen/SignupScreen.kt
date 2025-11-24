package com.example.easyshop.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.easyshop.AppUtil
import com.example.easyshop.R
import com.example.easyshop.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

@Composable
fun SignupScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    authViewModel: AuthViewModel = viewModel()
) {
    // Estados
    var email by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    Scaffold(
        containerColor = Color.Black // FONDO BASE NEGRO
    ) { innerPadding ->

        // DEGRADADO DE FONDO
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF1A1A1A), Color.Black)
                    )
                )
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()), // Scroll vital para teclados
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {

                // --- 1. TÍTULO 3D "GET STARTED" ---
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "Crear Cuenta",
                        style = TextStyle(
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF3344CC).copy(alpha = 0.3f)
                        ),
                        modifier = Modifier.offset(x = 4.dp, y = 4.dp)
                    )
                    Text(
                        text = "Crear Cuenta",
                        style = TextStyle(
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            shadow = Shadow(
                                color = Color(0xFF3344CC),
                                offset = Offset(0f, 0f),
                                blurRadius = 15f
                            )
                        )
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Únete a EasyShop hoy", color = Color.Gray, fontSize = 16.sp)

                Spacer(modifier = Modifier.height(24.dp))

                // --- 2. IMAGEN BANNER REDONDEADA ---
                // Usamos Box para darle borde y recorte a tu imagen existente
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .border(1.dp, Color.White.copy(0.1f), RoundedCornerShape(24.dp))
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.login_banner),
                        contentDescription = "Signup Banner",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // --- 3. CAMPO: NOMBRE ---
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre Completo", color = Color.Gray) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
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
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF3344CC)) }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // --- 4. CAMPO: EMAIL ---
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Correo Electrónico", color = Color.Gray) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
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
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = Color(0xFF3344CC)) }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // --- 5. CAMPO: PASSWORD ---
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Contraseña", color = Color.Gray) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
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
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF3344CC)) },
                    trailingIcon = {
                        IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                            Icon(
                                if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = null,
                                tint = Color.Gray
                            )
                        }
                    }
                )

                Spacer(modifier = Modifier.height(32.dp))

                // --- 6. BOTÓN REGISTRO 3D ---
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(55.dp)
                        .shadow(10.dp, RoundedCornerShape(50.dp), spotColor = Color(0xFF3344CC))
                        .clip(RoundedCornerShape(50.dp))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(Color(0xFF4A00E0), Color(0xFF8E2DE2)) // Azul/Morado Neon
                            )
                        )
                        .border(1.dp, Color.White.copy(0.2f), RoundedCornerShape(50.dp))
                        .clickable(enabled = !isLoading) {
                            focusManager.clearFocus()
                            isLoading = true
                            // Tu lógica original de ViewModel
                            authViewModel.signup(email, name, password) { success, errorMessage ->
                                if (success) {
                                    isLoading = false
                                    navController.navigate("home") {
                                        popUpTo("auth") { inclusive = true }
                                    }
                                } else {
                                    isLoading = false
                                    AppUtil.showToast(context, errorMessage ?: "Error al crear cuenta")
                                }
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                    } else {
                        Text(
                            "REGISTRARSE",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // --- 7. IR AL LOGIN ---
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("¿Ya tienes cuenta?", color = Color.Gray, fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Inicia Sesión",
                        color = Color(0xFF3344CC),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        modifier = Modifier.clickable {
                            // Navegar atrás al login
                            navController.navigate("login")
                        }
                    )
                }
            }
        }
    }
}