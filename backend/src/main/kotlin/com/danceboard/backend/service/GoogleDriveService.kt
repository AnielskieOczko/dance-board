package com.danceboard.backend.service

import com.danceboard.shared.dto.DriveFileInfo
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.InputStreamContent
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.File
import com.google.api.services.drive.model.Permission
import com.google.auth.http.HttpCredentialsAdapter
import com.google.auth.oauth2.UserCredentials
import java.io.InputStream
import org.slf4j.LoggerFactory

class GoogleDriveService(
        private val folderId: String,
        private val clientId: String,
        private val clientSecret: String,
        private val refreshToken: String,
) {
    private val logger = LoggerFactory.getLogger(GoogleDriveService::class.java)

    // Lazy — połączenie z Drive tworzone raz, przy pierwszym użyciu
    private val credentials by lazy {
        UserCredentials.newBuilder()
            .setClientId(clientId)
            .setClientSecret(clientSecret)
            .setRefreshToken(refreshToken)
            .build()
    }

    private val drive: Drive by lazy {
        val transport = GoogleNetHttpTransport.newTrustedTransport()
        val jsonFactory = GsonFactory.getDefaultInstance()

        Drive.Builder(transport, jsonFactory, HttpCredentialsAdapter(credentials))
            .setApplicationName("DanceBoard")
            .build()
    }

    fun getAccessToken(): String {
        credentials.refreshIfExpired()
        return credentials.accessToken.tokenValue
    }

    fun getFolderId(): String = folderId

    fun uploadVideo(fileName: String, inputStream: InputStream, mimeType: String): DriveFileInfo {
        logger.info("Uploading video '$fileName' to Drive folder $folderId")

        // 1. Upload pliku do folderu
        val metadata =
                File().apply {
                    name = fileName
                    parents = listOf(folderId)
                }
        val uploaded =
                drive.files()
                        .create(metadata, InputStreamContent(mimeType, inputStream))
                        .setFields("id, webViewLink, name")
                        .setSupportsAllDrives(true)
                        .execute()

        // 2. Ustaw "everyone with link can view" żeby iframe w app działał
        try {
            drive.permissions()
                    .create(
                            uploaded.id,
                            Permission().apply {
                                type = "anyone"
                                role = "reader"
                            }
                    )
                    .execute()
        } catch (e: Exception) {
            logger.warn("Set public read failed: ${e.message}")
        }

        return DriveFileInfo(
                fileId = uploaded.id,
                viewUrl = uploaded.webViewLink,
                fileName = uploaded.name,
        )
    }

    fun deleteVideo(fileId: String) {
        logger.info("Deleting Drive file $fileId")
        try {
            drive.files().delete(fileId).setSupportsAllDrives(true).execute()
        } catch (e: Exception) {
            logger.error("Failed to delete $fileId: ${e.message}")
        }
    }

    fun createResumableSession(fileName: String, mimeType: String, fileSize: Long?): String {
        val requestFactory = drive.requestFactory

        val metaData = mapOf("name" to fileName, "parents" to listOf(folderId))

        val url =
                com.google.api.client.http.GenericUrl(
                        "https://www.googleapis.com/upload/drive/v3/files?uploadType=resumable"
                )
        val content = com.google.api.client.http.json.JsonHttpContent(drive.jsonFactory, metaData)
        val request =
                requestFactory.buildPostRequest(url, content).apply {
                    headers.set("X-Upload-Content-Type", mimeType)
                    fileSize?.let { headers.set("X-Upload-Content-Length", it.toString()) }
                }

        val response = request.execute()
        return response.headers.location ?: throw IllegalStateException("Failed to create drive")
    }

    /**
     * Ustawia uprawnienia publiczne dla pliku i pobiera jego metadane (Link, ID, Nazwa).
     */
    fun finishDirectUpload(fileId: String): DriveFileInfo {
        logger.info("Finishing direct upload for file $fileId")

        // 1. Ustawienie uprawnień publicznych (anyone can view)
        try {
            drive.permissions()
                    .create(
                            fileId,
                            Permission().apply {
                                type = "anyone"
                                role = "reader"
                            }
                    )
                    .execute()
        } catch (e: Exception) {
            logger.warn("Set public read failed for $fileId: ${e.message}")
        }

        // 2. Pobranie szczegółowych metadanych (Link, ID, Nazwa)
        val file =
                drive.files()
                        .get(fileId)
                        .setFields("id, webViewLink, name")
                        .setSupportsAllDrives(true)
                        .execute()

        return DriveFileInfo(
                fileId = file.id,
                viewUrl = file.webViewLink,
                fileName = file.name,
        )
    }
}
