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

package com.gyugle.mitjul.server.feature.book.table

import com.gyugle.mitjul.server.core.database.Db
import com.gyugle.mitjul.server.core.domain.model.ReadingStatus
import org.jetbrains.exposed.v1.core.dao.id.java.UUIDTable
import org.jetbrains.exposed.v1.core.java.javaUUID
import org.jetbrains.exposed.v1.javatime.timestamp
import org.jetbrains.exposed.v1.jdbc.SchemaUtils

object Books : UUIDTable("books") {
    val ownerId = javaUUID("owner_id")
    val title = varchar("title", 200)
    val author = varchar("author", 120)
    val status = enumerationByName("status", 16, ReadingStatus::class)
    val totalPages = integer("total_pages")
    val currentPage = integer("current_page")
    val rating = integer("rating").nullable()
    val review = varchar("review", 300).nullable()
    val coverImageId = javaUUID("cover_image_id").nullable()
    val createdAt = timestamp("created_at")
    val finishedAt = timestamp("finished_at").nullable()

    init {
        index(false, ownerId, createdAt)
        index(false, ownerId, title)
    }
}

suspend fun Db.createBooksTable() = query { SchemaUtils.create(Books) }
