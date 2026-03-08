package com.danceboard.shared.dto

import com.danceboard.shared.DanceStyle
import com.danceboard.shared.DanceType
import com.danceboard.shared.DifficultyLevel
import kotlinx.serialization.Serializable

@Serializable
data class SearchFilters(
    val query: String? = null,           // Full-text search
    val danceType: DanceType? = null,
    val danceStyle: DanceStyle? = null,
    val difficultyLevel: DifficultyLevel? = null,
    val tags: List<String>? = null,
    val author: String? = null,
    val sortBy: SortField = SortField.CREATED_AT,
    val sortDirection: SortDirection = SortDirection.DESC,
    val page: Int = 0,
    val pageSize: Int = 20
)


@Serializable
enum class SortField { NAME, CREATED_AT, UPDATED_AT }
@Serializable
enum class SortDirection { ASC, DESC }