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

package com.gyugle.mitjul.server.feature.book.dto

import com.gyugle.mitjul.server.core.domain.error.ApiFailure
import com.gyugle.mitjul.server.core.domain.model.ReadingStatus
import java.time.Instant
import java.util.UUID
import kotlinx.serialization.Serializable

private const val TITLE_MAX = 200
private const val AUTHOR_MAX = 120
private const val REVIEW_MAX = 300
private const val PAGES_MAX = 20_000

@Serializable
data class BookRequest(
    val title: String,
    val author: String,
    val status: ReadingStatus,
    val totalPages: Int,
    val currentPage: Int = 0,
    val rating: Int? = null,
    val review: String? = null,
    val coverImageId: String? = null,
    val finishedAt: String? = null,
)

fun BookRequest.validated(): BookRequest {
    val trimmed = copy(title = title.trim(), author = author.trim(), review = review?.trim())
    val fields = buildMap {
        if (trimmed.title.isEmpty()) put("title", "blank")
        else if (trimmed.title.length > TITLE_MAX) put("title", "too_long")
        if (trimmed.author.isEmpty()) put("author", "blank")
        else if (trimmed.author.length > AUTHOR_MAX) put("author", "too_long")
        if (totalPages !in 1..PAGES_MAX) put("totalPages", "out_of_range")
        else if (currentPage !in 0..totalPages) put("currentPage", "out_of_range")
        if (rating != null && rating !in 1..5) put("rating", "out_of_range")
        if (trimmed.review != null && trimmed.review.length > REVIEW_MAX)
            put("review", "too_long")
        if (coverImageId != null && coverImageId.asUuid() == null)
            put("coverImageId", "malformed")
        if (status == ReadingStatus.DONE) {
            if (finishedAt == null) put("finishedAt", "blank")
            else if (finishedAt.asInstant() == null) put("finishedAt", "malformed")
        } else if (finishedAt != null) put("finishedAt", "not_allowed")
    }
    if (fields.isNotEmpty()) throw ApiFailure.Invalid(fields)
    return trimmed
}

fun String.asInstant(): Instant? = runCatching { Instant.parse(this) }.getOrNull()

fun String.asUuid(): UUID? = runCatching { UUID.fromString(this) }.getOrNull()
