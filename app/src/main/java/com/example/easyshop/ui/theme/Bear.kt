package com.example.easyshop.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import app.rive.ExperimentalRiveComposeAPI
import app.rive.RiveUI
import app.rive.runtime.kotlin.core.Fit
import com.example.easyshop.BearUiState
import com.example.easyshop.viewmodel.BearViewModel
import kotlinx.coroutines.isActive

@OptIn(ExperimentalRiveComposeAPI::class)
@Composable
fun Bear(
    viewModel: BearViewModel,
    modifier: Modifier = Modifier
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            while (isActive) {
                withFrameNanos {
                    viewModel.update()
                }
            }
        }
    }


    when (val uiState = uiState) {
        BearUiState.Loading -> Unit // Im too lazy to show loading
        is BearUiState.Loaded -> {
            RiveUI(
                file = uiState.riveFile,
                stateMachineName = BearViewModel.STATE_MACHINE_NAME,
                viewModelInstance = uiState.viewModelInstance,
                fit = Fit.CONTAIN,
                modifier = modifier
            )
        }
    }
}