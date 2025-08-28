package com.example.vodafoneempinfo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.text.endsWith
import kotlin.text.isBlank

@HiltViewModel
class MainViewModel @Inject constructor(
    private val authManager: AuthManager,
    private val sharePointRepository: SharePointRepository
) : ViewModel() {

    val authState: StateFlow<AuthState> = authManager.authState

    private val _appState = MutableStateFlow(AppState())
    val appState: StateFlow<AppState> = _appState.asStateFlow()

    init {
        initializeMsal()
    }

    private fun initializeMsal() {
        viewModelScope.launch {
            try {
                authManager.initializeMsal()
            } catch (e: Exception) {
                // AuthManager handles state updates internally
            }
        }
    }

    fun signIn() {
        viewModelScope.launch {
            try {
                authManager.signIn()
            } catch (e: Exception) {
                // AuthManager handles state updates internally
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            try {
                authManager.signOut()
                _appState.value = AppState() // Reset app state
            } catch (e: Exception) {
                // AuthManager handles state updates internally
            }
        }
    }

    fun updateFileName(fileName: String) {
        _appState.value = _appState.value.copy(fileName = fileName)
    }

    fun getFileContent() {
        val currentAuthState = authManager.authState.value
        val currentAppState = _appState.value

        if (!currentAuthState.isAuthenticated || currentAuthState.accessToken == null) {
            _appState.value = currentAppState.copy(
                error = "Please sign in first"
            )
            return
        }

        if (currentAppState.fileName.isBlank()) {
            _appState.value = currentAppState.copy(
                error = "Please enter a file name"
            )
            return
        }

        // Ensure file name ends with .txt
        val fileName = if (currentAppState.fileName.endsWith(".txt")) {
            currentAppState.fileName
        } else {
            "${currentAppState.fileName}.txt"
        }

        viewModelScope.launch {
            try {
                _appState.value = currentAppState.copy(
                    isLoading = true,
                    error = null,
                    fileContent = ""
                )

                val fileContent = sharePointRepository.getFileContent(
                    currentAuthState.accessToken,
                    fileName
                )

                _appState.value = _appState.value.copy(
                    isLoading = false,
                    fileContent = fileContent,
                    error = null
                )
            } catch (e: Exception) {
                _appState.value = _appState.value.copy(
                    isLoading = false,
                    error = "Failed to get file content: ${e.message}"
                )
            }
        }
    }

    fun clearError() {
        _appState.value = _appState.value.copy(error = null)
        authManager.clearError()
    }
}