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

package com.gyugle.mitjul.core.logging

import assertk.assertThat
import assertk.assertions.isEqualTo
import org.junit.jupiter.api.Test

class RedactTest {

    @Test
    fun redact_bearerHeader_hidesTokenKeepsScheme() {
        assertThat(redact("Authorization: Bearer abc.DEF-123_x=="))
            .isEqualTo("Authorization: Bearer ***")
    }

    @Test
    fun redact_jwtInBody_hidesAllThreeParts() {
        assertThat(redact("""{"accessToken":"eyJhbGciOi.eyJzdWIiOi.c2ln-_x","refreshToken":"eyJ1.eyJ2.c2ln"}"""))
            .isEqualTo("""{"accessToken":"***","refreshToken":"***"}""")
    }

    @Test
    fun redact_email_keepsFirstLetterAndDomain() {
        assertThat(redact("로그인 실패: reader@test.invalid"))
            .isEqualTo("로그인 실패: r***@test.invalid")
    }

    @Test
    fun redact_nothingSecret_returnsSameLine() {
        val line = "GET /api/v1/books?status=READING 200"
        assertThat(redact(line)).isEqualTo(line)
    }
}
