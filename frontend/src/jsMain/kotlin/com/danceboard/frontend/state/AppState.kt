package com.danceboard.frontend.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.danceboard.frontend.api.ApiClient
import com.danceboard.shared.dto.*

class AppState {
    private val apiClient = ApiClient()

    var materials by mutableStateOf<List<DanceMaterialResponse>>(emptyList())
    var totalCount by mutableStateOf(0L)
    var currentPage by mutableStateOf(0)
    var totalPages by mutableStateOf(0)
    var isLoading by mutableStateOf(false)
    var error by mutableStateOf<String?>(null)

    var editingMaterial by mutableStateOf<DanceMaterialResponse?>(null)

    var currentView by mutableStateOf(View.LIST)

    var searchFilters by mutableStateOf(SearchFilters())

    suspend fun loadMaterials() {
        isLoading = true
        error = null
        try {
            val response = apiClient.searchMaterials(searchFilters)
            materials = response.items
            totalCount = response.totalCount
            currentPage = response.page
            totalPages = response.totalPages

        } catch (e: Exception) {
            error = e.message
        } finally {
            isLoading = false
        }
    }

    suspend fun createMaterial(request: CreateMaterialRequest) {
        isLoading = true
        error = null
        try {
            val response = apiClient.createMaterial(request)
            currentView = View.LIST
            loadMaterials()
        } catch (e: Exception) {
            error = e.message
        } finally {
            isLoading = false
        }
    }

    suspend fun updateMaterial(id: String, request: UpdateMaterialRequest) {
        isLoading = true
        error = null
        try {
            val response = apiClient.updateMaterial(id, request)
            currentView = View.LIST
            editingMaterial = null
            loadMaterials()
        } catch (e: Exception) {
            error = e.message
        } finally {
            isLoading = false
        }
    }

    suspend fun deleteMaterial(id: String) {
        isLoading = true
        error = null
        try {
            apiClient.deleteMaterial(id)

        } catch (e: Exception) {
            error = e.message
        } finally {
            isLoading = false
        }
    }

    fun startEditing(material: DanceMaterialResponse) {
        editingMaterial = material
        currentView = View.FORM
    }

    fun startCreating() {
        editingMaterial = null
        currentView = View.FORM
    }

    fun goToList() {
        currentView = View.LIST
        editingMaterial = null
    }

    suspend fun updateFilters(filters: SearchFilters) {
        searchFilters = filters
        loadMaterials()
    }

}


enum class View {
    LIST, FORM
}