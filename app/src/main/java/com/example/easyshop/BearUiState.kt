package com.example.easyshop

import app.rive.RiveFile
import app.rive.ViewModelInstance

sealed class BearUiState {
    data object Loading : BearUiState()
    data class Loaded(
        val riveFile: RiveFile,
        val viewModelInstance: ViewModelInstance
    ) : BearUiState()
}