package com.danceboard.backend.service

import com.danceboard.backend.mapper.toResponse
import com.danceboard.backend.repository.DanceMaterialRepository
import com.danceboard.shared.dto.CreateMaterialRequest
import com.danceboard.shared.dto.DanceMaterialResponse
import com.danceboard.shared.dto.MaterialListResponse
import com.danceboard.shared.dto.SearchFilters
import com.danceboard.shared.dto.UpdateMaterialRequest
import org.slf4j.LoggerFactory
import java.util.UUID

class DanceMaterialService(
    private val danceMaterialRepository: DanceMaterialRepository,
) {

    private val logger = LoggerFactory.getLogger(DanceMaterialService::class.java)

    suspend fun create(request: CreateMaterialRequest): DanceMaterialResponse {
        logger.info("Creating material {}", request)
        val material = danceMaterialRepository.create(request) ?: throw RuntimeException("Error while creating material")
        return material.toResponse()
    }

    suspend fun getById(id: UUID): DanceMaterialResponse {
        logger.info("Getting material by id {}", id)
        val material = danceMaterialRepository.findById(id) ?: throw NoSuchElementException("Material not found: $id")
        return material.toResponse()
    }
    suspend fun update(id: UUID, request: UpdateMaterialRequest): DanceMaterialResponse {
        logger.info("Updating material {}", id)
        val updatedMaterial = danceMaterialRepository.update(id, request) ?: throw RuntimeException("Error while updating material")
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
    suspend fun list(page: Int, pageSize: Int): MaterialListResponse
        = search(SearchFilters(page = page, pageSize = pageSize))

}