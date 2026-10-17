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

import com.gyugle.mitjul.core.domain.DataError
import com.gyugle.mitjul.core.domain.Result
import com.gyugle.mitjul.feature.quote.data.dto.QuoteDto
import com.gyugle.mitjul.feature.quote.data.dto.QuoteRequestDto

interface QuotesRemoteDataSource {
    suspend fun getQuotes(after: String?, limit: Int): Result<List<QuoteDto>, DataError.Network>
    suspend fun postQuote(request: QuoteRequestDto): Result<QuoteDto, DataError.Network>
}
