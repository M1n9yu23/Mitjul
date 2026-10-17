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

package com.gyugle.mitjul.server.feature.quote.repository

import com.gyugle.mitjul.server.core.database.Db
import com.gyugle.mitjul.server.core.domain.model.Quote
import com.gyugle.mitjul.server.feature.quote.dto.QuoteRequest
import com.gyugle.mitjul.server.feature.quote.mapper.putQuote
import com.gyugle.mitjul.server.feature.quote.mapper.toQuote
import com.gyugle.mitjul.server.feature.quote.table.Quotes
import java.time.Instant
import java.util.UUID
import org.jetbrains.exposed.v1.core.Op
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.less
import org.jetbrains.exposed.v1.core.or
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update

class QuoteRepository(private val db: Db) {

    suspend fun insertOnce(owner: UUID, id: UUID, book: UUID, form: QuoteRequest, at: Instant): Quote? = db.query {
        val stored = Quotes.selectAll().where { Quotes.id eq id }.limit(1).singleOrNull()
        when {
            stored == null -> {
                Quotes.insert {
                    it[Quotes.id] = id
                    it[ownerId] = owner
                    it[createdAt] = at
                    it.putQuote(form, book)
                }
                row(owner, id)?.toQuote()
            }
            stored[Quotes.ownerId] == owner -> stored.toQuote()
            else -> null
        }
    }

    suspend fun update(owner: UUID, id: UUID, book: UUID, form: QuoteRequest): Quote? = db.query {
        val changed = Quotes.update({ mine(owner, id) }) { it.putQuote(form, book) }
        if (changed == 0) null else row(owner, id)?.toQuote()
    }

    suspend fun delete(owner: UUID, id: UUID): Boolean = db.query {
        Quotes.deleteWhere { mine(owner, id) } > 0
    }

    suspend fun page(owner: UUID, book: UUID?, after: UUID?, limit: Int): List<Quote> = db.query {
        val anchor = after?.let { row(owner, it) }
        Quotes.selectAll()
            .where { owned(owner, book) and after(anchor) }
            .orderBy(Quotes.createdAt to SortOrder.DESC, Quotes.id to SortOrder.DESC)
            .limit(limit)
            .map { it.toQuote() }
    }

    private fun row(owner: UUID, id: UUID): ResultRow? =
        Quotes.selectAll().where { mine(owner, id) }.limit(1).singleOrNull()

    private fun mine(owner: UUID, id: UUID): Op<Boolean> =
        (Quotes.ownerId eq owner) and (Quotes.id eq id)

    private fun owned(owner: UUID, book: UUID?): Op<Boolean> {
        val op: Op<Boolean> = Quotes.ownerId eq owner
        return if (book == null) op else op and (Quotes.bookId eq book)
    }

    private fun after(anchor: ResultRow?): Op<Boolean> {
        if (anchor == null) return Op.TRUE
        return (Quotes.createdAt less anchor[Quotes.createdAt]) or
            ((Quotes.createdAt eq anchor[Quotes.createdAt]) and
                (Quotes.id less anchor[Quotes.id].value))
    }
}
