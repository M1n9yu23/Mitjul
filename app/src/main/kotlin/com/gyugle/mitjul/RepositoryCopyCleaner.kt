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

package com.gyugle.mitjul

import com.gyugle.mitjul.core.domain.DataError
import com.gyugle.mitjul.core.domain.EmptyResult
import com.gyugle.mitjul.core.domain.Result
import com.gyugle.mitjul.core.domain.onSuccess
import com.gyugle.mitjul.feature.auth.domain.LocalCopyCleaner
import com.gyugle.mitjul.feature.library.domain.repository.BookRepository
import com.gyugle.mitjul.feature.quote.domain.repository.QuoteRepository

class RepositoryCopyCleaner(
    private val bookRepository: BookRepository,
    private val quoteRepository: QuoteRepository,
    private val cacheCleaner: CacheCleaner,
) : LocalCopyCleaner {

    override suspend fun clear(): EmptyResult<DataError.Local> {
        val books = bookRepository.clearLocalCopy()
        if (books is Result.Error) return books
        return quoteRepository.clearLocalCopy().onSuccess { cacheCleaner.clear() }
    }
}
