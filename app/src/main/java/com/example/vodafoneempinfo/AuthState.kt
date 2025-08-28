package com.example.vodafoneempinfo

data class AuthState(
    val isAuthenticated: Boolean = false,
    val accessToken: String? = null,
    val error: String? = null,
    val isLoading: Boolean = false
)