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

package com.gyugle.mitjul.server.feature.quote.mapper

import com.gyugle.mitjul.server.core.domain.model.Quote
import com.gyugle.mitjul.server.feature.quote.dto.QuoteRequest
import com.gyugle.mitjul.server.feature.quote.dto.QuoteResponse
import com.gyugle.mitjul.server.feature.quote.table.Quotes
import java.util.UUID
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.statements.UpdateBuilder

fun ResultRow.toQuote(): Quote = Quote(
    id = this[Quotes.id].value,
    bookId = this[Quotes.bookId],
    text = this[Quotes.text],
    page = this[Quotes.page],
    memo = this[Quotes.memo],
    createdAt = this[Quotes.createdAt],
)

fun Quote.toResponse(): QuoteResponse = QuoteResponse(
    id = id.toString(),
    bookId = bookId.toString(),
    text = text,
    page = page,
    memo = memo,
    createdAt = createdAt.toString(),
)

fun UpdateBuilder<*>.putQuote(form: QuoteRequest, book: UUID) {
    this[Quotes.bookId] = book
    this[Quotes.text] = form.text
    this[Quotes.page] = form.page
    this[Quotes.memo] = form.memo
}
