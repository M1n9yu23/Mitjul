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

package com.gyugle.mitjul.feature.library.data.mapper

import com.gyugle.mitjul.core.database.entity.BookEntity
import com.gyugle.mitjul.feature.library.data.dto.BookDto
import com.gyugle.mitjul.feature.library.data.dto.BookRequestDto
import com.gyugle.mitjul.feature.library.domain.model.Book
import com.gyugle.mitjul.feature.library.domain.model.NewBook
import com.gyugle.mitjul.feature.library.domain.model.ReadingStatus
import kotlin.time.Instant

fun Book.toBookEntity(): BookEntity = BookEntity(
    id = id,
    title = title,
    author = author,
    status = status.name,
    totalPages = totalPages,
    currentPage = currentPage,
    rating = rating,
    review = review,
    coverImageId = coverImageId,
    createdAt = createdAt.toEpochMilliseconds(),
    finishedAt = finishedAt?.toEpochMilliseconds(),
)

fun BookEntity.toBook(): Book = Book(
    id = id,
    title = title,
    author = author,
    status = ReadingStatus.valueOf(status),
    totalPages = totalPages,
    currentPage = currentPage,
    rating = rating,
    review = review,
    coverImageId = coverImageId,
    createdAt = Instant.fromEpochMilliseconds(createdAt),
    finishedAt = finishedAt?.let { Instant.fromEpochMilliseconds(it) },
)

fun BookDto.toBook(): Book = Book(
    id = id,
    title = title,
    author = author,
    status = status,
    totalPages = totalPages,
    currentPage = currentPage,
    rating = rating,
    review = review,
    coverImageId = coverImageId,
    createdAt = createdAt,
    finishedAt = finishedAt,
)

fun NewBook.toBookRequestDto(now: Instant): BookRequestDto {
    val isDone = status == ReadingStatus.DONE
    return BookRequestDto(
        title = title,
        author = author,
        status = status,
        totalPages = totalPages,
        currentPage = if (isDone) totalPages else 0,
        rating = null,
        review = null,
        coverImageId = coverImageId,
        finishedAt = if (isDone) now else null,
    )
}

fun Book.toBookRequestDto(): BookRequestDto = BookRequestDto(
    title = title,
    author = author,
    status = status,
    totalPages = totalPages,
    currentPage = currentPage,
    rating = rating,
    review = review,
    coverImageId = coverImageId,
    finishedAt = finishedAt,
)
