package com.danceboard.frontend.components

import androidx.compose.runtime.*
import com.danceboard.shared.DanceStyle
import com.danceboard.shared.DanceType
import com.danceboard.shared.DifficultyLevel
import com.danceboard.shared.dto.SearchFilters
import org.jetbrains.compose.web.dom.*
import org.jetbrains.compose.web.attributes.InputType

@Composable
fun SearchBar(
    filters: SearchFilters,
    onFiltersChanged: (SearchFilters) -> Unit
) {
    // Lokalny stan pola tekstowego
    var queryText by remember { mutableStateOf(filters.query ?: "") }

    Div(attrs = { classes("search-bar") }) {
        // Pole wyszukiwania
        Input(type = InputType.Text) {
            value(queryText)
            attr("placeholder", "Szukaj materiałów...")
            onInput { event ->
                queryText = event.value
                onFiltersChanged(filters.copy(query = queryText.ifBlank { null }))
            }
        }

        // Filtr: Typ tańca
        Select(attrs = {
            onChange { event ->
                val selected = event.value?.takeIf { it.isNotBlank() }
                onFiltersChanged(filters.copy(
                    danceType = selected?.let { DanceType.valueOf(it) }
                ))
            }
        }) {
            Option(value = "") { Text("Wszystkie typy") }
            DanceType.entries.forEach { type ->
                Option(value = type.name) { Text(type.name) }
            }
        }

        // Filtr: Styl tańca
        Select(attrs = {
            onChange { event ->
                val selected = event.value?.takeIf { it.isNotBlank() }
                onFiltersChanged(filters.copy(
                    danceStyle = selected?.let { DanceStyle.valueOf(it) }
                ))
            }
        }) {
            Option(value = "") { Text("Wszystkie style") }
            DanceStyle.entries.forEach { style ->
                Option(value = style.name) { Text(style.name) }
            }
        }

        // Filtr: Poziom
        Select(attrs = {
            onChange { event ->
                val selected = event.value?.takeIf { it.isNotBlank() }
                onFiltersChanged(filters.copy(
                    difficultyLevel = selected?.let { DifficultyLevel.valueOf(it) }
                ))
            }
        }) {
            Option(value = "") { Text("Wszystkie poziomy") }
            DifficultyLevel.entries.forEach { level ->
                Option(value = level.name) { Text(level.name) }
            }
        }
    }
}
