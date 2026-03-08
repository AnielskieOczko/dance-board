package com.danceboard.backend.mapper

import com.danceboard.backend.config.DanceMaterials
import com.danceboard.backend.config.DanceMaterials.driveViewUrl
import com.danceboard.backend.model.DanceMaterialEntity
import com.danceboard.shared.dto.DanceMaterialResponse
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.ResultRow

fun ResultRow.toEntity() = DanceMaterialEntity(
    id = this[DanceMaterials.id],
    name = this[DanceMaterials.name],
    description = this[DanceMaterials.description],
    tags = Json.decodeFromString(this[DanceMaterials.tags]) ,
    danceType = this[DanceMaterials.danceType],
    danceStyle = this[DanceMaterials.danceStyle],
    difficultyLevel = this[DanceMaterials.difficultyLevel],
    sourceUrl = this[DanceMaterials.sourceUrl],
    driveFileId = this[DanceMaterials.driveFileId],
    author = this[DanceMaterials.author],
    createdAt = this[DanceMaterials.createdAt],
    updatedAt = this[DanceMaterials.updatedAt]
)

fun DanceMaterialEntity.toResponse() = DanceMaterialResponse(
    id = id.toString(),
    name = name,
    description = description,
    tags = tags,
    danceType = danceType,
    danceStyle = danceStyle,
    difficultyLevel = difficultyLevel,
    sourceUrl = sourceUrl,
    driveFileId = driveFileId,
    driveViewUrl = driveViewUrl.toString(),
    author = author,
    createdAt = createdAt.toString(),                 // Instant → ISO string
    updatedAt = updatedAt.toString()
)