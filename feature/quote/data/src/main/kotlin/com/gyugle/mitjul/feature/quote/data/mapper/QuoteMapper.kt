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

package com.gyugle.mitjul.feature.quote.data.mapper

import com.gyugle.mitjul.core.database.entity.QuoteEntity
import com.gyugle.mitjul.feature.quote.data.dto.QuoteDto
import com.gyugle.mitjul.feature.quote.data.dto.QuoteRequestDto
import com.gyugle.mitjul.feature.quote.domain.model.NewQuote
import com.gyugle.mitjul.feature.quote.domain.model.Quote
import kotlin.time.Instant

fun Quote.toQuoteEntity(): QuoteEntity = QuoteEntity(
    id = id,
    bookId = bookId,
    text = text,
    page = page,
    memo = memo,
    createdAt = createdAt.toEpochMilliseconds(),
    isPending = isPending,
)

fun QuoteEntity.toQuote(): Quote = Quote(
    id = id,
    bookId = bookId,
    text = text,
    page = page,
    memo = memo,
    createdAt = Instant.fromEpochMilliseconds(createdAt),
    isPending = isPending,
)

fun QuoteDto.toQuote(): Quote = Quote(
    id = id,
    bookId = bookId,
    text = text,
    page = page,
    memo = memo,
    createdAt = createdAt,
    isPending = false,
)

fun NewQuote.toPendingQuote(id: String, createdAt: Instant): Quote = Quote(
    id = id,
    bookId = bookId,
    text = text,
    page = page,
    memo = memo,
    createdAt = createdAt,
    isPending = true,
)

fun Quote.toQuoteRequestDto(): QuoteRequestDto = QuoteRequestDto(
    id = id,
    bookId = bookId,
    text = text,
    page = page,
    memo = memo,
    createdAt = createdAt,
)
