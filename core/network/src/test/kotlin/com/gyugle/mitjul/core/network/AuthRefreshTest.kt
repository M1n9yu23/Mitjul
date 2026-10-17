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

package com.gyugle.mitjul.core.network

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.gyugle.mitjul.core.domain.DataError
import com.gyugle.mitjul.core.domain.Result
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class AuthRefreshTest {

    private val sentTokens = mutableListOf<String?>()

    private fun client(auth: AuthProvider): HttpClient {
        val engine = MockEngine { request ->
            val header = request.headers[HttpHeaders.Authorization]
            sentTokens += header
            if (header != "Bearer fresh") return@MockEngine respond("", HttpStatusCode.Unauthorized)
            respond("""{"title":"밑줄"}""", HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "application/json"))
        }
        return HttpClientFactory.create(engine, "https://test.invalid/api/v1", auth, isDebugBuild = false)
    }

    @Test
    fun getResult_expiredToken_refreshesOnceAndRetries() = runTest {
        val auth = FakeAuthProvider(BearerTokens("stale", "r1"), BearerTokens("fresh", "r2"))

        val result = client(auth).getResult<ProbeRoute, ProbeBody>(ProbeRoute())

        assertThat(result).isEqualTo(Result.Success(ProbeBody("밑줄")))
        assertThat(sentTokens).isEqualTo(listOf("Bearer stale", "Bearer fresh"))
        assertThat(auth.refreshCount).isEqualTo(1)
    }

    @Test
    fun getResult_refreshReturnsNull_isUnauthorizedAfterOneTry() = runTest {
        val auth = FakeAuthProvider(BearerTokens("stale", "r1"), renewed = null)

        val result = client(auth).getResult<ProbeRoute, ProbeBody>(ProbeRoute())

        assertThat(result).isEqualTo(Result.Error(DataError.Network.UNAUTHORIZED))
        assertThat(sentTokens).isEqualTo(listOf("Bearer stale"))
        assertThat(auth.refreshCount).isEqualTo(1)
    }
}
