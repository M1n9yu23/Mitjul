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
import io.ktor.client.request.HttpRequestData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import java.net.ConnectException
import java.net.UnknownHostException
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class HttpClientFactoryTest {

    private val sent = mutableListOf<HttpRequestData>()

    private fun client(status: HttpStatusCode, body: String): HttpClient {
        val engine = MockEngine { request ->
            sent += request
            respond(body, status, headersOf(HttpHeaders.ContentType, "application/json"))
        }
        return HttpClientFactory.create(
            engine,
            "https://test.invalid/api/v1",
            FakeAuthProvider(),
            isDebugBuild = false,
        )
    }

    @Test
    fun getResult_baseUrlWithoutTrailingSlash_keepsEveryPathSegment() = runTest {
        client(HttpStatusCode.OK, """{"title":"밑줄"}""")
            .getResult<ProbeRoute, ProbeBody>(ProbeRoute(page = 2))

        assertThat(sent.last().url.toString())
            .isEqualTo("https://test.invalid/api/v1/probe?page=2")
    }

    @Test
    fun getResult_serviceUnavailable_isServiceUnavailableError() = runTest {
        val result = client(HttpStatusCode.ServiceUnavailable, "")
            .getResult<ProbeRoute, ProbeBody>(ProbeRoute())

        assertThat(result).isEqualTo(Result.Error(DataError.Network.SERVICE_UNAVAILABLE))
    }

    @Test
    fun getResult_brokenSuccessBody_isSerializationError() = runTest {
        val result = client(HttpStatusCode.OK, "{")
            .getResult<ProbeRoute, ProbeBody>(ProbeRoute())

        assertThat(result).isEqualTo(Result.Error(DataError.Network.SERIALIZATION))
    }

    @Test
    fun getResult_unknownHost_isNoInternetError() = runTest {
        val engine = MockEngine { throw UnknownHostException("test.invalid") }
        val result = HttpClientFactory.create(engine, "https://test.invalid/api/v1", FakeAuthProvider(), isDebugBuild = false)
            .getResult<ProbeRoute, ProbeBody>(ProbeRoute())

        assertThat(result).isEqualTo(Result.Error(DataError.Network.NO_INTERNET))
    }

    @Test
    fun getResult_connectionRefused_isServiceUnavailableError() = runTest {
        val engine = MockEngine { throw ConnectException("Failed to connect") }
        val result = HttpClientFactory.create(engine, "https://test.invalid/api/v1", FakeAuthProvider(), isDebugBuild = false)
            .getResult<ProbeRoute, ProbeBody>(ProbeRoute())

        assertThat(result).isEqualTo(Result.Error(DataError.Network.SERVICE_UNAVAILABLE))
    }

    @Test
    fun postResult_withBody_sendsJson() = runTest {
        client(HttpStatusCode.OK, """{"title":"밑줄"}""")
            .postResult<ProbeRoute, ProbeBody, ProbeBody>(ProbeRoute(), ProbeBody("밑줄"))

        assertThat(sent.last().body.contentType?.toString()).isEqualTo("application/json")
    }

    @Test
    fun deleteResult_noContent_isSuccess() = runTest {
        val result = client(HttpStatusCode.NoContent, "").deleteResult(ProbeRoute())

        assertThat(result).isEqualTo(Result.Success(Unit))
    }
}
