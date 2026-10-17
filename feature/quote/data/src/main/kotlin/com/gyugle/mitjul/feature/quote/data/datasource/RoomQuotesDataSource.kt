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

package com.gyugle.mitjul.feature.quote.data.datasource

import android.database.sqlite.SQLiteFullException
import co.touchlab.kermit.Logger
import com.gyugle.mitjul.core.database.dao.QuoteDao
import com.gyugle.mitjul.core.domain.DataError
import com.gyugle.mitjul.core.domain.EmptyResult
import com.gyugle.mitjul.core.domain.Result
import com.gyugle.mitjul.feature.quote.data.mapper.toQuote
import com.gyugle.mitjul.feature.quote.data.mapper.toQuoteEntity
import com.gyugle.mitjul.feature.quote.domain.model.Quote
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomQuotesDataSource(private val dao: QuoteDao) : QuotesLocalDataSource {

    override fun observeQuotes(bookId: String): Flow<List<Quote>> =
        dao.observeByBook(bookId).map { entities -> entities.map { it.toQuote() } }

    override fun observeQuote(id: String): Flow<Quote?> = dao.observeById(id).map { it?.toQuote() }

    override suspend fun pending(): Result<List<Quote>, DataError.Local> =
        access { dao.pending().map { it.toQuote() } }

    override suspend fun replaceSent(quotes: List<Quote>): EmptyResult<DataError.Local> =
        access { dao.replaceSent(quotes.map { it.toQuoteEntity() }) }

    override suspend fun upsert(quote: Quote): EmptyResult<DataError.Local> =
        access { dao.upsertAll(listOf(quote.toQuoteEntity())) }

    override suspend fun clear(): EmptyResult<DataError.Local> = access { dao.deleteAll() }

    private suspend fun <T> access(block: suspend () -> T): Result<T, DataError.Local> = try {
        Result.Success(block())
    } catch (_: SQLiteFullException) {
        Result.Error(DataError.Local.DISK_FULL)
    } catch (e: Exception) {
        currentCoroutineContext().ensureActive()
        Logger.e { "문장을 로컬 DB에서 읽거나 저장하지 못했다: ${e::class.simpleName}" }
        Result.Error(DataError.Local.UNKNOWN)
    }
}
