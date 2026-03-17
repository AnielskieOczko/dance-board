package com.danceboard.frontend

import androidx.compose.runtime.*
import com.danceboard.frontend.components.*
import com.danceboard.frontend.state.AppState
import com.danceboard.frontend.state.View
import kotlinx.coroutines.launch
import org.jetbrains.compose.web.dom.*

@Composable
fun App() {
    // Stan aplikacji — jeden obiekt dla całego frontendu
    val appState = remember { AppState() }
    val scope = rememberCoroutineScope()

    // Załaduj materiały i zsynchronizuj URL przy starcie
    LaunchedEffect(Unit) {
        appState.syncUrlToState()
    }

    Div(attrs = { classes("app") }) {
        // ═══════════════════════════════════
        // Nagłówek
        // ═══════════════════════════════════
        Header(attrs = { classes("header") }) {
            H1 { Text("🎭 DanceBoard") }
            Button(attrs = {
                attr("type", "button")
                onClick {
                    if (appState.currentView == View.LIST) {
                        appState.navigateTo(View.FORM)
                    } else {
                        appState.navigateTo(View.LIST)
                    }
                }
            }) {
                Text(if (appState.currentView == View.LIST) "+ Dodaj materiał" else "← Lista")
            }
        }

        // Błąd
        appState.error?.let { errorMsg ->
            Div(attrs = { classes("error") }) { Text(errorMsg) }
        }

        // ═══════════════════════════════════
        // Widok — lista lub formularz
        // ═══════════════════════════════════
        when (appState.currentView) {
            View.LIST -> {
                // Wyszukiwarka
                SearchBar(
                    filters = appState.searchFilters,
                    onFiltersChanged = { newFilters ->
                        scope.launch { appState.updateFilters(newFilters) }
                    }
                )

                // Lista materiałów
                MaterialList(
                    materials = appState.materials,
                    totalCount = appState.totalCount,
                    currentPage = appState.currentPage,
                    totalPages = appState.totalPages,
                    isLoading = appState.isLoading,
                    onEdit = { material -> appState.navigateTo(View.FORM, material) },
                    onDelete = { material ->
                        scope.launch { appState.deleteMaterial(material.id) }
                    },
                    onShowDetails = { material ->
                        appState.navigateTo(View.DETAILS, material)
                    },
                    onPageChange = { page ->
                        scope.launch {
                            appState.updateFilters(appState.searchFilters.copy(page = page))
                        }
                    }
                )
            }
            View.FORM -> {
                MaterialForm(
                    existingMaterial = appState.editingMaterial,
                    uploadProgress = appState.uploadProgress,
                    onSave = { request, videoFile ->
                        scope.launch { appState.createMaterial(
                            request,
                            videoFile = videoFile
                        ) }
                    },
                    onUpdate = { id, request, videoFile ->
                        scope.launch { appState.updateMaterial(
                            id, request,
                            videoFile = videoFile
                        ) }
                    },
                    onCancel = { appState.navigateTo(View.LIST) }
                )
            }
            View.DETAILS -> {
                appState.selectedDanceMaterial?.let { material ->
                    MaterialDetails(
                        material = material,
                        onBack = { appState.navigateTo(View.LIST) },
                        onEdit = { appState.navigateTo(View.FORM, material) }
                    )
                }
            }
        }
    }
}
