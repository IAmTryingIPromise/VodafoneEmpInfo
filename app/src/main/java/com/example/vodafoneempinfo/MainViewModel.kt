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
    private val authRepository: AuthRepository,
    private val sharePointRepository: SharePointRepository
) : ViewModel() {

    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _appState = MutableStateFlow(AppState())
    val appState: StateFlow<AppState> = _appState.asStateFlow()

    init {
        initializeMsal()
    }

    private fun initializeMsal() {
        viewModelScope.launch {
            try {
                _authState.value = _authState.value.copy(isLoading = true)
                authRepository.initializeMsal()

                // Try to get token silently
                tryAcquireTokenSilent()
            } catch (e: Exception) {
                _authState.value = _authState.value.copy(
                    isLoading = false,
                    error = "Failed to initialize authentication: ${e.message}"
                )
            }
        }
    }

    private suspend fun tryAcquireTokenSilent() {
        try {
            val token = authRepository.acquireTokenSilent()
            _authState.value = _authState.value.copy(
                isLoading = false,
                isAuthenticated = true,
                accessToken = token,
                error = null
            )
        } catch (e: Exception) {
            _authState.value = _authState.value.copy(
                isLoading = false,
                isAuthenticated = false,
                error = null // Silent failure is expected if no account exists
            )
        }
    }

    fun signIn() {
        viewModelScope.launch {
            try {
                _authState.value = _authState.value.copy(isLoading = true, error = null)
                val token = authRepository.acquireTokenInteractive()
                _authState.value = _authState.value.copy(
                    isLoading = false,
                    isAuthenticated = true,
                    accessToken = token,
                    error = null
                )
            } catch (e: Exception) {
                _authState.value = _authState.value.copy(
                    isLoading = false,
                    isAuthenticated = false,
                    error = "Sign in failed: ${e.message}"
                )
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            try {
                _authState.value = _authState.value.copy(isLoading = true)
                authRepository.signOut()
                _authState.value = AuthState() // Reset to initial state
                _appState.value = AppState() // Reset app state
            } catch (e: Exception) {
                _authState.value = _authState.value.copy(
                    isLoading = false,
                    error = "Sign out failed: ${e.message}"
                )
            }
        }
    }

    fun updateFileName(fileName: String) {
        _appState.value = _appState.value.copy(fileName = fileName)
    }

    fun getFileContent() {
        val currentAuthState = _authState.value
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
        _authState.value = _authState.value.copy(error = null)
    }
}