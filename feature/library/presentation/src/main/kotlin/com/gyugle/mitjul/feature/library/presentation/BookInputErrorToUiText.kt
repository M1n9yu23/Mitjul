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

package com.gyugle.mitjul.feature.library.presentation

import com.gyugle.mitjul.core.presentation.UiText
import com.gyugle.mitjul.feature.library.domain.BookValidator
import com.gyugle.mitjul.feature.library.domain.error.BookInputError

fun BookInputError.toUiText(): UiText = when (this) {
    BookInputError.Title.BLANK -> UiText.StringResource(R.string.error_book_title_blank)
    BookInputError.Title.TOO_LONG -> UiText.StringResource(
        R.string.error_book_title_too_long,
        listOf(BookValidator.TITLE_MAX_LENGTH),
    )
    BookInputError.Author.BLANK -> UiText.StringResource(R.string.error_book_author_blank)
    BookInputError.Author.TOO_LONG -> UiText.StringResource(
        R.string.error_book_author_too_long,
        listOf(BookValidator.AUTHOR_MAX_LENGTH),
    )
    BookInputError.TotalPages.OUT_OF_RANGE -> UiText.StringResource(
        R.string.error_book_total_pages_out_of_range,
        listOf(1, BookValidator.TOTAL_PAGES_MAX),
    )
    BookInputError.CurrentPage.OUT_OF_RANGE -> UiText.StringResource(R.string.error_book_current_page_out_of_range)
}
