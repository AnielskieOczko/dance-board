package com.danceboard.backend.model

import com.danceboard.shared.DanceStyle
import com.danceboard.shared.DanceType
import com.danceboard.shared.DifficultyLevel
import java.util.UUID
import kotlinx.datetime.Instant

data class DanceMaterialEntity(
    val id: UUID,
    val name: String,
    val description: String?,
    val tags: List<String>,
    val danceType: DanceType,
    val danceStyle: DanceStyle,
    val difficultyLevel: DifficultyLevel,
    val sourceUrl: String?,
    val driveFileId: String?,
    val author: String,
    val createdAt: Instant,
    val updatedAt: Instant
)