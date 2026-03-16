package com.danceboard.shared.dto

import kotlinx.serialization.Serializable

@Serializable
data class UploadConfigResponse(
    val accessToken: String,
    val folderId: String
)
