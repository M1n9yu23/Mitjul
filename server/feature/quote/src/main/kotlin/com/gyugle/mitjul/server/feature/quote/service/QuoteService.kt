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

package com.gyugle.mitjul.server.feature.quote.service

import com.gyugle.mitjul.server.core.domain.error.ApiFailure
import com.gyugle.mitjul.server.feature.quote.dto.QuoteRequest
import com.gyugle.mitjul.server.feature.quote.dto.QuoteResponse
import com.gyugle.mitjul.server.feature.quote.dto.validated
import com.gyugle.mitjul.server.feature.quote.mapper.toResponse
import com.gyugle.mitjul.server.feature.quote.repository.QuoteRepository
import java.time.Instant
import java.util.UUID

const val DEFAULT_LIMIT = 50
private const val MAX_LIMIT = 200

class QuoteService(private val quotes: QuoteRepository) {

    suspend fun add(owner: UUID, request: QuoteRequest): QuoteResponse {
        val form = request.validated()
        val id = form.id?.asId("id") ?: UUID.randomUUID()
        val createdAt = form.createdAt?.asTime("createdAt") ?: Instant.now()
        val quote = quotes.insertOnce(owner, id, form.bookId.asId("bookId"), form, createdAt)
        return quote?.toResponse() ?: throw ApiFailure.Duplicate("id")
    }

    suspend fun replace(owner: UUID, id: String, request: QuoteRequest): QuoteResponse {
        val form = request.validated()
        return quotes.update(owner, id.asId("id"), form.bookId.asId("bookId"), form)?.toResponse()
            ?: throw ApiFailure.NotFound()
    }

    suspend fun remove(owner: UUID, id: String) {
        if (!quotes.delete(owner, id.asId("id"))) throw ApiFailure.NotFound()
    }

    suspend fun list(owner: UUID, book: String?, after: String?, limit: Int): List<QuoteResponse> =
        quotes.page(
            owner = owner,
            book = book?.asId("bookId"),
            after = after?.asId("after"),
            limit = limit.coerceIn(1, MAX_LIMIT),
        ).map { it.toResponse() }
}

private fun String.asId(field: String): UUID =
    runCatching { UUID.fromString(this) }.getOrNull()
        ?: throw ApiFailure.Invalid(mapOf(field to "malformed"))

private fun String.asTime(field: String): Instant =
    runCatching { Instant.parse(this) }.getOrNull()
        ?: throw ApiFailure.Invalid(mapOf(field to "malformed"))
