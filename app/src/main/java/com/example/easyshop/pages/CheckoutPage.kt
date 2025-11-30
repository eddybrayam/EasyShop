package com.example.easyshop.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
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
import com.example.easyshop.model.OrderModel

// PALETA PREMIUM
private object CheckoutColors {
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

enum class CheckoutStep {
    SUMMARY,
    PAYMENT_DATA,
    PROCESSING,
    SUCCESS
}

@Composable
fun CheckoutPage(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var currentStep by remember { mutableStateOf(CheckoutStep.SUMMARY) }

    val userModel = remember { mutableStateOf(UserModel()) }
    val productList = remember { mutableStateListOf<ProductModel>() }
    val subTotal = remember { mutableStateOf(0f) }
    val discount = remember { mutableStateOf(0f) }
    val tax = remember { mutableStateOf(0f) }
    val total = remember { mutableStateOf(0f) }

    var cardNumber by remember { mutableStateOf("") }
    var cardHolder by remember { mutableStateOf("") }
    var cardExpiry by remember { mutableStateOf("") }
    var cardCvv by remember { mutableStateOf("") }

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

    fun saveOrderToFirebase() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        val newOrder = OrderModel(
            orderId = System.currentTimeMillis().toString(),
            userId = uid,
            products = productList.toList(),
            totalPrice = total.value,
            address = userModel.value.address,
            status = "En Camino",
            date = System.currentTimeMillis()
        )

        Firebase.firestore.collection("users").document(uid)
            .collection("orders")
            .add(newOrder)
            .addOnSuccessListener {
                Firebase.firestore.collection("users").document(uid)
                    .update("cartItems", emptyMap<String, Int>())
                    .addOnSuccessListener {
                        currentStep = CheckoutStep.SUCCESS
                        AppUtil.showToast(context, "¡Pedido registrado con éxito!")
                    }
            }
            .addOnFailureListener {
                AppUtil.showToast(context, "Error al guardar el pedido")
            }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CheckoutColors.DarkBg)
    ) {
        when (currentStep) {
            CheckoutStep.SUMMARY -> {
                Column(modifier = Modifier.fillMaxSize()) {
                    // HEADER
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        CheckoutColors.MediumSurface,
                                        CheckoutColors.DarkBg.copy(alpha = 0.5f)
                                    )
                                )
                            )
                            .border(width = 1.dp, color = CheckoutColors.Divider)
                            .padding(20.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .width(5.dp)
                                    .height(28.dp)
                                    .background(
                                        brush = Brush.verticalGradient(
                                            colors = listOf(
                                                CheckoutColors.CyanAccent,
                                                CheckoutColors.CyanAccent.copy(alpha = 0.5f)
                                            )
                                        )
                                    )
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                "Resumen de Orden",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = CheckoutColors.TextPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(
                                    elevation = 8.dp,
                                    shape = RoundedCornerShape(16.dp),
                                    spotColor = CheckoutColors.CyanAccent.copy(alpha = 0.1f)
                                ),
                            shape = RoundedCornerShape(16.dp),
                            color = CheckoutColors.MediumSurface
                        ) {
                            Column(
                                modifier = Modifier
                                    .border(
                                        width = 1.dp,
                                        color = CheckoutColors.Divider,
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    .padding(16.dp)
                            ) {
                                Text(
                                    "Entregar a:",
                                    fontWeight = FontWeight.Bold,
                                    color = CheckoutColors.TextPrimary
                                )
                                Text(
                                    userModel.value.address,
                                    color = CheckoutColors.TextSecondary
                                )

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 12.dp)
                                        .height(1.dp)
                                        .background(CheckoutColors.Divider)
                                )

                                PremiumRowItem("Sub Total", subTotal.value)
                                PremiumRowItem("Descuento (-)", discount.value)
                                PremiumRowItem("Impuestos (+)", tax.value)

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 12.dp)
                                        .height(1.dp)
                                        .background(CheckoutColors.Divider)
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "Total",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp,
                                        color = CheckoutColors.TextPrimary
                                    )
                                    Text(
                                        text = "$ " + "%.2f".format(total.value),
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 22.sp,
                                        color = CheckoutColors.CyanAccent
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(55.dp)
                                .shadow(
                                    elevation = 12.dp,
                                    shape = RoundedCornerShape(20.dp),
                                    spotColor = CheckoutColors.CyanAccent.copy(alpha = 0.4f)
                                ),
                            shape = RoundedCornerShape(20.dp),
                            color = CheckoutColors.CyanAccent
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clickable(
                                        indication = null,
                                        interactionSource = remember { MutableInteractionSource() }
                                    ) {
                                        currentStep = CheckoutStep.PAYMENT_DATA
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Seleccionar Método de Pago",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }

            CheckoutStep.PAYMENT_DATA -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    // HEADER
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        CheckoutColors.MediumSurface,
                                        CheckoutColors.DarkBg.copy(alpha = 0.5f)
                                    )
                                )
                            )
                            .border(width = 1.dp, color = CheckoutColors.Divider)
                            .padding(20.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .width(5.dp)
                                    .height(28.dp)
                                    .background(
                                        brush = Brush.verticalGradient(
                                            colors = listOf(
                                                CheckoutColors.CyanAccent,
                                                CheckoutColors.CyanAccent.copy(alpha = 0.5f)
                                            )
                                        )
                                    )
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                "Pago con Tarjeta",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = CheckoutColors.TextPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        // TARJETA VISUAL
                        PremiumCreditCard(
                            number = cardNumber,
                            holderName = cardHolder,
                            expiry = cardExpiry
                        )

                        Spacer(modifier = Modifier.height(30.dp))

                        // FORMULARIO
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(
                                    elevation = 8.dp,
                                    shape = RoundedCornerShape(16.dp),
                                    spotColor = CheckoutColors.CyanAccent.copy(alpha = 0.1f)
                                ),
                            shape = RoundedCornerShape(16.dp),
                            color = CheckoutColors.MediumSurface
                        ) {
                            Column(
                                modifier = Modifier
                                    .border(
                                        width = 1.dp,
                                        color = CheckoutColors.Divider,
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    .padding(16.dp)
                            ) {
                                OutlinedTextField(
                                    value = cardNumber,
                                    onValueChange = {
                                        if (it.length <= 16 && it.all { char -> char.isDigit() }) {
                                            cardNumber = it
                                        }
                                    },
                                    label = { Text("Número de Tarjeta", color = CheckoutColors.TextSecondary) },
                                    modifier = Modifier.fillMaxWidth(),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    visualTransformation = CreditCardVisualTransformation(),
                                    singleLine = true,
                                    trailingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = CheckoutColors.CyanAccent) },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = CheckoutColors.CyanAccent,
                                        unfocusedBorderColor = CheckoutColors.Divider,
                                        focusedTextColor = CheckoutColors.TextPrimary,
                                        unfocusedTextColor = CheckoutColors.TextPrimary,
                                        cursorColor = CheckoutColors.CyanAccent
                                    )
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                OutlinedTextField(
                                    value = cardHolder,
                                    onValueChange = { cardHolder = it.uppercase() },
                                    label = { Text("Titular de la tarjeta", color = CheckoutColors.TextSecondary) },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = CheckoutColors.CyanAccent,
                                        unfocusedBorderColor = CheckoutColors.Divider,
                                        focusedTextColor = CheckoutColors.TextPrimary,
                                        unfocusedTextColor = CheckoutColors.TextPrimary,
                                        cursorColor = CheckoutColors.CyanAccent
                                    )
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    OutlinedTextField(
                                        value = cardExpiry,
                                        onValueChange = {
                                            if (it.length <= 4 && it.all { char -> char.isDigit() }) {
                                                cardExpiry = it
                                            }
                                        },
                                        label = { Text("MM/YY", color = CheckoutColors.TextSecondary) },
                                        modifier = Modifier.weight(1f),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        visualTransformation = ExpiryDateVisualTransformation(),
                                        singleLine = true,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = CheckoutColors.CyanAccent,
                                            unfocusedBorderColor = CheckoutColors.Divider,
                                            focusedTextColor = CheckoutColors.TextPrimary,
                                            unfocusedTextColor = CheckoutColors.TextPrimary,
                                            cursorColor = CheckoutColors.CyanAccent
                                        )
                                    )

                                    OutlinedTextField(
                                        value = cardCvv,
                                        onValueChange = { if (it.length <= 3 && it.all { char -> char.isDigit() }) cardCvv = it },
                                        label = { Text("CVV", color = CheckoutColors.TextSecondary) },
                                        modifier = Modifier.weight(1f),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                        visualTransformation = PasswordVisualTransformation(),
                                        singleLine = true,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = CheckoutColors.CyanAccent,
                                            unfocusedBorderColor = CheckoutColors.Divider,
                                            focusedTextColor = CheckoutColors.TextPrimary,
                                            unfocusedTextColor = CheckoutColors.TextPrimary,
                                            cursorColor = CheckoutColors.CyanAccent
                                        )
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(30.dp))

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(55.dp)
                                .shadow(
                                    elevation = 12.dp,
                                    shape = RoundedCornerShape(20.dp),
                                    spotColor = CheckoutColors.CyanAccent.copy(alpha = 0.4f)
                                ),
                            shape = RoundedCornerShape(20.dp),
                            color = CheckoutColors.CyanAccent
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clickable(
                                        indication = null,
                                        interactionSource = remember { MutableInteractionSource() }
                                    ) {
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
                                    color = Color.Black
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(30.dp))
                    }
                }
            }

            CheckoutStep.PROCESSING -> {
                PremiumProcessingView {
                    saveOrderToFirebase()
                }
            }

            CheckoutStep.SUCCESS -> {
                PremiumSuccessView()
            }
        }
    }
}

