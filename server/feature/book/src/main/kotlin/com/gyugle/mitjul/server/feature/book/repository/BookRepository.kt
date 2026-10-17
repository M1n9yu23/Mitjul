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

package com.gyugle.mitjul.server.feature.book.repository

import com.gyugle.mitjul.server.core.database.Db
import com.gyugle.mitjul.server.core.domain.model.Book
import com.gyugle.mitjul.server.core.domain.model.BookSort
import com.gyugle.mitjul.server.core.domain.model.ReadingStatus
import com.gyugle.mitjul.server.feature.book.dto.BookRequest
import com.gyugle.mitjul.server.feature.book.mapper.putBook
import com.gyugle.mitjul.server.feature.book.mapper.toBook
import com.gyugle.mitjul.server.feature.book.table.Books
import java.time.Instant
import java.util.UUID
import org.jetbrains.exposed.v1.core.Expression
import org.jetbrains.exposed.v1.core.LikePattern
import org.jetbrains.exposed.v1.core.Op
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.greater
import org.jetbrains.exposed.v1.core.less
import org.jetbrains.exposed.v1.core.like
import org.jetbrains.exposed.v1.core.lowerCase
import org.jetbrains.exposed.v1.core.or
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insertAndGetId
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update

class BookRepository(private val db: Db) {

    suspend fun insert(owner: UUID, form: BookRequest): Book = db.query {
        val id = Books.insertAndGetId {
            it[ownerId] = owner
            it[createdAt] = Instant.now()
            it.putBook(form)
        }
        row(owner, id.value)!!.toBook()
    }

    suspend fun find(owner: UUID, id: UUID): Book? = db.query { row(owner, id)?.toBook() }

    suspend fun update(owner: UUID, id: UUID, form: BookRequest): Book? = db.query {
        val changed = Books.update({ mine(owner, id) }) { it.putBook(form) }
        if (changed == 0) null else row(owner, id)?.toBook()
    }

    suspend fun delete(owner: UUID, id: UUID): Boolean = db.query {
        Books.deleteWhere { mine(owner, id) } > 0
    }

    suspend fun page(
        owner: UUID,
        status: ReadingStatus?,
        keyword: String?,
        sort: BookSort,
        after: UUID?,
        limit: Int,
    ): List<Book> = db.query {
        val anchor = after?.let { row(owner, it) }
        Books.selectAll()
            .where { shelf(owner, status, keyword) and sort.after(anchor) }
            .orderBy(*sort.order())
            .limit(limit)
            .map { it.toBook() }
    }

    private fun row(owner: UUID, id: UUID): ResultRow? =
        Books.selectAll().where { mine(owner, id) }.limit(1).singleOrNull()

    private fun mine(owner: UUID, id: UUID): Op<Boolean> =
        (Books.ownerId eq owner) and (Books.id eq id)

    private fun shelf(owner: UUID, status: ReadingStatus?, keyword: String?): Op<Boolean> {
        var op: Op<Boolean> = Books.ownerId eq owner
        if (status != null) op = op and (Books.status eq status)
        val word = keyword?.trim()?.lowercase()
        if (!word.isNullOrEmpty()) {
            val pattern = LikePattern("%", '!') + LikePattern.ofLiteral(word, '!') + "%"
            op = op and ((Books.title.lowerCase() like pattern) or
                (Books.author.lowerCase() like pattern))
        }
        return op
    }

    private fun BookSort.after(anchor: ResultRow?): Op<Boolean> {
        if (anchor == null) return Op.TRUE
        val id = anchor[Books.id].value
        return when (this) {
            BookSort.RECENT -> (Books.createdAt less anchor[Books.createdAt]) or
                ((Books.createdAt eq anchor[Books.createdAt]) and (Books.id less id))
            BookSort.TITLE -> (Books.title greater anchor[Books.title]) or
                ((Books.title eq anchor[Books.title]) and (Books.id greater id))
        }
    }

    private fun BookSort.order(): Array<Pair<Expression<*>, SortOrder>> = when (this) {
        BookSort.RECENT -> arrayOf(Books.createdAt to SortOrder.DESC, Books.id to SortOrder.DESC)
        BookSort.TITLE -> arrayOf(Books.title to SortOrder.ASC, Books.id to SortOrder.ASC)
    }
}
