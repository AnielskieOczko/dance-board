package com.danceboard.shared.dto

import kotlinx.serialization.Serializable

@Serializable
class UploadSessionResponse(
    val uploadUrl: String
)