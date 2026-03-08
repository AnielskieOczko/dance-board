package com.danceboard.shared.dto

import kotlinx.serialization.Serializable

@Serializable
data class MaterialListResponse(
    val items: List<DanceMaterialResponse>,
    val totalCount: Long,
    val page: Int,
    val pageSize: Int,
    val totalPages: Int
)