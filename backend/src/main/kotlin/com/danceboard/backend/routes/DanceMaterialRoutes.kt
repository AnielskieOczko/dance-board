package com.danceboard.backend.routes

import com.danceboard.backend.service.DanceMaterialService
import com.danceboard.shared.DanceStyle
import com.danceboard.shared.DanceType
import com.danceboard.shared.DifficultyLevel
import com.danceboard.shared.dto.*
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.server.request.receive
import io.ktor.server.request.receiveMultipart
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import io.ktor.utils.io.toByteArray
import java.io.InputStream
import java.util.UUID

fun Route.danceMaterialRoutes(service: DanceMaterialService) {
    route("/api/v1/materials") {
        get("/upload-config") {
            val response = service.getUploadConfig()
            call.respond(HttpStatusCode.OK, response)
        }

        get("/search") {
            val filters =
                    SearchFilters(
                            query = call.request.queryParameters["query"],
                            danceType =
                                    call.request.queryParameters["danceType"]?.let {
                                        DanceType.valueOf(it)
                                    },
                            danceStyle =
                                    call.request.queryParameters["danceStyle"]?.let {
                                        DanceStyle.valueOf(it)
                                    },
                            difficultyLevel =
                                    call.request.queryParameters["difficultyLevel"]?.let {
                                        DifficultyLevel.valueOf(it)
                                    },
                            tags =
                                    call.request.queryParameters["tags"]?.split(",")?.map { tag ->
                                        tag.trim()
                                    },
                            author = call.request.queryParameters["author"],
                            sortBy =
                                    call.request.queryParameters["sortBy"]?.let {
                                        SortField.valueOf(it)
                                    }
                                            ?: SortField.CREATED_AT,
                            sortDirection =
                                    call.request.queryParameters["sortDirection"]?.let {
                                        SortDirection.valueOf(it)
                                    }
                                            ?: SortDirection.DESC,
                            page = call.request.queryParameters["page"]?.toIntOrNull() ?: 0,
                            pageSize = call.request.queryParameters["pageSize"]?.toIntOrNull()
                                            ?: 20,
                    )
            val results = service.search(filters = filters)
            call.respond(HttpStatusCode.OK, results)
        }

        get("/{id}") {
            val id = UUID.fromString(call.parameters["id"])
            val material = service.getById(id)
            call.respond(HttpStatusCode.OK, material)
        }

        post {
            val request = call.receive<CreateMaterialRequest>()
            val created = service.create(request)
            call.respond(HttpStatusCode.Created, created)
        }

        put("/{id}") {
            val id = UUID.fromString(call.parameters["id"])
            val request = call.receive<UpdateMaterialRequest>()
            val updated = service.update(id, request)
            call.respond(HttpStatusCode.OK, updated)
        }

        delete("/{id}") {
            val id = UUID.fromString(call.parameters["id"])
            service.delete(id)
            call.respond(HttpStatusCode.NoContent)
        }

        post("/{id}/video") {
            val id = UUID.fromString(call.parameters["id"])
            // Zwiększamy limit dla uploadu wideo do 500MB (Ktor 3 domyślnie ma 50MB)
            val multipart = call.receiveMultipart(formFieldLimit = 500 * 1024 * 1024)
            var fileName: String? = null
            var inputStream: InputStream? = null
            var mimeType = "video/mp4"

            multipart.forEachPart { part ->
                if (part is PartData.FileItem) {
                    fileName = part.originalFileName ?: "video.mp4"
                    mimeType = part.contentType?.toString() ?: "video/mp4"
                    val bytes = part.provider().toByteArray()
                    inputStream = bytes.inputStream()
                }
                part.dispose()
            }

            // respond jest PO forEachPart — wszystkie części już odczytane
            val stream = inputStream
            if (stream == null) {
                call.respond(HttpStatusCode.BadRequest, "No file uploaded")
            } else {
                val updated = service.uploadVideo(id, fileName ?: "video.mp4", stream, mimeType)
                call.respond(HttpStatusCode.OK, updated)
            }
        }

        delete("/{id}/video") {
            val id = UUID.fromString(call.parameters["id"])
            val updated = service.deleteVideo(id)
            call.respond(HttpStatusCode.OK, updated)
        }

        post("/upload-session") {
            val request = call.receive<UploadSessionRequest>()
            val response = service.createUploadSession(request)
            call.respond(HttpStatusCode.OK, response)
        }

        post("/{id}/video-id") {
            val id = UUID.fromString(call.parameters["id"])
            val request = call.receive<DriveFileInfo>()
            val updated = service.attachVideoToMaterial(id, request.fileId)
            call.respond(HttpStatusCode.OK, updated)
        }


    }
}
