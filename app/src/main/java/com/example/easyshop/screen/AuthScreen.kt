package com.example.easyshop.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.easyshop.R

@Composable
fun AuthScreen(modifier: Modifier = Modifier,navController: NavController) {

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Image(
            painter = painterResource(id = R.drawable.banner),
            contentDescription = "Banner",
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp)
        )
        Spacer(modifier = Modifier.height(20.dp))
        Text(text = "Empieza tu experiencia de compra ahora",
            style = TextStyle(
                fontSize = 30.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.SemiBold,
            ),textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        )
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = "La mejor plataforma e-commerce con los mejores precios",
            style = TextStyle(
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            ),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(20.dp))

        Button(onClick = {
            navController.navigate("login")
        },
            modifier = Modifier.fillMaxWidth()
                .height(60.dp)
            ) {
            Text(text = "Iniciar Sesión")
        }
        Spacer(modifier = Modifier.height(20.dp))
        OutlinedButton(onClick = {
            navController.navigate("signup")
        },
            modifier = Modifier.fillMaxWidth()
                .height(60.dp)
        ) {
            Text(text = "Registrarse")
        }



    }
}