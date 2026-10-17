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

package com.gyugle.mitjul.feature.quote.domain.repository

import com.gyugle.mitjul.core.domain.DataError
import com.gyugle.mitjul.core.domain.EmptyResult
import com.gyugle.mitjul.core.domain.Result
import com.gyugle.mitjul.feature.quote.domain.model.NewQuote
import com.gyugle.mitjul.feature.quote.domain.model.Quote
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeQuoteRepository(
    private val addQuoteResult: EmptyResult<DataError.Local> = Result.Success(Unit),
    private val quote: Quote? = null,
) : QuoteRepository {

    var addedQuotes: List<NewQuote> = emptyList()
        private set

    var clearCalls = 0
        private set

    override fun observeQuotes(bookId: String): Flow<List<Quote>> = flowOf(emptyList())

    override fun observeQuote(id: String): Flow<Quote?> = flowOf(quote?.takeIf { it.id == id })

    override suspend fun refresh(): EmptyResult<DataError> = Result.Success(Unit)

    override suspend fun clearLocalCopy(): EmptyResult<DataError.Local> {
        clearCalls++
        return Result.Success(Unit)
    }

    override suspend fun addQuote(quote: NewQuote): EmptyResult<DataError.Local> {
        addedQuotes = addedQuotes + quote
        return addQuoteResult
    }

    override suspend fun sendPending(): EmptyResult<DataError> = Result.Success(Unit)
}
