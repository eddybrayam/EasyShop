package com.example.easyshop.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import app.rive.RiveFile
import app.rive.ViewModelInstance
import app.rive.ViewModelSource
import app.rive.core.CommandQueue
import app.rive.core.FileHandle
import app.rive.core.ViewModelInstanceHandle
import com.example.easyshop.BearUiState
import com.example.easyshop.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class BearViewModel(application: Application) : AndroidViewModel(application) {
    private val commandQueue = CommandQueue(viewModelScope)
    private var riveFileHandle: FileHandle = FileHandle(Long.MIN_VALUE)
    private var viewModelInstanceHandle: ViewModelInstanceHandle =
        ViewModelInstanceHandle(Long.MIN_VALUE)

    private val _uiState = MutableStateFlow<BearUiState>(BearUiState.Loading)
    val uiState = _uiState.asStateFlow()

    private val viewModelInstance: ViewModelInstance?
        get() = when (val uiState = _uiState.value) {
            is BearUiState.Loaded -> uiState.viewModelInstance
            BearUiState.Loading -> null
        }

    var checking: Boolean = false
        set(value) {
            viewModelInstance?.setBoolean("checking", value)
            field = value
        }
    var handUp: Boolean = false
        set(value) {
            viewModelInstance?.setBoolean("handup", value)
            field = value
        }
    var hlook: Float = 0f
        set(value) {
            viewModelInstance?.setNumber("hlook", value)
            field = value
        }


    init {
        viewModelScope.launch {
            val bytes = application.resources.openRawResource(R.raw.bear).use { it.readBytes() }
            riveFileHandle = commandQueue.loadFile(bytes)
            viewModelInstanceHandle = commandQueue.createViewModelInstance(
                riveFileHandle,
                ViewModelSource.Named(VIEWMODEL_NAME).defaultInstance()
            )
            val riveFile = RiveFile(riveFileHandle, commandQueue, viewModelScope)
            val viewModelInstance =
                ViewModelInstance(viewModelInstanceHandle, commandQueue, viewModelScope)
            _uiState.update {
                BearUiState.Loaded(
                    riveFile = riveFile,
                    viewModelInstance = viewModelInstance
                )
            }
        }
    }

    fun update() {
        commandQueue.pollMessages()
    }

    override fun onCleared() {
        if (_uiState.value is BearUiState.Loaded) {
            commandQueue.deleteFile(riveFileHandle)
            commandQueue.deleteViewModelInstance(viewModelInstanceHandle)
        }
        super.onCleared()
    }

    fun success() {
        viewModelInstance?.fireTrigger("success")
    }

    fun fail() {
        viewModelInstance?.fireTrigger("fail")
    }

    companion object {
        const val VIEWMODEL_NAME = "bearvm"
        const val STATE_MACHINE_NAME: String = "Login Machine"
    }
}