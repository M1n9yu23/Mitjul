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

package com.gyugle.mitjul.server.feature.auth.dto

import com.gyugle.mitjul.server.core.domain.error.ApiFailure
import kotlinx.serialization.Serializable

private const val EMAIL_MAX = 254
private const val PASSWORD_MIN = 12
private const val PASSWORD_MAX = 128

@Serializable
data class SignupRequest(
    val email: String,
    val password: String,
)

fun SignupRequest.validated(): SignupRequest {
    val fields = buildMap {
        val address = email.trim()
        if (address.isEmpty()) put("email", "blank")
        else if (address.length > EMAIL_MAX) put("email", "too_long")
        else if (!address.contains('@')) put("email", "malformed")
        if (password.length < PASSWORD_MIN) put("password", "too_short")
        else if (password.length > PASSWORD_MAX) put("password", "too_long")
    }
    if (fields.isNotEmpty()) throw ApiFailure.Invalid(fields)
    return copy(email = email.trim().lowercase())
}
