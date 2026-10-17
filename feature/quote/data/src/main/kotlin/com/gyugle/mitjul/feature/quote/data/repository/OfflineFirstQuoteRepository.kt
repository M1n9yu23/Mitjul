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

package com.gyugle.mitjul.feature.quote.data.repository

import com.gyugle.mitjul.core.domain.DataError
import com.gyugle.mitjul.core.domain.EmptyResult
import com.gyugle.mitjul.core.domain.Result
import com.gyugle.mitjul.core.domain.onSuccess
import com.gyugle.mitjul.feature.quote.data.datasource.QuotesLocalDataSource
import com.gyugle.mitjul.feature.quote.data.datasource.QuotesRemoteDataSource
import com.gyugle.mitjul.feature.quote.data.mapper.toPendingQuote
import com.gyugle.mitjul.feature.quote.data.mapper.toQuote
import com.gyugle.mitjul.feature.quote.data.mapper.toQuoteRequestDto
import com.gyugle.mitjul.feature.quote.domain.QuoteSyncScheduler
import com.gyugle.mitjul.feature.quote.domain.model.NewQuote
import com.gyugle.mitjul.feature.quote.domain.model.Quote
import com.gyugle.mitjul.feature.quote.domain.repository.QuoteRepository
import java.util.UUID
import kotlin.time.Clock
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

private const val PAGE_SIZE = 200

class OfflineFirstQuoteRepository(
    private val local: QuotesLocalDataSource,
    private val remote: QuotesRemoteDataSource,
    private val scheduler: QuoteSyncScheduler,
) : QuoteRepository {

    private val copyLock = Mutex()

    override fun observeQuotes(bookId: String): Flow<List<Quote>> = local.observeQuotes(bookId)

    override fun observeQuote(id: String): Flow<Quote?> = local.observeQuote(id)

    override suspend fun refresh(): EmptyResult<DataError> = copyLock.withLock { downloadAll() }

    private suspend fun downloadAll(): EmptyResult<DataError> {
        val quotes = mutableListOf<Quote>()
        var after: String? = null
        while (true) {
            val page = when (val result = remote.getQuotes(after, PAGE_SIZE)) {
                is Result.Success -> result.data
                is Result.Error -> return result
            }
            if (page.isEmpty()) break
            quotes += page.map { it.toQuote() }
            after = page.last().id
        }
        return local.replaceSent(quotes)
    }

    override suspend fun clearLocalCopy(): EmptyResult<DataError.Local> = copyLock.withLock { local.clear() }

    override suspend fun addQuote(quote: NewQuote): EmptyResult<DataError.Local> {
        val pending = quote.toPendingQuote(id = UUID.randomUUID().toString(), createdAt = Clock.System.now())
        return local.upsert(pending).onSuccess { scheduler.scheduleSync() }
    }

    override suspend fun sendPending(): EmptyResult<DataError> = copyLock.withLock {
        val quotes = when (val result = local.pending()) {
            is Result.Success -> result.data
            is Result.Error -> return result
        }
        for (quote in quotes) {
            when (val result = remote.postQuote(quote.toQuoteRequestDto())) {
                is Result.Success -> local.upsert(result.data.toQuote())
                is Result.Error -> return result
            }
        }
        Result.Success(Unit)
    }
}
