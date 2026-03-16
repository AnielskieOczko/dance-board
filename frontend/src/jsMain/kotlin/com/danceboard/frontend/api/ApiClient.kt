package com.danceboard.frontend.api

import com.danceboard.shared.dto.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.await
import kotlinx.serialization.json.*
import kotlinx.browser.window
import org.khronos.webgl.Int8Array
import org.w3c.dom.events.Event
import org.w3c.files.FileReader
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class ApiClient {
    private val client = HttpClient { install(ContentNegotiation) { json() } }

    private val baseUrl: String = run {
        val windowUrl = js("window.API_BASE_URL").unsafeCast<String?>()
        val currentOrigin = kotlinx.browser.window.location.origin

        when {
            // Jeśli jesteśmy lokalnie na porcie frontendu (8081), kierujemy na port backendu (8080)
            currentOrigin.contains("localhost:8081") || currentOrigin.contains("127.0.0.1:8081") ->
                    "http://localhost:8080/api/v1"

            // W przeciwnym razie używamy tego co w index.html lub domyślnego relatywnego adresu
            windowUrl != null -> windowUrl
            else -> "$currentOrigin/api/v1"
        }
    }

    suspend fun searchMaterials(filters: SearchFilters = SearchFilters()): MaterialListResponse {
        return client
                .get("$baseUrl/materials/search") {
                    filters.query?.let { parameter("query", it) }
                    filters.danceType?.let { parameter("danceType", it.name) }
                    filters.danceStyle?.let { parameter("danceStyle", it.name) }
                    filters.difficultyLevel?.let { parameter("difficultyLevel", it.name) }
                    filters.author?.let { parameter("author", it) }
                    parameter("sortBy", filters.sortBy.name)
                    parameter("sortDirection", filters.sortDirection.name)
                    parameter("page", filters.page)
                    parameter("pageSize", filters.pageSize)
                }
                .body()
    }

    suspend fun createMaterial(request: CreateMaterialRequest): DanceMaterialResponse {
        return client
                .post("$baseUrl/materials") {
                    contentType(ContentType.Application.Json)
                    setBody(request)
                }
                .body()
    }

    suspend fun updateMaterial(id: String, request: UpdateMaterialRequest): DanceMaterialResponse {
        return client
                .put("$baseUrl/materials/$id") {
                    contentType(ContentType.Application.Json)
                    setBody(request)
                }
                .body()
    }

    suspend fun deleteMaterial(id: String) {
        client.delete("$baseUrl/materials/$id")
    }

    suspend fun getMaterial(id: String): DanceMaterialResponse {
        return client.get("$baseUrl/materials/$id").body()
    }

    suspend fun uploadVideo(materialId: String, file: org.w3c.files.File): DanceMaterialResponse {
        val formData = org.w3c.xhr.FormData()
        formData.append("video", file, file.name)

        val response =
                kotlinx.browser
                        .window
                        .fetch(
                                "$baseUrl/materials/$materialId/video",
                                js("({ method: 'POST', body: formData })")
                                        .unsafeCast<org.w3c.fetch.RequestInit>()
                        )
                        .await()

        if (!response.ok) {
            val errorText = response.text().await()
            throw Exception("Failed to upload video: ${response.status} - $errorText")
        }

        val responseText = response.text().await()
        val json = kotlinx.serialization.json.Json { ignoreUnknownKeys = true }
        return json.decodeFromString(responseText)
    }

    suspend fun uploadVideoDirect(file: org.w3c.files.File, onProgress: (Double) -> Unit): String {
        // 1. Get token and folderId from backend
        val config = client.get("$baseUrl/materials/upload-config").body<UploadConfigResponse>()

        // 2. Create resumable session directly with Google
        val sessionResponse = client.post("https://www.googleapis.com/upload/drive/v3/files?uploadType=resumable") {
            header(HttpHeaders.Authorization, "Bearer ${config.accessToken}")
            contentType(ContentType.Application.Json)
            setBody("""{"name":"${file.name}","parents":["${config.folderId}"]}""")
        }

        val uploadUrl = sessionResponse.headers[HttpHeaders.Location]
            ?: throw Exception("No upload URL in Google Drive response")

        // 3. Upload the chunks/file
        val bytes = file.readToByteArray()
        client.put(uploadUrl) {
            contentType(ContentType.parse(file.type))
            setBody(bytes)
            onUpload { bytesSent, totalBytes ->
                onProgress(bytesSent.toDouble() / (totalBytes?.toDouble() ?: 1.0))
            }
        }

        // 4. Resolve fileId
        return resolveFileId(config.accessToken, file.name)
    }

    private suspend fun resolveFileId(accessToken: String, name: String): String {
        val response = client.get("https://www.googleapis.com/drive/v3/files") {
            header(HttpHeaders.Authorization, "Bearer $accessToken")
            parameter("q", "name = '$name'")
            parameter("fields", "files(id, name)")
            parameter("orderBy", "createdTime desc")
            parameter("pageSize", "1")
        }
        val text = response.body<String>()
        val json = Json.parseToJsonElement(text).jsonObject
        return json["files"]?.jsonArray?.get(0)?.jsonObject?.get("id")?.jsonPrimitive?.content
            ?: throw Exception("Could not resolve fileId from Google Drive")
    }

    suspend fun org.w3c.files.File.readToByteArray(): ByteArray = suspendCoroutine { cont ->
        val reader = FileReader()
        reader.onload = { _: Event ->
            val result: ByteArray = js("new Int8Array(this.result)").unsafeCast<ByteArray>()
            cont.resume(result)
        }
        reader.onerror = { _: Event ->
            cont.resumeWithException(RuntimeException("Failed to read file"))
        }
        reader.readAsArrayBuffer(this)
    }

    suspend fun finalizeVideoUpload(materialId: String, fileId: String): DanceMaterialResponse {
        return client
                .post("$baseUrl/materials/$materialId/video-id") {
                    contentType(ContentType.Application.Json)
                    setBody(DriveFileInfo(fileId = fileId, viewUrl = "", fileName = ""))
                }
                .body()
    }
}
