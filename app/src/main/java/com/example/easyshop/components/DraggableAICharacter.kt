package com.example.easyshop.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import app.rive.runtime.kotlin.RiveAnimationView
import app.rive.runtime.kotlin.core.Fit
import app.rive.runtime.kotlin.core.Loop
import com.example.easyshop.R
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun DraggableAICharacter(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    // Posición X e Y
    val offsetX = remember { Animatable(0f) }
    val offsetY = remember { Animatable(0f) }

    var isDragging by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Box(
        modifier = modifier
            .offset { IntOffset(offsetX.value.roundToInt(), offsetY.value.roundToInt()) }
            // 1. DETECTOR DE ARRASTRE (Solo se encarga de mover)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { isDragging = true },
                    onDragEnd = {
                        isDragging = false
                        // Efecto rebote al soltar
                        scope.launch {
                            offsetX.animateTo(0f, spring(dampingRatio = 0.5f, stiffness = Spring.StiffnessLow))
                        }
                        scope.launch {
                            offsetY.animateTo(0f, spring(dampingRatio = 0.5f, stiffness = Spring.StiffnessLow))
                        }
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        scope.launch {
                            offsetX.snapTo(offsetX.value + dragAmount.x)
                            offsetY.snapTo(offsetY.value + dragAmount.y)
                        }
                    }
                )
            }
    ) {
        // Pasamos el evento onClick hacia abajo
        RobocatVisuals(isDragging = isDragging, onClick = onClick)
    }
}

@Composable
fun RobocatVisuals(isDragging: Boolean, onClick: () -> Unit) {
    val scale = if (isDragging) 1.2f else 1.0f
    val shadowElevation = if (isDragging) 16.dp else 6.dp

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(100.dp)
            .scale(scale)
            .shadow(shadowElevation, CircleShape, spotColor = Color(0xFF3344CC), ambientColor = Color(0xFF3344CC))
            .clip(CircleShape)
            .background(Color.White)
    ) {
        // VISTA RIVE
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                RiveAnimationView(context).apply {
                    setRiveResource(
                        resId = R.raw.robocat,
                        fit = Fit.COVER,
                        alignment = app.rive.runtime.kotlin.core.Alignment.CENTER
                    )
                    play(loop = Loop.LOOP)
                }
            }
        )

        // 2. CAPA DE CLIC (IMPORTANTE)
        // Esta caja transparente cubre al robot y captura el clic
        // antes de que Rive se lo robe.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null // Sin efecto de onda para que se vea limpio
                ) {
                    // Solo ejecutamos el click si NO se está arrastrando
                    if (!isDragging) {
                        onClick()
                    }
                }
        )
    }
}