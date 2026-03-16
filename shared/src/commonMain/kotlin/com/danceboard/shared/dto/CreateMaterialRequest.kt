package com.danceboard.shared.dto

import com.danceboard.shared.DanceStyle
import com.danceboard.shared.DanceType
import com.danceboard.shared.DifficultyLevel
import kotlinx.serialization.Serializable

@Serializable
data class CreateMaterialRequest(
    val name: String,
    val description: String? = null,
    val tags: List<String> = emptyList(),
    val danceType: DanceType,
    val danceStyle: DanceStyle,
    val difficultyLevel: DifficultyLevel,
    val sourceUrl: String? = null,
    val author: String
)