@Composable
fun PremiumCreditCard(number: String, holderName: String, expiry: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(20.dp),
                spotColor = CheckoutColors.CyanAccent.copy(alpha = 0.3f)
            )
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        CheckoutColors.MediumSurface,
                        CheckoutColors.DarkSurface
                    )
                )
            )
            .border(
                width = 1.dp,
                color = CheckoutColors.CyanAccent.copy(alpha = 0.3f),
                shape = RoundedCornerShape(20.dp)
            )
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .size(50.dp, 35.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(CheckoutColors.CyanAccent.copy(alpha = 0.3f))
                )
                Text(
                    "VISA",
                    color = CheckoutColors.CyanAccent,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            }

            val formattedNumber = if (number.isEmpty()) "•••• •••• •••• ••••" else {
                number.chunked(4).joinToString(" ")
            }

            Text(
                text = formattedNumber,
                color = CheckoutColors.TextPrimary,
                fontSize = 22.sp,
                letterSpacing = 3.sp,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                fontWeight = FontWeight.Medium
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("TITULAR", color = CheckoutColors.TextSecondary, fontSize = 10.sp)
                    Text(
                        text = if (holderName.isEmpty()) "NOMBRE APELLIDO" else holderName,
                        color = CheckoutColors.TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column {
                    Text("EXPIRA", color = CheckoutColors.TextSecondary, fontSize = 10.sp)
                    val formattedDate = if (expiry.length >= 2) expiry.substring(0, 2) + "/" + expiry.substring(2) else expiry
                    Text(
                        text = if (formattedDate.isEmpty()) "MM/AA" else formattedDate,
                        color = CheckoutColors.TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun PremiumRowItem(title: String, value: Float) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            fontSize = 16.sp,
            color = CheckoutColors.TextSecondary
        )
        Text(
            text = "$ " + "%.2f".format(value),
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = CheckoutColors.TextPrimary
        )
    }
}

@Composable
fun PremiumProcessingView(onDone: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(4000)
        onDone()
    }
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.box_packing))
    val progress by animateLottieCompositionAsState(composition, iterations = LottieConstants.IterateForever)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CheckoutColors.DarkBg),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Procesando Pago...",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = CheckoutColors.TextPrimary
        )
        LottieAnimation(
            composition = composition,
            progress = { progress },
            modifier = Modifier.size(300.dp)
        )
    }
}

@Composable
fun PremiumSuccessView() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CheckoutColors.DarkBg),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            modifier = Modifier
                .size(100.dp)
                .shadow(
                    elevation = 20.dp,
                    shape = RoundedCornerShape(50.dp),
                    spotColor = CheckoutColors.CyanAccent.copy(alpha = 0.5f)
                ),
            shape = RoundedCornerShape(50.dp),
            color = CheckoutColors.MediumSurface
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("✓", fontSize = 50.sp, color = CheckoutColors.CyanAccent)
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            "¡Pago Exitoso!",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = CheckoutColors.TextPrimary
        )
        Text(
            "Tu orden ha sido confirmada",
            color = CheckoutColors.TextSecondary
        )
    }
}

// CLASES DE AYUDA
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