/*
 * Copyright 2026 MinGyu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.gyugle.mitjul.server.feature.auth.repository

import com.gyugle.mitjul.server.core.database.Db
import com.gyugle.mitjul.server.core.domain.model.User
import com.gyugle.mitjul.server.feature.auth.mapper.toUser
import com.gyugle.mitjul.server.feature.auth.table.Users
import java.util.UUID
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.plus
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update

class UserRepository(private val db: Db) {

    suspend fun insertIfEmailFree(email: String, passwordHash: String): Boolean = db.query {
        val taken = !Users.selectAll().where { Users.email eq email }.limit(1).empty()
        if (taken) return@query false
        Users.insert {
            it[Users.email] = email
            it[Users.passwordHash] = passwordHash
        }
        true
    }

    suspend fun findByEmail(email: String): User? = db.query {
        Users.selectAll().where { Users.email eq email }.limit(1)
            .map { it.toUser() }
            .singleOrNull()
    }

    suspend fun tokenVersionOf(id: UUID): Int? = db.query { readVersion(id) }

    suspend fun bumpTokenVersion(id: UUID): Int = db.query {
        Users.update({ Users.id eq id }) { it[Users.tokenVersion] = Users.tokenVersion + 1 }
        readVersion(id) ?: 0
    }

    private fun readVersion(id: UUID): Int? =
        Users.selectAll().where { Users.id eq id }.limit(1)
            .map { it[Users.tokenVersion] }
            .singleOrNull()
}
