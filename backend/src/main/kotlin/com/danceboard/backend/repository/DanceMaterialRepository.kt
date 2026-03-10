package com.danceboard.backend.repository

import com.danceboard.backend.config.DanceMaterials
import com.danceboard.backend.mapper.toEntity
import com.danceboard.backend.model.DanceMaterialEntity
import com.danceboard.shared.dto.CreateMaterialRequest
import com.danceboard.shared.dto.MaterialListResponse
import com.danceboard.shared.dto.SearchFilters
import com.danceboard.shared.dto.SortDirection
import com.danceboard.shared.dto.SortField
import com.danceboard.shared.dto.UpdateMaterialRequest
import kotlinx.datetime.Clock
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.like
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.lowerCase
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.update
import java.util.UUID
import javax.naming.directory.SearchControls
import kotlin.let


class DanceMaterialRepository {

    private suspend fun <T> dbQuery(block: suspend () -> T): T = newSuspendedTransaction { block() }

    suspend fun create(request: CreateMaterialRequest): DanceMaterialEntity? = dbQuery {
        val id = DanceMaterials.insert {
            it[name] = request.name
            it[tags] = Json.encodeToString(request.tags)
            it[danceType] = request.danceType
            it[danceStyle] = request.danceStyle
            it[difficultyLevel] = request.difficultyLevel
            it[updatedAt] = Clock.System.now()
            it[author] = request.author
            request.description?.let { description -> it[DanceMaterials.description] = description }
            request.sourceUrl?.let { sourceUrl -> it[DanceMaterials.sourceUrl] = sourceUrl }
        } get DanceMaterials.id


        DanceMaterials
            .selectAll()
            .where { DanceMaterials.id eq id }
            .singleOrNull()
            ?.toEntity()
    }

    suspend fun findById(id: UUID): DanceMaterialEntity? = dbQuery {
        DanceMaterials
            .selectAll()
            .where { DanceMaterials.id eq id }
            .singleOrNull()
            ?.toEntity()
    }

    suspend fun update(id: UUID, request: UpdateMaterialRequest): DanceMaterialEntity? = dbQuery {
        DanceMaterials.update({ DanceMaterials.id eq id }) {
            request.name?.let { value -> it[name] = value }
            request.description?.let { value -> it[description] = value }
            request.tags?.let { value -> it[tags] = Json.encodeToString(value) }
            request.danceType?.let { value -> it[danceType] = value }
            request.danceStyle?.let { value -> it[danceStyle] = value }
            request.difficultyLevel?.let { value -> it[difficultyLevel] = value }
            request.sourceUrl?.let { value -> it[sourceUrl] = value }
            it[updatedAt] = Clock.System.now()
        }
        DanceMaterials.selectAll()
            .where { DanceMaterials.id eq id }
            .singleOrNull()
            ?.toEntity()
    }

    suspend fun delete(id: UUID) = dbQuery {
        DanceMaterials.deleteWhere { DanceMaterials.id eq id }
    }

    suspend fun findAll(page: Int, pageSize: Int): Pair<List<DanceMaterialEntity>, Long> = dbQuery {
        val total = DanceMaterials.selectAll().count()

        val items = DanceMaterials
            .selectAll()
            .orderBy(DanceMaterials.createdAt, SortOrder.DESC)
            .limit(pageSize)
            .offset((page * pageSize).toLong())
            .map { it.toEntity() }

        Pair(items, total)
    }

    suspend fun search(filters: SearchFilters): Pair<List<DanceMaterialEntity>, Long> = dbQuery {
        val query = DanceMaterials.selectAll()

        filters.query?.let { searchText ->
            query.andWhere { (DanceMaterials.name.lowerCase() like "%${searchText.lowercase()}%") or
            (DanceMaterials.description.lowerCase() like "%${searchText.lowercase()}%") or
            (DanceMaterials.author.lowerCase() like "%${searchText.lowercase()}%")
            }
        }

        filters.danceType?.let { danceType ->
            query.andWhere { DanceMaterials.danceType eq danceType }
        }

        filters.danceStyle?.let { danceStyle ->
            query.andWhere { DanceMaterials.danceStyle eq danceStyle }
        }

        filters.difficultyLevel?.let { difficultyLevel ->
            query.andWhere { DanceMaterials.difficultyLevel eq difficultyLevel }
        }

        filters.author?.let { author ->
            query.andWhere { DanceMaterials.author eq author }
        }

        filters.tags?.let { tags ->
            query.andWhere { DanceMaterials.tags like "%${tags}%" }
        }

        val total = query.count()

        val sortColumn = when (filters.sortBy) {
            SortField.NAME -> DanceMaterials.name
            SortField.CREATED_AT -> DanceMaterials.createdAt
            SortField.UPDATED_AT -> DanceMaterials.updatedAt
        }

        val sortOrder =  when (filters.sortDirection) {
            SortDirection.ASC -> SortOrder.ASC
            SortDirection.DESC -> SortOrder.DESC
        }

        val items = query
            .orderBy(sortColumn, sortOrder)
            .limit(filters.pageSize)
            .offset((filters.page * filters.pageSize ).toLong())
            .map { it.toEntity() }

        Pair(items, total)
    }


}