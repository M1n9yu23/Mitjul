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

package com.gyugle.mitjul.feature.quote.domain

import com.gyugle.mitjul.core.domain.EmptyResult
import com.gyugle.mitjul.core.domain.Result
import com.gyugle.mitjul.feature.quote.domain.error.QuoteInputError

object QuoteValidator {
    const val TEXT_MAX_LENGTH = 1000
    const val MEMO_MAX_LENGTH = 300
    const val PAGE_MAX = 20_000

    fun validateText(text: String): EmptyResult<QuoteInputError.Text> =
        if (text.isBlank()) Result.Error(QuoteInputError.Text.BLANK) else Result.Success(Unit)

    fun validatePage(page: String): EmptyResult<QuoteInputError.Page> {
        if (page.isBlank()) return Result.Success(Unit)
        val number = page.trim().toIntOrNull()
        return if (number != null && number in 1..PAGE_MAX) {
            Result.Success(Unit)
        } else {
            Result.Error(QuoteInputError.Page.OUT_OF_RANGE)
        }
    }
}
