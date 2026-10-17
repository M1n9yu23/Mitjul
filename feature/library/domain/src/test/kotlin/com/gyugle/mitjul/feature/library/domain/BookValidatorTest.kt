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

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.gyugle.mitjul.core.domain.Result
import com.gyugle.mitjul.feature.library.domain.error.BookInputError
import org.junit.jupiter.api.Test

class BookValidatorTest {

    @Test
    fun validateTitle_aroundMaxLength_rejectsBlankAndOverTheLimit() {
        assertThat(BookValidator.validateTitle("   "))
            .isEqualTo(Result.Error(BookInputError.Title.BLANK))
        assertThat(BookValidator.validateTitle(" " + "가".repeat(200) + " "))
            .isEqualTo(Result.Success(Unit))
        assertThat(BookValidator.validateTitle("가".repeat(201)))
            .isEqualTo(Result.Error(BookInputError.Title.TOO_LONG))
    }

    @Test
    fun validateAuthor_aroundMaxLength_rejectsOnlyOverTheLimit() {
        assertThat(BookValidator.validateAuthor("가".repeat(120)))
            .isEqualTo(Result.Success(Unit))
        assertThat(BookValidator.validateAuthor("가".repeat(121)))
            .isEqualTo(Result.Error(BookInputError.Author.TOO_LONG))
    }

    @Test
    fun validateTotalPages_aroundBothLimits_acceptsOnlyNumbersInside() {
        listOf("1", " 20000 ").forEach {
            assertThat(BookValidator.validateTotalPages(it)).isEqualTo(Result.Success(Unit))
        }
        listOf("", "0", "20001", "300쪽").forEach {
            assertThat(BookValidator.validateTotalPages(it))
                .isEqualTo(Result.Error(BookInputError.TotalPages.OUT_OF_RANGE))
        }
    }

    @Test
    fun validateCurrentPage_aroundBothLimits_acceptsZeroToTotalPages() {
        listOf("0", " 300 ").forEach {
            assertThat(BookValidator.validateCurrentPage(it, totalPages = 300)).isEqualTo(Result.Success(Unit))
        }
        listOf("", "-1", "301").forEach {
            assertThat(BookValidator.validateCurrentPage(it, totalPages = 300))
                .isEqualTo(Result.Error(BookInputError.CurrentPage.OUT_OF_RANGE))
        }
    }
}
