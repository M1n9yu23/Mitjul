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

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.gyugle.mitjul.core.domain.Result
import com.gyugle.mitjul.feature.quote.domain.error.QuoteInputError
import org.junit.jupiter.api.Test

class QuoteValidatorTest {

    @Test
    fun validatePage_aroundBothLimits_acceptsBlankAndOneToMax() {
        listOf("", "  ", "1", " 20000 ").forEach {
            assertThat(QuoteValidator.validatePage(it)).isEqualTo(Result.Success(Unit))
        }
        listOf("0", "20001", "12쪽").forEach {
            assertThat(QuoteValidator.validatePage(it))
                .isEqualTo(Result.Error(QuoteInputError.Page.OUT_OF_RANGE))
        }
    }
}
