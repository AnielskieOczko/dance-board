package com.danceboard.shared.dto

import com.danceboard.shared.DanceStyle
import com.danceboard.shared.DanceType
import com.danceboard.shared.DifficultyLevel
import kotlinx.serialization.Serializable

@Serializable
data class DanceMaterialResponse(
    val id: String,
    val name: String,
    val description: String?,
    val tags: List<String>,
    val danceType: DanceType,
    val danceStyle: DanceStyle,
    val difficultyLevel: DifficultyLevel,
    val sourceUrl: String?,
    val driveFileId: String?,
    val driveViewUrl: String?,
    val author: String,
    val createdAt: String,  // ISO-8601
    val updatedAt: String   // ISO-8601
)
