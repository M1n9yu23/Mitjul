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

package com.gyugle.mitjul.feature.quote.presentation.quotelist

import com.gyugle.mitjul.core.presentation.UiText
import com.gyugle.mitjul.feature.quote.domain.QuoteValidator
import com.gyugle.mitjul.feature.quote.domain.error.QuoteInputError
import com.gyugle.mitjul.feature.quote.presentation.R

fun QuoteInputError.toUiText(): UiText = when (this) {
    QuoteInputError.Text.BLANK -> UiText.StringResource(R.string.error_quote_text_blank)
    QuoteInputError.Page.OUT_OF_RANGE -> UiText.StringResource(
        R.string.error_quote_page_out_of_range,
        listOf(QuoteValidator.PAGE_MAX),
    )
}
