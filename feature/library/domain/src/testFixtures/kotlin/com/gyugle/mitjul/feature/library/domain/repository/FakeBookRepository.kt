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

package com.gyugle.mitjul.feature.library.domain.repository

import com.gyugle.mitjul.core.domain.DataError
import com.gyugle.mitjul.core.domain.EmptyResult
import com.gyugle.mitjul.core.domain.Result
import com.gyugle.mitjul.feature.library.domain.model.Book
import com.gyugle.mitjul.feature.library.domain.model.BookSort
import com.gyugle.mitjul.feature.library.domain.model.NewBook
import com.gyugle.mitjul.feature.library.domain.model.ReadingStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeBookRepository(
    private val books: List<Book> = emptyList(),
    private val refreshResult: EmptyResult<DataError> = Result.Success(Unit),
    private val addBookResult: EmptyResult<DataError.Network> = Result.Success(Unit),
    private val uploadCoverResult: Result<String, DataError> = Result.Success("cover-1"),
) : BookRepository {

    var addedBooks: List<NewBook> = emptyList()
        private set

    var uploadedCovers: List<String> = emptyList()
        private set

    var updatedBooks: List<Book> = emptyList()
        private set

    var clearCalls = 0
        private set

    override fun observeBooks(status: ReadingStatus?, sort: BookSort): Flow<List<Book>> =
        flowOf(books.filter { status == null || it.status == status })

    override fun observeBook(id: String): Flow<Book?> = flowOf(books.firstOrNull { it.id == id })

    override suspend fun refresh(): EmptyResult<DataError> = refreshResult

    override suspend fun clearLocalCopy(): EmptyResult<DataError.Local> {
        clearCalls++
        return Result.Success(Unit)
    }

    override suspend fun addBook(book: NewBook): EmptyResult<DataError.Network> {
        addedBooks = addedBooks + book
        return addBookResult
    }

    override suspend fun updateBook(book: Book): EmptyResult<DataError.Network> {
        updatedBooks = updatedBooks + book
        return Result.Success(Unit)
    }

    override suspend fun prepareCover(uri: String): Result<String, DataError.Local> = Result.Success("$uri.jpg")

    override suspend fun uploadCover(path: String, onProgress: (Float) -> Unit): Result<String, DataError> {
        uploadedCovers = uploadedCovers + path
        return uploadCoverResult
    }

    override fun coverUrl(imageId: String): String = "https://test.invalid/api/v1/images/$imageId"
}
