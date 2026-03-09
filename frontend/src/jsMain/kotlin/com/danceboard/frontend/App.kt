package com.danceboard.frontend

import androidx.compose.runtime.Composable
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.H1
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.Text

@Composable
fun App() {
    Div {
        H1 { Text("🎭 DanceBoard") }
        P { Text("Twoja aplikacja do zarządzania materiałami tanecznymi!") }
    }
}