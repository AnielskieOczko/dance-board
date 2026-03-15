package com.danceboard.backend.service

import com.danceboard.shared.dto.DriveFileInfo
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.InputStreamContent
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.File
import com.google.api.services.drive.model.Permission
import com.google.auth.http.HttpCredentialsAdapter
import com.google.auth.oauth2.ClientId
import com.google.auth.oauth2.GoogleCredentials
import com.google.auth.oauth2.UserCredentials
import org.slf4j.LoggerFactory
import java.io.InputStream

class GoogleDriveService(
    private val folderId: String,
    private val clientId: String,
    private val clientSecret: String,
    private val refreshToken: String,
) {
    private val logger = LoggerFactory.getLogger(GoogleDriveService::class.java)

    // Lazy — połączenie z Drive tworzone raz, przy pierwszym użyciu
    private val drive: Drive by lazy {
        val transport = GoogleNetHttpTransport.newTrustedTransport()
        val jsonFactory = GsonFactory.getDefaultInstance()
        val credentials = UserCredentials.newBuilder()
            .setClientId(clientId)
            .setClientSecret(clientSecret)
            .setRefreshToken(refreshToken)
            .build()

        Drive.Builder(transport, jsonFactory, HttpCredentialsAdapter(credentials))
            .setApplicationName("DanceBoard")
            .build()
    }

    fun uploadVideo(fileName: String, inputStream: InputStream, mimeType: String): DriveFileInfo {
        logger.info("Uploading video '$fileName' to Drive folder $folderId")

        // 1. Upload pliku do folderu
        val metadata = File().apply {
            name = fileName
            parents = listOf(folderId)
        }
        val uploaded = drive.files()
            .create(metadata, InputStreamContent(mimeType, inputStream))
            .setFields("id, webViewLink, name")
            .setSupportsAllDrives(true)
            .execute()

        // 2. Ustaw "everyone with link can view" żeby iframe w app działał
        try {
            drive.permissions()
                .create(uploaded.id, Permission().apply {
                    type = "anyone"
                    role = "reader"
                })
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
            drive.files().delete(fileId)
                .setSupportsAllDrives(true)
                .execute()
        } catch (e: Exception) {
            logger.error("Failed to delete $fileId: ${e.message}")
        }
    }
}