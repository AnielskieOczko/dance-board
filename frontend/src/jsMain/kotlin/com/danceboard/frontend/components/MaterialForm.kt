package com.danceboard.frontend.components

import androidx.compose.runtime.*
import com.danceboard.shared.DanceStyle
import com.danceboard.shared.DanceType
import com.danceboard.shared.DifficultyLevel
import com.danceboard.shared.dto.CreateMaterialRequest
import com.danceboard.shared.dto.DanceMaterialResponse
import com.danceboard.shared.dto.UpdateMaterialRequest
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.attributes.selected
import org.jetbrains.compose.web.dom.*
import org.w3c.files.File

@Composable
fun MaterialForm(
    existingMaterial: DanceMaterialResponse?,  // null = tryb dodawania
    onSave: (CreateMaterialRequest, File?) -> Unit,
    onUpdate: (String, UpdateMaterialRequest, File?) -> Unit,
    onCancel: () -> Unit
) {
    val isEditing = existingMaterial != null

    // Stan formularza — inicjalizowany z istniejącego materiału lub pusty
    var name by remember { mutableStateOf(existingMaterial?.name ?: "") }
    var description by remember { mutableStateOf(existingMaterial?.description ?: "") }
    var tagsText by remember { mutableStateOf(existingMaterial?.tags?.joinToString(", ") ?: "") }
    var danceType by remember { mutableStateOf(existingMaterial?.danceType ?: DanceType.STANDARD) }
    var danceStyle by remember { mutableStateOf(existingMaterial?.danceStyle ?: DanceStyle.WALTZ) }
    var difficulty by remember { mutableStateOf(existingMaterial?.difficultyLevel ?: DifficultyLevel.BEGINNER) }
    var sourceUrl by remember { mutableStateOf(existingMaterial?.sourceUrl ?: "") }
    var author by remember { mutableStateOf(existingMaterial?.author ?: "") }
    var videoFile by remember { mutableStateOf<org.w3c.files.File?>(null) }

    Div(attrs = { classes("form-container") }) {
        H2 { Text(if (isEditing) "Edytuj materiał" else "Dodaj materiał") }

        // Nazwa
        Label { Text("Nazwa *") }
        Input(type = InputType.Text) {
            value(name)
            attr("placeholder", "np. Basic Waltz Box Step")
            onInput { name = it.value }
        }

        // Opis
        Label { Text("Opis") }
        TextArea(attrs = {
            value(description)
            attr("placeholder", "Opis materiału...")
            attr("rows", "3")
            onInput { description = it.value }
        })

        // Tagi (rozdzielone przecinkami)
        Label { Text("Tagi (rozdzielone przecinkami)") }
        Input(type = InputType.Text) {
            value(tagsText)
            attr("placeholder", "np. basics, footwork, technique")
            onInput { tagsText = it.value }
        }

        // Typ tańca
        Label { Text("Typ tańca *") }
        Select(attrs = {
            onChange { event ->
                event.value?.let { danceType = DanceType.valueOf(it) }
            }
        }) {
            DanceType.entries.forEach { type ->
                Option(value = type.name, attrs = {
                    if (type == danceType) selected()
                }) { Text(type.name) }
            }
        }

        // Styl tańca
        Label { Text("Styl tańca *") }
        Select(attrs = {
            onChange { event ->
                event.value?.let { danceStyle = DanceStyle.valueOf(it) }
            }
        }) {
            DanceStyle.entries.forEach { style ->
                Option(value = style.name, attrs = {
                    if (style == danceStyle) selected()
                }) { Text(style.name) }
            }
        }

        // Poziom trudności
        Label { Text("Poziom *") }
        Select(attrs = {
            onChange { event ->
                event.value?.let { difficulty = DifficultyLevel.valueOf(it) }
            }
        }) {
            DifficultyLevel.entries.forEach { level ->
                Option(value = level.name, attrs = {
                    if (level == difficulty) selected()
                }) { Text(level.name) }
            }
        }

        // URL źródła
        Label { Text("URL źródła") }
        Input(type = InputType.Url) {
            value(sourceUrl)
            attr("placeholder", "https://youtube.com/watch?v=...")
            onInput { sourceUrl = it.value }
        }

        Input(InputType.File, attrs = {
            attr("accept", "video/mp4,video/x-m4v,video/*")
            onChange { event ->
                videoFile = event.target.files?.item(0)
            }
        })

        // Autor
        Label { Text("Autor *") }
        Input(type = InputType.Text) {
            value(author)
            attr("placeholder", "Twoje imię")
            onInput { author = it.value }
        }

        // Przyciski
        Div(attrs = { classes("form-actions") }) {
            Button(attrs = {
                onClick {
                    val tags = tagsText.split(",").map { it.trim() }.filter { it.isNotBlank() }

                    if (isEditing) {
                        onUpdate(existingMaterial!!.id, UpdateMaterialRequest(
                            name = name,
                            description = description.ifBlank { null },
                            tags = tags,
                            danceType = danceType,
                            danceStyle = danceStyle,
                            difficultyLevel = difficulty,
                            sourceUrl = sourceUrl.ifBlank { null }
                        ), videoFile)
                    } else {
                        onSave(CreateMaterialRequest(
                            name = name,
                            description = description.ifBlank { null },
                            tags = tags,
                            danceType = danceType,
                            danceStyle = danceStyle,
                            difficultyLevel = difficulty,
                            sourceUrl = sourceUrl.ifBlank { null },
                            author = author
                        ), videoFile)
                    }
                }
            }) { Text(if (isEditing) "Zapisz zmiany" else "Dodaj") }

            Button(attrs = { onClick { onCancel() } }) { Text("Anuluj") }
        }
    }
}
