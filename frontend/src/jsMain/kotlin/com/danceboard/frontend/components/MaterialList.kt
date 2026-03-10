package com.danceboard.frontend.components

import androidx.compose.runtime.Composable
import com.danceboard.shared.dto.DanceMaterialResponse
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.H3
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text

@Composable
fun MaterialList(
    materials: List<DanceMaterialResponse>,
    totalCount: Long,
    currentPage: Int,
    totalPages: Int,
    isLoading: Boolean,
    onEdit: (DanceMaterialResponse) -> Unit,
    onDelete: (DanceMaterialResponse) -> Unit,
    onPageChange: (Int) -> Unit,
) {
    if (isLoading) {
        Div(attrs = { classes("loading") }) { Text("Loading...") }
        return
    }

    if (materials.isEmpty()) {
        Div(attrs = { classes("empty") }) {
            H3 { Text("Brak materiałów") }
            P { Text("Dodaj pierwszy materiał klikając przycisk powyżej.") }
        }
        return
    }

    // Info o wynikach
    P { Text("Znaleziono: $totalCount materiałów") }
    // Lista kart
    Div(attrs = { classes("material-list") }) {
        materials.forEach { material ->
            MaterialCard(
                material = material,
                onEdit = { onEdit(material) },
                onDelete = { onDelete(material) }
            )
        }
    }
    // Paginacja
    if (totalPages > 1) {
        Div(attrs = { classes("pagination") }) {
            Button(attrs = {
                onClick { onPageChange(currentPage - 1) }
                if (currentPage == 0) attr("disabled", "true")
            }) { Text("← Poprzednia") }
            Span { Text("Strona ${currentPage + 1} z $totalPages") }
            Button(attrs = {
                onClick { onPageChange(currentPage + 1) }
                if (currentPage >= totalPages - 1) attr("disabled", "true")
            }) { Text("Następna →") }
        }
    }
}
