package com.danceboard.backend.service

import com.danceboard.backend.mapper.toResponse
import com.danceboard.backend.repository.DanceMaterialRepository
import com.danceboard.shared.dto.CreateMaterialRequest
import com.danceboard.shared.dto.DanceMaterialResponse
import com.danceboard.shared.dto.MaterialListResponse
import com.danceboard.shared.dto.SearchFilters
import com.danceboard.shared.dto.UpdateMaterialRequest
import com.danceboard.shared.dto.UploadSessionRequest
import com.danceboard.shared.dto.UploadSessionResponse
import com.danceboard.shared.dto.UploadConfigResponse
import com.danceboard.shared.dto.DriveFileInfo
import java.io.InputStream
import java.util.UUID
import java.util.NoSuchElementException
import org.slf4j.LoggerFactory

class DanceMaterialService(
        private val danceMaterialRepository: DanceMaterialRepository,
        val googleDriveService: GoogleDriveService
) {

    private val logger = LoggerFactory.getLogger(DanceMaterialService::class.java)

    suspend fun uploadVideo(
            materialId: UUID,
            fileName: String,
            inputStream: InputStream,
            mimeType: String
    ): DanceMaterialResponse {
        val driveInfo = googleDriveService.uploadVideo(fileName, inputStream, mimeType)
        val updated =
                danceMaterialRepository.updateDriveInfo(
                        materialId,
                        driveInfo.fileId,
                        driveInfo.viewUrl
                )
                        ?: throw NoSuchElementException("Material not found: $materialId")
        return updated.toResponse()
    }

    suspend fun deleteVideo(materialId: UUID): DanceMaterialResponse {
        val material =
                danceMaterialRepository.findById(materialId)
                        ?: throw NoSuchElementException("Material not found: $materialId")
        material.driveFileId?.let { driveFileId -> googleDriveService.deleteVideo(driveFileId) }
        val updated =
                danceMaterialRepository.updateDriveInfo(materialId, null, null)
                        ?: throw NoSuchElementException(
                                "Material $materialId disappeared during video delete"
                        )
        return updated.toResponse()
    }

    suspend fun create(request: CreateMaterialRequest): DanceMaterialResponse {
        logger.info("Creating material {}", request)
        val material =
                danceMaterialRepository.create(request)
                        ?: throw RuntimeException("Error while creating material")
        return material.toResponse()
    }

    suspend fun getById(id: UUID): DanceMaterialResponse {
        logger.info("Getting material by id {}", id)
        val material =
                danceMaterialRepository.findById(id)
                        ?: throw NoSuchElementException("Material not found: $id")
        return material.toResponse()
    }
    suspend fun update(id: UUID, request: UpdateMaterialRequest): DanceMaterialResponse {
        logger.info("Updating material {}", id)
        val updatedMaterial =
                danceMaterialRepository.update(id, request)
                        ?: throw RuntimeException("Error while updating material")
        return updatedMaterial.toResponse()
    }
    suspend fun delete(id: UUID) {
        logger.info("Deleting material {}", id)
        val deleted = danceMaterialRepository.delete(id)
        if (deleted == 0) throw NoSuchElementException("Material with id $id not found")
    }
    suspend fun search(filters: SearchFilters): MaterialListResponse {
        logger.info("Searching materials {}", filters)
        val (entities, total) = danceMaterialRepository.search(filters)
        return MaterialListResponse(
                items = entities.map { it.toResponse() },
                totalCount = total,
                page = filters.page,
                pageSize = filters.pageSize,
                totalPages = ((total.toInt() + filters.pageSize - 1) / filters.pageSize),
        )
    }
    suspend fun list(page: Int, pageSize: Int): MaterialListResponse =
            search(SearchFilters(page = page, pageSize = pageSize))

    suspend fun attachVideoToMaterial(materialId: UUID, driveFileId: String): DanceMaterialResponse {
        logger.info("Attaching video {} to material {}", driveFileId, materialId)

        val driveInfo = googleDriveService.finishDirectUpload(driveFileId)

        val updated = danceMaterialRepository.updateDriveInfo(
            materialId,
            driveInfo.fileId,
            driveInfo.viewUrl) ?: throw NoSuchElementException("Material not found: $materialId")

        return updated.toResponse()
    }

    fun createUploadSession(request: UploadSessionRequest): UploadSessionResponse {
        val url = googleDriveService.createResumableSession(
            fileName = request.name,
            mimeType = request.mimeType,
            fileSize = request.size
        )
        return UploadSessionResponse(uploadUrl = url)
    }

    fun getUploadConfig(): UploadConfigResponse {
        return UploadConfigResponse(
            accessToken = googleDriveService.getAccessToken(),
            folderId = googleDriveService.getFolderId()
        )
    }
}
