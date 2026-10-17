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

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.gyugle.mitjul.core.domain.Result
import com.gyugle.mitjul.feature.library.domain.repository.FakeBookRepository
import com.gyugle.mitjul.feature.quote.domain.repository.FakeQuoteRepository
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class RepositoryCopyCleanerTest {

    @Test
    fun clear_emptiesBooksQuotesAndCacheOnce() = runTest {
        val books = FakeBookRepository()
        val quotes = FakeQuoteRepository()
        var cacheClears = 0

        val result = RepositoryCopyCleaner(books, quotes) { cacheClears++ }.clear()

        assertThat(result).isEqualTo(Result.Success(Unit))
        assertThat(listOf(books.clearCalls, quotes.clearCalls, cacheClears)).isEqualTo(listOf(1, 1, 1))
    }
}
