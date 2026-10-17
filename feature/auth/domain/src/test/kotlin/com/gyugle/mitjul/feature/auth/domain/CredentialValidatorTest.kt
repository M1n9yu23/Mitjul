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

package com.gyugle.mitjul.feature.auth.domain

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.gyugle.mitjul.core.domain.Result
import com.gyugle.mitjul.feature.auth.domain.error.CredentialError
import org.junit.jupiter.api.Test

class CredentialValidatorTest {

    private fun emailOfLength(length: Int): String {
        val domain = "@test.invalid"
        return "a".repeat(length - domain.length) + domain
    }

    @Test
    fun validateEmail_onlySpaces_isBlank() {
        assertThat(CredentialValidator.validateEmail("   "))
            .isEqualTo(Result.Error(CredentialError.Email.BLANK))
    }

    @Test
    fun validateEmail_withoutAtSign_isMalformed() {
        assertThat(CredentialValidator.validateEmail(" reader.test.invalid "))
            .isEqualTo(Result.Error(CredentialError.Email.MALFORMED))
    }

    @Test
    fun validateEmail_aroundMaxLength_rejectsOnlyOverTheLimit() {
        assertThat(CredentialValidator.validateEmail(emailOfLength(254)))
            .isEqualTo(Result.Success(Unit))
        assertThat(CredentialValidator.validateEmail(emailOfLength(255)))
            .isEqualTo(Result.Error(CredentialError.Email.TOO_LONG))
    }

    @Test
    fun validatePassword_aroundBothLimits_acceptsOnlyInside() {
        assertThat(CredentialValidator.validatePassword("a".repeat(11)))
            .isEqualTo(Result.Error(CredentialError.Password.TOO_SHORT))
        assertThat(CredentialValidator.validatePassword("a".repeat(12)))
            .isEqualTo(Result.Success(Unit))
        assertThat(CredentialValidator.validatePassword("a".repeat(128)))
            .isEqualTo(Result.Success(Unit))
        assertThat(CredentialValidator.validatePassword("a".repeat(129)))
            .isEqualTo(Result.Error(CredentialError.Password.TOO_LONG))
    }
}
