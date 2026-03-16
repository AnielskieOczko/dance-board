package com.danceboard.backend.config

import com.danceboard.shared.DanceStyle
import com.danceboard.shared.DanceType
import com.danceboard.shared.DifficultyLevel
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.Application
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.CurrentTimestamp
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp
import org.jetbrains.exposed.sql.transactions.transaction

import java.net.URI

fun Application.configureDatabases() {
    val hikariConfig = HikariConfig().apply {
        val rawUrl = environment.config.property("datasource.jdbcUrl").getString()
        jdbcUrl = if (rawUrl.startsWith("postgresql://") || rawUrl.startsWith("postgres://")) {
            val uri = URI(rawUrl.replaceFirst("postgres", "http")) // Use http as a placeholder scheme for parsing
            val host = uri.host
            val port = if (uri.port != -1) ":${uri.port}" else ""
            val path = uri.path
            val query = if (uri.query != null) "?${uri.query}" else ""
            "jdbc:postgresql://$host$port$path$query"
        } else {
            rawUrl
        }
        username = environment.config.property("datasource.username").getString()
        password = environment.config.property("datasource.password").getString()
        driverClassName = environment.config.property("datasource.driverClassName").getString()
        maximumPoolSize = environment.config.property("datasource.maxPoolSize").getString().toInt()
    }

    val dataSource = HikariDataSource(hikariConfig)
    Database.connect(dataSource)

    transaction {
        SchemaUtils.create(DanceMaterials, AppConfig)
    }
}

object DanceMaterials : Table("dance_materials") {
    val id = uuid("id").autoGenerate()
    val name = varchar("name", 255)
    val description = text("description").nullable()
    val tags = text("tags").default("[]")  // JSON array stored as text
    val danceType = enumerationByName<DanceType>("dance_type", 20)
    val danceStyle = enumerationByName<DanceStyle>("dance_style", 30)
    val difficultyLevel = enumerationByName<DifficultyLevel>("difficulty_level", 20)
    val sourceUrl = varchar("source_url", 2048).nullable()
    val driveFileId = varchar("drive_file_id", 255).nullable()
    val driveViewUrl = varchar("drive_view_url", 2048).nullable()
    val author = varchar("author", 100)
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)
    val updatedAt = timestamp("updated_at").defaultExpression(CurrentTimestamp)

    override val primaryKey = PrimaryKey(id)
}

object AppConfig : Table("app_config") {
    val key = varchar("key", 100)
    val value = varchar("value", 500)

    override val primaryKey = PrimaryKey(key)
}
