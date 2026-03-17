package com.danceboard.frontend.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.danceboard.frontend.api.ApiClient
import com.danceboard.shared.dto.*
import kotlinx.browser.window
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

@OptIn(DelicateCoroutinesApi::class)
class AppState {
    private val apiClient = ApiClient()

    var materials by mutableStateOf<List<DanceMaterialResponse>>(emptyList())
    var totalCount by mutableStateOf(0L)
    var currentPage by mutableStateOf(0)
    var totalPages by mutableStateOf(0)
    var isLoading by mutableStateOf(false)
    var uploadProgress by mutableStateOf<Double?>(null)
    var error by mutableStateOf<String?>(null)

    var editingMaterial by mutableStateOf<DanceMaterialResponse?>(null)

    var currentView by mutableStateOf(View.LIST)

    var searchFilters by mutableStateOf(SearchFilters())

    var selectedDanceMaterial: DanceMaterialResponse? by mutableStateOf(null)
    
    init {
        window.addEventListener("popstate", {
            kotlinx.coroutines.GlobalScope.launch {
                syncUrlToState()
            }
        })
    }

    fun navigateTo(view: View, material: DanceMaterialResponse? = null, pushState: Boolean = true) {
        currentView = view
        editingMaterial = null
        selectedDanceMaterial = null
        
        if (view == View.FORM) {
            editingMaterial = material
        } else if (view == View.DETAILS) {
            selectedDanceMaterial = material
        }

        if (pushState) {
            updateUrl(view, material?.id)
        }
    }

    private fun updateUrl(view: View, materialId: String? = null) {
        val path = when (view) {
            View.LIST -> "/"
            View.FORM -> if (materialId != null) "/edit/$materialId" else "/create"
            View.DETAILS -> "/details/$materialId"
        }
        window.history.pushState(null, "", path)
    }

    suspend fun syncUrlToState() {
        val path = window.location.pathname
        val parts = path.split("/").filter { it.isNotEmpty() }

        try {
            when {
                parts.isEmpty() -> {
                    currentView = View.LIST
                    loadMaterials()
                }
                parts[0] == "create" -> {
                    editingMaterial = null
                    currentView = View.FORM
                }
                parts[0] == "edit" && parts.size > 1 -> {
                    val id = parts[1]
                    isLoading = true
                    editingMaterial = apiClient.getMaterial(id)
                    currentView = View.FORM
                }
                parts[0] == "details" && parts.size > 1 -> {
                    val id = parts[1]
                    isLoading = true
                    selectedDanceMaterial = apiClient.getMaterial(id)
                    currentView = View.DETAILS
                }
                else -> {
                    // Fallback to list for unknown paths
                    currentView = View.LIST
                    loadMaterials()
                }
            }
        } catch (e: Exception) {
            error = "Could not load page: ${e.message}"
            currentView = View.LIST
            loadMaterials()
        } finally {
            isLoading = false
        }
    }

    suspend fun showDetails(material: DanceMaterialResponse) {
        navigateTo(View.DETAILS, material)
    }

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

    suspend fun createMaterial(request: CreateMaterialRequest, videoFile: org.w3c.files.File?) {
        isLoading = true
        error = null
        try {
            val response = apiClient.createMaterial(request)

            if (videoFile != null) {
                uploadProgress = 0.0
                val fileId = apiClient.uploadVideoDirect(videoFile) { progress ->
                    uploadProgress = progress
                }
                apiClient.finalizeVideoUpload(materialId = response.id, fileId = fileId)
                uploadProgress = null
            }

            currentView = View.LIST
            loadMaterials()
        } catch (e: Exception) {
            error = e.message
        } finally {
            isLoading = false
        }
    }

    suspend fun updateMaterial(
            id: String,
            request: UpdateMaterialRequest,
            videoFile: org.w3c.files.File?
    ) {
        isLoading = true
        error = null
        try {
            apiClient.updateMaterial(id, request)
            if (videoFile != null) {
                uploadProgress = 0.0
                val fileId = apiClient.uploadVideoDirect(videoFile) { progress ->
                    uploadProgress = progress
                }
                apiClient.finalizeVideoUpload(materialId = id, fileId = fileId)
                uploadProgress = null
            }

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
    LIST,
    FORM,
    DETAILS
}
