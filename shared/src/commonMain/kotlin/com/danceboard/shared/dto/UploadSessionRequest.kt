package com.danceboard.shared.dto

import kotlinx.serialization.Serializable

@Serializable
data class UploadSessionRequest(
    val name: String,
    val mimeType: String,
    val size: Long? = null
)