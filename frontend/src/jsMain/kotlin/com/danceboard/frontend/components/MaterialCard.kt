package com.danceboard.frontend.components

import androidx.compose.runtime.Composable
import com.danceboard.shared.dto.DanceMaterialResponse
import org.jetbrains.compose.web.dom.A
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.H3
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.Small
import org.jetbrains.compose.web.dom.Span
import org.jetbrains.compose.web.dom.Text

@Composable
fun MaterialCard(
    material: DanceMaterialResponse,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Div(attrs = { classes("card") }) {
        H3 { Text(material.name) }

        Div(attrs = { classes("card-meta") }) {
            Span(attrs = { classes("badge") }) { Text(material.danceType.name) }
            Span(attrs = { classes("badge") }) { Text(material.danceStyle.name) }
            Span(attrs = { classes("badge") }) { Text(material.difficultyLevel.name) }
        }

        material.description?.let { desc ->
            P { Text(desc) }
        }

        if (material.tags.isNotEmpty()) {
            Div(attrs = { classes("tags")}) {
                material.tags.forEach { tag ->
                    Span(attrs = { classes("tag") }) { Text(tag) }
                }
            }
        }

        Small { Text("Autor: ${material.author} | ${material.createdAt.substringBefore("T")}") }

        material.sourceUrl?.let { url ->
            Div {
                A(href = url, attrs = { attr("target", "_blank") }) { Text("Źródło →") }
            }
        }

        Div(attrs = { classes("card-actions") }) {
            Button(attrs = { onClick { onEdit() } }) { Text("Edytuj") }
            Button(attrs = { onClick { onDelete () } }) { Text("Usun") }
        }


    }
}