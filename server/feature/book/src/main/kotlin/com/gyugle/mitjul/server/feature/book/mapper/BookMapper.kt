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

package com.gyugle.mitjul.server.feature.book.mapper

import com.gyugle.mitjul.server.core.domain.model.Book
import com.gyugle.mitjul.server.feature.book.dto.BookRequest
import com.gyugle.mitjul.server.feature.book.dto.BookResponse
import com.gyugle.mitjul.server.feature.book.dto.asInstant
import com.gyugle.mitjul.server.feature.book.dto.asUuid
import com.gyugle.mitjul.server.feature.book.table.Books
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.statements.UpdateBuilder

fun ResultRow.toBook(): Book = Book(
    id = this[Books.id].value,
    title = this[Books.title],
    author = this[Books.author],
    status = this[Books.status],
    totalPages = this[Books.totalPages],
    currentPage = this[Books.currentPage],
    rating = this[Books.rating],
    review = this[Books.review],
    coverImageId = this[Books.coverImageId],
    createdAt = this[Books.createdAt],
    finishedAt = this[Books.finishedAt],
)

fun Book.toResponse(): BookResponse = BookResponse(
    id = id.toString(),
    title = title,
    author = author,
    status = status,
    totalPages = totalPages,
    currentPage = currentPage,
    rating = rating,
    review = review,
    coverImageId = coverImageId?.toString(),
    createdAt = createdAt.toString(),
    finishedAt = finishedAt?.toString(),
)

fun UpdateBuilder<*>.putBook(form: BookRequest) {
    this[Books.title] = form.title
    this[Books.author] = form.author
    this[Books.status] = form.status
    this[Books.totalPages] = form.totalPages
    this[Books.currentPage] = form.currentPage
    this[Books.rating] = form.rating
    this[Books.review] = form.review
    this[Books.coverImageId] = form.coverImageId?.asUuid()
    this[Books.finishedAt] = form.finishedAt?.asInstant()
}
