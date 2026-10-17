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

package com.gyugle.mitjul.feature.library.domain

import com.gyugle.mitjul.core.domain.EmptyResult
import com.gyugle.mitjul.core.domain.Result
import com.gyugle.mitjul.feature.library.domain.error.BookInputError

object BookValidator {
    const val TITLE_MAX_LENGTH = 200
    const val AUTHOR_MAX_LENGTH = 120
    const val TOTAL_PAGES_MAX = 20_000
    const val RATING_MAX = 5
    const val REVIEW_MAX_LENGTH = 300

    fun validateTitle(title: String): EmptyResult<BookInputError.Title> = when {
        title.isBlank() -> Result.Error(BookInputError.Title.BLANK)
        title.trim().length > TITLE_MAX_LENGTH -> Result.Error(BookInputError.Title.TOO_LONG)
        else -> Result.Success(Unit)
    }

    fun validateAuthor(author: String): EmptyResult<BookInputError.Author> = when {
        author.isBlank() -> Result.Error(BookInputError.Author.BLANK)
        author.trim().length > AUTHOR_MAX_LENGTH -> Result.Error(BookInputError.Author.TOO_LONG)
        else -> Result.Success(Unit)
    }

    fun validateTotalPages(totalPages: String): EmptyResult<BookInputError.TotalPages> {
        val pages = totalPages.trim().toIntOrNull()
        return if (pages != null && pages in 1..TOTAL_PAGES_MAX) {
            Result.Success(Unit)
        } else {
            Result.Error(BookInputError.TotalPages.OUT_OF_RANGE)
        }
    }

    fun validateCurrentPage(currentPage: String, totalPages: Int): EmptyResult<BookInputError.CurrentPage> {
        val page = currentPage.trim().toIntOrNull()
        return if (page != null && page in 0..totalPages) {
            Result.Success(Unit)
        } else {
            Result.Error(BookInputError.CurrentPage.OUT_OF_RANGE)
        }
    }
}
