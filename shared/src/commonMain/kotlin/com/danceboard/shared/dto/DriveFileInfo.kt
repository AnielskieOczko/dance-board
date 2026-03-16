package com.danceboard.shared.dto

import kotlinx.serialization.Serializable

@Serializable
data class DriveFileInfo(
    val fileId: String,
    val viewUrl: String,
    val fileName: String,
)