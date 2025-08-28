package com.example.vodafoneempinfo

// AppState.kt
data class AppState(
    val fileName: String = "",
    val fileContent: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

// Graph API Response Models
data class DriveItem(
    val id: String,
    val name: String,
    val downloadUrl: String? = null
)

data class DriveItemsResponse(
    val value: List<DriveItem>
)