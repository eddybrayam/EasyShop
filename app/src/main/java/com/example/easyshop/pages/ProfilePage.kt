package com.example.easyshop.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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

// PALETA PREMIUM
private object ProfileColors {
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
}

@Composable
fun ProfilePage(modifier: Modifier = Modifier, navController: NavController? = null) {
    val context = LocalContext.current
    val userModel = remember { mutableStateOf(UserModel()) }
    val auth = FirebaseAuth.getInstance()
    val db = Firebase.firestore

    var showNameDialog by remember { mutableStateOf(false) }
    var showAddressDialog by remember { mutableStateOf(false) }
    var showPassDialog by remember { mutableStateOf(false) }
    var tempInput by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        val uid = auth.currentUser?.uid
        if (uid != null) {
            db.collection("users").document(uid).get().addOnSuccessListener {
                val user = it.toObject(UserModel::class.java)
                if (user != null) userModel.value = user
            }
        }
    }

    fun updateUserData(field: String, value: String) {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid).update(field, value)
            .addOnSuccessListener {
                if (field == "name") userModel.value = userModel.value.copy(name = value)
                if (field == "address") userModel.value = userModel.value.copy(address = value)
                AppUtil.showToast(context, "Datos actualizados correctamente")
            }
            .addOnFailureListener {
                AppUtil.showToast(context, "Error al actualizar")
            }
    }

    fun updatePassword(newPass: String) {
        auth.currentUser?.updatePassword(newPass)
            ?.addOnSuccessListener {
                AppUtil.showToast(context, "Contraseña cambiada exitosamente")
            }
            ?.addOnFailureListener {
                AppUtil.showToast(context, "Error: Cierra sesión y vuelve a entrar para cambiar la contraseña")
            }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(ProfileColors.DarkBg)
        ) {
            // HEADER PREMIUM
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                ProfileColors.MediumSurface,
                                ProfileColors.DarkSurface
                            )
                        )
                    )
                    .border(
                        width = 1.dp,
                        color = ProfileColors.Divider
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Avatar con glow
                    Surface(
                        modifier = Modifier
                            .size(90.dp)
                            .shadow(
                                elevation = 16.dp,
                                shape = CircleShape,
                                spotColor = ProfileColors.CyanAccent.copy(alpha = 0.4f)
                            ),
                        shape = CircleShape,
                        color = ProfileColors.LightSurface
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            ProfileColors.LightSurface,
                                            ProfileColors.MediumSurface
                                        )
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                modifier = Modifier.size(50.dp),
                                tint = ProfileColors.CyanAccent
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Nombre editable
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = userModel.value.name.ifEmpty { "Usuario" },
                            color = ProfileColors.TextPrimary,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = {
                            tempInput = userModel.value.name
                            showNameDialog = true
                        }) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "Editar Nombre",
                                tint = ProfileColors.CyanAccent,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Text(
                        text = auth.currentUser?.email ?: "correo@ejemplo.com",
                        color = ProfileColors.TextSecondary,
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // OPCIONES DE CUENTA
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .width(5.dp)
                            .height(22.dp)
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        ProfileColors.CyanAccent,
                                        ProfileColors.CyanAccent.copy(alpha = 0.5f)
                                    )
                                )
                            )
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        "Mi Cuenta",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = ProfileColors.TextPrimary
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = 8.dp,
                            shape = RoundedCornerShape(16.dp),
                            spotColor = ProfileColors.CyanAccent.copy(alpha = 0.1f)
                        ),
                    shape = RoundedCornerShape(16.dp),
                    color = ProfileColors.MediumSurface
                ) {
                    Column(
                        modifier = Modifier.border(
                            width = 1.dp,
                            color = ProfileColors.Divider,
                            shape = RoundedCornerShape(16.dp)
                        )
                    ) {
                        PremiumProfileOption(
                            Icons.Default.List,
                            "Mis Pedidos",
                            onClick = { navController?.navigate("my_orders") }
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(ProfileColors.Divider)
                                .padding(horizontal = 16.dp)
                        )

                        PremiumProfileOption(
                            icon = Icons.Default.LocationOn,
                            title = "Dirección de Envío",
                            subtitle = userModel.value.address.ifEmpty { "Toca para agregar dirección" },
                            onClick = {
                                tempInput = userModel.value.address
                                showAddressDialog = true
                            }
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(ProfileColors.Divider)
                                .padding(horizontal = 16.dp)
                        )

                        PremiumProfileOption(
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

                // BOTÓN CERRAR SESIÓN
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(55.dp)
                        .shadow(
                            elevation = 12.dp,
                            shape = RoundedCornerShape(20.dp),
                            spotColor = Color(0xFFFF006E).copy(alpha = 0.3f)
                        ),
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFFFF006E)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ) {
                                auth.signOut()
                                navController?.navigate("login") { popUpTo(0) }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.ExitToApp,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Cerrar Sesión",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }
        }

        if (showNameDialog) {
            PremiumEditDialog(
                title = "Editar Nombre",
                initialValue = tempInput,
                onDismiss = { showNameDialog = false },
                onSave = { newName ->
                    updateUserData("name", newName)
                    showNameDialog = false
                }
            )
        }

        if (showAddressDialog) {
            PremiumEditDialog(
                title = "Actualizar Dirección",
                initialValue = tempInput,
                onDismiss = { showAddressDialog = false },
                onSave = { newAddress ->
                    updateUserData("address", newAddress)
                    showAddressDialog = false
                }
            )
        }

        if (showPassDialog) {
            PremiumEditDialog(
                title = "Nueva Contraseña",
                initialValue = "",
                isPassword = true,
                onDismiss = { showPassDialog = false },
                onSave = { newPass ->
                    if (newPass.length >= 6) {
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

@Composable
fun PremiumProfileOption(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier
                .size(40.dp)
                .shadow(
                    elevation = 4.dp,
                    shape = RoundedCornerShape(10.dp),
                    spotColor = ProfileColors.CyanAccent.copy(alpha = 0.2f)
                ),
            shape = RoundedCornerShape(10.dp),
            color = ProfileColors.LightSurface
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = ProfileColors.CyanAccent,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = ProfileColors.TextPrimary
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = ProfileColors.TextSecondary
                )
            }
        }

        Icon(
            Icons.Default.KeyboardArrowRight,
            contentDescription = null,
            tint = ProfileColors.CyanAccent,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
fun PremiumEditDialog(
    title: String,
    initialValue: String,
    isPassword: Boolean = false,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var text by remember { mutableStateOf(initialValue) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                color = ProfileColors.TextPrimary
            )
        },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = if (isPassword) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
                label = {
                    Text(
                        if (isPassword) "Nueva Contraseña" else "Escribe aquí",
                        color = ProfileColors.TextSecondary
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ProfileColors.CyanAccent,
                    unfocusedBorderColor = ProfileColors.Divider,
                    focusedTextColor = ProfileColors.TextPrimary,
                    unfocusedTextColor = ProfileColors.TextPrimary,
                    cursorColor = ProfileColors.CyanAccent
                )
            )
        },
        confirmButton = {
            Button(
                onClick = { onSave(text) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = ProfileColors.CyanAccent
                )
            ) {
                Text("Guardar", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = ProfileColors.TextSecondary)
            }
        },
        containerColor = ProfileColors.MediumSurface
    )
}