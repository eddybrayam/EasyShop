package com.example.easyshop.ui.theme

import android.content.res.Resources
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.input.OutputTransformation
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextField(
    state: TextFieldState,
    label: String,
    onFocusChange: (Boolean) -> Unit,
    onCursorPositionChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    outputTransformation: OutputTransformation? = null,
    trailingIcon: @Composable () -> Unit = {}
) {
    val scrollState = rememberScrollState()

    val interactionSource = remember { MutableInteractionSource() }
    var layoutCoords by remember { mutableStateOf<LayoutCoordinates?>(null) }
    var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }
    var cursorRect by remember { mutableStateOf<Rect?>(null) }

    LaunchedEffect(state.selection, textLayoutResult) {
        val result = textLayoutResult ?: return@LaunchedEffect
        cursorRect = runCatching { result.getCursorRect(state.selection.start) }.getOrNull()
    }

    LaunchedEffect(
        scrollState,
        cursorRect,
        layoutCoords
    ) {
        val rect = cursorRect ?: return@LaunchedEffect
        val coords = layoutCoords ?: return@LaunchedEffect
        val topLeft = coords.localToWindow(rect.topLeft)
        val screenWidthPx = Resources.getSystem().displayMetrics.widthPixels.toFloat()

        val cursorX = topLeft.x - scrollState.value
        val percentage = (cursorX / screenWidthPx) * 100f
        onCursorPositionChange(percentage)
    }

    BasicTextField(
        state = state,
        scrollState = scrollState,
        interactionSource = interactionSource,
        onTextLayout = { textLayoutResult = it() },
        lineLimits = TextFieldLineLimits.SingleLine,
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
        textStyle = LocalTextStyle.current.copy(MaterialTheme.colorScheme.onBackground),
        outputTransformation = outputTransformation,
        enabled = enabled,
        decorator = { innerTextField ->
            OutlinedTextFieldDefaults.DecorationBox(
                value = state.text.toString(),
                innerTextField = innerTextField,
                enabled = enabled,
                singleLine = true,
                visualTransformation = VisualTransformation.None,
                interactionSource = interactionSource,
                label = { Text(label) },
                contentPadding = PaddingValues(16.dp),
                trailingIcon = trailingIcon,
                container = {
                    OutlinedTextFieldDefaults.Container(
                        enabled = enabled,
                        isError = false,
                        interactionSource = interactionSource,
                    )
                }
            )
        },
        modifier = modifier
            .fillMaxWidth()
            .onFocusChanged {
                onFocusChange(it.isFocused)
            }
            .onGloballyPositioned { layoutCoords = it }
    )
}