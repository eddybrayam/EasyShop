package com.example.easyshop.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.*
import com.example.easyshop.AppUtil
import com.example.easyshop.R
import com.example.easyshop.model.ProductModel
import com.example.easyshop.model.UserModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.delay
import kotlin.math.absoluteValue
import androidx.compose.material.icons.filled.Lock
import androidx.compose.ui.draw.shadow
import com.example.easyshop.model.OrderModel

// Control del flujo de pantallas
enum class CheckoutStep {
    SUMMARY,      // 1. Ver carrito y total
    PAYMENT_DATA, // 2. Meter datos de tarjeta
    PROCESSING,   // 3. Animación caja
    SUCCESS       // 4. Listo
}

@Composable
fun CheckoutPage(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var currentStep by remember { mutableStateOf(CheckoutStep.SUMMARY) }

    // --- ESTADOS DE DATOS (FIREBASE) ---
    val userModel = remember { mutableStateOf(UserModel()) }
    val productList = remember { mutableStateListOf<ProductModel>() }
    val subTotal = remember { mutableStateOf(0f) }
    val discount = remember { mutableStateOf(0f) }
    val tax = remember { mutableStateOf(0f) }
    val total = remember { mutableStateOf(0f) }

    // --- ESTADOS DEL FORMULARIO DE TARJETA ---
    var cardNumber by remember { mutableStateOf("") }
    var cardHolder by remember { mutableStateOf("") }
    var cardExpiry by remember { mutableStateOf("") }
    var cardCvv by remember { mutableStateOf("") }

    // --- LÓGICA DE CÁLCULO ---
    fun calculateAndAssign() {
        subTotal.value = 0f
        productList.forEach {
            if (it.actualPrice.isNotEmpty()) {
                val qty = userModel.value.cartItems[it.id] ?: 0
                subTotal.value += it.actualPrice.toFloat() * qty
            }
        }
        discount.value = subTotal.value * (AppUtil.getDiscountPercentage() / 100)
        tax.value = subTotal.value * (AppUtil.getTaxPercentage() / 100)
        val calc = subTotal.value - discount.value + tax.value
        total.value = if (calc > 0) "%.2f".format(calc).toFloat() else 0f
    }

    // --- CARGA DE DATOS ---
    LaunchedEffect(key1 = Unit) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid != null) {
            Firebase.firestore.collection("users").document(uid).get()
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        val result = it.result.toObject(UserModel::class.java)
                        if (result != null) {
                            userModel.value = result
                            if (userModel.value.cartItems.isNotEmpty()) {
                                Firebase.firestore.collection("data").document("stock")
                                    .collection("products")
                                    .whereIn("id", userModel.value.cartItems.keys.toList())
                                    .get().addOnCompleteListener { task ->
                                        if (task.isSuccessful) {
                                            val res = task.result.toObjects(ProductModel::class.java)
                                            productList.clear()
                                            productList.addAll(res)
                                            calculateAndAssign()
                                        }
                                    }
                            }
                        }
                    }
                }
        }
    }

    // Función para guardar en Firebase y limpiar carrito
    fun saveOrderToFirebase() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        // 1. Crear el objeto Pedido
        val newOrder = OrderModel(
            orderId = System.currentTimeMillis().toString(), // ID simple basado en tiempo
            userId = uid,
            products = productList.toList(), // Hacemos una copia de la lista actual
            totalPrice = total.value,
            address = userModel.value.address,
            status = "En Camino",
            date = System.currentTimeMillis()
        )

        // 2. Guardar en la sub-colección "orders" del usuario
        Firebase.firestore.collection("users").document(uid)
            .collection("orders")
            .add(newOrder)
            .addOnSuccessListener {
                // 3. Si se guardó bien, LIMPIAMOS EL CARRITO del usuario
                Firebase.firestore.collection("users").document(uid)
                    .update("cartItems", emptyMap<String, Int>())
                    .addOnSuccessListener {
                        // Todo listo, pasamos a la pantalla de éxito
                        currentStep = CheckoutStep.SUCCESS
                        AppUtil.showToast(context, "¡Pedido registrado con éxito!")
                    }
            }
            .addOnFailureListener {
                AppUtil.showToast(context, "Error al guardar el pedido")
            }
    }

    // --- UI PRINCIPAL QUE CAMBIA SEGÚN EL PASO ---
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5)) // Fondo gris claro para contraste
            .padding(16.dp)
    ) {
        when (currentStep) {
            // PASO 1: RESUMEN DEL CARRITO
            // PASO 1: RESUMEN DEL CARRITO
            CheckoutStep.SUMMARY -> {
                Text("Resumen de Orden", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Entregar a:", fontWeight = FontWeight.Bold)
                        Text(userModel.value.address, color = Color.Gray)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                        // Aquí llamamos a la función corregida pasando el valor FLOAT (.value)
                        RowItem("Sub Total", subTotal.value)
                        RowItem("Descuento (-)", discount.value)
                        RowItem("Impuestos (+)", tax.value)

                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Total", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            // CORRECCIÓN AQUÍ: Formato limpio del Total
                            Text(
                                text = "$ " + "%.2f".format(total.value),
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp,
                                color = Color(0xFF3344CC)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(55.dp)
                        // 1. Sombra externa de color
                        .shadow(
                            elevation = 8.dp,
                            shape = RoundedCornerShape(50.dp),
                            spotColor = Color(0xFFFF3366),
                            ambientColor = Color(0xFFFF3366)
                        )
                        .clip(RoundedCornerShape(50.dp))
                        // 2. Fondo con degradado vertical (Volumen)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFFFF758C), // Rosa claro arriba
                                    Color(0xFFFF0040)  // Rojo intenso abajo
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
                            // TU LÓGICA ORIGINAL AQUÍ
                            currentStep = CheckoutStep.PAYMENT_DATA
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Seleccionar Método de Pago",
                        fontSize = 16.sp, // Un poco más pequeño porque el texto es largo
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        style = androidx.compose.ui.text.TextStyle(
                            shadow = androidx.compose.ui.graphics.Shadow(
                                color = Color.Black.copy(alpha = 0.2f),
                                blurRadius = 4f
                            )
                        )
                    )
                }
            }

            // PASO 2: SELECCIÓN Y FORMULARIO DE TARJETA (AQUÍ ESTÁ LA MAGIA)
            CheckoutStep.PAYMENT_DATA -> {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text("Pago con Tarjeta", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(20.dp))

                    // 1. LA TARJETA VISUAL DINÁMICA
                    // Le pasamos las variables de estado para que se actualice sola
                    DynamicCreditCard(
                        number = cardNumber,
                        holderName = cardHolder,
                        expiry = cardExpiry
                    )

                    Spacer(modifier = Modifier.height(30.dp))

                    // 2. EL FORMULARIO
                    Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
                        Column(modifier = Modifier.padding(16.dp)) {

                            // Campo Número de Tarjeta
                            OutlinedTextField(
                                value = cardNumber,
                                onValueChange = {
                                    if (it.length <= 16 && it.all { char -> char.isDigit() }) {
                                        cardNumber = it
                                    }
                                },
                                label = { Text("Número de Tarjeta") },
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                visualTransformation = CreditCardVisualTransformation(),
                                singleLine = true,
                                // HEMOS CAMBIADO EL ICONO AQUÍ A 'LOCK' PARA QUE NO TE DE ERROR
                                trailingIcon = { Icon(androidx.compose.material.icons.Icons.Default.Lock, contentDescription = null) }
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Campo Nombre
                            OutlinedTextField(
                                value = cardHolder,
                                onValueChange = { cardHolder = it.uppercase() },
                                label = { Text("Titular de la tarjeta") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                // Campo Fecha Expiración
                                OutlinedTextField(
                                    value = cardExpiry,
                                    onValueChange = {
                                        if (it.length <= 4 && it.all { char -> char.isDigit() }) {
                                            cardExpiry = it
                                        }
                                    },
                                    label = { Text("MM/YY") },
                                    modifier = Modifier.weight(1f),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    visualTransformation = ExpiryDateVisualTransformation(), // MAGIA: Agrega la barra /
                                    singleLine = true
                                )

                                // Campo CVV
                                OutlinedTextField(
                                    value = cardCvv,
                                    onValueChange = { if (it.length <= 3 && it.all { char -> char.isDigit() }) cardCvv = it },
                                    label = { Text("CVV") },
                                    modifier = Modifier.weight(1f),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                    visualTransformation = PasswordVisualTransformation(), // Oculta los puntos
                                    singleLine = true
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(30.dp))

                    // --- BOTÓN CON EFECTO 3D / RELIEVE ---
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(55.dp) // Un poco más alto para que luzca el efecto
                            // 1. LA SOMBRA EXTERNA DE COLOR (Hace que flote)
                            .shadow(
                                elevation = 10.dp,
                                shape = RoundedCornerShape(50.dp),
                                spotColor = Color(0xFFFF3366), // Sombra rojiza/rosa
                                ambientColor = Color(0xFFFF3366)
                            )
                            .clip(RoundedCornerShape(50.dp))
                            .background(
                                // 2. EL CUERPO CON VOLUMEN (Degradado Vertical)
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xFFFF758C), // Arriba: Rosa claro (Luz)
                                        Color(0xFFFF0040)  // Abajo: Rojo intenso (Sombra)
                                    )
                                )
                            )
                            // 3. EL BRILLO SUPERIOR (El toque final de relieve)
                            .border(
                                width = 1.dp,
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color.White.copy(alpha = 0.5f), // Borde blanco arriba
                                        Color.Transparent               // Desaparece abajo
                                    )
                                ),
                                shape = RoundedCornerShape(50.dp)
                            )
                            .clickable {
                                // TU LÓGICA DE VALIDACIÓN
                                if (cardNumber.length == 16 && cardExpiry.length == 4 && cardCvv.length == 3) {
                                    currentStep = CheckoutStep.PROCESSING
                                } else {
                                    AppUtil.showToast(context, "Por favor completa los datos correctamente")
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Pagar $ " + "%.2f".format(total.value),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            // Sombra suave en el texto para leerse mejor
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

            // PASO 3: ANIMACIÓN
            CheckoutStep.PROCESSING -> {
                ProcessingView {
                    // Cuando termina la animación (4 segundos), guardamos en la BD
                    saveOrderToFirebase()
                }
            }

            // PASO 4: ÉXITO
            CheckoutStep.SUCCESS -> {
                SuccessView()
            }
        }
    }
}

// --- COMPONENTE: TARJETA VISUAL DINÁMICA ---
@Composable
fun DynamicCreditCard(number: String, holderName: String, expiry: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(Color(0xFF0F2027), Color(0xFF203A43), Color(0xFF2C5364))
                )
            )
            .padding(24.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                // Chip
                Box(modifier = Modifier.size(50.dp, 35.dp).clip(RoundedCornerShape(6.dp)).background(Color(0xFFD4AF37).copy(alpha = 0.8f)))
                Text("VISA", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
            }

            // Número formateado para visualización
            val formattedNumber = if (number.isEmpty()) "•••• •••• •••• ••••" else {
                number.chunked(4).joinToString(" ")
            }

            Text(
                text = formattedNumber,
                color = Color.White,
                fontSize = 22.sp,
                letterSpacing = 3.sp,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                fontWeight = FontWeight.Medium
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("TITULAR", color = Color.Gray, fontSize = 10.sp)
                    Text(
                        text = if (holderName.isEmpty()) "NOMBRE APELLIDO" else holderName,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column {
                    Text("EXPIRA", color = Color.Gray, fontSize = 10.sp)
                    // Formateo visual de fecha
                    val formattedDate = if (expiry.length >= 2) expiry.substring(0, 2) + "/" + expiry.substring(2) else expiry
                    Text(
                        text = if (formattedDate.isEmpty()) "MM/AA" else formattedDate,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// --- CLASES DE AYUDA PARA FORMATEAR TEXTO (NO TOCAR, SOLO COPIAR) ---
// Esto hace que aparezcan espacios cada 4 números sin romper el input
class CreditCardVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val trimmed = if (text.text.length >= 16) text.text.substring(0..15) else text.text
        var out = ""
        for (i in trimmed.indices) {
            out += trimmed[i]
            if (i % 4 == 3 && i != 15) out += " "
        }
        val creditCardOffsetTranslator = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                if (offset <= 3) return offset
                if (offset <= 7) return offset + 1
                if (offset <= 11) return offset + 2
                if (offset <= 16) return offset + 3
                return 19
            }
            override fun transformedToOriginal(offset: Int): Int {
                if (offset <= 4) return offset
                if (offset <= 9) return offset - 1
                if (offset <= 14) return offset - 2
                if (offset <= 19) return offset - 3
                return 16
            }
        }
        return TransformedText(AnnotatedString(out), creditCardOffsetTranslator)
    }
}

// Esto pone la barra / automáticamente
class ExpiryDateVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val trimmed = if (text.text.length >= 4) text.text.substring(0..3) else text.text
        var out = ""
        for (i in trimmed.indices) {
            out += trimmed[i]
            if (i == 1) out += "/"
        }
        val offsetTranslator = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                if (offset <= 1) return offset
                if (offset <= 4) return offset + 1
                return 5
            }
            override fun transformedToOriginal(offset: Int): Int {
                if (offset <= 2) return offset
                if (offset <= 5) return offset - 1
                return 4
            }
        }
        return TransformedText(AnnotatedString(out), offsetTranslator)
    }
}

// --- VISTAS AUXILIARES ---
@Composable
fun RowItem(title: String, value: Float) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = title, fontSize = 16.sp, color = Color.Gray)
        // CORRECCIÓN AQUÍ: Usamos formato para asegurar 2 decimales y el signo $
        Text(
            text = "$ " + "%.2f".format(value),
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun ProcessingView(onDone: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(4000)
        onDone()
    }
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.box_packing))
    val progress by animateLottieCompositionAsState(composition, iterations = LottieConstants.IterateForever)

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Procesando Pago...", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        LottieAnimation(composition = composition, progress = { progress }, modifier = Modifier.size(300.dp))
    }
}

@Composable
fun SuccessView() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(modifier = Modifier.size(100.dp).background(Color(0xFFE8F5E9), RoundedCornerShape(50)).padding(20.dp), contentAlignment = Alignment.Center) {
            Text("✓", fontSize = 50.sp, color = Color(0xFF4CAF50))
        }
        Spacer(modifier = Modifier.height(20.dp))
        Text("¡Pago Exitoso!", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text("Tu orden ha sido confirmada", color = Color.Gray)
    }
}