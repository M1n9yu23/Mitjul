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

package com.gyugle.mitjul.core.session

import assertk.assertThat
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import com.gyugle.mitjul.core.network.HttpClientFactory
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import java.net.UnknownHostException
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class SessionAuthProviderTest {

    private val saved = AuthTokens("access-1", "refresh-1")
    private val expired = BearerTokens("access-1", "refresh-1")
    private val sent = mutableListOf<HttpRequestData>()

    private fun provider(
        store: FakeTokenStore,
        answer: MockRequestHandleScope.() -> HttpResponseData,
    ): SessionAuthProvider {
        val engine = MockEngine { request ->
            sent += request
            answer()
        }
        val client = HttpClientFactory.createPlain(engine, "https://test.invalid/api/v1", isDebugBuild = false)
        return SessionAuthProvider(client, store)
    }

    @Test
    fun refresh_tokensAlreadyRenewed_returnsThemWithoutRequest() = runTest {
        val store = FakeTokenStore(initial = AuthTokens("access-2", "refresh-2"))

        val tokens = provider(store) { error("요청을 보내면 안 된다") }.refresh(expired)

        assertThat(tokens?.accessToken).isEqualTo("access-2")
        assertThat(sent).isEmpty()
    }

    @Test
    fun refresh_serverAccepts_savesRotatedTokens() = runTest {
        val store = FakeTokenStore(initial = saved)
        val body = """{"accessToken":"access-2","refreshToken":"refresh-2"}"""

        provider(store) {
            respond(body, HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "application/json"))
        }.refresh(expired)

        assertThat(sent.single().url.toString()).isEqualTo("https://test.invalid/api/v1/auth/refresh")
        assertThat(sent.single().headers[HttpHeaders.Authorization]).isEqualTo("Bearer refresh-1")
        assertThat(store.saved).isEqualTo(AuthTokens("access-2", "refresh-2"))
    }

    @Test
    fun refresh_serverRejects_clearsStore() = runTest {
        val store = FakeTokenStore(initial = saved)

        val tokens = provider(store) { respond("", HttpStatusCode.Unauthorized) }.refresh(expired)

        assertThat(tokens).isNull()
        assertThat(store.saved).isNull()
    }

    @Test
    fun refresh_noInternet_keepsTokens() = runTest {
        val store = FakeTokenStore(initial = saved)

        val tokens = provider(store) { throw UnknownHostException("test.invalid") }.refresh(expired)

        assertThat(tokens).isNull()
        assertThat(store.saved).isEqualTo(saved)
    }
}
