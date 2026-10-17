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

package com.gyugle.mitjul.feature.auth.presentation

import com.gyugle.mitjul.core.presentation.UiText
import com.gyugle.mitjul.feature.auth.domain.CredentialValidator
import com.gyugle.mitjul.feature.auth.domain.error.CredentialError

fun CredentialError.toUiText(): UiText = when (this) {
    CredentialError.Email.BLANK -> UiText.StringResource(R.string.error_email_blank)
    CredentialError.Email.MALFORMED -> UiText.StringResource(R.string.error_email_malformed)
    CredentialError.Email.TOO_LONG -> UiText.StringResource(
        R.string.error_email_too_long,
        listOf(CredentialValidator.EMAIL_MAX_LENGTH),
    )
    CredentialError.Password.TOO_SHORT -> UiText.StringResource(
        R.string.error_password_too_short,
        listOf(CredentialValidator.PASSWORD_MIN_LENGTH),
    )
    CredentialError.Password.TOO_LONG -> UiText.StringResource(
        R.string.error_password_too_long,
        listOf(CredentialValidator.PASSWORD_MAX_LENGTH),
    )
}
