package com.example.vodafoneempinfo

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Singleton manager that bridges Activity context with ViewModels
 */
@Singleton
class AuthManager @Inject constructor() {

    private var activityAuthRepository: AuthRepository? = null

    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _currentUser = MutableStateFlow<String?>(null)
    val currentUser: StateFlow<String?> = _currentUser.asStateFlow()

    // Called from Activity to set the activity-scoped AuthRepository
    fun setAuthRepository(authRepository: AuthRepository) {
        this.activityAuthRepository = authRepository
    }

    // Called from Activity when it's destroyed
    fun clearAuthRepository() {
        this.activityAuthRepository = null
    }

    suspend fun initializeMsal(): Boolean {
        val authRepo = activityAuthRepository
            ?: throw Exception("AuthRepository not available - Activity may not be ready")

        return try {
            _authState.value = _authState.value.copy(isLoading = true)
            val result = authRepo.initializeMsal()

            // Try silent authentication
            try {
                val token = authRepo.acquireTokenSilent()
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
                    error = null // Silent failure is expected
                )
            }

            result
        } catch (e: Exception) {
            _authState.value = _authState.value.copy(
                isLoading = false,
                error = "Failed to initialize authentication: ${e.message}"
            )
            false
        }
    }

    suspend fun getCurrentUserDisplayName(): String? {
        val authRepo = activityAuthRepository ?: return null

        return withContext(Dispatchers.IO) {
            try {
                val accessToken = authRepo.getAccessToken() ?: return@withContext null

                val client = okhttp3.OkHttpClient()
                val request = okhttp3.Request.Builder()
                    .url("https://graph.microsoft.com/v1.0/me")
                    .addHeader("Authorization", "Bearer $accessToken")
                    .addHeader("Accept", "application/json")
                    .build()

                val response = client.newCall(request).execute()

                if (response.isSuccessful) {
                    val responseBody = response.body?.string() ?: ""
                    val jsonResponse = org.json.JSONObject(responseBody)
                    val displayName = jsonResponse.optString("displayName", "")
                    _currentUser.value = displayName
                    displayName
                } else {
                    null
                }
            } catch (e: Exception) {
                null
            }
        }
    }

    suspend fun signIn(): String? {
        val authRepo = activityAuthRepository
            ?: throw Exception("AuthRepository not available - Activity may not be ready")

        return try {
            _authState.value = _authState.value.copy(isLoading = true, error = null)
            val token = authRepo.acquireTokenInteractive()
            _authState.value = _authState.value.copy(
                isLoading = false,
                isAuthenticated = true,
                accessToken = token,
                error = null
            )

            // Get current user info after successful sign in
            getCurrentUserDisplayName()

            token
        } catch (e: Exception) {
            _authState.value = _authState.value.copy(
                isLoading = false,
                isAuthenticated = false,
                error = "Sign in failed: ${e.message}"
            )
            throw e
        }
    }

    suspend fun signOut() {
        val authRepo = activityAuthRepository
            ?: throw Exception("AuthRepository not available")

        try {
            _authState.value = _authState.value.copy(isLoading = true)
            authRepo.signOut()
            _authState.value = AuthState() // Reset to initial state
        } catch (e: Exception) {
            _authState.value = _authState.value.copy(
                isLoading = false,
                error = "Sign out failed: ${e.message}"
            )
            throw e
        }
    }

    suspend fun getAccessToken(): String? {
        val authRepo = activityAuthRepository
            ?: throw Exception("Authentication not available - please sign in through the main screen")

        return try {
            authRepo.acquireTokenSilent()
        } catch (e: Exception) {
            throw Exception("Authentication required. Please sign in through the main screen.")
        }
    }

    fun clearError() {
        _authState.value = _authState.value.copy(error = null)
    }
}