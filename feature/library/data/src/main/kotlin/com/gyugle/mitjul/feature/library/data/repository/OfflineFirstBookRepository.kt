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

package com.gyugle.mitjul.feature.library.data.repository

import com.gyugle.mitjul.core.domain.DataError
import com.gyugle.mitjul.core.domain.EmptyResult
import com.gyugle.mitjul.core.domain.Result
import com.gyugle.mitjul.core.domain.map
import com.gyugle.mitjul.feature.library.data.datasource.BooksLocalDataSource
import com.gyugle.mitjul.feature.library.data.datasource.BooksRemoteDataSource
import com.gyugle.mitjul.feature.library.data.datasource.CoversLocalDataSource
import com.gyugle.mitjul.feature.library.data.mapper.toBook
import com.gyugle.mitjul.feature.library.data.mapper.toBookRequestDto
import com.gyugle.mitjul.feature.library.domain.model.Book
import com.gyugle.mitjul.feature.library.domain.model.BookSort
import com.gyugle.mitjul.feature.library.domain.model.NewBook
import com.gyugle.mitjul.feature.library.domain.model.ReadingStatus
import com.gyugle.mitjul.feature.library.domain.repository.BookRepository
import kotlin.time.Clock
import kotlinx.coroutines.flow.Flow

private const val PAGE_SIZE = 100
internal const val COVER_MAX_BYTES = 2 * 1024 * 1024

class OfflineFirstBookRepository(
    private val local: BooksLocalDataSource,
    private val remote: BooksRemoteDataSource,
    private val covers: CoversLocalDataSource,
) : BookRepository {

    override fun observeBooks(status: ReadingStatus?, sort: BookSort): Flow<List<Book>> =
        local.observeBooks(status, sort)

    override fun observeBook(id: String): Flow<Book?> = local.observeBook(id)

    override suspend fun refresh(): EmptyResult<DataError> {
        val books = mutableListOf<Book>()
        var after: String? = null
        while (true) {
            val page = when (val result = remote.getBooks(after, PAGE_SIZE)) {
                is Result.Success -> result.data
                is Result.Error -> return result
            }
            if (page.isEmpty()) break
            books += page.map { it.toBook() }
            after = page.last().id
        }
        return local.replaceAll(books)
    }

    override suspend fun clearLocalCopy(): EmptyResult<DataError.Local> = local.replaceAll(emptyList())

    override suspend fun addBook(book: NewBook): EmptyResult<DataError.Network> {
        val created = when (val result = remote.postBook(book.toBookRequestDto(Clock.System.now()))) {
            is Result.Success -> result.data
            is Result.Error -> return result
        }
        local.upsert(created.toBook())
        return Result.Success(Unit)
    }

    override suspend fun updateBook(book: Book): EmptyResult<DataError.Network> {
        val saved = when (val result = remote.putBook(book.id, book.toBookRequestDto())) {
            is Result.Success -> result.data
            is Result.Error -> return result
        }
        local.upsert(saved.toBook())
        return Result.Success(Unit)
    }

    override suspend fun prepareCover(uri: String): Result<String, DataError.Local> = covers.prepare(uri)

    override suspend fun uploadCover(path: String, onProgress: (Float) -> Unit): Result<String, DataError> {
        val bytes = when (val result = covers.read(path)) {
            is Result.Success -> result.data
            is Result.Error -> return result
        }
        if (bytes.size > COVER_MAX_BYTES) return Result.Error(DataError.Network.PAYLOAD_TOO_LARGE)
        return remote.postImage(bytes, onProgress).map { it.id }
    }

    override fun coverUrl(imageId: String): String = remote.coverUrl(imageId)
}
