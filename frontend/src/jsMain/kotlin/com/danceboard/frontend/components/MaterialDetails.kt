package com.danceboard.frontend.components

import androidx.compose.runtime.Composable
import com.danceboard.shared.dto.DanceMaterialResponse
import org.jetbrains.compose.web.attributes.src
import org.jetbrains.compose.web.dom.Iframe
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.Text

@Composable
fun MaterialDetails(
    material: DanceMaterialResponse,
    onBack: () -> Unit,
    onEdit: () -> Unit,
) {


    material.driveViewUrl?.let { url ->
        Iframe(attrs = {
            attr("src", url.replace("/view?usp=drivesdk", "/preview"))
            attr("width", "100%")
            attr("height", "480")
            attr("allowfullscreen", "true")
        })
    } ?: run {
        P { Text("Brak wideo dla tego materiału.") }
    }
}