package com.example.vodafoneempinfo

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import com.google.gson.Gson
import java.io.IOException
import java.net.URLEncoder
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.io.use
import kotlin.jvm.java

@Singleton
class SharePointRepository @Inject constructor() {

    private val httpClient = OkHttpClient()
    private val gson = Gson()

    // Replace these with your actual SharePoint site details
    private val siteId = "cbsmcom.sharepoint.com,5a9063f8-42da-4ccc-ab8d-e3a137426a18,ac4fadb3-a47f-42c4-9c47-e8418eb134f9" // or use site URL path
    private val driveId = "b!-GOQWtpCzEyrjeOhN0JqGLOtT6x_pMRCnEfoQY6xNPkaj56tt0ekTJW3UffQKc2t" // or use drive name

    suspend fun getFileContent(accessToken: String, fileName: String): String {
        return withContext(Dispatchers.IO) {
            // First, get the file by name
            val fileItem = getFileByName(accessToken, fileName)

            // Then download the file content
            downloadFileContent(accessToken, fileItem.id)
        }
    }

    private suspend fun getFileByName(accessToken: String, fileName: String): DriveItem {
        val url = "https://graph.microsoft.com/v1.0/sites/$siteId/drives/$driveId/root/children?\$filter=name eq '$fileName'"

        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $accessToken")
            .addHeader("Accept", "application/json")
            .build()

        return withContext(Dispatchers.IO) {
            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    throw IOException("Failed to get file: ${response.code} ${response.message}")
                }

                val responseBody =
                    response.body?.string() ?: throw IOException("Empty response body")
                val driveItemsResponse = gson.fromJson(responseBody, DriveItemsResponse::class.java)

                if (driveItemsResponse.value.isEmpty()) {
                    throw IOException("File '$fileName' not found")
                }

                driveItemsResponse.value.first()
            }
        }
    }

    private suspend fun downloadFileContent(accessToken: String, fileId: String): String {
        val url = "https://graph.microsoft.com/v1.0/sites/$siteId/drives/$driveId/items/$fileId/content"

        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $accessToken")
            .build()

        return withContext(Dispatchers.IO) {
            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    throw IOException("Failed to download file content: ${response.code} ${response.message}")
                }

                response.body?.string() ?: throw IOException("Empty file content")
            }
        }
    }

    // Alternative method using site URL instead of site ID
    suspend fun getFileContentBySiteUrl(accessToken: String, siteUrl: String, libraryName: String, fileName: String): String {
        return withContext(Dispatchers.IO) {
            // Get site by URL
            val siteInfo = getSiteByUrl(accessToken, siteUrl)

            // Get drive by name
            val driveInfo = getDriveByName(accessToken, siteInfo.id, libraryName)

            // Get file by name
            val fileItem = getFileByNameFromDrive(accessToken, siteInfo.id, driveInfo.id, fileName)

            // Download content
            downloadFileContentFromDrive(accessToken, siteInfo.id, driveInfo.id, fileItem.id)
        }
    }

    private suspend fun getSiteByUrl(accessToken: String, siteUrl: String): DriveItem {
        val encodedUrl = URLEncoder.encode(siteUrl, "UTF-8")
        val url = "https://graph.microsoft.com/v1.0/sites/$encodedUrl"

        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $accessToken")
            .addHeader("Accept", "application/json")
            .build()

        return withContext(Dispatchers.IO) {
            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    throw IOException("Failed to get site: ${response.code} ${response.message}")
                }

                val responseBody =
                    response.body?.string() ?: throw IOException("Empty response body")
                gson.fromJson(responseBody, DriveItem::class.java)
            }
        }
    }

    private suspend fun getDriveByName(accessToken: String, siteId: String, libraryName: String): DriveItem {
        val url = "https://graph.microsoft.com/v1.0/sites/$siteId/drives?\$filter=name eq '$libraryName'"

        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $accessToken")
            .addHeader("Accept", "application/json")
            .build()

        return withContext(Dispatchers.IO) {
            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    throw IOException("Failed to get drive: ${response.code} ${response.message}")
                }

                val responseBody =
                    response.body?.string() ?: throw IOException("Empty response body")
                val driveItemsResponse = gson.fromJson(responseBody, DriveItemsResponse::class.java)

                if (driveItemsResponse.value.isEmpty()) {
                    throw IOException("Document library '$libraryName' not found")
                }

                driveItemsResponse.value.first()
            }
        }
    }

    private suspend fun getFileByNameFromDrive(accessToken: String, siteId: String, driveId: String, fileName: String): DriveItem {
        val url = "https://graph.microsoft.com/v1.0/sites/$siteId/drives/$driveId/root/children?\$filter=name eq '$fileName'"

        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $accessToken")
            .addHeader("Accept", "application/json")
            .build()

        return withContext(Dispatchers.IO) {
            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    throw IOException("Failed to get file: ${response.code} ${response.message}")
                }

                val responseBody =
                    response.body?.string() ?: throw IOException("Empty response body")
                val driveItemsResponse = gson.fromJson(responseBody, DriveItemsResponse::class.java)

                if (driveItemsResponse.value.isEmpty()) {
                    throw IOException("File '$fileName' not found")
                }

                driveItemsResponse.value.first()
            }
        }
    }

    private suspend fun downloadFileContentFromDrive(accessToken: String, siteId: String, driveId: String, fileId: String): String {
        val url = "https://graph.microsoft.com/v1.0/sites/$siteId/drives/$driveId/items/$fileId/content"

        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $accessToken")
            .build()

        return withContext(Dispatchers.IO) {
            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    throw IOException("Failed to download file content: ${response.code} ${response.message}")
                }

                response.body?.string() ?: throw IOException("Empty file content")
            }
        }
    }
}