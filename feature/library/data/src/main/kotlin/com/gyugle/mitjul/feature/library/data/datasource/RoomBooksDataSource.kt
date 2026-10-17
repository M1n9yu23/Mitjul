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

package com.gyugle.mitjul.feature.library.data.datasource

import android.database.sqlite.SQLiteFullException
import co.touchlab.kermit.Logger
import com.gyugle.mitjul.core.database.dao.BookDao
import com.gyugle.mitjul.core.domain.DataError
import com.gyugle.mitjul.core.domain.EmptyResult
import com.gyugle.mitjul.core.domain.Result
import com.gyugle.mitjul.feature.library.data.mapper.toBook
import com.gyugle.mitjul.feature.library.data.mapper.toBookEntity
import com.gyugle.mitjul.feature.library.domain.model.Book
import com.gyugle.mitjul.feature.library.domain.model.BookSort
import com.gyugle.mitjul.feature.library.domain.model.ReadingStatus
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomBooksDataSource(private val dao: BookDao) : BooksLocalDataSource {

    override fun observeBooks(status: ReadingStatus?, sort: BookSort): Flow<List<Book>> {
        val rows = when (sort) {
            BookSort.RECENT -> dao.observeRecent(status?.name)
            BookSort.TITLE -> dao.observeByTitle(status?.name)
        }
        return rows.map { entities -> entities.map { it.toBook() } }
    }

    override fun observeBook(id: String): Flow<Book?> = dao.observeById(id).map { it?.toBook() }

    override suspend fun replaceAll(books: List<Book>): EmptyResult<DataError.Local> =
        write { dao.replaceAll(books.map { it.toBookEntity() }) }

    override suspend fun upsert(book: Book): EmptyResult<DataError.Local> =
        write { dao.upsertAll(listOf(book.toBookEntity())) }

    private suspend fun write(block: suspend () -> Unit): EmptyResult<DataError.Local> = try {
        block()
        Result.Success(Unit)
    } catch (_: SQLiteFullException) {
        Result.Error(DataError.Local.DISK_FULL)
    } catch (e: Exception) {
        currentCoroutineContext().ensureActive()
        Logger.e { "책을 로컬 DB에 저장하지 못했다: ${e::class.simpleName}" }
        Result.Error(DataError.Local.UNKNOWN)
    }
}
