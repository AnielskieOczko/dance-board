package com.danceboard.shared.dto

import com.danceboard.shared.DanceStyle
import com.danceboard.shared.DanceType
import com.danceboard.shared.DifficultyLevel
import kotlinx.serialization.Serializable


@Serializable
data class UpdateMaterialRequest(
    val name: String? = null,
    val description: String? = null,
    val tags: List<String>? = null,
    val danceType: DanceType? = null,
    val danceStyle: DanceStyle? = null,
    val difficultyLevel: DifficultyLevel? = null,
    val sourceUrl: String? = null
)

