package com.danceboard.shared.dto

import kotlinx.serialization.Serializable

@Serializable
data class FileUploadResponse(
    val driveFileId: String,
    val driveViewUrl: String,
    val fileName: String,
    val fileSizeBytes: Long
)