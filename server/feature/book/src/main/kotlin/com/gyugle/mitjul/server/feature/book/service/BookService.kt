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

package com.gyugle.mitjul.server.feature.book.service

import com.gyugle.mitjul.server.core.domain.error.ApiFailure
import com.gyugle.mitjul.server.core.domain.model.BookSort
import com.gyugle.mitjul.server.core.domain.model.ReadingStatus
import com.gyugle.mitjul.server.feature.book.dto.BookRequest
import com.gyugle.mitjul.server.feature.book.dto.BookResponse
import com.gyugle.mitjul.server.feature.book.dto.validated
import com.gyugle.mitjul.server.feature.book.mapper.toResponse
import com.gyugle.mitjul.server.feature.book.repository.BookRepository
import java.util.UUID

const val DEFAULT_LIMIT = 20
private const val MAX_LIMIT = 100

class BookService(private val books: BookRepository) {

    suspend fun add(owner: UUID, request: BookRequest): BookResponse =
        books.insert(owner, request.validated()).toResponse()

    suspend fun get(owner: UUID, id: String): BookResponse =
        books.find(owner, id.asId("id"))?.toResponse() ?: throw ApiFailure.NotFound()

    suspend fun replace(owner: UUID, id: String, request: BookRequest): BookResponse =
        books.update(owner, id.asId("id"), request.validated())?.toResponse()
            ?: throw ApiFailure.NotFound()

    suspend fun remove(owner: UUID, id: String) {
        if (!books.delete(owner, id.asId("id"))) throw ApiFailure.NotFound()
    }

    suspend fun list(
        owner: UUID,
        status: ReadingStatus?,
        keyword: String?,
        sort: BookSort?,
        after: String?,
        limit: Int,
    ): List<BookResponse> = books.page(
        owner = owner,
        status = status,
        keyword = keyword,
        sort = sort ?: BookSort.RECENT,
        after = after?.asId("after"),
        limit = limit.coerceIn(1, MAX_LIMIT),
    ).map { it.toResponse() }
}

private fun String.asId(field: String): UUID =
    runCatching { UUID.fromString(this) }.getOrNull()
        ?: throw ApiFailure.Invalid(mapOf(field to "malformed"))
