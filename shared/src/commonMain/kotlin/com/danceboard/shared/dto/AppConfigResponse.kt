package com.danceboard.shared.dto

import kotlinx.serialization.Serializable

@Serializable
data class AppConfigResponse(
    val maxUploadSizeMb: Int
)