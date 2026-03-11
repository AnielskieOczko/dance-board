package com.danceboard.frontend.api

import com.danceboard.shared.dto.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*

class ApiClient {
    private val client = HttpClient {
        install(ContentNegotiation) {
            json()
        }
    }

    private val baseUrl = js("window.API_BASE_URL").unsafeCast<String?>()
        ?: "http://localhost:8080/api/v1"


    suspend fun searchMaterials(filters: SearchFilters = SearchFilters()): MaterialListResponse {
        return client.get("$baseUrl/materials/search") {
            filters.query?.let { parameter("query", it) }
            filters.danceType?.let { parameter("danceType", it.name) }
            filters.danceStyle?.let { parameter("danceStyle", it.name) }
            filters.difficultyLevel?.let { parameter("difficultyLevel", it.name) }
            filters.author?.let { parameter("author", it) }
            parameter("sortBy", filters.sortBy.name)
            parameter("sortDirection", filters.sortDirection.name)
            parameter("page", filters.page)
            parameter("pageSize", filters.pageSize)
        }.body()
    }

    suspend fun createMaterial(request: CreateMaterialRequest): DanceMaterialResponse {
        return client.post("$baseUrl/materials") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    suspend fun updateMaterial(id: String, request: UpdateMaterialRequest): DanceMaterialResponse {
        return client.put("$baseUrl/materials/$id") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    suspend fun deleteMaterial(id: String) {
        client.delete("$baseUrl/materials/$id")
    }

    suspend fun getMaterial(id: String): DanceMaterialResponse {
        return client.get("$baseUrl/materials/$id").body()
    }


}