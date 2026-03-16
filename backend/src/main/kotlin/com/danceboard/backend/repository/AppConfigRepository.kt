package com.danceboard.backend.repository

import com.danceboard.backend.config.AppConfig
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.update

class AppConfigRepository {

    private suspend fun <T> dbQuery(block: suspend () -> T): T = newSuspendedTransaction { block() }

    suspend fun get(key: String): String? = dbQuery {
        AppConfig.selectAll()
        .where { AppConfig.key eq key }
        .firstOrNull()
            ?.get(AppConfig.value)
    }

    suspend fun set(key: String, value: String): String? = dbQuery {

        val existing = AppConfig.selectAll()
            .where { AppConfig.key eq key }
            .firstOrNull()

        if (existing != null) {
            AppConfig.update({ AppConfig.key eq key }) {
                it[AppConfig.value] = value
            }
        } else {
            AppConfig.insert {
                it[AppConfig.key] = key
                it[AppConfig.value] = value
            }
        }
        value
    }



}