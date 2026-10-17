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

package com.gyugle.mitjul.server.feature.quote.table

import com.gyugle.mitjul.server.core.database.Db
import org.jetbrains.exposed.v1.core.dao.id.java.UUIDTable
import org.jetbrains.exposed.v1.core.java.javaUUID
import org.jetbrains.exposed.v1.javatime.timestamp
import org.jetbrains.exposed.v1.jdbc.SchemaUtils

object Quotes : UUIDTable("quotes") {
    val ownerId = javaUUID("owner_id")
    val bookId = javaUUID("book_id")
    val text = varchar("text", 1000)
    val page = integer("page").nullable()
    val memo = varchar("memo", 300).nullable()
    val createdAt = timestamp("created_at")

    init {
        index(false, ownerId, createdAt)
        index(false, ownerId, bookId, createdAt)
    }
}

suspend fun Db.createQuotesTable() = query { SchemaUtils.create(Quotes) }
