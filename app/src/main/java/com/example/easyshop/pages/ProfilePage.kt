package com.example.easyshop.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.easyshop.AppUtil
import com.example.easyshop.model.UserModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import androidx.compose.ui.platform.LocalContext

@Composable
fun ProfilePage(modifier: Modifier = Modifier, navController: NavController? = null) {
    val context = LocalContext.current
    val userModel = remember { mutableStateOf(UserModel()) }
    val auth = FirebaseAuth.getInstance()
    val db = Firebase.firestore

    // --- ESTADOS PARA LOS DIÁLOGOS DE EDICIÓN ---
    var showNameDialog by remember { mutableStateOf(false) }
    var showAddressDialog by remember { mutableStateOf(false) }
    var showPassDialog by remember { mutableStateOf(false) }

    // Variables temporales para lo que el usuario escribe
    var tempInput by remember { mutableStateOf("") }

    // Cargar datos de Firebase
    LaunchedEffect(Unit) {
        val uid = auth.currentUser?.uid
        if (uid != null) {
            db.collection("users").document(uid).get().addOnSuccessListener {
                val user = it.toObject(UserModel::class.java)
                if (user != null) userModel.value = user
            }
        }
    }

    // --- FUNCIÓN PARA ACTUALIZAR FIRESTORE (NOMBRE O DIRECCIÓN) ---
    fun updateUserData(field: String, value: String) {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid).update(field, value)
            .addOnSuccessListener {
                // Actualizamos la vista localmente
                if (field == "name") userModel.value = userModel.value.copy(name = value)
                if (field == "address") userModel.value = userModel.value.copy(address = value)
                AppUtil.showToast(context, "Datos actualizados correctamente")
            }
            .addOnFailureListener {
                AppUtil.showToast(context, "Error al actualizar")
            }
    }

    // --- FUNCIÓN PARA CAMBIAR CONTRASEÑA ---
    fun updatePassword(newPass: String) {
        auth.currentUser?.updatePassword(newPass)
            ?.addOnSuccessListener {
                AppUtil.showToast(context, "Contraseña cambiada exitosamente")
            }
            ?.addOnFailureListener {
                // Firebase exige loguearse recientemente para cambiar pass
                AppUtil.showToast(context, "Error: Cierra sesión y vuelve a entrar para cambiar la contraseña")
            }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
        ) {
            // --- CABECERA AZUL ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .background(
                        color = Color(0xFF3344CC),
                        shape = RoundedCornerShape(bottomStart = 30.dp, bottomEnd = 30.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Foto
                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .clip(CircleShape)
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(50.dp), tint = Color.Gray)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // NOMBRE EDITABLE
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = userModel.value.name.ifEmpty { "Usuario" },
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                        // Botón de Lápiz para el nombre
                        IconButton(onClick = {
                            tempInput = userModel.value.name
                            showNameDialog = true
                        }) {
                            Icon(Icons.Default.Edit, contentDescription = "Editar Nombre", tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(20.dp))
                        }
                    }

                    Text(
                        text = auth.currentUser?.email ?: "correo@ejemplo.com",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // --- LISTA DE OPCIONES ---
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text("Mi Cuenta", fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.padding(bottom = 10.dp))

                Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(16.dp)) {
                    Column {
                        // MIS PEDIDOS
                        ProfileOptionItem(Icons.Default.List, "Mis Pedidos") { navController?.navigate("my_orders") }

                        HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray)

                        // DIRECCIÓN (EDITABLE)
                        ProfileOptionItem(
                            icon = Icons.Default.LocationOn,
                            title = "Dirección de Envío",
                            subtitle = userModel.value.address.ifEmpty { "Toca para agregar dirección" },
                            onClick = {
                                tempInput = userModel.value.address
                                showAddressDialog = true
                            }
                        )

                        HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray)

                        // CAMBIAR CONTRASEÑA
                        ProfileOptionItem(
                            icon = Icons.Default.Lock,
                            title = "Cambiar Contraseña",
                            subtitle = "Actualizar seguridad",
                            onClick = {
                                tempInput = ""
                                showPassDialog = true
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // --- BOTÓN CERRAR SESIÓN ESTILO 3D ---
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(55.dp)
                        // 1. Sombra rojiza
                        .shadow(
                            elevation = 8.dp,
                            shape = RoundedCornerShape(50.dp),
                            spotColor = Color(0xFFD32F2F), // Sombra roja oscura
                            ambientColor = Color(0xFFD32F2F)
                        )
                        .clip(RoundedCornerShape(50.dp))
                        // 2. Degradado Vertical (Rojos intensos para Logout)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFFEF5350), // Rojo claro arriba (Luz)
                                    Color(0xFFB71C1C)  // Rojo oscuro abajo (Sombra)
                                )
                            )
                        )
                        // 3. Borde de brillo superior
                        .border(
                            width = 1.dp,
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.5f),
                                    Color.Transparent
                                )
                            ),
                            shape = RoundedCornerShape(50.dp)
                        )
                        .clickable {
                            auth.signOut()
                            navController?.navigate("login") { popUpTo(0) }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    // Contenido centrado: Icono + Texto
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = null,
                            tint = Color.White, // Icono blanco para contraste
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Cerrar Sesión",
                            color = Color.White, // Texto blanco
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            // Sombra suave en el texto
                            style = androidx.compose.ui.text.TextStyle(
                                shadow = androidx.compose.ui.graphics.Shadow(
                                    color = Color.Black.copy(alpha = 0.3f),
                                    blurRadius = 4f
                                )
                            )
                        )
                    }
                }
            }
        }

        // --- DIÁLOGO: EDITAR NOMBRE ---
        if (showNameDialog) {
            EditDialog(
                title = "Editar Nombre",
                initialValue = tempInput,
                onDismiss = { showNameDialog = false },
                onSave = { newName ->
                    updateUserData("name", newName)
                    showNameDialog = false
                }
            )
        }

        // --- DIÁLOGO: EDITAR DIRECCIÓN ---
        if (showAddressDialog) {
            EditDialog(
                title = "Actualizar Dirección",
                initialValue = tempInput,
                onDismiss = { showAddressDialog = false },
                onSave = { newAddress ->
                    updateUserData("address", newAddress)
                    showAddressDialog = false
                }
            )
        }

        // --- DIÁLOGO: CAMBIAR CONTRASEÑA ---
        if (showPassDialog) {
            EditDialog(
                title = "Nueva Contraseña",
                initialValue = "",
                isPassword = true,
                onDismiss = { showPassDialog = false },
                onSave = { newPass ->
                    if(newPass.length >= 6) {
                        updatePassword(newPass)
                        showPassDialog = false
                    } else {
                        AppUtil.showToast(context, "La contraseña debe tener al menos 6 caracteres")
                    }
                }
            )
        }
    }
}

// --- COMPONENTES REUTILIZABLES ---

@Composable
fun ProfileOptionItem(icon: ImageVector, title: String, subtitle: String? = null, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(40.dp).clip(RoundedCornerShape(8.dp)).background(Color(0xFFEEF1FF)), contentAlignment = Alignment.Center) {
            Icon(imageVector = icon, contentDescription = null, tint = Color(0xFF3344CC))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            if (subtitle != null) Text(text = subtitle, fontSize = 12.sp, color = Color.Gray)
        }
        Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = Color.Gray)
    }
}

@Composable
fun EditDialog(
    title: String,
    initialValue: String,
    isPassword: Boolean = false,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var text by remember { mutableStateOf(initialValue) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = title, fontWeight = FontWeight.Bold) },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = if (isPassword) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
                label = { Text(if(isPassword) "Nueva Contraseña" else "Escribe aquí") }
            )
        },
        confirmButton = {
            Button(onClick = { onSave(text) }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3344CC))) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = Color.Gray)
            }
        },
        containerColor = Color.White
    )
}