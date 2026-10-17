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

package com.gyugle.mitjul.server.feature.auth.table

import com.gyugle.mitjul.server.core.database.Db
import org.jetbrains.exposed.v1.core.dao.id.java.UUIDTable
import org.jetbrains.exposed.v1.jdbc.SchemaUtils

object Users : UUIDTable("users") {
    val email = varchar("email", 254).uniqueIndex()
    val passwordHash = varchar("password_hash", 120)
    val tokenVersion = integer("token_version").default(0)
}

suspend fun Db.createUsersTable() = query { SchemaUtils.create(Users) }
